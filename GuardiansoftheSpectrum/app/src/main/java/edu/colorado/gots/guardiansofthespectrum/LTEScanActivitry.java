package edu.colorado.gots.guardiansofthespectrum;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

/**
 * Created by jubar on 11/17/2016.
 */

public class LTEScanActivitry extends AppCompatActivity {
    SignalStrengthListener signalStrengthListener;

    TextView signalStrengthTextView, signalStrengthTextView2;
    TextView cellIDTextView;
    TextView cellMccTextView;
    TextView cellMncTextView;
    TextView cellPciTextView;
    TextView cellTacTextView;
    TextView timeAdvanceView;

    List<CellInfo> cellInfoList;
    int cellSig, cellID, cellMcc, cellMnc, cellPci, cellTac, timeAdvance = 0;

    TelephonyManager tm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ltescan);

        signalStrengthTextView = (TextView) findViewById(R.id.signalStrengthTextView);
        signalStrengthTextView2 = (TextView) findViewById(R.id.signalStrengthTextView2);
        cellIDTextView = (TextView) findViewById(R.id.cellIDTextView);
        cellMccTextView = (TextView) findViewById(R.id.cellMccTextView);
        cellMncTextView = (TextView) findViewById(R.id.cellMncTextView);
        cellPciTextView = (TextView) findViewById(R.id.cellPciTextView);
        cellTacTextView = (TextView) findViewById(R.id.cellTacTextView);
        timeAdvanceView = (TextView) findViewById(R.id.timingAdvanceView);

        //start the signal strength listener
        signalStrengthListener = new SignalStrengthListener();

        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(signalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        try {
            cellInfoList = tm.getAllCellInfo();
        } catch (Exception e) {
            Log.d("SignalStrength", "+++++++++++++++++++++++++++++++++++++++++ null array spot 1: " + e);
        }

        try {
            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoLte) {
                    // cast to CellInfoLte and call all the CellInfoLte methods you need
                    // gets RSRP cell signal strength:
                    cellSig = ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();

                    // Gets the LTE cell indentity: (returns 28-bit Cell Identity, Integer.MAX_VALUE if unknown)
                    cellID = ((CellInfoLte) cellInfo).getCellIdentity().getCi();

                    // Gets the LTE MCC: (returns 3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown)
                    cellMcc = ((CellInfoLte) cellInfo).getCellIdentity().getMcc();

                    // Gets theLTE MNC: (returns 2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown)
                    cellMnc = ((CellInfoLte) cellInfo).getCellIdentity().getMnc();

                    // Gets the LTE PCI: (returns Physical Cell Id 0..503, Integer.MAX_VALUE if unknown)
                    cellPci = ((CellInfoLte) cellInfo).getCellIdentity().getPci();

                    // Gets the LTE TAC: (returns 16-bit Tracking Area Code, Integer.MAX_VALUE if unknown)
                    cellTac = ((CellInfoLte) cellInfo).getCellIdentity().getTac();

                    //Gets the timing advance value for LTE
                    timeAdvance = ((CellInfoLte) cellInfo).getCellSignalStrength().getTimingAdvance();
                }
            }
        } catch (Exception e) {
            Log.d("SignalStrength", "++++++++++++++++++++++ null array spot 2: " + e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try{
            if(signalStrengthListener != null){tm.listen(signalStrengthListener, SignalStrengthListener.LISTEN_NONE);}
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        super.onDestroy();

        try{
            if(signalStrengthListener != null){tm.listen(signalStrengthListener, SignalStrengthListener.LISTEN_NONE);}
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private class SignalStrengthListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {

            //++++++++++++++++++++++++++++++++++

            ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(signalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);

            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            String ltestr = signalStrength.toString();
            String[] parts = ltestr.split(" ");
            String cellSig2 = parts[9];

            try {
                cellInfoList = tm.getAllCellInfo();
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoLte) {
                        // cast to CellInfoLte and call all the CellInfoLte methods you need
                        // gets RSRP cell signal strength:
                        cellSig = ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();

                        // Gets the LTE cell identity: (returns 28-bit Cell Identity, Integer.MAX_VALUE if unknown)
                        cellID = ((CellInfoLte) cellInfo).getCellIdentity().getCi();

                        // Gets the LTE MCC: (returns 3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown)
                        cellMcc = ((CellInfoLte) cellInfo).getCellIdentity().getMcc();

                        // Gets theLTE MNC: (returns 2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown)
                        cellMnc = ((CellInfoLte) cellInfo).getCellIdentity().getMnc();

                        // Gets the LTE PCI: (returns Physical Cell Id 0..503, Integer.MAX_VALUE if unknown)
                        cellPci = ((CellInfoLte) cellInfo).getCellIdentity().getPci();

                        // Gets the LTE TAC: (returns 16-bit Tracking Area Code, Integer.MAX_VALUE if unknown)
                        cellTac = ((CellInfoLte) cellInfo).getCellIdentity().getTac();

                        //Gets the timing advance value for LTE
                        timeAdvance = ((CellInfoLte) cellInfo).getCellSignalStrength().getTimingAdvance();

                    }
                }
            } catch (Exception e) {
                Log.d("SignalStrength", "+++++++++++++++++++++++++++++++ null array spot 3: " + e);
            }

            signalStrengthTextView.setText("RSRP 1 cell signal strength = " + cellSig + " dbm");
            signalStrengthTextView2.setText("RSRP 2 cell signal strength = " + cellSig2 + " dbm");
            cellIDTextView.setText("Cell Identity = " + cellID);
            cellMccTextView.setText("Mobile Country Code = " + cellMcc);
            cellMncTextView.setText("Mobile Network Code = " + cellMnc);
            cellPciTextView.setText("Physical Cell Id = " + cellPci);
            cellTacTextView.setText("Tracking Area Code = " + cellTac);
            timeAdvanceView.setText("Timing Advance value = " + timeAdvance);

            super.onSignalStrengthsChanged(signalStrength);

            //++++++++++++++++++++++++++++++++++++

        }
    }
}
