package edu.colorado.gots.guardiansofthespectrum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ScanActivity extends AppCompatActivity {

    TextView textView;
    ProgressBar bar;
    ScanDataReceiver receiver;

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
        Intent serviceIntent = new Intent(this, ScanService.class);
        serviceIntent.setAction(ScanService.GOTS_SCAN_FOREGROUND_START);
        startService(serviceIntent);
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

    private class ScanDataReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent i) {
            bar.setVisibility(GONE);
            textView.setText(i.getStringExtra(ScanService.GOTS_SCAN_SERVICE_RESULTS_EXTRA));
        }
    }
}