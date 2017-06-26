package net.nashihara.naroureader.widgets

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

class MyProgressDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (progressDialog != null) {
            return progressDialog as ProgressDialog
        }

        val args = arguments
        val title = args.getString(PARAM_TITLE)
        val message = args.getString(PARAM_MESSAGE)

        progressDialog = ProgressDialog(activity)
        if (title != "") {
            progressDialog!!.setTitle(title)
        }
        if (message != "") {
            progressDialog!!.setMessage(message)
        }
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        isCancelable = false

        return progressDialog as ProgressDialog
    }

    override fun dismiss() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    override fun getDialog(): Dialog? {
        return progressDialog
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog = null
    }

    companion object {
        private var progressDialog: ProgressDialog? = null

        private val PARAM_TITLE = "title"
        private val PARAM_MESSAGE = "message"

        fun newInstance(title: String, message: String): MyProgressDialogFragment {
            val fragment = MyProgressDialogFragment()

            val args = Bundle()
            args.putString(PARAM_TITLE, title)
            args.putString(PARAM_MESSAGE, message)
            fragment.arguments = args

            return fragment
        }
    }
}
