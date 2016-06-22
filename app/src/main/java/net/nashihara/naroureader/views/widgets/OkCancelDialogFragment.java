package net.nashihara.naroureader.views.widgets;

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

    public OkCancelDialogFragment() {}

    public static OkCancelDialogFragment newInstance(String title, String message, DialogInterface.OnClickListener onClickListener) {
        OkCancelDialogFragment fragment = new OkCancelDialogFragment();
        fragment.setTitle(title);
        fragment.setMessage(message);
        fragment.setOnClickListener(onClickListener);
        return fragment;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setOnClickListener(DialogInterface.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("cansel", (dialog, which) -> {
                onClickListener.onClick(dialog, which);
            })
            .setPositiveButton("Ok", (dialog, which) -> {
                onClickListener.onClick(dialog, which);
            })
            .create();
    }
}
