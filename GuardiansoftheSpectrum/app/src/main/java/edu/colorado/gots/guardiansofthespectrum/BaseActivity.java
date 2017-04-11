package edu.colorado.gots.guardiansofthespectrum;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
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

    protected Toolbar mToolbar;

    @Override
    //responsible for adding in the NavigationDrawer Items, and setting the
    //base content view
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setLogo(R.drawable.home);
        mToolbar.setTitle("NTIA");
        setSupportActionBar(mToolbar);
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
//        MenuInflater mMenuInflater = getMenuInflater();
//        mMenuInflater.inflate(R.menu.our_menu, menu);
//
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("NTIA");
        //mToolbar.setLogo(R.drawable.home);
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
                AboutDialogFragment aboutDialog = new AboutDialogFragment();
                aboutDialog.show(getSupportFragmentManager(), "aboutDialog");
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
                        AboutDialogFragment aboutDialog = new AboutDialogFragment();
                        aboutDialog.show(getSupportFragmentManager(), "aboutDialog");
                        break;
                    default:
                        throw new RuntimeException("unreachable");
                }
            }
        });

        /*mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, R.string
                .drawer_open, R.string.drawer_close) {
            *//**
         * Called when a drawer has settled in a completely closed state.
         *//*
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (mTitle != null){
                    getActionBar().setTitle(mTitle);
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            *//**
         * Called when a drawer has settled in a completely open state.
         *//*
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);
    }

    *//* Called whenever we call invalidateOptionsMenu() *//*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);//!!!!!!!!!!!!!!!!!!!!!!!
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);

    }*/
    }
}
