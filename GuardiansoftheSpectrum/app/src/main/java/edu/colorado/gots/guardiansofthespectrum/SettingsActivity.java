package edu.colorado.gots.guardiansofthespectrum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends LocationActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingsFragment fragment;
    private SwitchPreference serviceSwitch;
    private Intent serviceIntent;
    private boolean scanEnabled = true;
    private BatteryReceiver batteryReceiver;
    private CounterReceiver counterReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
        //serviceSwitch = (Switch) findViewById(R.id.scanServiceSwitch);
        //serviceSwitch.setChecked(switchState);
        serviceIntent = new Intent(this, ScanService.class);
        serviceIntent.setAction(ScanService.GOTS_SCAN_BACKGROUND_START);
        /*serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (isChecked && scanEnabled) {
                    //LSManager.checkAndResolvePermissions();
                    LSManager.connect();
                } else {
                    stopService(serviceIntent);
                    switchState = false;
                }
            }
        });*/
        fragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(R.id.settingsFragLayout, fragment).commit();
        //force outstanding transactions to complete, else, we can get null instead of
        //references to the preference items in the following line
        getFragmentManager().executePendingTransactions();
        serviceSwitch = (SwitchPreference) fragment.findPreference("serviceSwitch");
    }

    public void onResume() {
        super.onResume();
        fragment.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public void onPause() {
        super.onPause();
        fragment.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
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
    }

    public void onLocationNotEnabled() {
        serviceSwitch.setChecked(false);
    }

    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (key.equals("serviceSwitch")) {
            boolean isChecked = pref.getBoolean(key, false);
            if (isChecked && scanEnabled) {
                LSManager.connect();
            } else {
                stopService(serviceIntent);
            }
        } else if (key.equals("storageCap")) {
            String cap = pref.getString(key, "");
            ListPreference l = (ListPreference) fragment.findPreference(key);
            l.setSummary(getResources().getString(R.string.storageCapDesc, cap));
        }
    }

    //delete local data file storage
    public void initiateDelete(View v) {
        DataFileManager dFM = new DataFileManager(getApplicationContext());
        dFM.deleteAllDataFiles();
        CSVFileManager cFM = new CSVFileManager(getApplicationContext());
        cFM.deleteFiles();
    }

    public void initiateSend(View v) {
        Intent i = new Intent(this, SendActivity.class);
        startActivity(i);
    }

    public class CounterReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent){
            serviceSwitch.setTitle(String.format("Service running: %d\n",
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
