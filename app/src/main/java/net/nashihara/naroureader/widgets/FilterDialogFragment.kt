package net.nashihara.naroureader.widgets

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment

import net.nashihara.naroureader.R
import net.nashihara.naroureader.databinding.FragmentFilterDialogBinding

class FilterDialogFragment : DialogFragment() {

    private var title = ""
    private var listItems = arrayOf<String>()
    private var checked = booleanArrayOf()
    private var isLength: Boolean = false
    private var listener: OnDialogButtonClickListener? = null
    private lateinit var binding: FragmentFilterDialogBinding

    fun setTitle(title: String) {
        this.title = title
    }

    fun setListItems(listItems: Array<String>) {
        this.listItems = listItems
    }

    fun setChecked(checked: BooleanArray) {
        this.checked = checked
    }

    fun setmListener(mListener: OnDialogButtonClickListener) {
        this.listener = mListener
    }

    fun setLength(length: Boolean) {
        isLength = length
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity.layoutInflater
        binding = DataBindingUtil.inflate<FragmentFilterDialogBinding>(inflater, R.layout.fragment_filter_dialog, null, false)

        val onMultiChoiceClickListener = DialogInterface.OnMultiChoiceClickListener {
            _, which, isChecked -> checked[which] = isChecked }

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
                .setMultiChoiceItems(listItems, checked, onMultiChoiceClickListener)
                .setNegativeButton("cansel") { dialog, which -> dialog.dismiss() }
                .setNeutralButton("reset") { dialog, which ->
                    if (listener != null) {
                        listener!!.onNeutralButton(which)
                    }
                }
        if (isLength) {
            builder.setView(binding.root).setPositiveButton("OK") { dialog, which ->
                val max = binding.maxLength.text.toString()
                val min = binding.minLength.text.toString()
                if (listener != null) {
                    listener!!.onPositiveButton(which, checked, min, max)
                }
                dialog.dismiss()
            }
        } else {
            builder.setPositiveButton("OK") { dialog, which ->
                if (listener != null) {
                    listener!!.onPositiveButton(which, checked, "", "")
                }
                dialog.dismiss()
            }
        }
        return builder.create()
    }

    interface OnDialogButtonClickListener {
        fun onPositiveButton(which: Int, itemChecked: BooleanArray, min: String, max: String)
        fun onNeutralButton(which: Int)
    }

    companion object {

        fun newInstance(title: String, listItems: Array<String>?, checked: BooleanArray?,
                        isLength: Boolean, listener: OnDialogButtonClickListener): FilterDialogFragment {
            val fragment = FilterDialogFragment()
            fragment.setTitle(title)
            fragment.setListItems(listItems ?: arrayOf<String>())
            fragment.setChecked(checked ?: booleanArrayOf())
            fragment.setmListener(listener)
            fragment.setLength(isLength)
            return fragment
        }
    }
}
