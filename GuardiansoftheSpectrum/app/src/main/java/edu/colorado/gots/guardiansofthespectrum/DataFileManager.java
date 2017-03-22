package edu.colorado.gots.guardiansofthespectrum;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

/**
 * Class to handle working with the storage of data collected from scanning.
 */
public class DataFileManager {
    /**
     * The directory in which we store the data before we send it to the server.
     */
    private File newDataDir;

    /**
     * Creates and initializes the Manager.
     * @param c The Context of the application
     */
    DataFileManager(Context c) {
        newDataDir = c.getDir("new", Context.MODE_PRIVATE);
    }

    /**
     * Writes the provided JSON formatted data into a new data file.
     * @param JSONData The String of JSON data
     * @return <code>true</code> if the write completed successfully, <code>false</code> otherwise.
     */
    boolean writeToFile(String JSONData) {
        long time = new Date().getTime();
        File name = new File(newDataDir, String.format("%d", time));
        System.out.println(String.format("Writing data to file %s", name.getAbsolutePath()));
        FileOutputStream out;
        try {
            out = new FileOutputStream(name);
        } catch (FileNotFoundException e) {
            return false;
        }
        try {
            out.write(JSONData.getBytes());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Lists all data files.
     * @return An array of the data files
     */
    File[] listDataFiles() {
        return newDataDir.listFiles();
    }

    /**
     * Reads the contents of the data file with the provided name.
     * @param name The filename to read
     * @return The contents of the file, or <code>null</code> if an error occurs
     */
    String readDataFile(String name) {
        BufferedReader in;
        try {
            //get a convenience wrapper around the bare file reader to give better read()
            //functions
            in = new BufferedReader(new FileReader(new File(newDataDir, name)));
        } catch (FileNotFoundException e) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        String res = null;
        //snarf in the whole file
        do {
            try {
                res = in.readLine();
            } catch (IOException e) {
                res = null;
            }
            buf.append(res);
        } while (res != null);
        return buf.toString();
    }

    /**
     * Reads the contents of all data files.
     * @return An array containing the contents of each file.
     */
    String[] readAllDataFiles() {
        File[] newFiles = listDataFiles();
        String[] ret = new String[newFiles.length];
        for (int i = 0; i < newFiles.length; i++) {
            System.out.println(String.format("Reading file: %s\n", newFiles[i].getAbsolutePath()));
            ret[i] = readDataFile(newFiles[i].getName());
        }
        return ret;
    }

    /**
     * Deletes the file that has the provided name.
     * @param name The name of the file to delete
     * @return <code>true</code> if the file was deleted successfully, <code>false</code> if not
     */
    boolean deleteDataFile(String name) {
        File f = new File(newDataDir, name);
        if (f.exists()) {
            return f.delete();
        } else {
            return false;
        }
    }

    /**
     * Deletes all stored data files
     * @return <code>true</code> if all files were deleted successfully, <code>false</code> otherwise
     */
    boolean deleteAllDataFiles() {
        File[] s = listDataFiles();
        boolean ret = true;
        for (int i = 0; i < s.length; i++) {
            System.out.println(String.format("Deleting file: %s\n", s[i].getAbsolutePath()));
            ret = ret && deleteDataFile(s[i].getName());
        }
        return ret;
    }
}
