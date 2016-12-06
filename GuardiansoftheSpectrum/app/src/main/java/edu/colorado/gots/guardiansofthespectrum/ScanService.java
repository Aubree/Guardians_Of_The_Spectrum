package edu.colorado.gots.guardiansofthespectrum;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

public class ScanService extends IntentService {

    public ScanService() {
        //provides a name for our service thread
        super("ScanService");
    }

    //the main meat of our code can go in here. Once this function returns
    //our service will exit itself
    protected void onHandleIntent(Intent intent) {
        while (true) {
            System.out.println("Running background Scan Service\n");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
    }
}
