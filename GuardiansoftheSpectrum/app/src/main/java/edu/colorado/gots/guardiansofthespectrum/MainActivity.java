package edu.colorado.gots.guardiansofthespectrum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView mTextView;
    Switch serviceSwitch;
    Intent serviceIntent;
    private CounterReceiver counterReceiver;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        counterReceiver = new CounterReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(counterReceiver,
                                                                 new IntentFilter(ScanService.GOTS_COUNTER));
        mTextView = (TextView)findViewById(R.id.textview1);
        mTextView.setText(R.string.Hello);
        serviceSwitch = (Switch) findViewById(R.id.scanServiceSwitch);
        serviceIntent = new Intent(this, ScanService.class);
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (isChecked) {
                    //service is enabled so start it up!
                    startService(serviceIntent);
                } else {
                    stopService(serviceIntent);
                }
            }
        });
    }

    //start the scan activity after we get a button click
    public void initiateScan(View v) {
        Intent i = new Intent(this, ScanActivity.class);
        startActivity(i);
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
