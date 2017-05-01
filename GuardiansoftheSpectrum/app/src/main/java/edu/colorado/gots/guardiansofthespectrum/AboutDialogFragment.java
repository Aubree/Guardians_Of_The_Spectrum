package edu.colorado.gots.guardiansofthespectrum;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * About dialogue provides basic information about the app.
 */
public class AboutDialogFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_about, container, false);
        getDialog().setTitle("About");
        return rootView;
    }
}