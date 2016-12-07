package edu.colorado.gots.guardiansofthespectrum;


import android.location.Location;
import android.net.wifi.ScanResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;

public class JSONBuilder {
    //handle building our main JSON Results String
    public static String scanToJSON(List<ScanResult> wifi, Location current) {
        JSONObject main = new JSONObject();
        try {
            if (current != null) {
                main.put("Latitude", current.getLatitude());
                main.put("Longitude", current.getLongitude());
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
        JSONArray message = new JSONArray();
        for (int i = 0; i < data.length; i++) {
            //parse the JSON string
            JSONTokener tokener = new JSONTokener(data[i]);
            try {
                message.put(tokener.nextValue());
            } catch (JSONException e) {
                //this shouldn't happen since we're only storing well formed JSON
            }
        }
        String ret;
        try {
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
}
