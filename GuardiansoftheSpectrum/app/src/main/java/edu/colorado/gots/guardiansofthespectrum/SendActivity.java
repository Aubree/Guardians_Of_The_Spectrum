package edu.colorado.gots.guardiansofthespectrum;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class SendActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        //grab the text view for now to display aggregated JSON data
        textView = (TextView) findViewById(R.id.full_json_data);
    }

    public void onStart() {
        super.onStart();
        textView.setText(JSONBuilder.prepareSendData(new DataFileManager(getApplicationContext()).readAllDataFiles()));

        //Sending the Data
        //can uncomment once we get a server set up
        /*//set up the socket to send our data to the server
        Socket soc = new Socket(HOSTNAME, PORT);
        try {
            //convenience wrapper around the outputStream() that will handle flushing
            //the stream for us on writes and can handle format strings for us
            PrintWriter socWriter = new PrintWriter(soc.getOutputStream(), true);
            socWriter.println(data.toString());
            socWriter.close();
            soc.close();
            //we've sent the data, so we can clean up this
            for (int i = 0; i < newFiles.length; i++) {
                newFiles[i].delete();
            }
        } catch (IOException e) {}*/
    }
}
