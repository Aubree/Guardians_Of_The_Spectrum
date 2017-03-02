package edu.colorado.gots.guardiansofthespectrum;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

public abstract class LocationActivity extends BaseActivity implements LocationServicesManager.LocationServicesCallbacks {

    protected LocationServicesManager LSManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LSManager = new LocationServicesManager(this);
    }

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

    public void onConnectionResolveDialogDismissed() {
        LSManager.onDialogDismissed();
    }

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
