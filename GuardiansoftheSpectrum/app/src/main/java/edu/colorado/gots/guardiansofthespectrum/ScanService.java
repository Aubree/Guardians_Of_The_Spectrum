package edu.colorado.gots.guardiansofthespectrum;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.Date;
import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * The background scanning service for the app. This class is responsible for gathering data
 * about the LTE, WIFI, and Location information of the device.
 */
public class ScanService extends Service {

    /**
     * Handler responsible for sending messages to our background thread.
     * @see ScanServiceHandler
     */
    Handler serviceHandler;
    /**
     * The background thread for the various listeners to work in. Allows blocking while waiting
     * for incoming data.
     */
    HandlerThread backgroundThread;

    /**
     * The WifiManager instance of the Android operating system. Allows us to initiate WIFI
     * scans and receive results back.
     */
    WifiManager wifiManager;
    /**
     * Our listener for retrieving WIFI scan results.
     * @see WifiScanReceiver
     */
    WifiScanReceiver scanReceiver;
    /**
     * The TelephonyManager instance of the Android operating system. Allows us to register
     * listeners to detect various changes in phone state.
     */
    TelephonyManager tM;
    /**
     * Our listener for handling changing LTE events.
     * @see SignalStrengthListener
     */
    SignalStrengthListener signalStrengthListener;
    /**
     * Class to handle writing and storing JSON data to be sent to our server.
     */
    DataFileManager dataFileManager;
    /**
     * Class to handle writing and storing CSV data for use in MyInfoActivity.
     */
    CSVFileManager csvFileManager;
    /**
     * Class to handle managing various Location-based operations.
     */
    LocationServicesManager LSManager;
    /**
     * The pending intent that we use to receive Location updates from Google Play Services.
     */
    PendingIntent locationPendingIntent;

    /**
     * The current Location of the device.
     */
    volatile Location currentLocation;
    /**
     * The current LTE information of the device.
     */
    volatile CellInfoLte LTEInfo;
    /**
     *  The current LTE information of the device bundled with telephony info.
     */
    volatile LTE_Info LTE_Info;
    /**
     * The current WIFI information of the device.
     */
    volatile List<ScanResult> wifiInfo;

    /**
     * THIS IS TEMPORARY AND CAN BE REMOVED AT A LATER DATE!!
     * <p>
     * A counter value to indicate how long the service has been sleeping while waiting for
     * results. Passed back to the SettingsActivity for visual verification that the service is
     * running.
     * @see #GOTS_COUNTER
     * @see #GOTS_COUNTER_EXTRA
     */
    private int counter = 0;
    /**
     * Value indicating whether the service should be running. When set to <code>false</code>,
     * the service will attempt to stop itself.
     * @see #onDestroy()
     */
    private volatile boolean running = true;
    /**
     * Value tracking how the service was started. If <code>true</code>, the service was started by
     * toggling the switch inside SettingsActivity. In this case, only toggling the switch again will
     * cause the service to stop (meaning foreground scans from the ScanActivity will not end the
     * service when stopped). If <code>false</code>, the service was started from the ScanActivity,
     * and should be stopped when the scan stops.
     */
    private boolean backgroundStarted = false;
    /**
     * Value indicating whether the device has the ability to connect to an LTE network.
     * If <code>true</code>, we will wait for LTE results to come back when scanning. Otherwise,
     * we ignore any results from the LTE listener.
     */
    private boolean lteNetwork = false;

    /**
     * An Intent action which indicates the Intent contains an updated value of our counter to
     * display in the SettingsActivity. This calue can be accessed with GOTS_COUNTER_EXTRA.
     * @see #counter
     * @see #GOTS_COUNTER_EXTRA
     */
    public static final String GOTS_COUNTER = "edu.colorado.gots.guardainsofthespectrum.counter";
    /**
     * A key for Intent extras which will retrieve the updated counter value of an Intent sent with
     * the GOTS_COUNTER action.
     * @see #counter
     * @see #GOTS_COUNTER
     */
    public static final String GOTS_COUNTER_EXTRA = "edu.colorado.gots.guardiansofthespectrum.counter.extra";

    /**
     * An Intent action indicating that the intent contains a new Location for the service to use.
     * This is used to receive new location updated with our locationPendingIntent.
     * @see #locationPendingIntent
     */
    public static final String GOTS_SCAN_SERVICE_LOCATION = "edu.colorado.gots.guardiansofthespectrum.scan.service.location";
    /**
     * An Intent action indicating that the service is being started by the user in ScanActivity.
     * @see #onStartCommand(Intent, int, int)
     */
    public static final String GOTS_SCAN_FOREGROUND_START = "edu.colorado.gots.guardiansofthepectrum.scan.foreground.start";
    /**
     * An Intent action indicating that the service is being stopped by the user in ScanActivity.
     * @see #backgroundStarted
     * @see #onStartCommand(Intent, int, int)
     */
    public static final String GOTS_SCAN_FOREGROUND_END = "edu.colorado.gots.guardiansofthespectrum.scan.foreground.end";
    /**
     * An Intent action indicating that the service is being started by the user in SettingsActivity.
     * @see #backgroundStarted
     * @see #onStartCommand(Intent, int, int)
     */
    public static final String GOTS_SCAN_BACKGROUND_START = "edu.colorado.gots.guardiansofthespectrum.scan.background.start";
    /**
     * An Intent action indicating that the intent contains the results of our background scanning.
     * These results can be accessed by using various keys for the Extras.
     * @see #GOTS_SCAN_SERVICE_RESULTS_EXTRA
     * @see #GOTS_SCAN_SERVICE_RESULTS_CURRENT_WIFI_SSID
     * @see #GOTS_SCAN_SERVICE_RESULTS_CURRENT_WIFI_RSSI
     */
    public static final String GOTS_SCAN_SERVICE_RESULTS = "edu.colorado.gots.guardiansofthespectrum.scan.service.results";
    /**
     * A key for the Extras contained within an Intent that can obatin the results of the background
     * scan.
     * @see #GOTS_SCAN_SERVICE_RESULTS
     */
    public static final String GOTS_SCAN_SERVICE_RESULTS_EXTRA = "edu.colorado.gots.guardiansofthespectrum.scan.service.results.extra";
    public static final String GOTS_SCAN_SERVICE_RESULTS_CURRENT_WIFI_SSID = "edu.colorado.gots.guardiansofthespectrum.scan.service.results.current.wifi.ssid";
    public static final String GOTS_SCAN_SERVICE_RESULTS_CURRENT_WIFI_RSSI = "edu.colorado.gots.guardiansofthespectrum.scan.service.results.current.wifi.rssi";

    /**
     * Responsible for setting the initial state of the service. Registers all necessary listeners,
     * and creates the background thread for the main work to be done.
     */
    public void onCreate() {
        //start up the background thread
        backgroundThread = new HandlerThread("ScanService", Process.THREAD_PRIORITY_BACKGROUND);
        backgroundThread.start();
        //get handle that we can pass messages to
        serviceHandler = new ScanServiceHandler(backgroundThread.getLooper());
        dataFileManager = new DataFileManager(getApplicationContext());
        csvFileManager = new CSVFileManager(getApplicationContext());
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
        //see if we have an LTE network
        lteNetwork = tM.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE;
        System.out.println("LTE Network detected: " + lteNetwork);
        //register to get location updates
        LSManager = new LocationServicesManager(this);
        LSManager.connect();
        Intent locationIntent = new Intent(this, ScanService.class);
        locationIntent.setAction(GOTS_SCAN_SERVICE_LOCATION);
        locationPendingIntent = PendingIntent.getService(this, 0, locationIntent, FLAG_UPDATE_CURRENT);
        LSManager.requestLocationUpdates(locationPendingIntent);
    }

    /**
     * Called when the service receives a command to start. Preceded by onCreate() if the service
     * is not already running
     * @param i The Intent set by the Android operating system containing information on how the
     *          service should be started
     * @param flags
     * @param startId A unique number associated with the command to start the service
     * @return A constant to tell the Android OS not to restart the service blindly if it is killed
     * prematurely.
     * @see Context#startService(Intent)
     */
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

    /**
     * This is a required stub in order to extend Service. Will always return null as binding is
     * not supported by this service.
     * @param i Ignored
     * @return Always null
     */
    public IBinder onBind(Intent i) {
        //not providing binding for this service
        return null;
    }

    /**
     * Called when the service is being stopped. Responsible for unregistering all our necessary
     * listeners.
     */
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

    /**
     * Class that handles receiving messages for our background scanning thread.
     */
    private class ScanServiceHandler extends Handler {
        public ScanServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Check if all necessary information has returned from the listeners based on what we're
         * listening for. WIFI and Location are always required to come back, but LTE is only
         * necessary if the device has access to an LTE network
         * @return <code>true</code> if we need to continue to wait. <code>false</code> if all
         * necessary information has come back
         * @see #lteNetwork
         * @see #currentLocation
         * @see #wifiInfo
         * @see #LTEInfo
         */
        private boolean checkInfo() {
            if (lteNetwork) {
                return (currentLocation == null || LTEInfo == null || wifiInfo == null) && running;
            } else {
                return (currentLocation == null || wifiInfo == null) && running;
            }
        }

        /**
         * Handle incoming messages to the scanning thread. This is where the main work occurs.
         * We sleep until all results come back from the listeners, then write the results to
         * persistent storage to be used later.
         * @param m Ignored
         */
        public void handleMessage(Message m) {
            //just do the scan
            while (running) {
                while (checkInfo()) {
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
                if (!running) {
                    break;
                }
                String jsonText = JSONBuilder.scanToJSON(LTE_Info, wifiInfo, currentLocation);
                dataFileManager.writeToFile(jsonText);
                Intent resultsIntent = new Intent(GOTS_SCAN_SERVICE_RESULTS);
                resultsIntent.putExtra(GOTS_SCAN_SERVICE_RESULTS_EXTRA, jsonText);
                WifiInfo currentWifi = wifiManager.getConnectionInfo();
                resultsIntent.putExtra(GOTS_SCAN_SERVICE_RESULTS_CURRENT_WIFI_SSID, currentWifi.getSSID());
                resultsIntent.putExtra(GOTS_SCAN_SERVICE_RESULTS_CURRENT_WIFI_RSSI, currentWifi.getRssi());
                LocalBroadcastManager.getInstance(ScanService.this).sendBroadcast(resultsIntent);
                csvFileManager.writeData(new Date().getTime(), LTEInfo.getCellSignalStrength().getDbm(),
                        currentWifi.getSSID(), currentWifi.getRssi());
                LTEInfo = null;
                wifiInfo = null;
            }
            stopSelf();
        }
    }

    /**
     * Listener class for receiving results from the WIFI scans.
     */
    private class WifiScanReceiver extends BroadcastReceiver {
        /**
         * Called when new WIFI results are available in the WifiManager instance.
         * @param context Ignored
         * @param intent Ignored
         */
        public void onReceive(Context context, Intent intent) {
            System.out.println("getting new wifi results\n");
            wifiInfo = wifiManager.getScanResults();
            wifiManager.startScan();
        }
    }

    /**
     * Listener class for receiving information about LTE and device state changes.
     */
    private class SignalStrengthListener extends PhoneStateListener {
        /**
         * Called when the Cell information of the device changes
         * @param info The list of cells the device can see.
         */
        public void onCellInfoChanged(List<CellInfo> info) {
            System.out.println("cell info changed\n");
            LTEInfo = getLTEInfo(info);
            super.onCellInfoChanged(info);
        }

        /**
         * Called when the signal strength of the phone changes.
         * @param signalStrength Ignored
         */
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {
            System.out.println("signal strength changed\n");
            String ltestr = signalStrength.toString();
            String[] parts = ltestr.split(" ");
            String rsrq = parts[10];
            String cqi = parts[12];
            String rssnr = parts[11];
            Log.d("SS Changed", "rsrq = " + parts[10]);
            Log.d("SS Changed", "cqi = " + parts[12]);
            Log.d("SS Changed", "rssnr = " + parts[11]);
            // adjusted value: rsrp(parts[9]) + 80
            Log.d("SS Changed", "LTE SS = " + parts[8]);
            LTEInfo = getLTEInfo(tM.getAllCellInfo());
            LTE_Info = new LTE_Info(LTEInfo, rsrq, cqi, rssnr);
            super.onSignalStrengthsChanged(signalStrength);
        }

        /**
         * Extract the LTE info from a list of cells
         * @param info The cell list
         * @return The LTE information if present, <code>null</code> otherwise
         */
        private CellInfoLte getLTEInfo(List<CellInfo> info) {
            if (info == null) {
                return null;
            }
            for (CellInfo i : info) {
                if (i instanceof CellInfoLte) {
                    return (CellInfoLte) i;
                }
            }
            return null;
        }
    }

    /**
     *  Wrapper class to combine LTE objects and values.
     */
    protected class LTE_Info{
        private CellInfoLte LTEinfo;
        private String rsrq;
        private String rssnr;
        private String cqi;

        private LTE_Info(CellInfoLte info, String rsrq, String cqi, String rssnr){
            this.LTEinfo = info;
            this.cqi = cqi;
            this.rsrq = rsrq;
            this.rssnr = rssnr;
        }

        public String getRsrq() {
            return rsrq;
        }

        public String getRssnr() {
            return rssnr;
        }

        public String getCqi() {
            return cqi;
        }

        public CellInfoLte getLTEinfo() {
            return LTEinfo;
        }
    }
}
