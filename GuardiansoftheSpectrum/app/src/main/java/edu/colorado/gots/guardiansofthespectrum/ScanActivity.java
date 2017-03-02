package edu.colorado.gots.guardiansofthespectrum;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

//public class ScanActivity extends BaseActivity implements LocationServicesManager.LocationServicesCallbacks {
public class ScanActivity extends LocationActivity {

    TextView textView;
    ProgressBar bar;
    ScanDataReceiver receiver;
    //LocationServicesManager LSManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        textView = (TextView) findViewById(R.id.wifi_scanStat);
        bar = (ProgressBar) findViewById(R.id.scanProgressBar);
        bar.setVisibility(VISIBLE);
        receiver = new ScanDataReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(ScanService.GOTS_SCAN_SERVICE_RESULTS));
    }

    protected void onStart() {
        super.onStart();
        //LSManager = new LocationServicesManager(this);
        LSManager.checkAndResolvePermissions();
    }

    protected void onStop() {
        Intent serviceIntent = new Intent(this, ScanService.class);
        serviceIntent.setAction(ScanService.GOTS_SCAN_FOREGROUND_END);
        startService(serviceIntent);
        super.onStop();
    }

    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }


    /*protected void onActivityResult(int requestCode, int returnCode, Intent i) {
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
    }*/

    public void onLocationEnabled() {
        Intent serviceIntent = new Intent(this, ScanService.class);
        serviceIntent.setAction(ScanService.GOTS_SCAN_FOREGROUND_START);
        startService(serviceIntent);
    }

    public void onLocationNotEnabled() {
        bar.setVisibility(GONE);
    }

    private class ScanDataReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent i) {
            bar.setVisibility(GONE);
            textView.setText(i.getStringExtra(ScanService.GOTS_SCAN_SERVICE_RESULTS_EXTRA));
        }
    }
}