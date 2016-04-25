package net.nashihara.naroureader.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class OkCancelDialogFragment extends DialogFragment {
    public static final int OK = -1;
    public static final int CANSEL = -2;

    private String title = "";
    private String message = "";
    private DialogInterface.OnClickListener onClickListener = null;

    public OkCancelDialogFragment(String title, String message, DialogInterface.OnClickListener onClickListener) {
        this.title = title;
        this.message = message;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("cansel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickListener.onClick(dialog, which);
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickListener.onClick(dialog, which);
                    }
                })
                .create();
    }
}
