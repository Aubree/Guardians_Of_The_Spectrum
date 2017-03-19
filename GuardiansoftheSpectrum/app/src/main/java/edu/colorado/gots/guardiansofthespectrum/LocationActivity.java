package edu.colorado.gots.guardiansofthespectrum;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Class that contains functionality for Activities that need to handle checking for Location
 * permissions and Google Play API connections.
 */
public abstract class LocationActivity extends BaseActivity implements LocationServicesManager.LocationServicesCallbacks {

    /**
     * An instance of LocationServicesManager to handle connecting to Google Play and getting
     * permissions for various things needed to scan.
     */
    protected LocationServicesManager LSManager;

    /**
     * Called when starting the Activity for the first time. Responsible for initializing
     * our LocationServicesManager instance.
     * @param savedInstanceState Ignored
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LSManager = new LocationServicesManager(this);
    }

    /**
     * Called when an activity we started to resolve an issue is finished. This handles requests
     * for Location permissions in pre API23 (Marshmallow) versions of Android, and resolvable
     * errors in connecting to Google Play Services such as it being out of date on the phone.
     * @param requestCode Identify what the request was
     * @param returnCode The return code of the request
     * @param i Ignored
     */
    protected void onActivityResult(int requestCode, int returnCode, Intent i) {
        switch (requestCode) {
            //resolution for old-style checkLocationSettings
            case LocationServicesManager.LOCATION_SERVICE_RESOLUTION:
                if (returnCode != Activity.RESULT_OK) {
                    //changes not made successfully. just gripe for now
                    Toast.makeText(getApplicationContext(), "Location services needed to send data", Toast.LENGTH_SHORT).show();
                    onLocationNotEnabled();
                } else {
                    onLocationEnabled();
                }
                break;
            //resolution for googleApiClient connection failure
            case LocationServicesManager.CONNECTION_RESOLUTION:
                if (returnCode != Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "This app requires the Google Play Services APIs to function. Please get that resolved", Toast.LENGTH_SHORT).show();
                } else {
                    LSManager.connect();
                }
            default:
                break;
        }
    }

    /**
     * Called when the error dialog created by a Google Play API error is dismissed from the screen
     * by the user. Forwards this information on to the LocationServicesManager instance.
     * @see LocationServicesManager#onDialogDismissed()
     */
    public void onConnectionResolveDialogDismissed() {
        LSManager.onDialogDismissed();
    }

    /**
     * Called when the app requests dangerous permissions (Location and Phone state) in API23 or
     * later versions of Android.
     * @param requestCode Identify what the request was for
     * @param permissions Ignored
     * @param grantResults Array of the permissions tatus for each requested permission
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LocationServicesManager.SCAN_PERMISSIONS_REQUEST:
                for (int i = 0; i < grantResults.length; i++) {
                    //make sure all permissions got granted
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "We need these permissions to scan. Please enable them <('-')>",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                LSManager.checkAndResolvePermissions();
                break;
            default:
                break;
        }
    }
}
