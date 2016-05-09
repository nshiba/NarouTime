package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.FragmentSearchBinding;
import net.nashihara.naroureader.dialogs.OkCancelDialogFragment;
import net.nashihara.naroureader.listeners.OnFragmentReplaceListener;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private Context context;
    private OnFragmentReplaceListener replaceListener;

    private int sortItem = 0;
    private int timeItem = 0;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            return;
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);

        // 並び順
        ArrayAdapter<CharSequence> adapter
                = ArrayAdapter.createFromResource(context, R.array.sort_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.sortSpinner.setAdapter(adapter);
        binding.sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortItem = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 読了目安時間
        adapter = ArrayAdapter.createFromResource(context, R.array.time_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.timeSpinner.setAdapter(adapter);
        binding.timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                timeItem = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int limit;
                String limitStr = binding.editLimit.getText().toString();
                if (limitStr.equals("")) {
                    limit = 0;
                }
                else {
                    limit = Integer.parseInt(limitStr);
                }

                if (limit > 500) {
                    OkCancelDialogFragment.newInstance("エラー", "最大取得件数は500件です。", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            reload();
                        }
                    }).show(getFragmentManager(), "okcancel");
                    return;
                }

                int min, max;
                String minLength = binding.minLength.getText().toString();
                String maxLength = binding.maxLength.getText().toString();

                if (minLength.equals("")) {
                    min = 0;
                }
                else {
                    min = Integer.parseInt(minLength);
                }

                if (maxLength.equals("")) {
                    max = 0;
                }
                else {
                    max = Integer.parseInt(maxLength);
                }

                SearchRecyclerViewFragment fragment = SearchRecyclerViewFragment.newInstance(
                        binding.editNcode.getText().toString(), limit, sortItem, binding.editSearch.getText().toString(),
                        binding.editNotSearch.getText().toString(), binding.keywordTitle.isChecked(),
                        binding.keywordStory.isChecked(), binding.keywordKeyword.isChecked(), binding.keywordWriter.isChecked(),
                        timeItem, max, min, binding.end.isChecked(), binding.stop.isChecked(), binding.pickup.isChecked());

                replaceListener.onFragmentReplaceAction(fragment, "検索結果", null);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        this.replaceListener = (OnFragmentReplaceListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void reload() {
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
