package net.nashihara.naroureader.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class NovelDownloadDialogFragment extends DialogFragment {
    private static final String TAG = NovelDownloadDialogFragment.class.getSimpleName();
    private ProgressDialog dialog;

    private int max;
    private String title;
    private String message;

    public NovelDownloadDialogFragment(int max, String title, String message) {
        this.title = title;
        this.message = message;
        this.max = max;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new ProgressDialog(getActivity());
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setMax(max);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        return dialog;
    }

    public void setProgress(int value) {
        dialog.setProgress(value);
    }

    public int getProgress() {
        return dialog.getProgress();
    }
}