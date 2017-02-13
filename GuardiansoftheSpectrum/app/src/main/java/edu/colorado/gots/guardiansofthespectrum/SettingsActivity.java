package edu.colorado.gots.guardiansofthespectrum;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends BaseActivity {
    private CounterReceiver counterReceiver;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //set up a quick and dirty listener to receiver counter updates from the service
        counterReceiver = new CounterReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(counterReceiver,
                new IntentFilter(ScanService.GOTS_COUNTER));

        serviceSwitch = (Switch) findViewById(R.id.scanServiceSwitch);
        serviceIntent = new Intent(this, ScanService.class);
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (isChecked && scanEnabled) {
                    //service is enabled so start it up!
                    startService(serviceIntent);
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
