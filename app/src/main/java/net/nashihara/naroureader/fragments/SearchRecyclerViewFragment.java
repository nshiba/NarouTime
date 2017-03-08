package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.adapters.NovelDetailRecyclerViewAdapter;
import net.nashihara.naroureader.presenter.SearchRecyclerPresenter;
import net.nashihara.naroureader.databinding.FragmentSearchRecyclerBinding;
import net.nashihara.naroureader.databinding.ItemRankingRecyclerBinding;
import net.nashihara.naroureader.entities.NovelItem;
import net.nashihara.naroureader.listeners.FragmentTransactionListener;
import net.nashihara.naroureader.views.SearchRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchRecyclerViewFragment extends Fragment implements SearchRecyclerView {
    private static final String TAG = SearchRecyclerViewFragment.class.getSimpleName();

    private static final String ARG_NCODE = "ncode";
    private static final String ARG_LIMIT = "limit";
    private static final String ARG_SORT_ORDER = "sort";
    private static final String ARG_SEARCH = "search";
    private static final String ARG_NOT_SEARCH = "not_search";
    private static final String ARG_TARGET_TITLE = "title";
    private static final String ARG_TARGET_STORY = "story";
    private static final String ARG_TARGET_KEYWORD = "keyword";
    private static final String ARG_TARGET_WRITER = "writer";
    private static final String ARG_TIME = "time";
    private static final String ARG_MAX_LENGTH = "max";
    private static final String ARG_MIN_LENGTH = "min";
    private static final String ARG_END = "end";
    private static final String ARG_STOP = "stop";
    private static final String ARG_PICKUP = "pickup";
    private static final String ARG_GENRE_LIST = "genre";

    private String ncode;

    private String search;

    private String notSearch;

    private int limit;

    private int sortOrder;

    private int time;

    private int maxLength;

    private int minLength;

    private boolean targetTitle;

    private boolean targetStory;

    private boolean targetKeyword;

    private boolean targetWriter;

    private boolean end;

    private boolean stop;

    private boolean pickup;

    private ArrayList<Integer> genreList;

    private FragmentSearchRecyclerBinding binding;

    private Context context;

    private RecyclerView recyclerView;

    private FragmentTransactionListener replaceListener;

    private SearchRecyclerPresenter controller;

    private List<NovelItem> allItems = new ArrayList<>();

    public SearchRecyclerViewFragment() {}

    public static SearchRecyclerViewFragment newInstance(
        String ncode, int limit, int sortOrder, String search, String notSearch, boolean targetTitle,
        boolean targetStory, boolean targetKeyword, boolean targetWriter, int time,
        int maxLength, int minLength, boolean end, boolean stop, boolean pickup, ArrayList<Integer> genreList) {

        SearchRecyclerViewFragment fragment = new SearchRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NCODE, ncode);
        args.putString(ARG_SEARCH, search);
        args.putString(ARG_NOT_SEARCH, notSearch);
        args.putInt(ARG_LIMIT, limit);
        args.putInt(ARG_SORT_ORDER, sortOrder);
        args.putInt(ARG_TIME, time);
        args.putInt(ARG_MAX_LENGTH, maxLength);
        args.putInt(ARG_MIN_LENGTH, minLength);
        args.putBoolean(ARG_TARGET_TITLE, targetTitle);
        args.putBoolean(ARG_TARGET_STORY, targetStory);
        args.putBoolean(ARG_TARGET_KEYWORD, targetKeyword);
        args.putBoolean(ARG_TARGET_WRITER, targetWriter);
        args.putBoolean(ARG_END, end);
        args.putBoolean(ARG_STOP, stop);
        args.putBoolean(ARG_PICKUP, pickup);
        args.putIntegerArrayList(ARG_GENRE_LIST, genreList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            Log.d(TAG, "onCreate: args null");
            return;
        }

        ncode = args.getString(ARG_NCODE);
        search = args.getString(ARG_SEARCH);
        notSearch = args.getString(ARG_NOT_SEARCH);
        limit = args.getInt(ARG_LIMIT);
        sortOrder = args.getInt(ARG_SORT_ORDER);
        time = args.getInt(ARG_TIME);
        maxLength = args.getInt(ARG_MAX_LENGTH);
        minLength = args.getInt(ARG_MIN_LENGTH);
        targetTitle = args.getBoolean(ARG_TARGET_TITLE);
        targetStory = args.getBoolean(ARG_TARGET_STORY);
        targetKeyword = args.getBoolean(ARG_TARGET_KEYWORD);
        targetWriter = args.getBoolean(ARG_TARGET_WRITER);
        end = args.getBoolean(ARG_END);
        stop = args.getBoolean(ARG_STOP);
        pickup = args.getBoolean(ARG_PICKUP);
        genreList = args.getIntegerArrayList(ARG_GENRE_LIST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_recycler, container, false);

        controller = new SearchRecyclerPresenter(this);

        recyclerView = binding.recycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        NovelDetailRecyclerViewAdapter adapter = new NovelDetailRecyclerViewAdapter(context, true);
        recyclerView.setAdapter(adapter);

        controller.searchNovel(ncode, limit, sortOrder, search, notSearch, targetTitle, targetStory,
            targetKeyword, targetWriter, time, maxLength, minLength, end, stop, pickup, genreList);

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.replaceListener = (FragmentTransactionListener) context;
        this.context = context;

        if (controller != null) {
            controller.attach(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        controller.detach();
    }

    private void reload() {
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    private void onLoadError() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnReload.setVisibility(View.VISIBLE);
        binding.btnReload.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnReload.setVisibility(View.GONE);
            reload();
        });
    }
    @Override
    public void showRecyclerView(List<NovelItem> novelItems) {
        if (novelItems == null) {
            binding.noResultNovel.setVisibility(View.VISIBLE);
            return;
        }

        NovelDetailRecyclerViewAdapter adapter = (NovelDetailRecyclerViewAdapter) recyclerView.getAdapter();
        adapter.clearData();
        adapter.addDataOf(novelItems);
        allItems = new ArrayList<>(novelItems);
        binding.progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        adapter.setOnItemClickListener(new NovelDetailRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, ItemRankingRecyclerBinding binding) {
                if (view.getId() == R.id.btn_expand) {
                    if (binding.allStory.getVisibility() == View.GONE) {
                        binding.allStory.setVisibility(View.VISIBLE);
                        binding.keyword.setVisibility(View.VISIBLE);
                        binding.btnExpand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    } else {
                        binding.allStory.setVisibility(View.GONE);
                        binding.keyword.setVisibility(View.GONE);
                        binding.btnExpand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                } else {
                    NovelItem item = ((NovelDetailRecyclerViewAdapter) recyclerView.getAdapter()).getList().get(position);
                    replaceListener.replaceFragment(NovelTableRecyclerViewFragment.newInstance(item.getNovelDetail().getNcode()), item.getNovelDetail().getTitle(), item);
                }
            }

            @Override
            public void onItemLongClick(View view, int position, ItemRankingRecyclerBinding binding) {

            }
        });
    }

    @Override
    public void showError() {
        onLoadError();
    }
}
