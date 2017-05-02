package edu.colorado.gots.guardiansofthespectrum;

import android.content.Intent;
import android.content.res.Configuration;
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
    private ActionBarDrawerToggle mDrawerToggle;
    protected Toolbar mToolbar;

    /**
     * Responsible for setting up the ActionBar, populating the Navigation Drawer,
     * and setting the base Content View which will allow the Navigation Drawer to
     * appear in all subclassed Activities
     * @param savedInstanceState The saved state from any previous instances
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24px);

        setSupportActionBar(mToolbar);

        //prevents title from showing on the bar
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        addDrawerItems();
    }

    /**
     * Override that allows us to inflate the requested layout into the <code>FrameView</code> in
     * the base layout, rather than replace the entire screen. This allows the NavigationDrawer to
     * appear in every screen.
     * @param layoutResID The ID of the layout we wish to display
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        FrameLayout content = (FrameLayout) findViewById(R.id.contentLayout);
        content.addView(getLayoutInflater().inflate(layoutResID, null));
    }

    //DELETE
    /**
     * Called when we need to populate the ActionBar with our various menu items
     * @param menu The base menu into which we will inflate our custom menu
     * @return Always true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.our_menu, menu);
        return true;
    }

    /**
     * Called when an Item in the ActionBar is selected by the user.
     * @param item The item that was clicked
     * @return True if the item selected can be handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_home:
                if(!(this instanceof MainActivity)){
                    NavUtils.navigateUpFromSameTask(BaseActivity.this);
                }
                return true;
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

    /**
     * Populate the <code>NavigationDrawer</code> with the necessary items, and
     * set up listeners to handle when they are selected.
     */
    private void addDrawerItems() {
        final DrawerLayout navLayout = (DrawerLayout) findViewById(R.id.sidebar);
        ListView navList = (ListView) findViewById(R.id.navList);
        //setting adapter for the list view.
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.navDrawerLabels));
        navList.setAdapter(adapter);
        //set the list's click listener.
        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                navLayout.closeDrawers();
                switch (position) {
                    case 0:
                        if(!(BaseActivity.this instanceof MainActivity)){
                            NavUtils.navigateUpFromSameTask(BaseActivity.this);
                        }
                        break;
                    case 1:
                        Intent scan = new Intent(BaseActivity.this, ScanActivity.class);
                        startActivity(scan);
                        break;
                    case 2:
                        Intent info = new Intent(BaseActivity.this, MyInfoActivity.class);
                        startActivity(info);
                        break;
                    case 3:
                        Intent settings = new Intent(BaseActivity.this, SettingsActivity.class);
                        startActivity(settings);
                        break;
                    case 4:
                        AboutDialogFragment aboutDialog = new AboutDialogFragment();
                        aboutDialog.show(getSupportFragmentManager(), "aboutDialog");
                        break;
                    default:
                        throw new RuntimeException("unreachable");
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(BaseActivity.this, navLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close);
        // Set the drawer toggle as the DrawerListener
        navLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Override needed by <code>ActionBarDrawerToggle</code>
     * Must call <code>ActionBarDrawerToggle.syncState()</code> to force
     * Drawer into appropriate open/close state after reloading screen
     * @param savedInstanceState saved state as passed to <code>onCreate</code>
     */
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /**
     * Override needed by the <code>ActionBarDrawerToggle</code> class.
     * We must call <code>ActionBarDrawerToggle.onConfigurationChanged</code>
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
