package edu.colorado.gots.guardiansofthespectrum;

import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

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
            //load in our certificate authority information
            CertificateFactory cf;
            Certificate cA;
            InputStream certInfo = getResources().openRawResource(R.raw.cert);
            try {
                cf = CertificateFactory.getInstance("X.509");
                cA = cf.generateCertificate(certInfo);
            } catch (CertificateException e) {
                return "can't load certificate authority information";
            }
            try {
                certInfo.close();
            } catch (IOException e) {}
            //store certificate in storage to initialize trustmanagers
            KeyStore store;
            try {
                store = KeyStore.getInstance(KeyStore.getDefaultType());
                store.load(null, null);
                store.setCertificateEntry("cA", cA);
            } catch (KeyStoreException e) {
                return "can't create keystore for certificate information";
            } catch (IOException e) {
                return "can't create keystore for certificate information";
            } catch (NoSuchAlgorithmException e) {
                return "can't create keystore for certificate information";
            } catch (CertificateException e) {
                return "can't create keystore for certificate information";
            }
            //create trustmanager to handle making credential information
            TrustManagerFactory tmf;
            try {
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(store);
            } catch (NoSuchAlgorithmException e) {
                return "can't create trust managers for certificates";
            } catch (KeyStoreException e) {
                return "can't create trust managers for certificates";
            }
            //create context in which our SSL connection will operate
            SSLContext sslContext;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);
            } catch (NoSuchAlgorithmException e) {
                return "cannot establish SSL context";
            } catch (KeyManagementException e) {
                return "cannot establish SSL context";
            }
            //initialize connection to server
            int port = Integer.parseInt(params[1]);
            HttpsURLConnection serverConnection;
            try {
                //URL server = new URL("https", params[0], port, "post_point");
                URL server = new URL("https", "gotspec.tk", 443, "post_point");
                System.out.println(String.format("url: %s", server.toExternalForm()));
                serverConnection = (HttpsURLConnection) server.openConnection();
                serverConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                //make connection use a post method
                serverConnection.setRequestMethod("POST");
                //allow outgoing data
                serverConnection.setDoOutput(true);
                //set HTTP header info
                serverConnection.setRequestProperty("Content-Type", "application/json");
                serverConnection.setRequestProperty("Content-Length", String.valueOf(params[2].getBytes().length));
                serverConnection.setRequestProperty("Host", "gotspec.tk:" + String.valueOf(443));
                //connect
                serverConnection.connect();
            } catch (MalformedURLException e) {
                return "bad ip and port specification";
            } catch (IOException e) {
                return "can't connect to server";
            }

            try {
                //Data Output stream works better than other Higher-level
                //constructs like PrintWriter which seem to silently swallow
                //certain characters (probably due to mixing line-buffered streams
                //with non-line buffered streams)
                DataOutputStream socStream = new DataOutputStream(serverConnection.getOutputStream());
                socStream.writeBytes(params[2]);
                socStream.close();
                serverConnection.disconnect();
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
