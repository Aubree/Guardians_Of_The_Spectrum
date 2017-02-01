package edu.colorado.gots.guardiansofthespectrum;


import android.location.Location;
import android.net.wifi.ScanResult;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;

public class JSONBuilder {
    //handle building our main JSON Results String
    public static String scanToJSON(List<CellInfo> lte, List<ScanResult> wifi, Location current) {
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

    //take multiple JSON strings from data files, parse them, combine them into
    //our transfer spec JSON, and return the string representation
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

    //build array of JSON objects containing our wifi scan results
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

    //build a JSON object out of a single wifi ScanResult object
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
    
    private static JSONObject buildLTEJSON(List<CellInfo> lte) {
        JSONObject ret = new JSONObject();
       try {
           for (CellInfo cellInfo : lte) {
               if (cellInfo instanceof CellInfoLte) {
                   CellInfoLte ci = (CellInfoLte) cellInfo;
                   ret.put("Dbm", ci.getCellSignalStrength().getDbm());
                   ret.put("CellID", ci.getCellIdentity().getCi());
                   ret.put("MCC", ci.getCellIdentity().getMcc());
                   ret.put("MNC", ci.getCellIdentity().getMnc());
                   ret.put("PCI", ci.getCellIdentity().getPci());
                   ret.put("TAC", ci.getCellIdentity().getTac());
                   ret.put("TimingAdvance", ci.getCellSignalStrength().getTimingAdvance());
               }
           }
       } catch (JSONException e) {
           return new JSONObject();
       }
    }

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
