package net.nashihara.naroureader.widgets;

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

    public static ListDailogFragment newInstance(String title, String[] listItems, DialogInterface.OnClickListener onClickListener) {
        ListDailogFragment fragment = new ListDailogFragment();
        fragment.setTitle(title);
        fragment.setListItems(listItems);
        fragment.setOnClickListener(onClickListener);
        return fragment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getListItems() {
        return listItems;
    }

    public void setListItems(String[] listItems) {
        this.listItems = listItems;
    }

    public DialogInterface.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(DialogInterface.OnClickListener onClickListener) {
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
