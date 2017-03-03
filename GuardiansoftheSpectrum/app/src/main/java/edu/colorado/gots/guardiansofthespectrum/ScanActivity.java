package edu.colorado.gots.guardiansofthespectrum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

//public class ScanActivity extends BaseActivity implements LocationServicesManager.LocationServicesCallbacks {
public class ScanActivity extends LocationActivity {

    TextView textView;
    ProgressBar bar;
    ScanDataReceiver receiver;
    private boolean scanning = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        textView = (TextView) findViewById(R.id.wifi_scanStat);
        bar = (ProgressBar) findViewById(R.id.scanProgressBar);
        bar.setVisibility(VISIBLE);
        receiver = new ScanDataReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(ScanService.GOTS_SCAN_SERVICE_RESULTS));
        LSManager.connect();
    }

    protected void onStop() {
        //only send an intent to the service if it's running successfully
        //(i.e. all permission checks have passed and googleApiClient has
        //connected), otherwise we'll start running into errors if permissions
        //and connections aren't present because telling the service to stop will
        //start it up
        if (scanning) {
            Intent serviceIntent = new Intent(this, ScanService.class);
            serviceIntent.setAction(ScanService.GOTS_SCAN_FOREGROUND_END);
            startService(serviceIntent);
        }
        super.onStop();
    }

    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void onConnected() {
        LSManager.checkAndResolvePermissions();
    }

    public void onLocationEnabled() {
        System.out.println("starting service from scan activity");
        scanning = true;
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