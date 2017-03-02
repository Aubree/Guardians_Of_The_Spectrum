package edu.colorado.gots.guardiansofthespectrum;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class LocationServicesManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    interface LocationServicesCallbacks {
        void onLocationEnabled();
        void onLocationNotEnabled();
        void onConnectionResolveDialogDismissed();
    }

    final static int LOCATION_SERVICE_RESOLUTION = 0;
    final static int CONNECTION_RESOLUTION = 1;
    final static int SCAN_PERMISSIONS_REQUEST = 0;

    private final static String GOTS_CONNECTION_ERROR = "edu.colorado.gots.guardiansofthespectrum.connection.error";

    private GoogleApiClient client;
    private boolean resolvingError = false;
    private Activity activity;


    LocationServicesManager(Context c) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(c);
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);
        builder.addApi(LocationServices.API);
        client = builder.build();
        client.connect();
        if (c instanceof Activity) {
            activity = (Activity) c;
        }
    }

    //make sure location services are enabled and if not, get the user to do it
    void checkAndResolvePermissions() {
        class LocationResolveTask extends AsyncTask<Void, Void, Void> {
            protected Void doInBackground(Void... params) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE}, SCAN_PERMISSIONS_REQUEST);
                }
                while (!client.isConnected()) {
                    try {
                        System.out.println("waiting on connection in resolve\n");
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                //set up the location request options for how we want to get locations
                LocationRequest request = new LocationRequest();
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                request.setInterval(5000);
                request.setFastestInterval(1000);
                //check our location settings
                LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder();
                settingsBuilder.addLocationRequest(request);
                PendingResult<LocationSettingsResult> settings = LocationServices.SettingsApi.checkLocationSettings(client, settingsBuilder.build());
                //set the callback function for when the results arrive. If Location service is not enabled,
                //we'll start the resolution process which will launch a dialog allowing the user to enable
                //the service or leave it off
                settings.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    public void onResult(LocationSettingsResult res) {
                        com.google.android.gms.common.api.Status status = res.getStatus();
                        int statusCode = status.getStatusCode();
                        if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                //this will launch the dialog. will call onActivityResult when dialog
                                //is completed.
                                System.out.println("starting location resolution\n");
                                status.startResolutionForResult(activity, LOCATION_SERVICE_RESOLUTION);
                            } catch (IntentSender.SendIntentException e) {}
                        } else if (statusCode == LocationSettingsStatusCodes.SUCCESS) {
                            System.out.println("location settings are already set\n");
                            try {
                                ((LocationServicesCallbacks) activity).onLocationEnabled();
                            } catch (ClassCastException e) {
                                System.out.println("cannot trigger location enabled callback\n");
                            }
                        }
                    }
                });
                return null;
            }
        }
        new LocationResolveTask().execute();
    }

    void requestLocationUpdates(final PendingIntent intent) {
        class RequestLocationTask extends AsyncTask<Void, Void, Void> {
            protected Void doInBackground(Void... param) {
                while (!client.isConnected()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                LocationRequest request = new LocationRequest();
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                request.setInterval(5000);
                request.setFastestInterval(1000);
                LocationServices.FusedLocationApi.requestLocationUpdates(client, request, intent);
                return null;
            }
        }
        new RequestLocationTask().execute();
    }

    void removeLocationUpdates(PendingIntent intent) {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, intent);
    }

    public void onConnected(Bundle connectionHint) {

    }

    public void onConnectionSuspended(int cause) {

    }

    public void onConnectionFailed(ConnectionResult result) {
        if (resolvingError) {
            return;
        } else if (result.hasResolution()) {
            resolvingError = true;
            try {
                result.startResolutionForResult(activity, CONNECTION_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                client.connect();
            }
        } else {
            showErrorDialog(result.getErrorCode());
        }
    }

    private void showErrorDialog(int code) {
        ConnectionErrorDialogFragment frag = new ConnectionErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(GOTS_CONNECTION_ERROR, code);
        frag.setArguments(args);
        frag.show(activity.getFragmentManager(), "connectionErrorFragment");
    }

    public void onDialogDismissed() {
        resolvingError = false;
    }

    public static class ConnectionErrorDialogFragment extends DialogFragment {
        public void onAttach(Context c) {
            super.onAttach(c);
            if (!(c instanceof LocationServicesCallbacks)) {
                throw new ClassCastException(c.toString() + " must implement LocationServicesCallbacks");
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int error = this.getArguments().getInt(GOTS_CONNECTION_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(this.getActivity(), error, CONNECTION_RESOLUTION);
        }

        public void onDismiss(DialogInterface dialog) {
            ((LocationServicesCallbacks) getActivity()).onConnectionResolveDialogDismissed();
        }
    }
}
