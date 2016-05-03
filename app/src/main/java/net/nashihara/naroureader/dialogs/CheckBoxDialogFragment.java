package net.nashihara.naroureader.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class CheckBoxDialogFragment extends DialogFragment {
    private static final String TAG = CheckBoxDialogFragment.class.getSimpleName();

    private String title = "";
    private String[] listItems = new String[]{};
    private DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener = null;
    private OnDialogButtonClickListener mListener;

    public CheckBoxDialogFragment() {}

    public static CheckBoxDialogFragment newInstance(String title, String[] listItems, DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener, OnDialogButtonClickListener listener) {
        CheckBoxDialogFragment fragment = new CheckBoxDialogFragment();
        fragment.setTitle(title);
        fragment.setListItems(listItems);
        fragment.setOnMultiChoiceClickListener(onMultiChoiceClickListener);
        fragment.setmListener(listener);
        return fragment;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setListItems(String[] listItems) {
        this.listItems = listItems;
    }

    public void setOnMultiChoiceClickListener(DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener) {
        this.onMultiChoiceClickListener = onMultiChoiceClickListener;
    }

    public void setmListener(OnDialogButtonClickListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMultiChoiceItems(listItems, null, onMultiChoiceClickListener)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onPositiveButton(which);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("cansel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onNeutralButton(which);
                    }
                })
                .create();
    }

    public interface OnDialogButtonClickListener{
        public abstract void onPositiveButton(int which);
        public abstract void onNeutralButton(int which);
    }
}
