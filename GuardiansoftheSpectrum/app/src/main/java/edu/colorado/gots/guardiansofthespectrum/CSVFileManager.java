package edu.colorado.gots.guardiansofthespectrum;


import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CSVFileManager {
    public class CSVEntry {
        long time;
        int dbm;
        String ssid;
        int rssi;

        public CSVEntry(long time, int dbm, String ssid, int rssi) {
            this.time = time;
            this.dbm = dbm;
            this.ssid = ssid;
            this.rssi = rssi;
        }

        public CSVEntry(String data) {
            String[] fields = data.split(", ");
            this.time = Long.parseLong(fields[0]);
            this.dbm = Integer.parseInt(fields[1]);
            this.ssid = fields[2];
            this.rssi = Integer.parseInt(fields[3]);
        }

        public String toCSVString() {
            return String.format("%d, %d, %s, %d", time, dbm, ssid, rssi);
        }

        public long getTime() {
            return time;
        }

        public int getDbm() {
            return dbm;
        }

        public String getSsid() {
            return ssid;
        }

        public int getRssi() {
            return rssi;
        }
    }

    public class CSVData {
        private List<CSVEntry> newest;
        private List<CSVEntry> daily;
        CSVData(List<CSVEntry> newest, List<CSVEntry> daily) {
            this.newest = newest;
            this.daily = daily;
        }
        List<CSVEntry> getNewestData() {
            return newest;
        }
        List<CSVEntry> getDailyData() {
            return daily;
        }
        List<CSVEntry> getAllData() {
            List<CSVEntry> ret = new ArrayList<CSVEntry>(daily);
            ret.addAll(newest);
            return ret;
        }
    }
    private File csvDataDir;
    private File newest;
    private File daily;

    CSVFileManager(Context c) {
        csvDataDir = c.getDir("csv", Context.MODE_PRIVATE);
        newest = new File(csvDataDir, "newest");
        daily = new File(csvDataDir, "daily");
        try {
            newest.createNewFile();
            daily.createNewFile();
        } catch (IOException e) {

        }
    }

    private List<CSVEntry> readFile(File name) {
        List<CSVEntry> ret = new ArrayList<CSVEntry>();
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(name));
        } catch (FileNotFoundException e) {
            return ret;
        }
        String res = "";
        do {
            try {
                res = in.readLine();
            } catch (IOException e) {
                res = null;
            }
            ret.add(new CSVEntry(res));
        } while (res != null);
        return ret;
    }

    private long getFirstTimestamp(File name) {
        FileReader reader;
        try {
            reader = new FileReader(name);
        } catch (IOException e) {
            return -1;
        }
        StringBuffer buf = new StringBuffer();
        int res;
        /*do {
            try {
                res = reader.read();
            } catch (IOException e) {
                return -1;
            }
            buf.appendCodePoint(res);
        } while (Character.isDigit(res));*/
        try {
            for (res = reader.read(); res != -1 && Character.isDigit(res); res = reader.read()) {
                buf.appendCodePoint(res);
            }
        } catch (IOException e) {
            return -1;
        }
        try {
            return Long.parseLong(buf.toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean writeFile(File file, String data, boolean append) {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file, append);
            out.write(data.getBytes());
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public CSVData readData() {
        return new CSVData(readFile(daily), readFile(newest));
    }

    public boolean writeData(long time, int dbm, String ssid, int rssi) {
        long old = getFirstTimestamp(newest);
        long day = 24 * 60 * 60 * 1000;
        if (old == -1 || time - old < day) {
            //we can just append the data into the newest file
            return writeFile(newest, new CSVEntry(time, dbm, ssid, rssi).toCSVString(), true);
        } else {
            //we need to average everything in our newest file into our daily file, then
            //we can clear it out and write our data to it
            List<CSVEntry> data = readFile(newest);
            int totalDbm = 0;
            int totalRssi = 0;
            Map<String, Integer> counts = new HashMap<String, Integer>();
            for (int i = 0; i < data.size(); i++) {
                totalDbm += data.get(i).getDbm();
                totalRssi += data.get(i).getRssi();
                if (counts.containsKey(data.get(i).getSsid())) {
                    Integer c = counts.get(data.get(i).getSsid());
                    counts.put(data.get(i).getSsid(), c + 1);
                } else {
                    counts.put(data.get(i).getSsid(), 1);
                }
            }
            int maxCount = 0;
            String maxString = "";
            Iterator<Map.Entry<String, Integer>> it = counts.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> obj = it.next();
                if (obj.getValue() > maxCount) {
                    maxString = obj.getKey();
                }
            }
            boolean res = writeFile(daily, new CSVEntry(old, totalDbm / data.size(), maxString, totalRssi / data.size()).toCSVString(), false);
            return res && writeFile(newest, new CSVEntry(time, dbm, ssid, rssi).toCSVString(), false);
        }
    }

    public boolean deleteFiles() {
        return newest.delete() && daily.delete();
    }
}
