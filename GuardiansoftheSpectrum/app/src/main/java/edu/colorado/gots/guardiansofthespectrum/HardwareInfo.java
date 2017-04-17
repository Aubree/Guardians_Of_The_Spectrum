package edu.colorado.gots.guardiansofthespectrum;


import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class HardwareInfo extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware_info);


        TextView textView = (TextView) findViewById(R.id.hardware_info);
        textView.setTextSize(getResources().getDimension(R.dimen.textsize));
        textView.getPaddingTop();
        textView.setTextSize(17);
        textView.setText("Hardware information: " + System.getProperty("line.separator") +
                "- hardware name: " + Build.HARDWARE + System.getProperty("line.separator") +
                "- device name: " + Build.DEVICE + System.getProperty("line.separator") +
                        "- model: " + Build.MODEL + System.getProperty("line.separator") +
                "- manufacture name: " + Build.MANUFACTURER + System.getProperty("line.separator") +
                "- API level: " + Build.VERSION.RELEASE + System.getProperty("line.separator") +
                "- OS version: " + System.getProperty("os.version") + System.getProperty("line.separator") +
                "- OS API level: " + android.os.Build.VERSION.SDK_INT + System.getProperty("line.separator") +
                "- base OS: " + Build.VERSION.BASE_OS + System.getProperty("line.separator") +
                "- user: " + Build.USER + System.getProperty("line.separator") +
                "- codename: " + Build.VERSION.CODENAME + System.getProperty("line.separator") +
                "- build (incremental value): " + Build.VERSION.INCREMENTAL + System.getProperty("line.separator") +
                "- build ID: " + Build.DISPLAY + System.getProperty("line.separator") +
                        "- host: " + Build.HOST + System.getProperty("line.separator") +
                "- security patch level: " + Build.VERSION.SECURITY_PATCH + System.getProperty("line.separator"));
    }

}
