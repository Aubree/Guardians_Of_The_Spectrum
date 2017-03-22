package edu.colorado.gots.guardiansofthespectrum;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

public abstract class BaseActivity extends AppCompatActivity {
    private ArrayAdapter<String> mAdapter;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    @Override
    //responsible for adding in the NavigationDrawer Items, and setting the
    //base content view
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);
        addDrawerItems();
    }

    @Override
    //instead of overriding the base content view, we inflate the requested one
    //into the main FrameLayout inside the DrawerLayout
    public void setContentView(@LayoutRes int layoutResID) {
        FrameLayout content = (FrameLayout) findViewById(R.id.contentLayout);
        content.addView(getLayoutInflater().inflate(layoutResID, null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.our_menu, menu);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //mToolbar.setTitle("NTIA");
        mToolbar.setLogo(R.drawable.home);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            /*
            case R.id.activity_main:
                NavUtils.navigateUpFromSameTask(this);
                return true; */
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
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void addDrawerItems() {
        String[] mTitles = {"Scan", "My Info", "Settings", "About"};
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.sidebar);
        mDrawerList = (ListView) findViewById(R.id.navList);
        //setting adapter for the list view.
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mTitles);
        mDrawerList.setAdapter(mAdapter);
        //set the list's click listener.
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent scan = new Intent(BaseActivity.this, ScanActivity.class);
                        startActivity(scan);
                        break;
                    case 1:
                        Intent info = new Intent(BaseActivity.this, MyInfoActivity.class);
                        startActivity(info);
                        break;
                    case 2:
                        Intent settings = new Intent(BaseActivity.this, SettingsActivity.class);
                        startActivity(settings);
                        break;
                    case 3:
                        Intent about = new Intent(BaseActivity.this, AboutActivity.class);
                        startActivity(about);
                        break;
                    default:
                        throw new RuntimeException("unreachable");
                }
            }
        });
    }
}
