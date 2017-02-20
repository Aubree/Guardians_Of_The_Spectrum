package edu.colorado.gots.guardiansofthespectrum;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends BaseActivity {
    private Switch serviceSwitch;
    private Intent serviceIntent;
    private boolean scanEnabled = true;
    private BatteryReceiver batteryReceiver;
    private CounterReceiver counterReceiver;
    private LocationServicesManager LSManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        //set up a listener to manage low-battery notifications
        batteryReceiver = new BatteryReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        registerReceiver(batteryReceiver, intentFilter);

        //set up a quick and dirty listener to receiver counter updates from the service
        counterReceiver = new CounterReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(counterReceiver,
                new IntentFilter(ScanService.GOTS_COUNTER));

        //grab location manager
        LSManager = LocationServicesManager.getInstance(getApplicationContext());
        serviceSwitch = (Switch) findViewById(R.id.scanServiceSwitch);
        serviceIntent = new Intent(this, ScanService.class);
        serviceIntent.setAction(ScanService.GOTS_SCAN_BACKGROUND_START);
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (isChecked && scanEnabled) {
                    LSManager.checkAndResolvePermissions(SettingsActivity.this);
                } else {
                    stopService(serviceIntent);
                }
            }
        });
    }

    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(counterReceiver);
        unregisterReceiver(batteryReceiver);
        super.onDestroy();
    }

    public void onLocationEnabled() {
        startService(serviceIntent);
    }

    public void onLocationNotEnabled() {
        serviceSwitch.setChecked(false);
    }

    //delete local data file storage
    public void initiateDelete(View v) {
        DataFileManager dFM = new DataFileManager(getApplicationContext());
        dFM.deleteAllDataFiles();
    }

    public void initiateSend(View v) {
        Intent i = new Intent(this, SendActivity.class);
        startActivity(i);
    }

    public class CounterReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent){
            serviceSwitch.setText(String.format("Service running: %d\n",
                    intent.getIntExtra(ScanService.GOTS_COUNTER_EXTRA, 0)));
        }
    }

    private class BatteryReceiver extends BroadcastReceiver  {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_LOW)) {
                scanEnabled = false;
                serviceSwitch.setChecked(false);
            } else if (action.equals(Intent.ACTION_BATTERY_OKAY)) {
                scanEnabled = true;
            }
        }
    }

}
