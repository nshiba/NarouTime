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
import android.widget.Toast;

import net.nashihara.naroureader.DividerItemDecoration;
import net.nashihara.naroureader.OnFragmentReplaceListener;
import net.nashihara.naroureader.R;
import net.nashihara.naroureader.adapters.RankingRecyclerViewAdapter;
import net.nashihara.naroureader.databinding.FragmentRankingRecyclerBinding;
import net.nashihara.naroureader.databinding.RankingListItemBinding;
import net.nashihara.naroureader.entities.NovelItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import narou4j.Narou;
import narou4j.Ranking;
import narou4j.entities.Novel;
import narou4j.entities.NovelRank;
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

    private static final String PARAM_TYPE = "rankingType";

    public static RankingRecyclerViewFragment newInstance(String type) {
        RankingRecyclerViewFragment fragment = new RankingRecyclerViewFragment();
        Log.d(TAG, "newInstance: " + type);
        Bundle args = new Bundle();
        args.putString(PARAM_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public RankingRecyclerViewFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: recyclerfragment");
        mContext = context;
        mReplaceListener = (OnFragmentReplaceListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: recyclerfragment");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: recyclerfragment");
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ranking_recycler, container, false);

        mRecyclerView = binding.recycler;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext));

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: recyclerfragment");
        RankingRecyclerViewAdapter adapter = new RankingRecyclerViewAdapter(mContext);
        mRecyclerView.setAdapter(adapter);

        Bundle args = getArguments();
        if (args != null) {
            String typeStr = args.getString(PARAM_TYPE);
            Log.d(TAG, "onActivityCreated: " + typeStr);

            if (typeStr != null && typeStr.equals("all")) {
                getTotalRanking();
            }
            else {
                final RankingType type = RankingType.valueOf(typeStr);
                getRanking(type);
            }
        }
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
                    }

                    @Override
                    public void onNext(List<NovelItem> novelItems) {
                        onMyNext(novelItems);
                    }
                });
    }

    public void onMyNext(List<NovelItem> novelItems) {
        Log.d(TAG, "onNext: add data novelItems: " + novelItems.size());

        RankingRecyclerViewAdapter adapter = (RankingRecyclerViewAdapter) mRecyclerView.getAdapter();
        adapter.clearData();
        adapter.addDataOf(novelItems);
        binding.progressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);

        adapter.setOnItemClickListener(new RankingRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, RankingListItemBinding binding) {
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
                    NovelItem item = ((RankingRecyclerViewAdapter) mRecyclerView.getAdapter()).getList().get(position);
                    mReplaceListener.onFragmentReplaceAction(NovelTableRecyclerViewFragment.newInstance(item.getNovelDetail().getNcode()), item.getNovelDetail().getTitle());
                }
            }

            @Override
            public void onItemLongClick(View view, final int position, RankingListItemBinding binding) {

                RankingRecyclerViewAdapter adapter = (RankingRecyclerViewAdapter) mRecyclerView.getAdapter();
                Log.d(TAG, "onItemLongClick: position -> " + position + "\n" + adapter.getList().get(position).toString());

                final NovelItem item = adapter.getList().get(position);
                String[] strings = new String[]
                        {"小説を読む", "ダウンロード", "ブラウザで小説ページを開く", "ブラウザで作者ページを開く"};
                ListDailogFragment listDialog =
                        new ListDailogFragment(item.getNovelDetail().getTitle(), strings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: {
                                        NovelItem item = ((RankingRecyclerViewAdapter) mRecyclerView.getAdapter()).getList().get(position);
                                        mReplaceListener.onFragmentReplaceAction(NovelTableRecyclerViewFragment.newInstance(item.getNovelDetail().getNcode()), item.getNovelDetail().getTitle());
                                        break;
                                    }
                                    case 1: {
                                        Toast.makeText(getActivity(), "未実装の機能", Toast.LENGTH_SHORT).show();
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
}
