package edu.colorado.gots.guardiansofthespectrum;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;


public class MyInfoActivity extends MainActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }
}
