package edu.colorado.gots.guardiansofthespectrum;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

//class to handle working with our JSON files
public class DataFileManager {
    private File newDataDir;

    DataFileManager(Context c) {
        newDataDir = c.getDir("new", Context.MODE_PRIVATE);
    }

    //write JSON data into a file with the current timestamp as the filename
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

    //list all new (i.e. not yet sent) JSON files
    File[] listDataFiles() {
        return newDataDir.listFiles();
    }

    //read the contents a the new (i.e. not yet sent) JSON file
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

    //read contents of ALL new (i.e. not yet sent) JSON data files
    String[] readAllDataFiles() {
        File[] newFiles = listDataFiles();
        String[] ret = new String[newFiles.length];
        for (int i = 0; i < newFiles.length; i++) {
            System.out.println(String.format("Reading file: %s\n", newFiles[i].getAbsolutePath()));
            ret[i] = readDataFile(newFiles[i].getName());
        }
        return ret;
    }

    //delete a new (i.e. not yet sent) JSON data file
    boolean deleteDataFile(String name) {
        File f = new File(newDataDir, name);
        if (f.exists()) {
            return f.delete();
        } else {
            return false;
        }
    }

    //delete ALL new (i.e. not yet sent) JSON data files
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
