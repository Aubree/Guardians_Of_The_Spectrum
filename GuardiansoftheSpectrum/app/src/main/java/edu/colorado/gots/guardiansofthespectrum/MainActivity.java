package edu.colorado.gots.guardiansofthespectrum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
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

        Button b2 = (Button) findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                link.onClick(MainActivity.this, getSupportFragmentManager());
            }
        });

        addDrawerItems();

    }

    private void addDrawerItems() {
        String[] mTtitles = { "Scan", "My Info", "Settings", "About"};
        mDrawerLayout = (DrawerLayout) findViewById(R.id.sidebar);
        mDrawerList = (ListView) findViewById(R.id.navList);
        //setting adapter for the list view.
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mTtitles);
        mDrawerList.setAdapter(mAdapter);
        //set the list's click listener.
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                switch (position){
                    case 0:
                        Intent scan = new Intent(MainActivity.this, ScanActivity.class);
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
    }

}