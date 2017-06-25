package net.nashihara.naroureader.utils

import android.support.v4.app.FragmentManager

import com.pavelsikun.vintagechroma.ChromaDialog
import com.pavelsikun.vintagechroma.IndicatorMode
import com.pavelsikun.vintagechroma.OnColorSelectedListener
import com.pavelsikun.vintagechroma.colormode.ColorMode

object ColorPickerDialog {
    fun show(manager: FragmentManager, targetColor: Int, listener: OnColorSelectedListener) {
        ChromaDialog.Builder()
                .initialColor(targetColor)
                .colorMode(ColorMode.ARGB)
                .indicatorMode(IndicatorMode.HEX)
                .onColorSelected(listener)
                .create().show(manager, "ChromaDialog")
    }
}
