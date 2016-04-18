package net.nashihara.naroureader.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by naoya on 16/02/28.
 */
public class MyProgressDialogFragment extends DialogFragment {
    private static final String TAG = MyProgressDialogFragment.class.getSimpleName();
    private static ProgressDialog mProgressDialog = null;

    private static String PARAM_TITLE = "title";
    private static String PARAM_MESSAGE = "message";

    public static MyProgressDialogFragment newInstance(String title, String message) {
        MyProgressDialogFragment fragment = new MyProgressDialogFragment();

        Bundle args = new Bundle();
        args.putString(PARAM_TITLE, title);
        args.putString(PARAM_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mProgressDialog != null) {
            return mProgressDialog;
        }

        Bundle args = getArguments();
        String title = args.getString(PARAM_TITLE);
        String message = args.getString(PARAM_MESSAGE);

        mProgressDialog = new ProgressDialog(getActivity());
        if (!title.equals("")) {
            mProgressDialog.setTitle(title);
        }
        if (!message.equals("")) {
            mProgressDialog.setMessage(message);
        }
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setCancelable(false);

        return mProgressDialog;
    }

    @Override
    public void dismiss() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public Dialog getDialog() {
        return mProgressDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProgressDialog = null;
    }
}
