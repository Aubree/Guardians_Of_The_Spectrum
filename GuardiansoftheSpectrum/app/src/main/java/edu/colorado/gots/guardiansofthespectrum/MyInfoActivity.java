package edu.colorado.gots.guardiansofthespectrum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * My info provides local phone information: LTE and WiFi signal strength, device information.
 * Local visualization is done using a free tool MPAnroidChart
 * https://github.com/PhilJay/MPAndroidChart
 * Bar chart code is following ListViewBarChartActivity.java
 */
public class MyInfoActivity extends BaseActivity {
    /**
     * Listview creates a view, in which graphs are treated as a list.
     */
    private ListView my_listview;
    /**
     * mTfLight styles the text that is on the graphs.
     */
    protected Typeface mTfLight;
    /**
     * CSV manager brings signal information.
     * @see CSVFileManager
     */
    CSVFileManager csvManager;
    /**
     * Button for graphs leads to a window that gives additional information about graphs.
     * @see ReadingGraphFragment
     */
    Button graphs_button;
    /**
     * Button for hardware leads to the device information activity.
     * @see HardwareInfo
     */
    Button button_hardware;

    /**
     * On create sets up the window, puts listview and buttons.
     * Calls for reading data from CSV file.
     * @see #generateData(int, String)
     * @param savedInstanceState The saved state from any previous instances
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gets the layout of this activity.
        setContentView(R.layout.activity_myinfo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);

        // Setting up buttons.
        graphs_button = (Button) findViewById(R.id.button_graphs);
        graphs_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ReadingGraphFragment GraphDialog = new ReadingGraphFragment();
                GraphDialog.show(getSupportFragmentManager(), "Graph_dialog");
            }
        });
        button_hardware = (Button) findViewById(R.id.button_hardware);
        button_hardware.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyInfoActivity.this, HardwareInfo.class);
                startActivity(intent);
            }
        });

        // Bar graphs are set up within the Listview.
        // Data for the bars is generated in generate.Data()
        // Once the graphs are ready, they need to go into the ChartDataAdapter to set them into
        // the listview.
        my_listview = (ListView) findViewById(R.id.id_list_view);
        ArrayList<BarData> list = new ArrayList<BarData>();
        list.add(generateData(1, "Effective Cell Connection Strength in dBm."));
        list.add(generateData(2, "Effective WiFi Connection Strength in dBm."));
        ChartDataAdapter my_adapter = new ChartDataAdapter(getApplicationContext(), list);
        my_listview.setAdapter(my_adapter);
    }
    /**
     * Adapter is taken directly from MPAndoid Charts.
     * Adapter sets the view in the requested graph type.
     * Here adapter sets the view into the bar chart: colors, text, size, etc.
     */
    private class ChartDataAdapter extends ArrayAdapter<BarData>{
        public ChartDataAdapter(Context context, List<BarData> objects){
            super(context, 0, objects);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            BarData data = getItem(position);
            // Holder is a wrapper for the graph.
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.list_item_barchart, null);
                // Putting bar chart into holder.
                holder.chart = (BarChart) convertView.findViewById(R.id.chart);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            // apply styling.
            // MPAndroid charts does not support Y axis label.
            // "About Graphs" button explains Y - signal strength, X - time of measurement.
            data.setValueTypeface(mTfLight);
            data.setValueTextColor(Color.BLACK);

            Description descriptionX = new Description();
            descriptionX.setText("Time");
            holder.chart.setDescription(descriptionX);
            holder.chart.getDescription().setEnabled(true);

            Legend legend = holder.chart.getLegend();
            // Sets the size of the legend forms/shapes.
            legend.setFormSize(10f);
            // Sets circle for the legend.
            legend.setForm(Legend.LegendForm.CIRCLE);
            legend.setTextSize(12f);
            legend.setTextColor(Color.BLACK);
            // Sets the space between the legend entries on the x and y-axis.
            legend.setXEntrySpace(5f);
            legend.setYEntrySpace(5f);


            holder.chart.setDrawGridBackground(false);
            XAxis xAxis = holder.chart.getXAxis();
            holder.chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
            holder.chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTypeface(mTfLight);
            xAxis.setDrawGridLines(false);
            // Sets x-axis number line.
            xAxis.setGranularity(1f);
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

            // Sets data to the chart.
            holder.chart.setData(data);
            holder.chart.setFitBars(true);

            // Refreshes the chart.
            holder.chart.invalidate();
            holder.chart.animateY(700);

            return convertView;
        }
        private class ViewHolder {

            BarChart chart;
        }
    }

    /**
     * Taking data from CSV file and adjusting it for the bar graph representation.
     * @param cnt
     * @param str
     * @return
     */
    private BarData generateData(int cnt, String str) {

        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
        String data = "";
        String time = "";
        int time_int = 0;
        // Cell signal strength.
        float dbm;
        // Service identifier.
        String ssid = "";
        // WiFi signal strength.
        float rssi;
        int count = 0;

        // Getting data from the CSV file written by CSVFileManager.java
        csvManager = new CSVFileManager(getApplicationContext());
        List<CSVFileManager.CSVEntry> csvData = csvManager.readData().getAllData();
        for (CSVFileManager.CSVEntry e : csvData) {
            time += String.format("", e.getTime());
            ssid = String.format("", e.getSsid());

            // Both signal strengths will be in the negative, which makes it hard to show in a bar
            // chart format. Because of this, we are adding values to the data and making a note
            // of that in MyInfoActivity "About Graphs" button.
            dbm = e.getDbm() - (-150);
            rssi = e.getRssi() - (-127);

            if (cnt == 2){
                    entries.add(new BarEntry(count, rssi));
            }
            else {
                    entries.add(new BarEntry(count, dbm));
            }
            count++;
        }
        BarDataSet d = new BarDataSet(entries, str);
        d.setColors(ColorTemplate.getHoloBlue());
        d.setBarShadowColor(Color.rgb(203, 203, 203));

        BarData cd = new BarData(d);
        return cd;
    }
}