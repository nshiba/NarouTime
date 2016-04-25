package net.nashihara.naroureader.fragments;

import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceFragmentCompat;

import net.nashihara.naroureader.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    public SettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SwitchPreference switchPreference = (SwitchPreference) findPreference(getString(R.string.auto_bookmark));
        switchPreference.setChecked(true);

        switchPreference = (SwitchPreference) findPreference(getString(R.string.auto_remove_bookmark));
        switchPreference.setChecked(true);
    }
}
