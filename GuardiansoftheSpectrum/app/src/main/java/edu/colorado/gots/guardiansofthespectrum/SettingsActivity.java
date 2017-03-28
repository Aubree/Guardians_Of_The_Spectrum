package edu.colorado.gots.guardiansofthespectrum;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;


public class SettingsActivity extends LocationActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingsFragment fragment;
    private SwitchPreference serviceSwitch;
    private Intent serviceIntent;
    private boolean scanEnabled = true;
    private BatteryReceiver batteryReceiver;
    private CounterReceiver counterReceiver;

    private void pushNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        //taskbar icon
        builder.setSmallIcon(R.drawable.notification);
        //title and text on notification
        builder.setContentTitle(getResources().getString(R.string.notificationTitle));
        builder.setContentText(getResources().getString(R.string.notificationText));
        //display updating running time
        builder.setUsesChronometer(true);
        //set category for the notification
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        //create intent to start when user taps on the notification
        Intent i = new Intent(this, SettingsActivity.class);
        //enable navigation by pressing back button when tapping notification
        TaskStackBuilder taskBuilder = TaskStackBuilder.create(this);
        taskBuilder.addParentStack(SettingsActivity.class);
        taskBuilder.addNextIntent(i);
        //set pending intent to call on notification tap
        builder.setContentIntent(taskBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
        //send out the notification
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, builder.build());
    }

    private void cancelNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
    }

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
        //push notification if we are allowed to
        if (fragment.getPreferenceScreen().getSharedPreferences().getBoolean("notificationEnabled", true)) {
            pushNotification();
        }
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
                cancelNotification();
            }
        } else if (key.equals("storageCap")) {
            String cap = pref.getString(key, "");
            ListPreference l = (ListPreference) fragment.findPreference(key);
            l.setSummary(getResources().getString(R.string.storageCapDesc, cap));
        } else if (key.equals("notificationEnabled")) {
            boolean isEnabled = pref.getBoolean(key, true);
            CheckBoxPreference c = (CheckBoxPreference) fragment.findPreference(key);
            if (isEnabled) {
                c.setSummary(getResources().getString(R.string.notifyOnDesc));
                //push a notification if the service is running
                if (fragment.getPreferenceScreen().getSharedPreferences().getBoolean("serviceSwitch", false)) {
                    pushNotification();
                }
            } else {
                c.setSummary(getResources().getString(R.string.notifyOffDesc));
                cancelNotification();
            }
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
