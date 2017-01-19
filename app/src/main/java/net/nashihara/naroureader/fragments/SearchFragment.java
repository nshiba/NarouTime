package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.controller.SearchController;
import net.nashihara.naroureader.databinding.FragmentSearchBinding;
import net.nashihara.naroureader.listeners.OnFragmentReplaceListener;
import net.nashihara.naroureader.views.SearchView;
import net.nashihara.naroureader.widgets.FilterDialogFragment;
import net.nashihara.naroureader.widgets.OkCancelDialogFragment;

import java.util.ArrayList;

public class SearchFragment extends Fragment implements SearchView {

    private FragmentSearchBinding binding;
    private Context context;
    private OnFragmentReplaceListener replaceListener;

    private int sortItem = 0;
    private int timeItem = 0;

    private boolean[] genreChecked;
    private String[] genreStrings;

    private SearchController controller;

    public SearchFragment() { }

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

        controller = new SearchController(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);

        setupSort();
        setupGenre();
        setupReadTime();
        setupSearchButton();

        return binding.getRoot();
    }

    private void setupSearchButton() {
        binding.btnSearch.setOnClickListener(v -> controller.shapeSearchQuery(
            binding.editNcode.getText().toString(),
            binding.editLimit.getText().toString(),
            sortItem,
            binding.editSearch.getText().toString(),
            binding.editNotSearch.getText().toString(),
            binding.keywordTitle.isChecked(),
            binding.keywordStory.isChecked(),
            binding.keywordKeyword.isChecked(),
            binding.keywordWriter.isChecked(),
            timeItem,
            binding.maxLength.getText().toString(),
            binding.minLength.getText().toString(),
            binding.end.isChecked(),
            binding.stop.isChecked(),
            binding.pickup.isChecked(),
            genreChecked
        ));
    }

    private void setupReadTime() {
        // 読了目安時間
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.time_spinner, android.R.layout.simple_spinner_item);
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
    }

    private void setupGenre() {
        genreStrings = getResources().getStringArray(R.array.genres);
        genreChecked = new boolean[genreStrings.length];
        // ジャンル
        for (int i = 0; i < genreChecked.length; i++) {
            genreChecked[i] = false;
        }

        binding.btnGenre.setOnClickListener(v -> FilterDialogFragment.newInstance("ジャンル選択", genreStrings, genreChecked, false,
            new FilterDialogFragment.OnDialogButtonClickListener() {
                @Override
                public void onPositiveButton(int which, boolean[] itemChecked, String min, String max) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < itemChecked.length; i++) {
                        if (itemChecked[i]) {
                            builder.append(genreStrings[i]).append(" ");
                        }
                    }
                    binding.genreText.setText(builder.toString());
                }

                @Override
                public void onNeutralButton(int which) {
                    for (int i = 0; i < genreChecked.length; i++) {
                        genreChecked[i] = false;
                    }
                    binding.genreText.setText("\n\n指定なし\n\n");
                }
            }).show(getFragmentManager(), "filter"));
    }

    private void setupSort() {
        // 並び順
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            context, R.array.sort_spinner, android.R.layout.simple_spinner_item);

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

    @Override
    public void showResult(
        String ncode,
        int limit,
        int sortOrder,
        String search,
        String notSearch,
        boolean targetTitle,
        boolean targetStory,
        boolean targetKeyword,
        boolean targetWriter,
        int time,
        int maxLength,
        int minLength,
        boolean end,
        boolean stop,
        boolean pickup,
        ArrayList<Integer> genreList) {

        SearchRecyclerViewFragment fragment = SearchRecyclerViewFragment.newInstance(
            ncode, limit, sortItem, search, notSearch, targetTitle, targetStory, targetKeyword,
            targetWriter, time, maxLength, minLength, end, stop, pickup, genreList);

        replaceListener.onFragmentReplaceAction(fragment, "検索結果", null);
    }

    @Override
    public void showError() {
        OkCancelDialogFragment.newInstance("エラー", "最大取得件数は500件です。", (dialog, which) -> {
            dialog.dismiss();
            reload();
        }).show(getFragmentManager(), "okcancel");
    }
}
