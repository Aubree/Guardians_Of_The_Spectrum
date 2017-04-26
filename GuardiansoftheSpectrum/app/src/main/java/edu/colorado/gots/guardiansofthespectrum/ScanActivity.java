package edu.colorado.gots.guardiansofthespectrum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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

    List<Entry> LTEentries;
    List<Entry> WIFIentries;
    LineChart chart1;
    LineChart chart2;
    TextView LTEtext;
    TextView WIFItext;
    TextView waitingText;
    ImageButton moreInfoLTE;
    ImageButton moreInfoWIFI;
    FloatingActionButton pauseFAB;
    int count;
    Boolean pauseGraph;

    private boolean scanning = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        //initialize line chart
        chart1 = (LineChart) findViewById(R.id.chart1);
        chart2 = (LineChart) findViewById(R.id.chart2);
        InitGraph(chart1, true);
        InitGraph(chart2, false);
        // initialize data set for chart
        LTEentries = new ArrayList<>();
        WIFIentries = new ArrayList<>();
        count = 1;
        pauseGraph = false;

        LTEtext = (TextView) findViewById(R.id.text_LTE);
        LTEtext.setText("LTE Data:");
        WIFItext = (TextView) findViewById(R.id.text_Wifi);
        WIFItext.setText("WIFI Data:");
        waitingText = (TextView) findViewById(R.id.waiting_text);
        waitingText.setVisibility(VISIBLE);

        moreInfoLTE = (ImageButton) findViewById(R.id.info1);
        moreInfoLTE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LTEInfoDialogFragment LTEDialog = new LTEInfoDialogFragment();
                LTEDialog.show(getSupportFragmentManager(), "LTE_info_Dialog");
            }
        });

        moreInfoWIFI = (ImageButton) findViewById(R.id.info2);
        moreInfoWIFI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WIFIInfoDialogFragment WIFIDialog = new WIFIInfoDialogFragment();
                WIFIDialog.show(getSupportFragmentManager(), "WIFI_info_Dialog");
            }
        });

        pauseFAB = (FloatingActionButton) findViewById(R.id.pause);
        pauseFAB.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                pauseGraph = !pauseGraph;
                if(pauseGraph) {
                    pauseFAB.setImageResource(android.R.drawable.ic_media_play);
                    Toast.makeText(getApplicationContext(), "Scan Paused", Toast.LENGTH_SHORT).show();
                } else {
                    pauseFAB.setImageResource(android.R.drawable.ic_media_pause);
                    Toast.makeText(getApplicationContext(), "Resuming Scan", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bar = (ImageView) findViewById(R.id.scanProgressAnim);
        ((AnimationDrawable) bar.getDrawable()).start();
        bar.setVisibility(VISIBLE);


        receiver = new ScanDataReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(ScanService.GOTS_SCAN_SERVICE_RESULTS));
        LSManager.connect();
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
     * Parses data received from scan data receiver.
     * If the data received is valid i.e. not NULL and Dbm and rssi are not max int,
     * will pass data to be added to corresponding datasets.
     * @param dbm Int Dbm of the current LTE signal strength
     * @param wifi_ssid String name of the currently connected wifi ap
     * @param wifi_rssi Int signal strength of currently connected wifi ap
     */
    protected void ParseData(int dbm, String wifi_ssid, int wifi_rssi){
        //String[] objects = data.split(Pattern.quote("{"));
        Log.d("ParseData", Integer.toString(dbm));
        Log.d("ParseData", wifi_ssid);
        Log.d("ParseData", Integer.toString(wifi_rssi));

        if(!pauseGraph) {
            if (dbm < 0 || wifi_rssi < 0) {
                addEntry(dbm, wifi_ssid, wifi_rssi);
                count++;
            }
        }
    }

    /**
     * Initializes the line chart for either LTE or WIFI and sets the appearance of graph
     * @param chart LineChant to initialized
     * @param LTE Boolean to determine to setup for LTE (true) or WIFI (false)
     */
    private void InitGraph(LineChart chart, boolean LTE){
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);
        // set an alternative background color
        chart.setBackgroundColor(Color.WHITE);
        chart.setNoDataText("No Data");

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

        if(LTE) {
            LimitLine ll = new LimitLine(-120f, "Poor Signal");
            ll.setLineColor(Color.RED);
            ll.setLineWidth(4f);
            ll.setTextColor(Color.BLACK);
            ll.setTextSize(12f);

            LimitLine l2 = new LimitLine(-95f, "Excellent Signal");
            l2.setLineColor(Color.GREEN);
            l2.setLineWidth(4f);
            l2.setTextColor(Color.BLACK);
            l2.setTextSize(12f);

            leftAxis.addLimitLine(ll);
            leftAxis.addLimitLine(l2);
            leftAxis.setAxisMinimum(-130f);
            leftAxis.setAxisMaximum(-60f);
        }
        else
        {
            LimitLine ll = new LimitLine(-85f, "Poor Signal");
            ll.setLineColor(Color.RED);
            ll.setLineWidth(4f);
            ll.setTextColor(Color.BLACK);
            ll.setTextSize(12f);

            LimitLine l2 = new LimitLine(-55f, "Strong Signal");
            l2.setLineColor(Color.GREEN);
            l2.setLineWidth(4f);
            l2.setTextColor(Color.BLACK);
            l2.setTextSize(12f);

            leftAxis.addLimitLine(ll);
            leftAxis.addLimitLine(l2);
            leftAxis.setAxisMinimum(-100f);
            leftAxis.setAxisMaximum(-40f);
        }
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);

        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.rgb(0, 0, 255));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
        chart.setData(new LineData());
    }

    private class ScanDataReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent i) {
            ((AnimationDrawable) bar.getDrawable()).stop();
            bar.setVisibility(GONE);
            waitingText.setVisibility(GONE);
		    chart1.setVisibility(VISIBLE);
            chart2.setVisibility(VISIBLE);
            LTEtext.setVisibility(VISIBLE);
            WIFItext.setVisibility(VISIBLE);
            moreInfoLTE.setVisibility(VISIBLE);
            moreInfoWIFI.setVisibility(VISIBLE);
            pauseFAB.setVisibility(VISIBLE);
            ParseData(i.getIntExtra(ScanService.GOTS_SCAN_SERVICE_RESULTS_LTE_DBM, Integer.MAX_VALUE),
                    i.getStringExtra(ScanService.GOTS_SCAN_SERVICE_RESULTS_CURRENT_WIFI_SSID),
                    i.getIntExtra(ScanService.GOTS_SCAN_SERVICE_RESULTS_CURRENT_WIFI_RSSI, Integer.MAX_VALUE));
        }
    }

    /**
     * Add an entry to the LTE and WIFI dataset and refresh graph.
     * @param dbm Int value of the LTE signal strength
     * @param wifi_ssid String Name of the curent connected WIFI ap
     * @param wifi_rssi Int signal strength of current connected WIFI ap
     */
    private void addEntry(int dbm, String wifi_ssid, int wifi_rssi) {

        if(dbm < 0) {
            LineData LTEdata = chart1.getData();
            ILineDataSet LTEset = LTEdata.getDataSetByIndex(0);

            if(LTEset == null) {
                LTEset = createSet(true);
                LTEdata.addDataSet(LTEset);
            }
            LTEdata.addEntry(new Entry(LTEset.getEntryCount(), dbm), 0);
            LTEdata.notifyDataChanged();
            // let the charts know it's data has changed
            chart1.notifyDataSetChanged();
            chart1.moveViewToX(LTEdata.getEntryCount());
        }


        if(wifi_rssi < 0 && wifi_rssi > -127) {
            LineData WIFIdata = chart2.getData();
            ILineDataSet WIFIset = WIFIdata.getDataSetByIndex(0);

            if(WIFIset == null) {
                WIFIset = createSet(false);
                WIFIdata.addDataSet(WIFIset);
            }
            WIFIdata.addEntry(new Entry(WIFIset.getEntryCount(), wifi_rssi), 0);
            WIFIdata.notifyDataChanged();
            // let the charts know it's data has changed
            chart2.notifyDataSetChanged();
            chart2.moveViewToX(WIFIdata.getEntryCount());
        }

        // limit the number of visible entries
        chart1.setVisibleXRangeMaximum(8);
        chart2.setVisibleXRangeMaximum(8);

    }

    /**
     * Creates a new LineDataSet and initializes settings
     * @param LTE Boolean value to determine if LTE(true) or WIFI(false) dataset
     * @return returns a dataset for either LTE or WIFI
     */
    private LineDataSet createSet(boolean LTE) {
        LineDataSet set;
        if(LTE) {
            set = new LineDataSet(null, "LTE dBm");
        } else {
            set = new LineDataSet(null, "WIFI dBm");
        }
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);

        return set;
    }
}

