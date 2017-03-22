package edu.colorado.gots.guardiansofthespectrum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;


public class MyInfoActivity extends BaseActivity {
    private WebView myWebView;
    String[] info_options = {"Hardware Info", "Cell Connection", "WiFi Connection"};
    private ListView my_listview;

    CSVFileManager csvManager;

    //Make image view and set it to gone -> set visibility
//Then in Java code write a listener
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_myinfo);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setTextSize(getResources().getDimension(R.dimen.textsize));
        textView.getPaddingTop();
        textView.setText("Hardware information: " + System.getProperty("line.separator") + "- " +
                "hardware name: " + Build
                .HARDWARE + System.getProperty("line.separator") + "- device name: " + Build
                .DEVICE + System.getProperty("line.separator") + "- " +
                "manufacture name: " + Build.MANUFACTURER);

        my_listview = (ListView) findViewById(R.id.id_list_view);

        ArrayList<BarData> list = new ArrayList<BarData>();


        list.add(generateData(1, "Connection Info"));
        list.add(generateData(2, "WiFi Info"));

        ChartDataAdapter my_adapter = new ChartDataAdapter(getApplicationContext(), list);
        my_listview.setAdapter(my_adapter);

        csvManager = new CSVFileManager(getApplicationContext());
        String data = "";
        for (CSVFileManager.CSVEntry e : csvManager.readData().getAllData()) {
            data += String.format("time: %d, dbm: %d, ssid: %s, rssi: %d\n", e.getTime(), e.getDbm(), e.getSsid(), e.getRssi());
        }
        ((TextView) findViewById(R.id.dataDemo)).setText(data);
    }

    private class ChartDataAdapter extends ArrayAdapter<BarData>{
        public ChartDataAdapter(Context context, List<BarData> objects){
            super(context, 0, objects);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            BarData data = getItem(position);
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.list_item_barchart, null);
                holder.chart = (BarChart) convertView.findViewById(R.id.chart);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // apply styling
            //data.setValueTypeface(mTfLight);
            data.setValueTextColor(Color.BLACK);
            holder.chart.getDescription().setEnabled(false);
            holder.chart.setDrawGridBackground(false);

            XAxis xAxis = holder.chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            //xAxis.setTypeface(mTfLight);
            xAxis.setDrawGridLines(false);


            xAxis.setValueFormatter(new IAxisValueFormatter() {
                private SimpleDateFormat hour_format = new SimpleDateFormat("HH");
                @Override
                public String getFormattedValue(float value, AxisBase axis) {

                    long millis = TimeUnit.HOURS.toMillis((long) value);
                    return hour_format.format(new Date(millis));
                }
            });



            YAxis leftAxis = holder.chart.getAxisLeft();
            //leftAxis.setTypeface(mTfLight);
            leftAxis.setLabelCount(5, false);
            leftAxis.setSpaceTop(15f);

            YAxis rightAxis = holder.chart.getAxisRight();
            //rightAxis.setTypeface(mTfLight);
            rightAxis.setLabelCount(5, false);
            rightAxis.setSpaceTop(15f);

            // set data
            holder.chart.setData(data);
            holder.chart.setFitBars(true);

            // do not forget to refresh the chart
            holder.chart.invalidate();
            holder.chart.animateY(700);

            return convertView;
        }
        private class ViewHolder {

            BarChart chart;
        }
    }
    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return
     */
    private BarData generateData(int cnt, String str) {

        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

        for (int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i, (float) (Math.random() * 70) + 30));
        }
        BarDataSet d = new BarDataSet(entries, str);
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);
        d.setBarShadowColor(Color.rgb(203, 203, 203));

        ArrayList<IBarDataSet> sets = new ArrayList<IBarDataSet>();
        sets.add(d);

        BarData cd = new BarData(sets);
        cd.setBarWidth(0.9f);
        return cd;
    }
}



