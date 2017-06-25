package net.nashihara.naroureader.widgets

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog

class OkCancelDialogFragment : DialogFragment() {

    private var title = ""
    private var message = ""
    private var onClickListener: DialogInterface.OnClickListener? = null

    fun setTitle(title: String) {
        this.title = title
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun setOnClickListener(onClickListener: DialogInterface.OnClickListener?) {
        this.onClickListener = onClickListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("cancel") { dialog, which -> onClickListener?.onClick(dialog, which) }
                .setPositiveButton("Ok") { dialog, which -> onClickListener?.onClick(dialog, which) }
                .create()
    }

    companion object {
        val OK = -1
        val CANCEL = -2

        fun newInstance(title: String, message: String, onClickListener: DialogInterface.OnClickListener?): OkCancelDialogFragment {
            val fragment = OkCancelDialogFragment()
            fragment.setTitle(title)
            fragment.setMessage(message)
            fragment.setOnClickListener(onClickListener)
            return fragment
        }
    }
}
