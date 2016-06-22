package net.nashihara.naroureader.views.widgets;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.FragmentFilterDialogBinding;

public class FilterDialogFragment extends DialogFragment {
    private static final String TAG = FilterDialogFragment.class.getSimpleName();

    private String title = "";
    private String[] listItems = new String[]{};
    private boolean[] checked = new boolean[]{};
    private boolean isLength;
    private DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener = null;
    private OnDialogButtonClickListener mListener;
    FragmentFilterDialogBinding binding;

    public FilterDialogFragment() {}

    public static FilterDialogFragment newInstance(String title, String[] listItems, boolean[] checked, boolean isLength, OnDialogButtonClickListener listener) {
        FilterDialogFragment fragment = new FilterDialogFragment();
        fragment.setTitle(title);
        fragment.setListItems(listItems);
        fragment.setChecked(checked);
        fragment.setmListener(listener);
        fragment.setLength(isLength);
        return fragment;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setListItems(String[] listItems) {
        this.listItems = listItems;
    }

    public void setChecked(boolean[] checked) {
        this.checked = checked;
    }

    public void setmListener(OnDialogButtonClickListener mListener) {
        this.mListener = mListener;
    }

    public void setLength(boolean length) {
        isLength = length;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter_dialog, null, false);

        DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener
            = (dialog, which, isChecked) -> checked[which] = isChecked;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
            .setMultiChoiceItems(listItems, checked, onMultiChoiceClickListener)
            .setNegativeButton("cansel", (dialog, which) -> {
                dialog.dismiss();
            })
            .setNeutralButton("reset", (dialog, which) -> {
                mListener.onNeutralButton(which);
            });
        if (isLength) {
            builder.setView(binding.getRoot()).setPositiveButton("OK", (dialog, which) -> {
                String max = binding.maxLength.getText().toString();
                String min = binding.minLength.getText().toString();
                mListener.onPositiveButton(which, checked, min, max);
                dialog.dismiss();
            });
        }
        else {
            builder.setPositiveButton("OK", (dialog, which) -> {
                mListener.onPositiveButton(which, checked, "", "");
                dialog.dismiss();
            });
        }
        return builder.create();
    }

    public interface OnDialogButtonClickListener{
        public void onPositiveButton(int which, boolean[] itemChecked, String min, String max);
        public void onNeutralButton(int which);
    }
}
