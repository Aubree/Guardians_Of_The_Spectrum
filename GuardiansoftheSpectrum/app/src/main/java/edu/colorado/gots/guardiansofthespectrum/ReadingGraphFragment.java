package edu.colorado.gots.guardiansofthespectrum;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Reading graph fragment explains how to read graphs. Since MPAndroid visualization
 *  does not provide space to define y-axis and both graphs have data that is adjusted.
 */
public class ReadingGraphFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_graph_reading, container, false);
        getDialog().setTitle("Reading graphs");
        return rootView;
    }
}
