package edu.colorado.gots.guardiansofthespectrum;

import android.app.IntentService;
import android.content.Intent;

public class ScanService extends IntentService {

    private boolean running = true;

    public ScanService() {
        //provides a name for our service thread
        super("ScanService");
    }

    //this will be called when the stopService() command is run from the main home screen
    public void onDestroy() {
        //tell our scan that it needs to stop
        running = false;
        super.onDestroy();
    }

    //the main meat of our code can go in here. Once this function returns
    //our service will exit itself
    protected void onHandleIntent(Intent intent) {
        while (running) {
            System.out.println("Running background Scan Service\n");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
    }

}
