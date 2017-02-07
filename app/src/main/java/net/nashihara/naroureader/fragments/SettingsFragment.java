package net.nashihara.naroureader.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.utils.ColorPickerDialog;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private SharedPreferences pref;

    public SettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String title = (String) preference.getTitle();

        if (title.equals("文字色")) {
            int text = pref.getInt(getString(R.string.body_text), 0);
            if (text == 0) {
                text = ContextCompat.getColor(getContext(), R.color.colorText);
            }
            ColorPickerDialog.show(
                getFragmentManager(),
                text,
                color -> pref.edit().putInt(getString(R.string.body_text), color).apply());
        }

        if (title.equals("背景色")) {
            int background = pref.getInt(getString(R.string.body_background), 0);
            if (background == 0) {
                background = ContextCompat.getColor(getContext(), R.color.colorBackground);
            }
            ColorPickerDialog.show(
                getFragmentManager(),
                background,
                color -> pref.edit().putInt(getString(R.string.body_background), color).apply());
        }

        return super.onPreferenceTreeClick(preference);
    }
}
