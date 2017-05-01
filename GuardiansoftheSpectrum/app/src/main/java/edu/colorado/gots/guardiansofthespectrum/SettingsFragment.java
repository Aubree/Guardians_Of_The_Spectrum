package edu.colorado.gots.guardiansofthespectrum;


import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Fragment that will be attached to our Settings Activity.
 * Responsible for loading in the various preferences and options
 * from the XML files and displaying them.
 */
public class SettingsFragment extends PreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
