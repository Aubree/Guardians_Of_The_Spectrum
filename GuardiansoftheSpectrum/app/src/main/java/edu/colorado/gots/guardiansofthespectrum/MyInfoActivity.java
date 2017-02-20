package edu.colorado.gots.guardiansofthespectrum;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyInfoActivity extends BaseActivity {
    TextView mTextView;
    String [] LTEdata;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);

        // in this example, a LineChart is initialized from xml
        LineChart chart = (LineChart) findViewById(R.id.chart);

        mTextView = (TextView)findViewById(R.id.textView4);
        mTextView.setMovementMethod(new ScrollingMovementMethod());

        LTEdata = new DataFileManager(getApplicationContext()).readAllDataFiles();

        mTextView.setText(Arrays.toString(LTEdata));
        //mTextView.setText(LTEdata[0]);
    }
}
