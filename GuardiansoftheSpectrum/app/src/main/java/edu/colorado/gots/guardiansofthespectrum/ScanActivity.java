package edu.colorado.gots.guardiansofthespectrum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.Integer.parseInt;

//public class ScanActivity extends BaseActivity implements LocationServicesManager.LocationServicesCallbacks {
public class ScanActivity extends LocationActivity {

    ImageView bar;
    ScanDataReceiver receiver;

    List<Entry> entries;
    LineChart chart;
    int count;

    private boolean scanning = false;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        //initialize line chart
        chart = (LineChart) findViewById(R.id.chart);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);


        // set an alternative background color
        chart.setBackgroundColor(Color.WHITE);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.rgb(0, 0, 255));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f); // one hour
        /*xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });*/

        YAxis leftAxis = chart.getAxisLeft();

        LimitLine ll = new LimitLine(-120f, "Poor Signal");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(4f);
        ll.setTextColor(Color.BLACK);
        ll.setTextSize(12f);

        LimitLine l2 = new LimitLine(-90f, "Strong Signal");
        l2.setLineColor(Color.GREEN);
        l2.setLineWidth(4f);
        l2.setTextColor(Color.BLACK);
        l2.setTextSize(12f);

        leftAxis.addLimitLine(ll);
        leftAxis.addLimitLine(l2);

        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(-130f);
        leftAxis.setAxisMaximum(-80f);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.rgb(0, 0, 255));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        // initialize data set for chart
        entries = new ArrayList<>();
        count = 1;


        bar = (ImageView) findViewById(R.id.scanProgressAnim);
        ((AnimationDrawable) bar.getDrawable()).start();
        bar.setVisibility(VISIBLE);
        receiver = new ScanDataReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(ScanService.GOTS_SCAN_SERVICE_RESULTS));
        LSManager.connect();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    protected void onStop() {
        //only send an intent to the service if it's running successfully
        //(i.e. all permission checks have passed and googleApiClient has
        //connected), otherwise we'll start running into errors if permissions
        //and connections aren't present because telling the service to stop will
        //start it up
        if (scanning) {
            Intent serviceIntent = new Intent(this, ScanService.class);
            serviceIntent.setAction(ScanService.GOTS_SCAN_FOREGROUND_END);
            startService(serviceIntent);
        }
        super.onStop();
    }

    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void onConnected() {
        LSManager.checkAndResolvePermissions();
    }

    public void onLocationEnabled() {
        System.out.println("starting service from scan activity");
        scanning = true;
        Intent serviceIntent = new Intent(this, ScanService.class);
        serviceIntent.setAction(ScanService.GOTS_SCAN_FOREGROUND_START);
        startService(serviceIntent);
    }

    public void onLocationNotEnabled() {
        bar.setVisibility(GONE);
    }


    /**
     *
     * Parses data received from scan data receiver.
     * The data parsed will be sent to create a data set for graph.
     * @param data String of the JSON object from ScanDataReceiver
     */
    protected void ParseData(String data){
        //String[] objects = data.split(Pattern.quote("{"));
        Log.d("ParseData", data);
        int pos = data.indexOf("Dbm");
        int endPos = data.indexOf("CellID");


        // check to ensure the LTE object was not null
        if( (pos != -1) && (endPos != -1) ) {
            // example of the string: Dbm":-104,"CellID
            String sdbm = data.substring(pos + 5, endPos - 2);
            int dbm = parseInt(sdbm);
            // discard unwanted values of dbm
            if(dbm < 0) {
                entries.add(new Entry(count, dbm));
                count++;
            }
        }

        if (entries.size() > 0){
            LineDataSet dataSet = new LineDataSet(entries, "LTE Dbm"); // add entries to dataset
            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSet.setColor(ColorTemplate.getHoloBlue());
            dataSet.setValueTextColor(ColorTemplate.getHoloBlue());
            dataSet.setLineWidth(1.5f);
            LineData lineData = new LineData(dataSet);
            //chart.animateX(3000);
            chart.setData(lineData);
            chart.invalidate(); // refresh
        }
    }

    private class ScanDataReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent i) {
            ((AnimationDrawable) bar.getDrawable()).stop();
            bar.setVisibility(GONE);
            chart.setVisibility(VISIBLE);
            ParseData(i.getStringExtra(ScanService.GOTS_SCAN_SERVICE_RESULTS_EXTRA));
        }
    }



}