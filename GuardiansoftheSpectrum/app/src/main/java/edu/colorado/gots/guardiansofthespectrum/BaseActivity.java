package edu.colorado.gots.guardiansofthespectrum;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import edu.colorado.gots.guardiansofthespectrum.AboutActivity;
import edu.colorado.gots.guardiansofthespectrum.MainActivity;
import edu.colorado.gots.guardiansofthespectrum.MyInfoActivity;
import edu.colorado.gots.guardiansofthespectrum.R;
import edu.colorado.gots.guardiansofthespectrum.ScanActivity;
import edu.colorado.gots.guardiansofthespectrum.SettingsActivity;

public class BaseActivity extends MainActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.our_menu, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_scan){
            Intent i = new Intent(this, ScanActivity.class);
            startActivity(i);
            return true;
        }
        if(item.getItemId() == R.id.action_my_info){
            Intent i = new Intent(this, MyInfoActivity.class);
            startActivity(i);
            return true;
        }
        if(item.getItemId() == R.id.action_settings){
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        if(item.getItemId() == R.id.action_about){
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
