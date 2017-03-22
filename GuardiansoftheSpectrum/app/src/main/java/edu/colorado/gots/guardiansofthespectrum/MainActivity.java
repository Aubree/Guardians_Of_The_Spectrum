package edu.colorado.gots.guardiansofthespectrum;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;


public class MainActivity extends BaseActivity {
    private LinkDialogFragment link;
    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create visualization using PowerBI
        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.loadUrl("https://app.powerbi.com/view?r=eyJrIjoiMjYxMTNiMzQtYjNlNi00ZGM1LTk1MWMtYmRhMzQ2OWZhNGIxIiwidCI6IjNkZWQ4YjFiLTA3MGQtNDYyOS04MmU0LWMwYjAxOWY0NjA1NyIsImMiOjZ9");
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);


        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        link = new LinkDialogFragment();

        Button b2 = (Button) findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                link.onClick(MainActivity.this, getSupportFragmentManager());
            }
        });

    }

}