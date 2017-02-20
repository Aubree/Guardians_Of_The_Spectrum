package edu.colorado.gots.guardiansofthespectrum;


import android.app.Activity;
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
import android.widget.Toast;

public class SettingsActivity extends BaseActivity implements LocationServicesManager.LocationServicesCallback {
    private Switch serviceSwitch;
    private Intent serviceIntent;
    private CounterReceiver counterReceiver;
    private LocationServicesManager LSManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

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
        super.onDestroy();
    }

    //called when an activity we start gets completed. In this case, we're interested
    //in the ResultCallback we set up for out LocationSettingsRequest. After the user
    //completes the dialogue, we will parse the results here.
    protected void onActivityResult(int requestCode, int returnCode, Intent i) {
        switch (requestCode) {
            case LocationServicesManager.LOCATION_SERVICE_RESOLUTION:
                if (returnCode != Activity.RESULT_OK) {
                    //changes not made successfully. just gripe for now
                    Toast.makeText(getApplicationContext(), "Location services needed to send data", Toast.LENGTH_SHORT).show();
                    serviceSwitch.setChecked(false);
                } else {
                    System.out.println("sending start service request\n");
                    //trigger service start
                    startService(serviceIntent);
                }
                break;
            default:
                break;
        }
    }

    public void onLocationEnabled() {
        startService(serviceIntent);
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

    

}
