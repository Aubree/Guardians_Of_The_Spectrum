package edu.colorado.gots.guardiansofthespectrum;


import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;

/**
 * Class containing functionality to build JSON formatted strings of the data we've collected in
 * a scan.
 */
public class JSONBuilder {
    /**
     * Builds the main JSON formatted string of our results.
     * @param lte The LTE data collected or <code>null</code> if none
     * @param wifi The WIFI data collected or <code>null</code> if none
     * @param current The current Location, or <code>null</code> if none
     * @return The JSON string. Will always be valid, but may be an empty JSON object if an error
     * occurs.
     */
    public static String scanToJSON(ScanService.LTE_Info lte, List<ScanResult> wifi, Location current) {
        JSONObject main = new JSONObject();
        try {
            if (current != null) {
                main.put("Latitude", current.getLatitude());
                main.put("Longitude", current.getLongitude());
            }
            if (lte != null) {
                main.put("LTE", buildLTEJSON(lte));
            }
            if (wifi != null) {
                main.put("Wifi", buildWifiJSON(wifi));
            }
            return main.toString();
        } catch (JSONException e) {
            return new JSONObject().toString();
        }
    }

    /**
     * Combines the multiple JSON strings from the stored data files, combines them into a single
     * JSON string which conforms to our Transfer Specification for transmitting the data to the
     * server.
     * @param data A String array with the individual JSON strings from each stored data file
     * @return The full JSON string adhering to our transfer spec, and ready to be added into
     * the body of a POST request to our server
     */
    public static String prepareSendData(String[] data) {
        JSONObject message = new JSONObject();
        JSONArray scanData = new JSONArray();
        for (int i = 0; i < data.length; i++) {
            //parse the JSON string
            JSONTokener tokener = new JSONTokener(data[i]);
            try {
                scanData.put(tokener.nextValue());
            } catch (JSONException e) {
                //this shouldn't happen since we're only storing well formed JSON
            }
        }
        String ret;
        try {
            message.put("metadata", buildMetadataJSON());
            message.put("data", scanData);
            ret = message.toString(4);
        } catch (JSONException e) {
            ret = "JSON PARSING FAILED";
        }
        return ret;
    }

    /**
     * Builds a JSON Array which contains JSON objects representing the WIFI data collected during
     * a scan.
     * @param res The List of WIFI ScanResults
     * @return The JSONArray of our results. Will be empty if an error occurs
     */
    private static JSONArray buildWifiJSON(List<ScanResult> res) {
        JSONArray wifi_array = new JSONArray();
        try {
            for (int i = 0; i < res.size(); i++) {
                wifi_array.put(i, scanToJSON(res.get(i)));
            }
        } catch (JSONException e) {
            return new JSONArray();
        }
        return wifi_array;
    }

    /**
     * Builds a JSON Object out of a single WIFI ScanResult object.
     * @param res A ScanResult object returned from the WifiManager class.
     * @return The JSONObject. May be empty if an error occurs.
     */
    private static JSONObject scanToJSON(ScanResult res) {
        JSONObject ret = new JSONObject();
        try {
            ret.put("SSID", res.SSID);
            ret.put("BSSID", res.BSSID);
            ret.put("frequency", res.frequency);
            ret.put("RSSI", res.level);
            ret.put("timestamp", res.timestamp);
        } catch (JSONException e) {
            return new JSONObject();
        }
        return ret;
    }

    /**
     * Builds a JSON Object from the data gathered from a scan.
     * @param lte The LTE information collected from the scan
     * @return A JSONObject representing the LTE information. Will be empty if an error occurs.
     */
    private static JSONObject buildLTEJSON(ScanService.LTE_Info lte) {
        JSONObject ret = new JSONObject();
       try {
           System.out.println("building lte JSON\n");
           ret.put("Dbm", lte.getLTEinfo().getCellSignalStrength().getDbm());
           ret.put("CellID", lte.getLTEinfo().getCellIdentity().getCi());
           ret.put("MCC", lte.getLTEinfo().getCellIdentity().getMcc());
           ret.put("MNC", lte.getLTEinfo().getCellIdentity().getMnc());
           ret.put("PCI", lte.getLTEinfo().getCellIdentity().getPci());
           ret.put("TAC", lte.getLTEinfo().getCellIdentity().getTac());
           ret.put("TimingAdvance", lte.getLTEinfo().getCellSignalStrength().getTimingAdvance());
           ret.put("RSSNR", lte.getRssnr());
           ret.put("CQI", lte.getCqi());
           //ret.put("RSRP", lte.getRsrp());
           return ret;
       } catch (JSONException e) {
           System.out.println("lte json fail\n");
           return new JSONObject();
       }
    }

    /**
     * Builds a JSON Object containing information about the phone that did the scanning.
     * @return A JSONObject with various metadata. Will be empty if an error occurs.
     */
    private static JSONObject buildMetadataJSON() {
        JSONObject ret = new JSONObject();
        try {
            ret.put("Board", Build.BOARD);
            ret.put("Bootloader", Build.BOOTLOADER);
            ret.put("Brand", Build.BRAND);
            ret.put("Device", Build.DEVICE);
            ret.put("Display", Build.DISPLAY);
            ret.put("Fingerprint", Build.FINGERPRINT);
            ret.put("Hardware", Build.HARDWARE);
            ret.put("Host", Build.HOST);
            ret.put("ID", Build.ID);
            ret.put("Manufacturer", Build.MANUFACTURER);
            ret.put("Model", Build.MODEL);
            ret.put("Product", Build.PRODUCT);
            ret.put("Serial", Build.SERIAL);
            ret.put("Tags", Build.TAGS);
            ret.put("Time", Build.TIME);
            ret.put("Type", Build.TYPE);
            ret.put("User", Build.USER);
            ret.put("Radio", Build.getRadioVersion());
            ret.put("Codename", Build.VERSION.CODENAME);
            ret.put("Incremental", Build.VERSION.INCREMENTAL);
            ret.put("Release", Build.VERSION.RELEASE);
            ret.put("SDKint", Build.VERSION.SDK_INT);
        } catch (JSONException e) {
            return new JSONObject();
        }
        return ret;
    }
}
