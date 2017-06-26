package net.nashihara.naroureader.widgets

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog

class ListDialogFragment : DialogFragment() {

    var title = ""
    var listItems = arrayOf<String>()
    var onClickListener: DialogInterface.OnClickListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setTitle(title)
                .setItems(listItems, onClickListener)
                .create()
    }

    companion object {

        fun newInstance(title: String, listItems: Array<String>, onClickListener: DialogInterface.OnClickListener): ListDialogFragment {
            val fragment = ListDialogFragment()
            fragment.title = title
            fragment.listItems = listItems
            fragment.onClickListener = onClickListener
            return fragment
        }
    }
}
