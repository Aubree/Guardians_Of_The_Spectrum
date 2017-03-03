package edu.colorado.gots.guardiansofthespectrum;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class MainActivity extends BaseActivity {
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

    }

}