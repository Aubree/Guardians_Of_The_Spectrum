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

//public class SettingsActivity extends BaseActivity implements LocationServicesManager.LocationServicesCallbacks {
public class SettingsActivity extends LocationActivity {
    private Switch serviceSwitch;
    private Intent serviceIntent;
    private boolean scanEnabled = true;
    private static boolean switchState = false;
    private BatteryReceiver batteryReceiver;
    private CounterReceiver counterReceiver;

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
        //LSManager = new LocationServicesManager(this);
        serviceSwitch = (Switch) findViewById(R.id.scanServiceSwitch);
        serviceSwitch.setChecked(switchState);
        serviceIntent = new Intent(this, ScanService.class);
        serviceIntent.setAction(ScanService.GOTS_SCAN_BACKGROUND_START);
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (isChecked && scanEnabled) {
                    //LSManager.checkAndResolvePermissions();
                    LSManager.connect();
                } else {
                    stopService(serviceIntent);
                    switchState = false;
                }
            }
        });
    }

    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(counterReceiver);
        unregisterReceiver(batteryReceiver);
        super.onDestroy();
    }

    public void onConnected() {
        LSManager.checkAndResolvePermissions();
    }

    public void onLocationEnabled() {
        startService(serviceIntent);
        switchState = true;
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


    /*protected void onActivityResult(int requestCode, int returnCode, Intent i) {
        switch (requestCode) {
            case LocationServicesManager.LOCATION_SERVICE_RESOLUTION:
                if (returnCode != Activity.RESULT_OK) {
                    //changes not made successfully. just gripe for now
                    Toast.makeText(getApplicationContext(), "Location services needed to send data", Toast.LENGTH_SHORT).show();
                    onLocationNotEnabled();
                } else {
                    onLocationEnabled();
                }
                break;
            default:
                break;
        }
    }*/

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
