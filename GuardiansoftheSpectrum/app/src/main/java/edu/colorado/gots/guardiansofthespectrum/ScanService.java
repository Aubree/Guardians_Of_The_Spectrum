package edu.colorado.gots.guardiansofthespectrum;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class ScanService extends IntentService {

    private boolean running = true;
    private int counter = 0;

    public static final String GOTS_COUNTER = "edu.colorado.gots.guardainsofthespectrum.counter";
    public static final String GOTS_COUNTER_EXTRA = "edu.colorado.gots.guardiansofthespectrum.counter.extra";

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
                /*
                counter++;
                Intent broadcast = new Intent(this, MainActivity.CounterReceiver.class);
                broadcast.setAction(GOTS_COUNTER);
                broadcast.putExtra(GOTS_COUNTER_EXTRA, counter);
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
            */

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
