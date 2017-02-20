package edu.colorado.gots.guardiansofthespectrum;

import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class SendActivity extends AppCompatActivity implements ServerDialogFragment.ServerDialogListener {

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
        //note: call this for data.
        textView.setText(JSONBuilder.prepareSendData(new DataFileManager(getApplicationContext()).readAllDataFiles()));
        DialogFragment serverInfo = new ServerDialogFragment();
        serverInfo.show(getSupportFragmentManager(), "serverInfo");
    }

    //quick helper function because even though java will let you nest classes, functions
    //are apparently a different story...
    private String getDialogText(DialogFragment dialog, int id) {
        return ((EditText) dialog.getDialog().findViewById(id)).getText().toString();
    }

    public void onDialogPositiveClick(DialogFragment dialog) {
        String IP = getDialogText(dialog, R.id.serverIP);
        String port = getDialogText(dialog, R.id.serverPort);
        System.out.println(String.format("opening socket to %s:%s\n", IP, port));

        new SendTask().execute(IP, port, textView.getText().toString());
    }

    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    private class SendTask extends AsyncTask<String, Void, String> {
        private ProgressBar bar;

        //show our progress bar
        protected void onPreExecute() {
            bar = (ProgressBar) findViewById(R.id.sendProgressBar);
            bar.setVisibility(VISIBLE);
        }

        //send the data to our server
        protected String doInBackground(String... params) {
            InetAddress server;
            try {
                server = InetAddress.getByName(params[0]);
            } catch (UnknownHostException e) {
                //this should not happen since we only ever pass in a string representation of an
                //IP address, thus no host checking or dns is ever done
                return "Creating ip address for server failed";
            }
            //the main socket to our server
            Socket soc;
            try {
                soc = new Socket(server, Integer.parseInt(params[1]));
            } catch (IOException e) {
                //something went wrong while opening the socket
                System.out.println(String.format("exception: message: %s, cause: %s\n", e.getMessage(), e.getCause()));
                return "Couldn't connect to server";
            }

            try {
                //convenience wrapper around the bare output stream that will handle flushing
                //it for us and can handle format strings
                PrintWriter socWriter = new PrintWriter(soc.getOutputStream(), true);
                //output POST headers
                socWriter.printf("POST /post_point HTTP/1.1\n");
                socWriter.printf("Content-Type: application/json\n");
                socWriter.printf("Host: %s:%s\n", params[0], params[1]);
                socWriter.printf("Content-Length: %d\n", params[2].length());
                socWriter.printf("\n");
                //output our data
                socWriter.println(params[2]);
                socWriter.close();
                soc.close();
            } catch (IOException e) {
                System.out.println(String.format("exception: message %s, cause %s\n", e.getMessage(), e.getCause()));
                return "Sending data to server failed";
            }
            System.out.println("Send successful\n");
            return "Data Sent";
        }

        //hide the progress bar and display the status of the send
        protected void onPostExecute(String result) {
            bar.setVisibility(GONE);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        }
    }
}
