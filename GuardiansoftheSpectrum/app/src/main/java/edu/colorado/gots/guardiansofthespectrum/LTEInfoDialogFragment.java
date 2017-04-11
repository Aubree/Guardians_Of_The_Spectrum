package edu.colorado.gots.guardiansofthespectrum;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jubar on 4/6/2017.
 */

public class LTEInfoDialogFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_lteinfo, container, false);
        getDialog().setTitle("More Info:");
        return rootView;
    }
}
