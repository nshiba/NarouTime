package net.nashihara.naroureader.widgets;

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

    public NovelDownloadDialogFragment() {}

    public static NovelDownloadDialogFragment newInstance(int max, String title, String message) {
        NovelDownloadDialogFragment fragment = new NovelDownloadDialogFragment();
        fragment.setMax(max);
        fragment.setTitle(title);
        fragment.setMessage(message);
        return fragment;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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