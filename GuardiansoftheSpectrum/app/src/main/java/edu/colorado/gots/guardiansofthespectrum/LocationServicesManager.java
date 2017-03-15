package edu.colorado.gots.guardiansofthespectrum;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

/**
 * Wrapper around Google play API for handling Location requests and checking permissions
 */
public class LocationServicesManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Various callbacks this class makes to its encompassing Activity when certain events have occurred
     */
    interface LocationServicesCallbacks {
        /**
         * Location retrieval has been enabled and all permissions have been granted
         */
        void onLocationEnabled();
        /**
         * Location retrieval has not been enabled and/or some permissions have been denied by the user
         */
        void onLocationNotEnabled();
        /**
         * The user has dismissed the error dialog created by an error in the Google API
         */
        void onConnectionResolveDialogDismissed();
        /**
         * A connection has been made successfully to the Google API server.
         */
        void onConnected();
    }

    /**
     * Request code for <code>onActivityResult(int, int, Intent)</code> indicating that the request
     * is for resolving Location permissions in the pre Marshmallow Android permission model.
     * @see LocationActivity#onActivityResult(int, int, Intent)
     */
    final static int LOCATION_SERVICE_RESOLUTION = 0;
    /**
     * Request code for <code>onActivityResult(int, int, Intent)</code> indicating that the request
     * is for resolving a connection error to the Google Play API.
     * @see LocationActivity#onActivityResult(int, int, Intent)
     */
    final static int CONNECTION_RESOLUTION = 1;

    /**
     * Request code for <code>onRequestPermissionsResult(int, String[], int[])</code> indicating that
     * the request is for resolving permission requests in the API23 or later versions of Android.
     * @see LocationActivity#onRequestPermissionsResult(int, String[], int[])
     */
    final static int SCAN_PERMISSIONS_REQUEST = 0;

    /**
     * Key for extra in bundle sent to Error dialog when an error has occurred in connecting to the
     * Google Play API.
     * @see #showErrorDialog(int)
     * @see ConnectionErrorDialogFragment#onCreateDialog(Bundle)
     *
     */
    private final static String GOTS_CONNECTION_ERROR = "edu.colorado.gots.guardiansofthespectrum.connection.error";

    /**
     * Instance of GoogleApiClient we use to make connections and request Locations.
     */
    private GoogleApiClient client;
    /**
     * Value indicating if we are working on resolving a connection error.
     */
    private boolean resolvingError = false;
    /**
     * A reference to our encompassing Activity.
     */
    private Activity activity;


    /**
     * Create our Google API client, add in its necessary API references, and register its
     * event listeners.
     * @param c The context from which this constructor is being called.
     */
    LocationServicesManager(Context c) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(c);
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);
        builder.addApi(LocationServices.API);
        client = builder.build();
        //if we're making this from an activity, grab it for callbacks
        //we don't need to hang on to the service though
        if (c instanceof Activity) {
            activity = (Activity) c;
            System.out.println("connecting client for activity " + activity);
        }
    }

    /**
     * Connect the Google client.
     */
    void connect() {
        client.reconnect();
    }

    /**
     * Checks permission state and starts resolutions asking the user to enable the necessary
     * permissions if they are inadequate. If permissions are already valid, the encompassing
     * activity will have its <code>onLocationEnabled()</code> callback invoked. Otherwise, either
     * <code>onActivityResult(int, int, Intent)</code> or <code>onRequestPermissionsResult(int, String[], int[])</code>
     * will be called when the user intervention is complete.
     * @see LocationServicesCallbacks#onLocationEnabled()
     * @see LocationActivity#onActivityResult(int, int, Intent)
     * @see LocationActivity#onRequestPermissionsResult(int, String[], int[])
     */
    void checkAndResolvePermissions() {
        class LocationResolveTask extends AsyncTask<Void, Void, Void> {
            protected Void doInBackground(Void... params) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE}, SCAN_PERMISSIONS_REQUEST);
                    return null;
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

    /** Requests that the supplied PendingIntent be invoked on changes in Location.
     * @param intent The Pending intent to invoke
     */
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

    /**
     * Request that the supplied Pending Intent no longer be invoked when the Location changes.
     * @param intent The Pending Intent
     */
    void removeLocationUpdates(final PendingIntent intent) {
        class RemoveLocationTask extends AsyncTask<Void, Void, Void> {
            public Void doInBackground(Void... params) {
                while (!client.isConnected()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                LocationServices.FusedLocationApi.removeLocationUpdates(client, intent);
                return null;
            }
        }
        new RemoveLocationTask().execute();
    }

    /**
     * Called when a connection to the Google Play API has been successfully established. Will
     * call the encompassing Activity's <code>onConnected()</code> callback
     * @param connectionHint Ignored
     * @see LocationServicesCallbacks#onConnected()
     */
    public void onConnected(Bundle connectionHint) {
        //only trigger callback if invoked from activity, not service
        if (activity != null) {
            ((LocationServicesCallbacks) activity).onConnected();
        }
    }

    /**
     * Stub function need to implement the <code>GoogleAPIClient.ConnectionCallbacks</code> Interface
     * @param cause Ignored
     */
    public void onConnectionSuspended(int cause) {

    }

    /**
     * Callback invoked when the connection to the Google Play API is unsuccessful. If the error can
     * be resolved, a call to the encompassing Activity's <code>onActivityResult(int, int, Intent)</code>
     * function will be made. Otherwise, we display an error to the user and trigger the Activity's
     * <code>onLocationNotEnabled()</code> callback.
     * @param result The result of the connection attempt
     * @see LocationActivity#onActivityResult(int, int, Intent)
     * @see #showErrorDialog(int)
     * @see LocationServicesCallbacks#onLocationNotEnabled()
     */
    public void onConnectionFailed(ConnectionResult result) {
        System.out.println("connecting client failed for activity " + activity);
        if (resolvingError) {
            return;
        } else if (result.hasResolution()) {
            resolvingError = true;
            try {
                System.out.println("starting client resolution for activity " + activity);
                result.startResolutionForResult(activity, CONNECTION_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                client.connect();
            }
        } else {
            System.out.println("unresolvable error in connecting client for activity " + activity);
            showErrorDialog(result.getErrorCode());
            ((LocationServicesCallbacks) activity).onLocationNotEnabled();
        }
    }

    /**
     * Trigger an error dialog for the specified Google API ConnectionResult error code.
     * @param code The error code, as returned by <code>ConnectionResult.getErrorCode()</code>
     */
    private void showErrorDialog(int code) {
        ConnectionErrorDialogFragment frag = new ConnectionErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(GOTS_CONNECTION_ERROR, code);
        frag.setArguments(args);
        frag.show(activity.getFragmentManager(), "connectionErrorFragment");
    }

    /**
     * Called when the user dismisses the error dialog generated when a connection attempt to the
     * Google Play API fails. We mark that we are no longer attempting to resolve an error.
     */
    public void onDialogDismissed() {
        resolvingError = false;
    }

    /**
     * The error dialog we show when a connection attempt to the Google Play API fails.
     */
    public static class ConnectionErrorDialogFragment extends DialogFragment {
        /**
         * Called when we attach our error dialog into an activity.
         * @param c The Context into which we are attaching
         */
        public void onAttach(Context c) {
            super.onAttach(c);
            if (!(c instanceof LocationServicesCallbacks)) {
                throw new ClassCastException(c.toString() + " must implement LocationServicesCallbacks");
            }
        }

        /**
         * Called when the dialog is created
         * @param savedInstanceState Collection of data representing the state of the dialog
         * @return The error Dialog
         */
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int error = this.getArguments().getInt(GOTS_CONNECTION_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(this.getActivity(), error, CONNECTION_RESOLUTION);
        }

        /**
         * Called when the user dismisses the error dialog box. Triggers a call to the Activity's
         * <code>onConnectionResolveDialogDismissed()</code> callback.
         * @param dialog Ignored
         * @see LocationServicesCallbacks#onConnectionResolveDialogDismissed()
         */
        public void onDismiss(DialogInterface dialog) {
            ((LocationServicesCallbacks) getActivity()).onConnectionResolveDialogDismissed();
        }
    }
}
