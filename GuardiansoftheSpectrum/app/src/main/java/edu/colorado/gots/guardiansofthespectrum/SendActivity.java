package edu.colorado.gots.guardiansofthespectrum;

import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManagerFactory;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * This Activity is most likely temporary unless the user should be allowed to initiate the sending
 * of data explicitly. Unless this is the case, the only necessary part of this to keep is the
 * <code>ScanTask</code> inner class, as this does the necessary communication.
 */
public class SendActivity extends AppCompatActivity implements ServerDialogFragment.ServerDialogListener {

    /**
     * A reference to the view for displaying the collected data.
     */
    TextView textView;

    /**
     * Create the activity and set necessary state
     * @param savedInstanceState The saved state from any previous Instances
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        //grab the text view for now to display aggregated JSON data
        textView = (TextView) findViewById(R.id.full_json_data);
    }

    /**
     * Called when the activity starts execution. Initiates the dialog to allow the user to send data.
     */
    public void onStart() {
        super.onStart();
        //note: call this for data.
        textView.setText(JSONBuilder.prepareSendData(new DataFileManager(getApplicationContext()).readAllDataFiles()));
        DialogFragment serverInfo = new ServerDialogFragment();
        serverInfo.show(getSupportFragmentManager(), "serverInfo");
    }


    /**
     * Called when the user selects the "OK" button on the dialog
     * @param dialog Ignored
     */
    public void onDialogPositiveClick(DialogFragment dialog) {
        new SendTask().execute(textView.getText().toString());
    }

    /**
     * Called when the user selects "cancel" or dismisses the dialog.
     * @param dialog Ignored
     */
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    /**
     * Perform the actual sending of collected scan data in a one-time background thread
     */
    private class SendTask extends AsyncTask<String, Void, String> {
        /**
         * A reference to a progress bar to allow the user to see work is occurring
         */
        private ProgressBar bar;

        /**
         * Called when the task is starting. Sets upt he progress bar.
         */
        protected void onPreExecute() {
            bar = (ProgressBar) findViewById(R.id.sendProgressBar);
            bar.setVisibility(VISIBLE);
        }

        /**
         * Called after <code>onPreExecute()</code>. Handles the actual sending of data
         * in a background thread.
         * @param params The arguments passed to <code>SendTask().execute()</code>. Index 0 contains
         *               the string of JSON data
         * @return A string indicating the result of the send
         */
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
            System.out.println(String.format("generated certificate: %s\n", cA.toString()));
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
            System.out.println(String.format("created keystore: %s\n", store.toString()));
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
            HttpsURLConnection serverConnection;
            try {
                URL server = new URL("https", "gotspec.tk", 443, "post_point");
                System.out.println(String.format("url: %s", server.toExternalForm()));
                serverConnection = (HttpsURLConnection) server.openConnection();
                serverConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                //make connection use a post method
                serverConnection.setRequestMethod("POST");
                //allow outgoing data
                serverConnection.setDoOutput(true);
                //allow incoming data
                serverConnection.setDoInput(true);
                //set HTTP header info
                serverConnection.setRequestProperty("Content-Type", "application/json");
                serverConnection.setRequestProperty("Content-Length", String.valueOf(params[0].getBytes().length));
                serverConnection.setRequestProperty("Host", "gotspec.tk:443");

                Map<String, List<String>> m = serverConnection.getRequestProperties();
                for (Map.Entry<String, List<String>> e : m.entrySet()) {
                    System.out.println("Property: " + e.getKey() + ":");
                    for (String s : e.getValue()) {
                        System.out.println("\t" + s + "\n");
                    }
                }

                //connect
                serverConnection.connect();
            } catch (MalformedURLException e) {
                return "bad ip and port specification";
            } catch (IOException e) {
                return "can't connect to server";
            }
            System.out.println(String.format("httpsurlconnection: %s\n", serverConnection.toString()));
            try {
                for (Certificate c : serverConnection.getServerCertificates()) {
                    System.out.println(String.format("server cert: %s\n", c.toString()));
                }
            } catch (SSLPeerUnverifiedException e) {
                return "Bad Server Certificates";
            }
            /*HttpURLConnection serverConnection;
            try {
                URL server = new URL("http", "gotspec.tk", 443, "post_point");
                System.out.println(String.format("url: %s", server.toExternalForm()));
                serverConnection = (HttpURLConnection) server.openConnection();
                //serverConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                //make connection use a post method
                serverConnection.setRequestMethod("POST");
                //allow outgoing data
                serverConnection.setDoOutput(true);
                //allow incoming data
                serverConnection.setDoInput(true);
                //set HTTP header info
                serverConnection.setRequestProperty("Content-Type", "application/json");
                serverConnection.setRequestProperty("Content-Length", String.valueOf(params[0].getBytes().length));
                //serverConnection.setRequestProperty("Host", "gotspec.tk:5000");
                serverConnection.connect();
            } catch (MalformedURLException e) {
                return "bad ip and port specification";
            } catch (IOException e) {
                return "can't connect to server";
            }*/

            try {
                //Data Output stream works better than other Higher-level
                //constructs like PrintWriter which seem to silently swallow
                //certain characters (probably due to mixing line-buffered streams
                //with non-line buffered streams)
                DataOutputStream socStream = new DataOutputStream(serverConnection.getOutputStream());
                socStream.writeBytes(params[0]);
                System.out.println(String.format("wrote %d bytes\n", socStream.size()));
                socStream.close();
                System.out.println(String.format("response: %s\n", serverConnection.getResponseMessage()));
                BufferedReader in = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
                String responseLine;
                while ((responseLine = in.readLine()) != null) {
                    System.out.println(responseLine);
                }
                in.close();
                serverConnection.disconnect();
            } catch (IOException e) {
                System.out.println(String.format("exception: message %s, cause %s\n", e.getMessage(), e.getCause()));
                return "Sending data to server failed";
            }
            System.out.println("Send successful\n");
            return "Data Sent";
        }

        /**
         * Called after <code>doInBackground(String[])</code> completes execution.
         * @param result The result of the send
         * @see #doInBackground(String[])
         */
        protected void onPostExecute(String result) {
            bar.setVisibility(GONE);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        }
    }
}
