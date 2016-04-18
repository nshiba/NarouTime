package net.nashihara.naroureader.fragments;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.DividerItemDecoration;
import net.nashihara.naroureader.R;
import net.nashihara.naroureader.adapters.RankingRecycerViewAdapter;
import net.nashihara.naroureader.databinding.FragmentRankingRecyclerBinding;
import net.nashihara.naroureader.entities.NovelItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import narou4j.Narou;
import narou4j.Novel;
import narou4j.NovelRank;
import narou4j.Ranking;
import narou4j.enums.RankingType;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RankingRecyclerViewFragment extends Fragment implements RankingRecycerViewAdapter.OnItemClickListener {
    private static final String TAG = RankingRecyclerViewFragment.class.getSimpleName();

    private FragmentRankingRecyclerBinding binding;
    private Context mContext;
    private Fragment mFragment;
    private RecyclerView mRecyclerView;

    private static final String PARAM_TYPE = "rankingType";

    public static RankingRecyclerViewFragment newInstance(RankingType type) {
        RankingRecyclerViewFragment fragment = new RankingRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_TYPE, type.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public RankingRecyclerViewFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mFragment = this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ranking_recycler, container, false);

        mRecyclerView = binding.recycler;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext));

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RankingRecycerViewAdapter adapter = new RankingRecycerViewAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);

        NovelItem item = new NovelItem();
        Novel novel = new Novel();
        novel.setTitle("test");
        item.setNovelDetail(novel);
        item.setRankingPoint(300);
        ArrayList<NovelItem> list = new ArrayList();
        list.add(item);

        NovelItem item2 = new NovelItem();
        Novel novel2 = new Novel();
        novel2.setTitle("ssss");
        item2.setNovelDetail(novel2);
        item2.setRankingPoint(400);
        list.add(item2);

        adapter.clearData();
        adapter.addDataOf(list);

        Bundle args = getArguments();
        if (args != null) {
            final RankingType type = RankingType.valueOf(args.getString(PARAM_TYPE));

            Observable.create(new Observable.OnSubscribe<HashMap<String, NovelItem>>() {
                @Override
                public void call(Subscriber<? super HashMap<String, NovelItem>> subscriber) {
                    HashMap<String, NovelItem> map = new HashMap<>();
                    Ranking ranking = new Ranking();
                    for (NovelRank rank : ranking.getRanking(type)) {
                        NovelItem novelItem = new NovelItem();
                        novelItem.setRankingPoint(rank.getPt());
                        map.put(rank.getNcode(), novelItem);
                    }
                    subscriber.onNext(map);
                    subscriber.onCompleted();
                }
            })
                    .flatMap(new Func1<HashMap<String, NovelItem>, Observable<List<NovelItem>>>() {
                        @Override
                        public Observable<List<NovelItem>> call(final HashMap<String, NovelItem> stringNovelItemHashMap) {
                            return Observable.create(new Observable.OnSubscribe<List<NovelItem>>() {
                                @Override
                                public void call(Subscriber<? super List<NovelItem>> subscriber) {
                                    Narou narou = new Narou();

                                    HashMap<String, NovelItem> map = stringNovelItemHashMap;
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
                                        item.setNovelDetail(novel);
                                        items.add(item);
                                    }

                                    subscriber.onNext(items);
                                    subscriber.onCompleted();
                                }
                            });
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<List<NovelItem>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(List<NovelItem> novelItems) {
                            Log.d(TAG, "onNext: add data novelItems: " + novelItems.size());

                            RankingRecycerViewAdapter adapter = (RankingRecycerViewAdapter) mRecyclerView.getAdapter();
                            adapter.clearData();
                            adapter.addDataOf(novelItems);
                        }
                    });
        }
    }

    @Override
    public void onItemClick(RankingRecycerViewAdapter adapter, int position, NovelItem item) {

    }
}
