package net.nashihara.naroureader.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
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
            handleColorChange(R.string.body_text, R.color.colorText);
        }

        if (title.equals("背景色")) {
            handleColorChange(R.string.body_background, R.color.colorBackground);
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void handleColorChange(@StringRes int stringResId, @ColorRes int colorResId) {
        int prefColor = pref.getInt(getString(stringResId), 0);
        int targetColor = prefColor == 0 ? ContextCompat.getColor(getContext(), colorResId) : prefColor;

        ColorPickerDialog.show(
          getFragmentManager(),
          targetColor,
          color -> pref.edit().putInt(getString(stringResId), color).apply());
    }
}
