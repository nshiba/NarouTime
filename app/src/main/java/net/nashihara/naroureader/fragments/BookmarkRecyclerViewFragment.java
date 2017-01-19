package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.activities.NovelViewActivity;
import net.nashihara.naroureader.adapters.SimpleRecyclerViewAdapter;
import net.nashihara.naroureader.controller.BookmarkRecyclerController;
import net.nashihara.naroureader.databinding.FragmentSimpleRecycerViewBinding;
import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.views.BookmarkRecyclerView;

import java.util.ArrayList;

public class BookmarkRecyclerViewFragment extends Fragment implements BookmarkRecyclerView {

    private SimpleRecyclerViewAdapter adapter;

    private Context context;

    private FragmentSimpleRecycerViewBinding binding;

    private BookmarkRecyclerController controller;

    public BookmarkRecyclerViewFragment() {}

    public static BookmarkRecyclerViewFragment newInstance() {
        BookmarkRecyclerViewFragment fragment = new BookmarkRecyclerViewFragment();
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

        controller = new BookmarkRecyclerController(this);

        adapter = new SimpleRecyclerViewAdapter(context);
        adapter.setOnItemClickListener((view, position) -> startNovelActivity(position));

        controller.fetchBookmarkNovels();
    }

    private void startNovelActivity(int position) {
        final Novel4Realm novel = adapter.getList().get(position);

        Intent intent = new Intent(context, NovelViewActivity.class);
        intent.putExtra("ncode", novel.getNcode());
        intent.putExtra("page", novel.getBookmark());
        intent.putExtra("title", novel.getTitle());
        intent.putExtra("writer", novel.getWriter());
        intent.putExtra("totalPage", novel.getTotalPage());

        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_simple_recycer_view, container, false);

        binding.recycler.setLayoutManager(new LinearLayoutManager(context));
        binding.recycler.setAdapter(adapter);

        binding.progressBar.setVisibility(View.GONE);
        binding.recycler.setVisibility(View.VISIBLE);

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        controller.detach();
    }

    @Override
    public void showBookmarks(ArrayList<Novel4Realm> novels) {
        adapter.clearData();
        adapter.addDataOf(novels);
    }
}
