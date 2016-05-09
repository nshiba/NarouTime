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
import net.nashihara.naroureader.listeners.OnFragmentReplaceListener;

import java.util.ArrayList;
import java.util.List;

import narou4j.Narou;
import narou4j.entities.Novel;
import narou4j.entities.NovelRank;
import narou4j.enums.NovelType;
import narou4j.enums.OutputOrder;
import narou4j.enums.SearchWordTarget;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchRecyclerViewFragment extends Fragment {
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

    private FragmentSearchRecyclerBinding binding;
    private Context context;
    private RecyclerView recyclerView;
    private OnFragmentReplaceListener replaceListener;

    private ArrayList<NovelItem> allItems;

    public SearchRecyclerViewFragment() {}

    public static SearchRecyclerViewFragment newInstance(
        String ncode, int limit, int sortOrder, String search, String notSearch, boolean targetTitle,
        boolean targetStory, boolean targetKeyword, boolean targetWriter, int time,
        int maxLength, int minLength, boolean end, boolean stop, boolean pickup) {

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_recycler, container, false);

        recyclerView = binding.recycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext));

        NovelDetailRecyclerViewAdapter adapter = new NovelDetailRecyclerViewAdapter(context, true);
        recyclerView.setAdapter(adapter);

        searchNovel().subscribe(new Subscriber<List<Novel>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: ", e.fillInStackTrace());
                onLoadError();
            }

            @Override
            public void onNext(List<Novel> novels) {
                if (novels == null) {
                    return;
                }

                List<NovelItem> novelItems = new ArrayList<>();
                for (int i = 0; i < novels.size(); i++) {
                    Novel novel = novels.get(i);
                    NovelItem item = new NovelItem();
                    NovelRank rank = new NovelRank();

                    novel.setNcode(novel.getNcode().toLowerCase());

                    rank.setNcode(novel.getNcode());
                    rank.setRank(i);

                    item.setNovelDetail(novel);
                    item.setRank(rank);

                    novelItems.add(item);
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
                            replaceListener.onFragmentReplaceAction(NovelTableRecyclerViewFragment.newInstance(item.getNovelDetail().getNcode()), item.getNovelDetail().getTitle(), item);
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position, ItemRankingRecyclerBinding binding) {

                    }
                });
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.replaceListener = (OnFragmentReplaceListener) context;
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

    private Observable<List<Novel>> searchNovel() {
        return Observable.create(new Observable.OnSubscribe<List<Novel>>() {
            @Override
            public void call(Subscriber<? super List<Novel>> subscriber) {
                Narou narou = new Narou();

                if (!ncode.equals("")) {
                    Novel novel = narou.getNovel(ncode);

                    if (novel == null) {
                        subscriber.onNext(null);
                    }
                    else {
                        List<Novel> list = new ArrayList<>();
                        list.add(novel);
                        subscriber.onNext(list);
                    }
                }
                else {
                    if (!search.equals("")) {
                        narou.setSearchWord(search);
                    }

                    if (limit == 0) {
                        narou.setLim(50);
                    }
                    else {
                        narou.setLim(limit);
                    }

                    setSortOrder(narou, sortOrder);
                    setTime(narou, time);

                    if (minLength != 0 || maxLength != 0) {
                        narou.setCharacterLength(minLength, maxLength);
                    }

                    if (!notSearch.equals("")) {
                        narou.setNotWord(notSearch);
                    }
                    if (targetTitle) {
                        narou.setSearchTarget(SearchWordTarget.TITLE);
                    }
                    if (targetStory) {
                        narou.setSearchTarget(SearchWordTarget.SYNOPSIS);
                    }
                    if (targetKeyword) {
                        narou.setSearchTarget(SearchWordTarget.KEYWORD);
                    }
                    if (targetWriter) {
                        narou.setSearchTarget(SearchWordTarget.WRITER);
                    }

                    if (end) {
                        narou.setNovelType(NovelType.ALL_NOVEL);
                    }
                    else {
                        narou.setNovelType(NovelType.ALL_SERIES);
                    }

                    if (stop) {
                        narou.setExcludeStop(true);
                    }

                    if (pickup) {
                        narou.setPickup(true);
                    }

                    List<Novel> novels = narou.getNovels();
                    novels.remove(0);
                    subscriber.onNext(novels);
                }

                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private void setSortOrder(Narou narou, int order) {
        switch (order) {
            case 1: {
                narou.setOrder(OutputOrder.BOOKMARK_COUNT);
                break;
            }
            case 2: {
                narou.setOrder(OutputOrder.REVIEW_COUNT);
                break;
            }
            case 3: {
                narou.setOrder(OutputOrder.TOTAL_POINT);
                break;
            }
            case 4: {
                narou.setOrder(OutputOrder.TOTAL_POINT_ASC);
                break;
            }
            case 5: {
                narou.setOrder(OutputOrder.IMPRESSION_COUNT);
                break;
            }
            case 6: {
                narou.setOrder(OutputOrder.HYOKA_COUNT);
                break;
            }
            case 7: {
                narou.setOrder(OutputOrder.HYOKA_COUNT_ASC);
                break;
            }
            case 8: {
                narou.setOrder(OutputOrder.WEEKLY_UU);
                break;
            }
            case 9: {
                narou.setOrder(OutputOrder.CHARACTER_LENGTH_ASC);
                break;
            }
            case 10: {
                narou.setOrder(OutputOrder.CHARACTER_LENGTH_DESC);
                break;
            }
            case 11: {
                narou.setOrder(OutputOrder.NCODE_DESC);
                break;
            }
            case 12: {
                narou.setOrder(OutputOrder.OLD);
                break;
            }
        }
    }

    private void setTime(Narou narou, int time) {
        switch (time) {
            case 1: {
                narou.setReadTime(0, 5);
                break;
            }
            case 2: {
                narou.setReadTime(5, 10);
                break;
            }
            case 3: {
                narou.setReadTime(10, 30);
                break;
            }
            case 4: {
                narou.setReadTime(30, 60);
                break;
            }
            case 5: {
                narou.setReadTime(60, 120);
                break;
            }
            case 6: {
                narou.setReadTime(120, 180);
                break;
            }
            case 7: {
                narou.setReadTime(180, 240);
                break;
            }
            case 8: {
                // 異世界
                narou.setReadTime(240, 300);
                break;
            }
            case 9: {
                narou.setReadTime(300, 360);
                break;
            }
            case 10: {
                narou.setReadTime(360, 420);
                break;
            }
            case 11: {
                narou.setReadTime(420, 480);
                break;
            }
            case 12: {
                narou.setReadTime(480, 540);
                break;
            }
            case 13: {
                narou.setReadTime(540, 600);
                break;
            }
            case 14: {
                narou.setReadTime(600, 0);
                break;
            }
        }
    }
}
