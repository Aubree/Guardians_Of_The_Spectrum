package edu.colorado.gots.guardiansofthespectrum;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


public class MainActivity extends BaseActivity {
    private LinkDialogFragment link;
    private ArrayAdapter<String> mAdapter;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Sets up a top bar.
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        link = new LinkDialogFragment();

        Button b2 = (Button) findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                link.onClick(MainActivity.this, getSupportFragmentManager());
            }
        });

        //Sets up a sidebar.
        addDrawerItems();

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
                        Intent scan = new Intent(getApplicationContext(), ScanActivity.class);
                        startActivity(scan);
                        break;
                    case 1:
                        Intent info = new Intent(MainActivity.this, MyInfoActivity.class);
                        startActivity(info);
                        break;
                    case 2:
                        Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settings);
                        break;
                    case 3:
                        Intent about = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(about);
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
        */
    }
}