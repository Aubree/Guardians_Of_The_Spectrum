package edu.colorado.gots.guardiansofthespectrum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Switch;

import android.widget.TextView;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    TextView mTextView;
    Switch serviceSwitch;
    Intent serviceIntent;

    protected boolean scanEnabled = true;
    private BatteryReceiver batteryReceiver;


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        //set up a listener to manage low-battery notifications
        batteryReceiver = new BatteryReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        registerReceiver(batteryReceiver, new IntentFilter());


        mTextView = (TextView)findViewById(R.id.textview1);
        mTextView.setText(R.string.Hello);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.our_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_scan:
                Intent scan = new Intent(this, ScanActivity.class);
                startActivity(scan);
                return true;
            case R.id.action_my_info:
                Intent info = new Intent(this, MyInfoActivity.class);
                startActivity(info);
                return true;
            case R.id.action_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.action_about:
                Intent about = new Intent(this, SettingsActivity.class);
                startActivity(about);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    public void onDestroy() {
        unregisterReceiver(batteryReceiver);
        super.onDestroy();
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