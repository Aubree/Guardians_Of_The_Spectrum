package edu.colorado.gots.guardiansofthespectrum;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;


public class MyInfoActivity extends BaseActivity {
    private WebView myWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setTextColor(0xFFFFFFFF);
        textView.setTextSize(getResources().getDimension(R.dimen.textsize));
        textView.getPaddingTop();
        textView.setText("Hardware information: " + System.getProperty("line.separator") + "- " +
                "hardware name: " + Build
                .HARDWARE + System.getProperty("line.separator") + "- device name: " + Build
                .DEVICE + System.getProperty("line.separator") + "- " +
                "manufacture name: " + Build.MANUFACTURER);

        BarChart myinfo_chart = (BarChart) findViewById(R.id.chart);

        //enable interaction
        myinfo_chart.setTouchEnabled(true);
        myinfo_chart.setDragEnabled(true);
        myinfo_chart.setScaleEnabled(true);
        myinfo_chart.setScaleXEnabled(true);
        myinfo_chart.setScaleYEnabled(true);
        myinfo_chart.setPinchZoom(true);
        myinfo_chart.setDoubleTapToZoomEnabled(true);

        //chart fling / deceleration
        myinfo_chart.setDragDecelerationEnabled(true);
        myinfo_chart.setDragDecelerationFrictionCoef(1);

        XAxis xline = myinfo_chart.getXAxis();
        xline.setPosition(XAxis.XAxisPosition.BOTTOM);
        xline.setTextSize(10f);
        xline.setTextColor(Color.RED);
        xline.setDrawAxisLine(true);
        xline.setDrawGridLines(false);

        YAxis yline = myinfo_chart.getAxisLeft();
        yline.setDrawLabels(false); // no axis labels
        yline.setDrawAxisLine(false); // no axis line
        yline.setDrawGridLines(false); // no grid lines
        yline.setDrawZeroLine(true); // draw a zero line
        myinfo_chart.getAxisRight().setEnabled(false); // no right axis

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 30f));
        entries.add(new BarEntry(1f, 80f));
        entries.add(new BarEntry(2f, 60f));
        entries.add(new BarEntry(3f, 50f));
        // gap of 2f
        entries.add(new BarEntry(5f, 70f));
        entries.add(new BarEntry(6f, 60f));

        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        BarData lineData = new BarData(set);
        lineData.setBarWidth(0.9f); // set custom bar width
        myinfo_chart.setData(lineData);
        myinfo_chart.setFitBars(true); // make the x-axis fit exactly all bars
        myinfo_chart.invalidate(); // refresh

    }


}


/*

myWebView = (WebView) findViewById(R.id.activity_webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);


        //myWebView.loadUrl("http://beta.html5test.com/");
        String content = "<html>"
                + "  <head>"
                + "    <script type=\"text/javascript\" src=\"jsapi.js\"></script>"
                + "    <script type=\"text/javascript\">"
                + "      google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"
                + "      google.setOnLoadCallback(drawChart);"
                + "      function drawChart() {"
                + "        var data = google.visualization.arrayToDataTable(["
                + "          ['Year', 'Sales', 'Expenses'],"
                + "          ['2010',  1000,      400],"
                + "          ['2011',  1170,      460],"
                + "          ['2012',  660,       1120],"
                + "          ['2013',  1030,      540]"
                + "        ]);"
                + "        var options = {"
                + "          title: 'Truiton Performance',"
                + "          hAxis: {title: 'Year', titleTextStyle: {color: 'red'}}"
                + "        };"
                + "        var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));"
                + "        chart.draw(data, options);"
                + "      }"
                + "    </script>"
                + "  </head>"
                + "  <body>"
                + "    <div id=\"chart_div\" style=\"width: 1000px; height: 500px;\"></div>"
                + "	   <img style=\"padding: 0; margin: 0 0 0 330px; display: block;\" src=\"truiton.png\"/>"
                + "  </body>" + "</html>";

        webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.requestFocusFromTouch();
        myWebView.loadDataWithBaseURL( "file:///android_asset/", content, "text/html", "utf-8", null );
        myWebView.loadUrl("file:///android_asset/Code.html"); // Can be used in this way too.

 */