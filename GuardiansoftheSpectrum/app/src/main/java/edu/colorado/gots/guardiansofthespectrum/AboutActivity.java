package edu.colorado.gots.guardiansofthespectrum;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.widget.PopupWindow;

public class AboutActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_about); //to be changed

        //AboutDialogFragment dialog = new AboutDialogFragment();
        //dialog.show(getSupportFragmentManager(),"dialog");
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*0.7),(int)(height*0.7));
       //Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
       //setSupportActionBar(mToolbar);
   }
}
