package net.nashihara.naroureader.widgets

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

class NovelDownloadDialogFragment : DialogFragment() {
    private var dialog: ProgressDialog? = null

    var max: Int = 0
    var title: String? = null
    var message: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = ProgressDialog(activity)
        dialog!!.setTitle(title)
        dialog!!.setMessage(message)
        dialog!!.max = max
        dialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)

        return dialog!!
    }

    var progress: Int
        get() = dialog?.progress ?: 0
        set(value) { dialog?.progress = value }

    companion object {

        fun newInstance(max: Int, title: String, message: String): NovelDownloadDialogFragment {
            val fragment = NovelDownloadDialogFragment()
            fragment.max = max
            fragment.title = title
            fragment.message = message
            return fragment
        }
    }
}