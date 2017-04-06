package edu.colorado.gots.guardiansofthespectrum;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;



public class MyInfoActivity extends BaseActivity {
    String[] info_options = {"Hardware Info", "Cell Connection", "WiFi Connection"};
    private ListView my_listview;
    protected Typeface mTfLight;
    private  Typeface mTypeFaceLight;
    CSVFileManager csvManager;

    //Make image view and set it to gone -> set visibility
//Then in Java code write a listener
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        my_listview = (ListView) findViewById(R.id.id_list_view);
        ArrayList<BarData> list = new ArrayList<BarData>();
        list.add(generateData(1, "Cell Connection Info"));
        //list.add(generateData(2, "WiFi Info"));
        ChartDataAdapter my_adapter = new ChartDataAdapter(getApplicationContext(), list);
        my_listview.setAdapter(my_adapter);
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setTextSize(getResources().getDimension(R.dimen.textsize));
        textView.getPaddingTop();
        textView.setText("Hardware information: " + System.getProperty("line.separator") + "- " +
                "hardware name: " + Build
                .HARDWARE + System.getProperty("line.separator") + "- device name: " + Build
                .DEVICE + System.getProperty("line.separator") + "- " +
                "manufacture name: " + Build.MANUFACTURER);
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
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            // apply styling
            data.setValueTypeface(mTfLight);
            data.setValueTextColor(Color.BLACK);
            holder.chart.getDescription().setEnabled(true);
            holder.chart.getDescription();
            holder.chart.setDrawGridBackground(false);
            XAxis xAxis = holder.chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.TOP);
            //holder.chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
            xAxis.setTypeface(mTfLight);
            xAxis.setDrawGridLines(false);
            holder.chart.setDragEnabled(true);
            holder.chart.setScaleEnabled(true);
            holder.chart.setHighlightPerDragEnabled(true);
            holder.chart.setBackgroundColor(Color.WHITE);
            holder.chart.setNoDataText("No Data");


            YAxis leftAxis = holder.chart.getAxisLeft();
            leftAxis.setTypeface(mTfLight);
            leftAxis.setLabelCount(5, false);
            leftAxis.setSpaceTop(15f);
            leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
            leftAxis.setTextColor(ColorTemplate.getHoloBlue());
            leftAxis.setDrawGridLines(true);
            leftAxis.setGranularityEnabled(true);

            leftAxis.setYOffset(-9f);
            leftAxis.setTextColor(Color.rgb(0, 0, 255));


            YAxis rightAxis = holder.chart.getAxisRight();
            rightAxis.setEnabled(false);
//            rightAxis.setTypeface(mTfLight);
//            rightAxis.setLabelCount(5, false);
//            rightAxis.setSpaceTop(15f);

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

    private BarData generateData(int cnt, String str) {

        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
        String data = "";
        String time = "";
        float dbm; //power
        String ssid = ""; //service identifier
        float rssi; //signal strength

        csvManager = new CSVFileManager(getApplicationContext());

        List<CSVFileManager.CSVEntry> csvData = csvManager.readData().getAllData();
        for (CSVFileManager.CSVEntry e : csvData) {
            time += String.format("", e.getTime());
            ssid = String.format("", e.getSsid());
            dbm = e.getDbm();
            rssi = e.getRssi();
            if (cnt == 1){
                for (int i = 0; i < csvData.size(); i++) {
                    entries.add(new BarEntry(i, rssi));
                }
            }
            else
                for (int i = 0; i < csvData.size(); i++) {
                    entries.add(new BarEntry(i, dbm));
                }
        }

        BarDataSet d = new BarDataSet(entries, str);
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);
        d.setBarShadowColor(Color.rgb(203, 203, 203));

        BarData cd = new BarData(d);
        return cd;
    }
}



//LINE CHART
/*
public class MyInfoActivity extends BaseActivity {
    String[] info_options = {"Hardware Info", "Cell Connection", "WiFi Connection"};
    private ListView my_listview;

    private LineChart mChart;
    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;

    CSVFileManager csvManager;

    //Make image view and set it to gone -> set visibility
//Then in Java code write a listener
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_myinfo);

        mChart = (LineChart) findViewById(R.id.infoChart1);
        mChart.setDrawGridBackground(false);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        // mChart.setBackgroundColor(Color.GRAY);

        // x-axis limit line
//        LimitLine llXAxis = new LimitLine(10f, "Index 10");
//        llXAxis.setLineWidth(4f);
//        llXAxis.enableDashedLine(10f, 10f, 0f);
//        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line


//        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
//
//        LimitLine ll1 = new LimitLine(150f, "Upper Limit");
//        ll1.setLineWidth(4f);
//        ll1.enableDashedLine(10f, 10f, 0f);
//        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
//        ll1.setTextSize(10f);
//        ll1.setTypeface(tf);
//
//        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
//        ll2.setLineWidth(4f);
//        ll2.enableDashedLine(10f, 10f, 0f);
//        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//        ll2.setTextSize(10f);
//        ll2.setTypeface(tf);

//        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
//        leftAxis.addLimitLine(ll1);
//        leftAxis.addLimitLine(ll2);
//        leftAxis.setAxisMaximum(200f);
//        leftAxis.setAxisMinimum(-50f);
//        //leftAxis.setYOffset(20f);
//        leftAxis.enableGridDashedLine(10f, 10f, 0f);
//        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
//        leftAxis.setDrawLimitLinesBehindData(true);
//
//        mChart.getAxisRight().setEnabled(false);

        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        setData(1, "Cell Connection Info");

        mChart.animateX(2500);
        //mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);

        // // dont forget to refresh the drawing
        mChart.invalidate();

//        TextView textView = (TextView) findViewById(R.id.textView);
//        textView.setTextSize(getResources().getDimension(R.dimen.textsize));
//        textView.getPaddingTop();
//        textView.setText("Hardware information: " + System.getProperty("line.separator") + "- " +
//                "hardware name: " + Build
//                .HARDWARE + System.getProperty("line.separator") + "- device name: " + Build
//                .DEVICE + System.getProperty("line.separator") + "- " +
//                "manufacture name: " + Build.MANUFACTURER);

//        my_listview = (ListView) findViewById(R.id.id_list_view);

//        ArrayList<BarData> list = new ArrayList<BarData>();
//
//
//        list.add(generateData(1, "Cell Connection Info"));
//        list.add(generateData(2, "WiFi Info"));
//
//        ChartDataAdapter my_adapter = new ChartDataAdapter(getApplicationContext(), list);
//        my_listview.setAdapter(my_adapter);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }



    private void setData(int cnt, String str) {

        ArrayList<Entry> entries = new ArrayList<Entry>();
        String data = "";
        String time = "";
        float dbm; //power
        String ssid = ""; //service identifier
        float rssi; //signal strength

        csvManager = new CSVFileManager(getApplicationContext());

        List<CSVFileManager.CSVEntry> csvData = csvManager.readData().getAllData();
        for (CSVFileManager.CSVEntry e : csvData) {
            time += String.format("", e.getTime());
            ssid = String.format("", e.getSsid());
            dbm = e.getDbm();
            rssi = e.getRssi();
            if (cnt == 1){
                for (int i = 0; i < csvData.size(); i++) {
                    entries.add(new Entry(i, rssi));
                }
            }
            else
                for (int i = 0; i < csvData.size(); i++) {
                    entries.add(new Entry(i, dbm));
                }
        }


    }
}


 */