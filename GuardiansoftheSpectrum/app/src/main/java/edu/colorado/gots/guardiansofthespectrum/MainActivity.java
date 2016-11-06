package edu.colorado.gots.guardiansofthespectrum;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView mTextView;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView)findViewById(R.id.textview1);
        mTextView.setText(R.string.Hello);
    }

    //start the scan activity after we get a button click
    public void initiateScan(View v) {
        Intent i = new Intent(this, WifiScanActivity.class);
        startActivity(i);
    }
}
