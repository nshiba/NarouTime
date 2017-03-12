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
import net.nashihara.naroureader.databinding.FragmentSearchRecyclerBinding;
import net.nashihara.naroureader.databinding.ItemRankingRecyclerBinding;
import net.nashihara.naroureader.entities.NovelItem;
import net.nashihara.naroureader.entities.Query;
import net.nashihara.naroureader.listeners.FragmentTransactionListener;
import net.nashihara.naroureader.presenter.SearchRecyclerPresenter;
import net.nashihara.naroureader.views.SearchRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchRecyclerViewFragment extends Fragment implements SearchRecyclerView {
    private static final String TAG = SearchRecyclerViewFragment.class.getSimpleName();

    private static final String ARG_QUERY = "query";

    private static final String ARG_GENRE_LIST = "genre_list";

    private Query query;

    private ArrayList<Integer> genreList;

    private FragmentSearchRecyclerBinding binding;

    private Context context;

    private RecyclerView recyclerView;

    private FragmentTransactionListener replaceListener;

    private SearchRecyclerPresenter controller;

    private List<NovelItem> allItems = new ArrayList<>();

    public SearchRecyclerViewFragment() {}

    public static SearchRecyclerViewFragment newInstance(Query query, ArrayList<Integer> genreList) {

        SearchRecyclerViewFragment fragment = new SearchRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_QUERY, query);
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

        query = args.getParcelable(ARG_QUERY);
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

        controller.searchNovel(query, genreList);

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
