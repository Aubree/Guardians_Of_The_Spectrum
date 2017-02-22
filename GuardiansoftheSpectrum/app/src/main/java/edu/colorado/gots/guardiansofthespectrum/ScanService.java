package edu.colorado.gots.guardiansofthespectrum;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.android.gms.location.LocationResult;

import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class ScanService extends Service {

    Handler serviceHandler;
    HandlerThread backgroundThread;

    WifiManager wifiManager;
    WifiScanReceiver scanReceiver;
    TelephonyManager tM;
    SignalStrengthListener signalStrengthListener;
    DataFileManager dataFileManager;
    LocationServicesManager LSManager;
    PendingIntent locationPendingIntent;

    volatile Location currentLocation;
    volatile List<CellInfo> cellInfo;
    volatile List<ScanResult> wifiInfo;

    private int counter = 0;
    private volatile boolean running = true;
    private boolean backgroundStarted = false;

    public static final String GOTS_COUNTER = "edu.colorado.gots.guardainsofthespectrum.counter";
    public static final String GOTS_COUNTER_EXTRA = "edu.colorado.gots.guardiansofthespectrum.counter.extra";

    public static final String GOTS_SCAN_SERVICE_LOCATION = "edu.colorado.gots.guardiansofthespectrum.scan.service.location";
    public static final String GOTS_SCAN_FOREGROUND_START = "edu.colorado.gots.guardiansofthepectrum.scan.foreground.start";
    public static final String GOTS_SCAN_FOREGROUND_END = "edu.colorado.gots.guardiansofthespectrum.scan.foreground.end";
    public static final String GOTS_SCAN_BACKGROUND_START = "edu.colorado.gots.guardiansofthespectrum.scan.background.start";
    public static final String GOTS_SCAN_SERVICE_RESULTS = "edu.colorado.gots.guardiansofthespectrum.scan.service.results";
    public static final String GOTS_SCAN_SERVICE_RESULTS_EXTRA = "edu.colorado.gots.guardiansofthespectrum.scan.service.results.extra";

    public void onCreate() {
        //start up the background thread
        backgroundThread = new HandlerThread("ScanService", Process.THREAD_PRIORITY_BACKGROUND);
        backgroundThread.start();
        //get handle that we can pass messages to
        serviceHandler = new ScanServiceHandler(backgroundThread.getLooper());
        dataFileManager = new DataFileManager(getApplicationContext());
        //grab the wifi manager instance
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //instantiate a receiver class. defined below
        scanReceiver = new WifiScanReceiver();
        //ask for scan to start
        wifiManager.startScan();
        //hook up our receiver class to get called when results are available
        registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //grab telephony manager instance
        tM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //set up lte state listener
        signalStrengthListener = new SignalStrengthListener();
        //register lte listener
        tM.listen(signalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_INFO);
        //register to get location updates
        LSManager = LocationServicesManager.getInstance(this);
        Intent locationIntent = new Intent(this, ScanService.class);
        locationIntent.setAction(GOTS_SCAN_SERVICE_LOCATION);
        locationPendingIntent = PendingIntent.getService(this, 0, locationIntent, FLAG_UPDATE_CURRENT);
        LSManager.requestLocationUpdates(locationPendingIntent);
    }

    public int onStartCommand(Intent i, int flags, int startId) {
        System.out.printf("Starting service %d\n", startId);
        String action = null;
        if (i != null) {
            action = i.getAction();
        }
        if (action != null && action.equals(GOTS_SCAN_SERVICE_LOCATION)) {
            //extract new location if it is valid
            if (LocationResult.hasResult(i)) {
                currentLocation = LocationResult.extractResult(i).getLastLocation();
            }
        }  else if (action != null && action.equals(GOTS_SCAN_FOREGROUND_END)) {
            //stop the service if we started it up with a foreground scan
            if (!backgroundStarted) {
                stopSelf();
            }
        } else if (action != null && action.equals(GOTS_SCAN_BACKGROUND_START)) {
            //mark that we got started by the user enabling the background scan
            //only the user disabling this scan will cause us to stop
            backgroundStarted = true;
        }
        //run the scan if we need to
        if (action == null || action.equals(GOTS_SCAN_FOREGROUND_START) || action.equals(GOTS_SCAN_BACKGROUND_START)) {
            //prepare message to send to worker thread
            Message msg = serviceHandler.obtainMessage();
            msg.arg1 = startId;
            serviceHandler.sendMessage(msg);
        }
        //ask to be restarted if killed off by the system
        //return START_STICKY;
        return START_NOT_STICKY;
    }

    public IBinder onBind(Intent i) {
        //not providing binding for this service
        return null;
    }

    //note that because we unhook the receivers here, the inner while
    //loop will never get new results after we get the stop signal.
    //Unfortunately, it doesn't seem like we can move these calls to the
    //end of the handleMessage function, otherwise we start to leak memory
    //and we STILL have the same problem of never getting new results
    //for some reason. Thus, we have a check inside the inner while loop as
    //well to break out if running becomes false
    public void onDestroy() {
        System.out.println("setting running to false\n");
        running = false;
        //unhook our receiver class
        unregisterReceiver(scanReceiver);
        //unregister our lte listener
        tM.listen(signalStrengthListener, PhoneStateListener.LISTEN_NONE);
        //remove location updates
        LSManager.removeLocationUpdates(locationPendingIntent);
        //backgroundThread.quit();
        super.onDestroy();
    }

    private class ScanServiceHandler extends Handler {
        public ScanServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message m) {
            //just do the scan
            while (running) {
                while ((currentLocation == null || cellInfo == null || wifiInfo == null) && running) {
                //while ((cellInfo == null || wifiInfo == null) && running) {
                    try {
                        System.out.printf("waiting... running is %b\n", running);
                        Thread.sleep(100);
                        counter++;
                        Intent broadcast = new Intent(ScanService.this, SettingsActivity.CounterReceiver.class);
                        broadcast.setAction(GOTS_COUNTER);
                        broadcast.putExtra(GOTS_COUNTER_EXTRA, counter);
                        LocalBroadcastManager.getInstance(ScanService.this).sendBroadcast(broadcast);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                String jsonText = JSONBuilder.scanToJSON(cellInfo, wifiInfo, currentLocation);
                dataFileManager.writeToFile(jsonText);
                Intent resultsIntent = new Intent(GOTS_SCAN_SERVICE_RESULTS);
                resultsIntent.putExtra(GOTS_SCAN_SERVICE_RESULTS_EXTRA, jsonText);
                LocalBroadcastManager.getInstance(ScanService.this).sendBroadcast(resultsIntent);
                cellInfo = null;
                wifiInfo = null;
            }
            stopSelf();
        }
    }

    //private class to handle receiving the wifi results
    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            System.out.println("getting new wifi results\n");
            wifiInfo = wifiManager.getScanResults();
            wifiManager.startScan();
        }
    }

    private class SignalStrengthListener extends PhoneStateListener {

        public void onCellInfoChanged(List<CellInfo> info) {
            System.out.println("cell info changed\n");
            cellInfo = info;
            super.onCellInfoChanged(info);
        }

        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {
            System.out.println("signal strength changed\n");
            cellInfo = tM.getAllCellInfo();
            super.onSignalStrengthsChanged(signalStrength);
        }
    }
}
