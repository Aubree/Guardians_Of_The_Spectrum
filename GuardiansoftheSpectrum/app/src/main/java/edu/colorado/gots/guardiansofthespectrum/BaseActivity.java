package edu.colorado.gots.guardiansofthespectrum;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class BaseActivity extends AppCompatActivity implements LocationServicesManager.LocationServicesCallbacks {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.our_menu, menu);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("NTIA");
        //mToolbar.setLogo(R.drawable.tool_logo);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_scan:
                LocationServicesManager.getInstance(getApplicationContext()).checkAndResolvePermissions(this);
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
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    protected void onActivityResult(int requestCode, int returnCode, Intent i) {
        switch (requestCode) {
            case LocationServicesManager.LOCATION_SERVICE_RESOLUTION:
                if (returnCode != Activity.RESULT_OK) {
                    //changes not made successfully. just gripe for now
                    Toast.makeText(getApplicationContext(), "Location services needed to send data", Toast.LENGTH_SHORT).show();
                    this.onLocationNotEnabled();
                } else {
                    this.onLocationEnabled();
                }
                break;
            default:
                break;
        }
    }

    public void onLocationEnabled() {
        Intent scan = new Intent(this, ScanActivity.class);
        startActivity(scan);
    }

    public void onLocationNotEnabled() {

    }
}
