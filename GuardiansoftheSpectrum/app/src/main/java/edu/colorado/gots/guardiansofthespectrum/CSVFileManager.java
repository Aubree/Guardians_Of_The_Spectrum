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

/**
 * Class to handle storing, and retrieving data collected by the scan in a format suitable for
 * displaying in the MyInfo Activity.
 */
public class CSVFileManager {
    /**
     * Wrapper class around a single enry in our CSV files
     */
    public class CSVEntry {
        /**
         * The reported scan timestamp
         */
        long time;
        /**
         * The reported LTE DBm
         */
        int dbm;
        /**
         * The reported WIFI SSID
         */
        String ssid;
        /**
         * The reported WIFI RSSI
         */
        int rssi;

        /**
         * Default Constructor. Initializes all fields to "invalid".
         */
        private CSVEntry() {
            this.time = -1L;
            this.dbm = Integer.MAX_VALUE;
            this.ssid = "invalid";
            this.rssi = Integer.MAX_VALUE;
        }

        /**
         * Constructor for data in parsed format
         * @param time The timestamp of the scan
         * @param dbm The reported DBm of the LTE network
         * @param ssid The SSID of the currently connected WIFI
         * @param rssi The RSSI of the currently connected WIFI
         */
        public CSVEntry(long time, int dbm, String ssid, int rssi) {
            this.time = time;
            this.dbm = dbm;
            this.ssid = ssid;
            this.rssi = rssi;
        }

        /**
         * Cosntructor for data in bare CSV format
         * @param data A row from the CSV file
         */
        public CSVEntry(String data) {
            //call our default constructor
            //needs to be first statement. we override the values
            //if our data isn't null
            this();
            System.out.println("CSVEntry: " + data);
            if (data != null) {
                String[] fields = data.split(", ");
                this.time = Long.parseLong(fields[0]);
                this.dbm = Integer.parseInt(fields[1]);
                this.ssid = fields[2];
                this.rssi = Integer.parseInt(fields[3]);
            }
        }

        /**
         * Translate the entry back into a CSV styled String
         * @return The CSV string
         */
        public String toCSVString() {
            return String.format("%d, %d, %s, %d\n", time, dbm, ssid, rssi);
        }

        /**
         * @return The timestamp of the scan
         */
        public long getTime() {
            return time;
        }

        /**
         * @return The LTE DBm collected the scan
         */
        public int getDbm() {
            return dbm;
        }

        /**
         * @return The SSID of the connected WIFI collected by the scan
         */
        public String getSsid() {
            return ssid;
        }

        /**
         * @return The RSSI of the connected WIFI of the scan
         */
        public int getRssi() {
            return rssi;
        }
    }

    /**
     * A class containing the result of reading the stored CSV files
     */
    public class CSVData {
        /**
         * The list of scan results inside of the last 24 hours
         */
        private List<CSVEntry> newest;
        /**
         * The list of average scan results for the last several days
         */
        private List<CSVEntry> daily;

        /**
         * Construct the class containing the stored CSV data
         * @param newest The list of new scan results
         * @param daily The list of daily averaged scan results
         */
        CSVData(List<CSVEntry> newest, List<CSVEntry> daily) {
            this.newest = newest;
            this.daily = daily;
        }

        /**
         * @return The list of CSVEntry containing the most recent (i.e. less than
         * 24 hours old) scan results
         * @see CSVEntry
         */
        List<CSVEntry> getNewestData() {
            return newest;
        }

        /**
         * @return The list of CSVEntry containing the daily averaged results of scans
         * @see CSVEntry
         */
        List<CSVEntry> getDailyData() {
            return daily;
        }

        /**
         * @return The list of CSVEntry containing both the most recent results and the
         * daily averaged results. No distinction is given between them.
         * @see CSVEntry
         * @see #getNewestData()
         * @see #getDailyData()
         */
        List<CSVEntry> getAllData() {
            List<CSVEntry> ret = new ArrayList<CSVEntry>(daily);
            ret.addAll(newest);
            return ret;
        }
    }

    /**
     * A reference to the directory in private internal storage containing our CSV files
     */
    private File csvDataDir;
    /**
     * A reference to the file containing the newest (i.e. less than 24 hours old) scan results
     */
    private File newest;
    /**
     * A reference to the file containing the daily averages of the scan results
     */
    private File daily;

    /**
     * Construct a new CSVFileManager instance to handle writing to and reading from CSV files
     * of scan results
     * @param c The current context
     */
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

    /**
     * Returns the contents of the specified CSV file as a list of CSVEntry
     * @param name The CSV file to read
     * @return The list of CSVEntry representing the contents of the file
     * @see CSVEntry
     */
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
            if (res != null) {
                ret.add(new CSVEntry(res));
            }
        } while (res != null);
        return ret;
    }

    /**
     * Reads the timestamp of the first scan in the specified CSV file
     * @param name The CSV file to read
     * @return The timestamp of the first scan in the file or -1 on error.
     */
    private long getFirstTimestamp(File name) {
        FileReader reader;
        try {
            reader = new FileReader(name);
        } catch (IOException e) {
            return -1;
        }
        StringBuffer buf = new StringBuffer();
        try {
            for (int res = reader.read(); res != -1 && Character.isDigit(res); res = reader.read()) {
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

    /**
     * Write CSV data to the specified file
     * @param file The file to write to
     * @param data The CSV formatted data to write
     * @param append If true, append to the file if it exists. Otherwise, overwrite the file
     * @return True if the write was successful, false otherwise
     */
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

    /**
     * Read the contents of all CSV files stored and return them as as CSVData object
     * @return The contents of the stored CSV files
     * @see CSVData
     */
    public CSVData readData() {
        return new CSVData(readFile(daily), readFile(newest));
    }

    /**
     * Write the specified data to the CSV containing the newest scan results. If the earliest
     * scan found in this file has a time stamp more than 24 hours previous to this current data,
     * the contents of the file will be averaged before this write occurs, and the results written
     * to the daily averages file.
     * @param time The timestamp of the scan
     * @param dbm The reported LTE Dbm
     * @param ssid The reported WIFI SSID
     * @param rssi The reported WIFI RSSI
     * @return True if the write was successful, false otherwise
     */
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

    /**
     * Delete all stored CSV Files
     * @return True if all files were deleted successfully, false otherwise
     */
    public boolean deleteFiles() {
        return newest.delete() && daily.delete();
    }
}
