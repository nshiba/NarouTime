package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.adapters.NovelDetailRecyclerViewAdapter;
import net.nashihara.naroureader.presenter.RankingRecyclerPresenter;
import net.nashihara.naroureader.databinding.FragmentRankingRecyclerBinding;
import net.nashihara.naroureader.databinding.ItemRankingRecyclerBinding;
import net.nashihara.naroureader.entities.NovelItem;
import net.nashihara.naroureader.listeners.FragmentTransactionListener;
import net.nashihara.naroureader.utils.DownloadUtils;
import net.nashihara.naroureader.views.RankingRecyclerView;
import net.nashihara.naroureader.widgets.FilterDialogFragment;
import net.nashihara.naroureader.widgets.ListDailogFragment;
import net.nashihara.naroureader.widgets.NovelDownloadDialogFragment;
import net.nashihara.naroureader.widgets.OkCancelDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import narou4j.entities.Novel;
import narou4j.enums.NovelGenre;

public class RankingRecyclerViewFragment extends Fragment implements RankingRecyclerView {

    private static final String TAG = RankingRecyclerViewFragment.class.getSimpleName();

    private FragmentRankingRecyclerBinding binding;

    private Context context;

    private FragmentTransactionListener replaceListener;

    private ArrayList<NovelItem> allItems = new ArrayList<>();

    private static final String PARAM_TYPE = "rankingType";

    private RankingRecyclerPresenter controller;

    public static RankingRecyclerViewFragment newInstance(String type) {
        RankingRecyclerViewFragment fragment = new RankingRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public RankingRecyclerViewFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        replaceListener = (FragmentTransactionListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ranking_recycler, container, false);

        controller = new RankingRecyclerPresenter(this);

        binding.fab.setOnClickListener(v -> setupFab());

        binding.recycler.setLayoutManager(new LinearLayoutManager(context));

        NovelDetailRecyclerViewAdapter adapter = setupRecyclerView();
        binding.recycler.setAdapter(adapter);

        Bundle args = getArguments();
        if (args != null) {
            String typeStr = args.getString(PARAM_TYPE, "");
            controller.fetchRanking(typeStr);
        }

        return binding.getRoot();
    }

    private void setupFab() {
        ArrayList<String> filters =
            new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.genres)));
        filters.add(0, "完結済み");

        final NovelGenre[] filterIds =NovelGenre.values();


        boolean[] checked = new boolean[filters.size()];
        checked[0] = false;
        for (int i = 1; i < checked.length; i++) {
            checked[i] = true;
        }
        FilterDialogFragment checkBoxDialog = FilterDialogFragment
            .newInstance("小説絞込み", filters.toArray(new String[0]), checked, true,
                new FilterDialogFragment.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveButton(int which, boolean[] itemChecked, String min, String max) {
                        controller.filterNovelRanking(allItems, filterIds, itemChecked, min, max);
                    }

                    @Override
                    public void onNeutralButton(int which) {

                        NovelDetailRecyclerViewAdapter adapter = (NovelDetailRecyclerViewAdapter) binding.recycler.getAdapter();
                        adapter.getList().clear();
                        adapter.getList().addAll(allItems);
                    }
                });
        checkBoxDialog.show(getFragmentManager(), "multiple");
    }

    private NovelDetailRecyclerViewAdapter setupRecyclerView() {
        NovelDetailRecyclerViewAdapter adapter = new NovelDetailRecyclerViewAdapter(context, false);
        adapter.setOnItemClickListener(new NovelDetailRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, ItemRankingRecyclerBinding itemBinding) {
                if (view.getId() != R.id.btn_expand) {
                    NovelItem item = ((NovelDetailRecyclerViewAdapter) binding.recycler.getAdapter()).getList().get(position);
                    replaceListener.replaceFragment(NovelTableRecyclerViewFragment.newInstance(item.getNovelDetail().getNcode()), item.getNovelDetail().getTitle(), item);
                    return;
                }

                if (itemBinding.allStory.getVisibility() == View.GONE) {
                    itemBinding.allStory.setVisibility(View.VISIBLE);
                    itemBinding.keyword.setVisibility(View.VISIBLE);
                    itemBinding.btnExpand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                } else {
                    itemBinding.allStory.setVisibility(View.GONE);
                    itemBinding.keyword.setVisibility(View.GONE);
                    itemBinding.btnExpand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                }
            }

            @Override
            public void onItemLongClick(View view, final int position, ItemRankingRecyclerBinding itemBinding) {

                NovelDetailRecyclerViewAdapter adapter = (NovelDetailRecyclerViewAdapter) binding.recycler.getAdapter();

                final NovelItem item = adapter.getList().get(position);
                String[] strings = new String[]
                    {"小説を読む", "ダウンロード", "ブラウザで小説ページを開く", "ブラウザで作者ページを開く"};
                ListDailogFragment listDialog =
                    ListDailogFragment.newInstance(item.getNovelDetail().getTitle(), strings,
                        (dialog, which) -> longClickListDialogListener(which, position, item));
                listDialog.show(getFragmentManager(), "list_dialog");
            }
        });

        return adapter;
    }

    private void longClickListDialogListener(int which, int position, NovelItem item) {
        switch (which) {
            case 0: {
                NovelItem item1 = ((NovelDetailRecyclerViewAdapter) binding.recycler.getAdapter()).getList().get(position);
                replaceListener.replaceFragment(NovelTableRecyclerViewFragment.newInstance(
                    item1.getNovelDetail().getNcode()), item1.getNovelDetail().getTitle(), item1);
                break;
            }
            case 1: {
                DownloadUtils downloadUtils = new DownloadUtils() {
                    @Override
                    public void onDownloadSuccess(NovelDownloadDialogFragment dialog, final Novel novel) {
                        dialog.dismiss();

                        OkCancelDialogFragment okCancelDialog =
                            OkCancelDialogFragment.newInstance("ダウンロード完了", "ダウンロードした小説を開きますか？",
                                (dialog1, which1) -> {
                                    dialog1.dismiss();
                                    if (OkCancelDialogFragment.OK == which1) {
                                        NovelItem novelItem = new NovelItem();
                                        novelItem.setNovelDetail(novel);
                                        replaceListener.replaceFragment(
                                            NovelTableRecyclerViewFragment.newInstance(novel.getNcode()),
                                            novelItem.getNovelDetail().getTitle(), novelItem);
                                    }
                                });
                        okCancelDialog.show(getFragmentManager(), "okcansel");
                    }

                    @Override
                    public void onDownloadError(NovelDownloadDialogFragment dialog) {
                        Log.d(TAG, "onDownloadError: ");
                        dialog.dismiss();
                    }
                };
                downloadUtils.novelDownlaod(item.getNovelDetail(), getFragmentManager(), context);
                break;
            }
            case 2: {
                String url = "http://ncode.syosetu.com/" + item.getNovelDetail().getNcode() + "/";
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            }
            case 3: {
                String url = "http://mypage.syosetu.com/" + item.getNovelDetail().getUserId() + "/";
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            }
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public Context getContext() {
        return context;
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
    public void showRanking(List<NovelItem> novelItems) {
        if (allItems.isEmpty()) {
            allItems = new ArrayList<>(novelItems);
        }

        NovelDetailRecyclerViewAdapter adapter = (NovelDetailRecyclerViewAdapter) binding.recycler.getAdapter();
        adapter.clearData();
        adapter.addDataOf(novelItems);
        binding.progressBar.setVisibility(View.GONE);
        binding.recycler.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError() {
        onLoadError();
    }
}
