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
            case LocationServicesManager.LOCATION_SERVICE_RESOLUTION:
                if (returnCode != Activity.RESULT_OK) {
                    //changes not made successfully. just gripe for now
                    Toast.makeText(getApplicationContext(), "Location services needed to send data", Toast.LENGTH_SHORT).show();
                    this.onLocationNotEnabled();
                } else {
                    this.onLocationEnabled();
                }
                break;
            case LocationServicesManager.CONNECTION_RESOLUTION:
                if (returnCode != Activity.RESULT_OK) {

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
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "We need these permissions to scan. Please enable them <('-')>",
                                Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                break;
            default:
                break;
        }
    }
}
