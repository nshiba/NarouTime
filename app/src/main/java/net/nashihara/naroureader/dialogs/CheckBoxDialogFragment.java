package net.nashihara.naroureader.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public abstract class CheckBoxDialogFragment extends DialogFragment {
    private static final String TAG = CheckBoxDialogFragment.class.getSimpleName();

    private String title = "";
    private String[] listItems = new String[]{};
    private DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener = null;

    public CheckBoxDialogFragment(String title, String[] listItems, DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener) {
        this.title = title;
        this.listItems = listItems;
        this.onMultiChoiceClickListener = onMultiChoiceClickListener;
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
                        onPositiveButton(which);
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
                        onNeutralButton(which);
                    }
                })
                .create();
    }

    public abstract void onPositiveButton(int which);
    public abstract void onNeutralButton(int which);
}
