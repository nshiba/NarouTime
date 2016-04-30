package net.nashihara.naroureader.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class ListDailogFragment extends DialogFragment {
    private static final String TAG = ListDailogFragment.class.getSimpleName();

    private String title = "";
    private String[] listItems = new String[]{};
    private DialogInterface.OnClickListener onClickListener = null;

    public ListDailogFragment() {}

    public ListDailogFragment(String title, String[] listItems, DialogInterface.OnClickListener onClickListener) {
        this.title = title;
        this.listItems = listItems;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setItems(listItems, onClickListener)
                .create();
    }
}
