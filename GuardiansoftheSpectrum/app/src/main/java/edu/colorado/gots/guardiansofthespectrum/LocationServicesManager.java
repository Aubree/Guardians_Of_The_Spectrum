package edu.colorado.gots.guardiansofthespectrum;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

class LocationServicesManager implements GoogleApiClient.ConnectionCallbacks {

    interface LocationServicesCallbacks {
        void onLocationEnabled();
        void onLocationNotEnabled();
    }

    final static int LOCATION_SERVICE_RESOLUTION = 0;

    private static GoogleApiClient client;
    private static LocationServicesManager instance = null;

    private LocationServicesManager(Context c) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(c);
        builder.addConnectionCallbacks(this);
        //builder.addOnConnectionFailedListener(this);
        builder.addApi(LocationServices.API);
        client = builder.build();
        client.connect();
    }

    public static LocationServicesManager getInstance(Context c) {
        synchronized(LocationServicesManager.class) {
            if (instance == null) {
                instance = new LocationServicesManager(c);
            }
        }
        return instance;
    }

    //make sure location services are enabled and if not, get the user to do it
    void checkAndResolvePermissions(final Activity activity) {
        class LocationResolveTask extends AsyncTask<Void, Void, Void> {
            protected Void doInBackground(Void... params) {
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
}
