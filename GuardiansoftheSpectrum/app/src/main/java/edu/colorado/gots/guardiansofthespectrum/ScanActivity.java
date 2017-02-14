package edu.colorado.gots.guardiansofthespectrum;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.CellInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class ScanActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //codes indicating why onActivityResult is being called
    //also passed with startResolutionForResult
    private final int LOCATION_SERVICE_RESOLUTION = 0;

    TextView textView;
    WifiManager wifiManager;
    WifiScanReceiver scanReceiver;
    TelephonyManager tM;
    SignalStrengthListener signalStrengthListener;
    GoogleApiClient googleClient;
    LocationRequest request;
    DataFileManager dataFileManager;
                
    Location currentLocation;
    List<CellInfo> cellInfo;
    List<ScanResult> wifiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        textView = (TextView) findViewById(R.id.wifi_scanStat);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        dataFileManager = new DataFileManager(getApplicationContext());
        //initialize scan data object for our async task
        //scanData = new ScanData();
        //grab the wifi manager instance
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //instantiate a receiver class. defined below
        scanReceiver = new WifiScanReceiver();
        //grab telephony manager instance
        tM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //set up lte state listener
        signalStrengthListener = new SignalStrengthListener();
        //set up our google client for location services
        GoogleApiClient.Builder apiBuilder = new GoogleApiClient.Builder(this);
        apiBuilder.addConnectionCallbacks(this);
        apiBuilder.addOnConnectionFailedListener(this);
        apiBuilder.addApi(LocationServices.API);
        googleClient = apiBuilder.build();
        //set up the location request options for how we want to get locations
        request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        request.setFastestInterval(1000);
        //check our location settings
        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder();
        settingsBuilder.addLocationRequest(request);
        PendingResult<LocationSettingsResult> settings = LocationServices.SettingsApi.checkLocationSettings(googleClient, settingsBuilder.build());
        //set the callback function for when the results arrive. If Location service is not enabled,
        //we'll start the resolution process which will launch a dialogue allowing the user to enable
        //the service or leave it off
        settings.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            public void onResult(LocationSettingsResult res) {
                Status status = res.getStatus();
                int statusCode = status.getStatusCode();
                if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        //this is launch the dialogue. will call onActivityResult when dialogue
                        //is completed.
                        status.startResolutionForResult(ScanActivity.this, LOCATION_SERVICE_RESOLUTION);
                    } catch (IntentSender.SendIntentException e) {}
                }
            }
        });
    }

    protected void onStart() {
        super.onStart();
        //register lte listener
        tM.listen(signalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        //ask for scan to start
        wifiManager.startScan();
        //hook up our receiver class to get called when results are available
        registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //initiate connection with google API
        googleClient.connect();

        new ScanTask().execute();
    }

    protected void onStop() {
        //unhook from google
        googleClient.disconnect();
        //unhook our receiver class
        unregisterReceiver(scanReceiver);
        //unregister our lte listener
        tM.listen(signalStrengthListener, PhoneStateListener.LISTEN_NONE);
        super.onStop();
    }

    //called when an activity we start gets completed. In this case, we're interested
    //in the ResultCallback we set up for out LocationSettingsRequest. After the user
    //completes the dialogue, we will parse the results here.
    protected void onActivityResult(int requestCode, int returnCode, Intent i) {
        //make sure that it was our startResolutionForResult that triggered this
        switch (requestCode) {
            case LOCATION_SERVICE_RESOLUTION:
                if (returnCode != Activity.RESULT_OK) {
                    //changes not made successfully. just gripe for now
                    Toast.makeText(getApplicationContext(), "Location services needed to send data", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    //implement ConnectionCallbacks interface
    //called when GoogleAPIClient is ready to be used
    public void onConnected(Bundle connectionHint) {
        System.out.println("onConnected callback\n");
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleClient);
        //OK, so the GoogleAPIClient can only return the LAST location value it had and will only
        //store a backlog if AT LEAST one client is connected. Odds are, we'll be the first client
        //so there will be no current value to read right away. Thus, we'll need to set up a request
        //for a location and implement a listener if we get a NULL value indicating that there's no
        //last location to read.
        if (currentLocation == null) {
            System.out.println("Sending location request\n");
            LocationServices.FusedLocationApi.requestLocationUpdates(googleClient, request, this);
        }
    }

    public void onConnectionSuspended(int h) {

    }

    //implement the OnConnectionFailed Listener interface
    //called if connecting to the GoogleAPIClient fails
    public void onConnectionFailed(ConnectionResult result) {

    }

    //implement the FusedLocationAPI's LocationListener interface
    //called when location changes (or we get the first one)
    public void onLocationChanged(Location newLocation) {
        //we can stop getting updates now. we only need one
        System.out.println("Receiving new location\n");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleClient, this);
        currentLocation = newLocation;
    }

    //private class to handle receiving the wifi results
    private class WifiScanReceiver extends BroadcastReceiver {
        //must implement to inherit from Broadcast Receiver
        //called when desired results arrive
        public void onReceive(Context context, Intent intent) {
            wifiInfo = wifiManager.getScanResults();
        }
    }
                
    private class SignalStrengthListener extends PhoneStateListener {
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {
            cellInfo = tM.getAllCellInfo();
            super.onSignalStrengthsChanged(signalStrength);
        }
    }

    /*private class ScanData {
        public List<CellInfo> cellInfo;
        public List<ScanResult> wifiInfo;
        public Location currentLocation;

        ScanData() {
            cellInfo = null;
            wifiInfo = null;
            currentLocation = null;
        }
    }*/

    private class ScanTask extends AsyncTask<Void, Void, String> {

        private ProgressBar bar;

        protected void onPreExecute() {
            bar = (ProgressBar) findViewById(R.id.scanProgressBar);
            bar.setVisibility(VISIBLE);
        }

        protected String doInBackground(Void... params) {
            while (currentLocation == null || cellInfo == null || wifiInfo == null) {
                try {
                    System.out.println("waiting...\n");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return JSONBuilder.scanToJSON(cellInfo, wifiInfo, currentLocation);
        }

        protected void onPostExecute(String result) {
            bar.setVisibility(GONE);
            if (!dataFileManager.writeToFile(result)) {
                Toast.makeText(getApplicationContext(), "Writing data to local file failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Writing data to local file succeeded", Toast.LENGTH_SHORT).show();
            }
            textView.setText(result);
        }
    }
}
