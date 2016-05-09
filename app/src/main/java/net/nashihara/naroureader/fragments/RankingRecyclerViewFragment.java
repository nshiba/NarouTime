package net.nashihara.naroureader.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.adapters.NovelDetailRecyclerViewAdapter;
import net.nashihara.naroureader.databinding.FragmentRankingRecyclerBinding;
import net.nashihara.naroureader.databinding.ItemRankingRecyclerBinding;
import net.nashihara.naroureader.dialogs.FilterDialogFragment;
import net.nashihara.naroureader.dialogs.ListDailogFragment;
import net.nashihara.naroureader.dialogs.NovelDownloadDialogFragment;
import net.nashihara.naroureader.dialogs.OkCancelDialogFragment;
import net.nashihara.naroureader.entities.NovelItem;
import net.nashihara.naroureader.listeners.OnFragmentReplaceListener;
import net.nashihara.naroureader.utils.DownloadUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import narou4j.Narou;
import narou4j.Ranking;
import narou4j.entities.Novel;
import narou4j.entities.NovelRank;
import narou4j.enums.NovelGenre;
import narou4j.enums.OutputOrder;
import narou4j.enums.RankingType;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RankingRecyclerViewFragment extends Fragment {
    private static final String TAG = RankingRecyclerViewFragment.class.getSimpleName();

    private FragmentRankingRecyclerBinding binding;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private OnFragmentReplaceListener mReplaceListener;
    private ArrayList<NovelItem> allItems = new ArrayList<>();

    private static final String PARAM_TYPE = "rankingType";

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
        mContext = context;
        mReplaceListener = (OnFragmentReplaceListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ranking_recycler, container, false);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] filters = new String[]{
                        "完結済み", "文学", "恋愛", "歴史", "推理", "ファンタジー",
                        "SF", "ホラー", "コメディー", "冒険", "学園",
                        "戦記", "童話", "詩", "エッセイ", "リプレイ", "その他"
                };
                boolean[] checked = new boolean[filters.length];
                checked[0] = false;
                for (int i = 1; i < checked.length; i++) {
                    checked[i] = true;
                }

                FilterDialogFragment checkBoxDialog = FilterDialogFragment
                        .newInstance("小説絞込み", filters, checked, new FilterDialogFragment.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveButton(int which, boolean[] itemChecked, String min, String max) {
                        Set<NovelGenre> trueSet = new HashSet<>();
                        ArrayList<NovelItem> filterList = new ArrayList<>();
                        ArrayList<NovelItem> resultList = new ArrayList<>();
                        int maxLength = 0;
                        int minLength = 0;

                        if (!min.equals("")) {
                            minLength = Integer.parseInt(min);
                        }
                        if (!max.equals("")) {
                            maxLength = Integer.parseInt(max);
                        }

                        for (int i = 0; i < itemChecked.length; i++) {
                            if (i == 0) {
                                // 完結済チェック
                                if (itemChecked[i]) {
                                    for (int j = 0; j < allItems.size(); j++) {
                                        Log.d(TAG, "onPositiveButton: isContinue: " + allItems.get(j).getNovelDetail().getIsNovelContinue());
                                        if (allItems.get(j).getNovelDetail().getIsNovelContinue() == 0) {
                                            filterList.add(allItems.get(j));
                                        }
                                    }
                                }
                                else {
                                    filterList.addAll(allItems);
                                }

                                continue;
                            }

                            // ジャンルチェック
                            NovelGenre checkedGenre = NovelGenre.valueOf(i);
                            if (itemChecked[i]) {
                                trueSet.add(checkedGenre);
                            }
                        }

                        for (int i = 0; i < filterList.size(); i++) {
                            NovelItem target = filterList.get(i);

                            if (!trueSet.contains(target.getNovelDetail().getGenre())) {
                                continue;
                            }

                            // 文字数チェック
                            if (maxLength <= 0) {
                                if (minLength > target.getNovelDetail().getNumberOfChar()) {
                                    continue;
                                }
                            }
                            else {
                                if (minLength > target.getNovelDetail().getNumberOfChar() || target.getNovelDetail().getNumberOfChar() > maxLength) {
                                    continue;
                                }
                            }

                            resultList.add(target);
                        }

                        NovelDetailRecyclerViewAdapter adapter = (NovelDetailRecyclerViewAdapter) mRecyclerView.getAdapter();
                        adapter.getList().clear();
                        adapter.getList().addAll(resultList);
                    }

                            @Override
                    public void onNeutralButton(int which) {

                        NovelDetailRecyclerViewAdapter adapter = (NovelDetailRecyclerViewAdapter) mRecyclerView.getAdapter();
                        adapter.getList().clear();
                        adapter.getList().addAll(allItems);
                    }
                });
                checkBoxDialog.show(getFragmentManager(), "multiple");
            }
        });

        mRecyclerView = binding.recycler;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext));

        NovelDetailRecyclerViewAdapter adapter = new NovelDetailRecyclerViewAdapter(mContext, false);
        mRecyclerView.setAdapter(adapter);

        Bundle args = getArguments();
        if (args != null) {
            String typeStr = args.getString(PARAM_TYPE);

            if (typeStr != null && typeStr.equals("all")) {
                getTotalRanking();
            }
            else {
                final RankingType type = RankingType.valueOf(typeStr);
                getRanking(type);
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public Context getContext() {
        return mContext;
    }

    private void getTotalRanking() {
        Observable.create(new Observable.OnSubscribe<List<NovelItem>>() {
            @Override
            public void call(Subscriber<? super List<NovelItem>> subscriber) {

                Narou narou = new Narou();
                narou.setOrder(OutputOrder.TOTAL_POINT);
                narou.setLim(301);
                List<Novel> novels = narou.getNovels();

                novels.remove(0);

                List<NovelItem> items = new ArrayList<>();
                NovelItem item;
                for (Novel novel : novels) {
                    // ncodeが大文字と小文字が混在しているので小文字に統一
                    novel.setNcode(novel.getNcode().toLowerCase());

                    item = new NovelItem();
                    NovelRank rank = new NovelRank();
                    rank.setPt(novel.getGlobalPoint());
                    item.setNovelDetail(novel);
                    item.setRank(rank);
                    items.add(item);
                }

                subscriber.onNext(items);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<NovelItem>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e.fillInStackTrace());
                        onLoadError();
                    }

                    @Override
                    public void onNext(List<NovelItem> novelItems) {
                        onMyNext(novelItems);
                    }
                });
    }

    private void getRanking(final RankingType type) {
        Observable.create(new Observable.OnSubscribe<HashMap<String, NovelItem>>() {
            @Override
            public void call(Subscriber<? super HashMap<String, NovelItem>> subscriber) {
                HashMap<String, NovelItem> map = new HashMap<>();
                Ranking ranking = new Ranking();
                for (NovelRank rank : ranking.getRanking(type)) {
                    NovelItem novelItem = new NovelItem();
                    rank.setRankingType(type);
                    novelItem.setRank(rank);
                    map.put(rank.getNcode(), novelItem);
                }
                subscriber.onNext(map);
                subscriber.onCompleted();
            }
        })
                .flatMap(new Func1<HashMap<String, NovelItem>, Observable<HashMap<String, NovelItem>>>() {
                    @Override
                    public Observable<HashMap<String, NovelItem>> call(final HashMap<String, NovelItem> map) {
                        return Observable.create(new Observable.OnSubscribe<HashMap<String, NovelItem>>() {
                            @Override
                            public void call(Subscriber<? super HashMap<String, NovelItem>> subscriber) {
                                Ranking ranking = new Ranking();
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(new Date());

                                switch (type) {
                                    case DAILY: {
                                        cal.add(Calendar.DAY_OF_MONTH, -2);
                                        break;
                                    }
                                    case WEEKLY: {
                                        cal.add(Calendar.DAY_OF_MONTH, -7);
                                        break;
                                    }
                                    case MONTHLY: {
                                        cal.add(Calendar.DAY_OF_MONTH, -31);
                                        break;
                                    }
                                    case QUARTET: {
                                        cal.add(Calendar.DAY_OF_MONTH, -31);
                                        break;
                                    }
                                }

                                List<NovelRank> ranks = ranking.getRanking(type, cal.getTime());
                                for (NovelRank rank : ranks) {
                                    NovelItem item = map.get(rank.getNcode());

                                    if (item == null) {
                                        continue;
                                    }

                                    item.setPrevRank(rank);
                                    map.put(rank.getNcode(), item);
                                }

                                subscriber.onNext(map);
                                subscriber.onCompleted();
                            }
                        });
                    }
                })
                .flatMap(new Func1<HashMap<String, NovelItem>, Observable<List<NovelItem>>>() {
                    @Override
                    public Observable<List<NovelItem>> call(final HashMap<String, NovelItem> map) {
                        return Observable.create(new Observable.OnSubscribe<List<NovelItem>>() {
                            @Override
                            public void call(Subscriber<? super List<NovelItem>> subscriber) {
                                Narou narou = new Narou();

                                Set set = map.keySet();
                                String[] array = new String[set.size()];
                                set.toArray(array);

                                narou.setNCode(array);
                                narou.setLim(300);
                                List<Novel> novels = narou.getNovels();

                                novels.remove(0);

                                ArrayList<NovelItem> items = new ArrayList<>();
                                for (Novel novel : novels) {
                                    NovelItem item = map.get(novel.getNcode());
                                    if (item != null) {
                                        // 大文字 → 小文字
                                        novel.setNcode(novel.getNcode().toLowerCase());

                                        item.setNovelDetail(novel);
                                        items.add(item);
                                    }
                                }

                                subscriber.onNext(items);
                                subscriber.onCompleted();
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<NovelItem>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e.fillInStackTrace());
                        onLoadError();
                    }

                    @Override
                    public void onNext(List<NovelItem> novelItems) {
                        onMyNext(novelItems);
                    }
                });
    }

    public void onMyNext(List<NovelItem> novelItems) {
        NovelDetailRecyclerViewAdapter adapter = (NovelDetailRecyclerViewAdapter) mRecyclerView.getAdapter();
        adapter.clearData();
        adapter.addDataOf(novelItems);
        allItems = new ArrayList<>(novelItems);
        binding.progressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);

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
                }
                else {
                    NovelItem item = ((NovelDetailRecyclerViewAdapter) mRecyclerView.getAdapter()).getList().get(position);
                    mReplaceListener.onFragmentReplaceAction(NovelTableRecyclerViewFragment.newInstance(item.getNovelDetail().getNcode()), item.getNovelDetail().getTitle(), item);
                }
            }

            @Override
            public void onItemLongClick(View view, final int position, ItemRankingRecyclerBinding binding) {

                NovelDetailRecyclerViewAdapter adapter = (NovelDetailRecyclerViewAdapter) mRecyclerView.getAdapter();

                final NovelItem item = adapter.getList().get(position);
                String[] strings = new String[]
                        {"小説を読む", "ダウンロード", "ブラウザで小説ページを開く", "ブラウザで作者ページを開く"};
                ListDailogFragment listDialog =
                        ListDailogFragment.newInstance(item.getNovelDetail().getTitle(), strings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: {
                                        NovelItem item = ((NovelDetailRecyclerViewAdapter) mRecyclerView.getAdapter()).getList().get(position);
                                        mReplaceListener.onFragmentReplaceAction(NovelTableRecyclerViewFragment.newInstance(item.getNovelDetail().getNcode()), item.getNovelDetail().getTitle(), item);
                                        break;
                                    }
                                    case 1: {
                                        DownloadUtils downloadUtils = new DownloadUtils() {
                                            @Override
                                            public void onDownloadSuccess(NovelDownloadDialogFragment dialog, final Novel novel) {
                                                dialog.dismiss();

                                                OkCancelDialogFragment okCancelDialog =
                                                        OkCancelDialogFragment.newInstance("ダウンロード完了", "ダウンロードした小説を開きますか？", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                                if (OkCancelDialogFragment.OK == which) {
                                                                    NovelItem novelItem = new NovelItem();
                                                                    novelItem.setNovelDetail(novel);
                                                                    mReplaceListener.onFragmentReplaceAction(
                                                                            NovelTableRecyclerViewFragment.newInstance(novel.getNcode()), novelItem.getNovelDetail().getTitle(), novelItem);
                                                                }
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
                                        downloadUtils.novelDownlaod(item.getNovelDetail(), getFragmentManager(), mContext);
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
                        });
                listDialog.show(getFragmentManager(), "list_dialog");
            }
        });
    }

    private void reload() {
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    private void onLoadError() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnReload.setVisibility(View.VISIBLE);
        binding.btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnReload.setVisibility(View.GONE);
                reload();
            }
        });
    }
}
