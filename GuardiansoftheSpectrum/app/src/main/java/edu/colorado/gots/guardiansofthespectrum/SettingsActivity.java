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

/**
 * This Activity acts as the Settings page for the application and provides an interface
 * for users to alter the behavior of the app.
 */
public class SettingsActivity extends LocationActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * The fragment containing the UI for the various options we provide.
     */
    private SettingsFragment fragment;
    /**
     * The toggle switch allowing the user to start and stop the background service that performs
     * the scan.
     */
    private SwitchPreference serviceSwitch;
    /**
     * A reference to the stored Intent used to start the background service if the user chooses
     * to start it and grants all necessary permissions for its operation.
     */
    private Intent serviceIntent;
    /**
     * A flag intended to be used with the <code>BatteryReceiver</code> class to prevent the app
     * from allowing the background scanning service from being started if the system is low on
     * battery power.
     * @see BatteryReceiver
     */
    private boolean scanEnabled = true;
    /**
     * A reference to the <code>BatteryReceiver</code> class to handle receiving power notifications
     * from the Android operating system.
     */
    private BatteryReceiver batteryReceiver;

    /**
     * Pushes a notification to the user's taskbar indicating that there is a background scan
     * running.
     * @see #cancelNotification()
     */
    private void pushNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        //taskbar icon
        builder.setSmallIcon(R.drawable.wave_notif);
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

    /**
     * Remove the notification from the user's taskbar indicating that a background scan
     * is running.
     * @see #pushNotification()
     */
    private void cancelNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
    }

    /**
     * Initialize the necessary activity state and layout
     * @param savedInstanceState Ignored
     */
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

        serviceIntent = new Intent(this, ScanService.class);
        serviceIntent.setAction(ScanService.GOTS_SCAN_BACKGROUND_START);
        fragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(R.id.settingsFragLayout, fragment).commit();
        //force outstanding transactions to complete, else, we can get null instead of
        //references to the preference items in the following line
        getFragmentManager().executePendingTransactions();
        serviceSwitch = (SwitchPreference) fragment.findPreference("serviceSwitch");
    }

    /**
     * Called when app resumes its execution. Responsible for registering the activity to listen for
     * changes in the options state.
     * @see #onSharedPreferenceChanged(SharedPreferences, String)
     */
    public void onResume() {
        super.onResume();
        fragment.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Call when the app stops executing. Responsible for unregistering the activity to listen for
     * changes in the options.
     * @see #onSharedPreferenceChanged(SharedPreferences, String)
     */
    public void onPause() {
        super.onPause();
        fragment.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when the activity is no longer needed. Responsible for unregistering the receiver for
     * incoming battery level notifications.
     */
    public void onDestroy() {
        unregisterReceiver(batteryReceiver);
        super.onDestroy();
    }

    /**
     * Called when the Google API client successfully establishes a connection. We can now issue
     * a request to check the current permissions granted to the app to see if we can start a
     * background scan if the user wants one.
     * @see LocationServicesManager#checkAndResolvePermissions()
     * @see LocationServicesManager.LocationServicesCallbacks
     */
    public void onConnected() {
        LSManager.checkAndResolvePermissions();
    }

    /**
     * Called when we have all necessary permissions to start a background scan. The service will
     * be started and a notification will be pushed to the user's taskbar if allowed.
     * @see LocationServicesManager.LocationServicesCallbacks
     */
    public void onLocationEnabled() {
        startService(serviceIntent);
        //push notification if we are allowed to
        if (fragment.getPreferenceScreen().getSharedPreferences().getBoolean("notificationEnabled", true)) {
            pushNotification();
        }
    }

    /**
     * Called when permissions are insufficient to allow a background scan sto start.
     * @see LocationServicesManager.LocationServicesCallbacks
     */
    public void onLocationNotEnabled() {
        serviceSwitch.setChecked(false);
    }

    /**
     * Called when the user changes the state of one of the options on the screen.
     * @param pref The new state of the options
     * @param key The key assigned to the option that was changed
     */
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

    /**
     * Delete all local data storage
     * @param v Ignored
     */
    public void initiateDelete(View v) {
        DataFileManager dFM = new DataFileManager(getApplicationContext());
        dFM.deleteAllDataFiles();
        CSVFileManager cFM = new CSVFileManager(getApplicationContext());
        cFM.deleteFiles();
    }

    /**
     * Send collected scan data to the server
     * @param v Ignored
     * @see SendActivity
     */
    public void initiateSend(View v) {
        Intent i = new Intent(this, SendActivity.class);
        startActivity(i);
    }

    /**
     * Class responsible for receiving battery level notifications from the Android OS
     */
    private class BatteryReceiver extends BroadcastReceiver  {
        /**
         * Called when a new notification is received
         * @param context Ignored
         * @param intent An intent containing the information
         */
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
