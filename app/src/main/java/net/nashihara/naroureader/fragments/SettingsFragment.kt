package net.nashihara.naroureader.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceManager
import com.pavelsikun.vintagechroma.OnColorSelectedListener

import net.nashihara.naroureader.R
import net.nashihara.naroureader.utils.ColorPickerDialog

class SettingsFragment : PreferenceFragmentCompat() {

    private val pref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val title = preference.title as String
        if (title == "文字色") {
            handleColorChange(R.string.body_text, R.color.colorText)
        }

        if (title == "背景色") {
            handleColorChange(R.string.body_background, R.color.colorBackground)
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun handleColorChange(@StringRes stringResId: Int, @ColorRes colorResId: Int) {
        val prefColor = pref.getInt(getString(stringResId), 0)
        val targetColor = if (prefColor == 0) ContextCompat.getColor(context, colorResId) else prefColor

        ColorPickerDialog.show( fragmentManager, targetColor, OnColorSelectedListener {
            color -> pref.edit().putInt(getString(stringResId), color).apply() })
    }
}
