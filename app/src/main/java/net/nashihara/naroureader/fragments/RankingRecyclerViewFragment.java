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
import net.nashihara.naroureader.databinding.ListItemBinding;
import net.nashihara.naroureader.entities.NovelItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import narou4j.Narou;
import narou4j.Novel;
import narou4j.NovelRank;
import narou4j.Ranking;
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
    private Fragment mFragment;
    private RecyclerView mRecyclerView;

    private MyProgressDialogFragment progressDialog;

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
        mRecyclerView.addOnItemTouchListener(
                new RankingRecycerViewAdapter.RecyclerItemClickListener(getActivity(),
                new RankingRecycerViewAdapter.RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        ListItemBinding binding = DataBindingUtil.bind(view);

                        if (binding.allStory.getVisibility() == View.GONE) {
                            binding.story.setVisibility(View.GONE);
                            binding.allStory.setVisibility(View.VISIBLE);
                            binding.keyword.setVisibility(View.VISIBLE);
                        } else {
                            binding.story.setVisibility(View.VISIBLE);
                            binding.allStory.setVisibility(View.GONE);
                            binding.keyword.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        RankingRecycerViewAdapter adapter = (RankingRecycerViewAdapter) mRecyclerView.getAdapter();
                        Log.d(TAG, "onItemLongClick: " + adapter.getList().get(position).toString());
                    }
                }));

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RankingRecycerViewAdapter adapter = new RankingRecycerViewAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);

        progressDialog = MyProgressDialogFragment.newInstance("", "loading...");
        progressDialog.show(getFragmentManager(), "load");

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
                    item.setNovelDetail(novel);
                    item.setRankingPoint(novel.getGlobalPoint());
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
                    public void onCompleted() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        Log.e(TAG, "onError: ", e.fillInStackTrace());
                    }

                    @Override
                    public void onNext(List<NovelItem> novelItems) {
                        Log.d(TAG, "onNext: add data novelItems: " + novelItems.size());

                        RankingRecycerViewAdapter adapter = (RankingRecycerViewAdapter) mRecyclerView.getAdapter();
                        adapter.clearData();
                        adapter.addDataOf(novelItems);
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
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
                    novelItem.setRankingPoint(rank.getPt());
                    novelItem.setRanking(rank.getRank());
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<NovelItem>>() {
                    @Override
                    public void onCompleted() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        Log.e(TAG, "onError: ", e.fillInStackTrace());
                    }

                    @Override
                    public void onNext(List<NovelItem> novelItems) {
                        Log.d(TAG, "onNext: add data novelItems: " + novelItems.size());

                        RankingRecycerViewAdapter adapter = (RankingRecycerViewAdapter) mRecyclerView.getAdapter();
                        adapter.clearData();
                        adapter.addDataOf(novelItems);
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                });
    }
}
