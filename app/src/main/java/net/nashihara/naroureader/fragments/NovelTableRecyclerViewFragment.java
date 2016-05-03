package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.RealmUtils;
import net.nashihara.naroureader.adapters.NovelTableRecyclerViewAdapter;
import net.nashihara.naroureader.databinding.FragmentNovelTableViewBinding;
import net.nashihara.naroureader.databinding.ItemTableRecyclerBinding;
import net.nashihara.naroureader.dialogs.OkCancelDialogFragment;
import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.entities.NovelTable4Realm;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import narou4j.Narou;
import narou4j.entities.Novel;
import narou4j.entities.NovelBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class NovelTableRecyclerViewFragment extends Fragment {
    private static final String TAG = NovelTableRecyclerViewFragment.class.getSimpleName();

    private static final String PARAM_NCODE = "ncode";
    private Realm realm;
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    private ArrayList<String> bodyTitles;
    private String title;
    private String writer;
    private String ncode;
    private int totalPage;
    private Context mContext;
    private OnNovelSelectionListener mListener;
    private RecyclerView mRecyclerView;
    private FragmentNovelTableViewBinding binding;

    public NovelTableRecyclerViewFragment() {}

    public static NovelTableRecyclerViewFragment newInstance(String ncode) {
        NovelTableRecyclerViewFragment fragment = new NovelTableRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_NCODE, ncode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mListener = (OnNovelSelectionListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ncode = getArguments().getString(PARAM_NCODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_novel_table_view, container, false);

        mRecyclerView = binding.recycler;
        final LinearLayoutManager manager = new LinearLayoutManager(mContext) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        manager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(manager);
        NovelTableRecyclerViewAdapter adapter = new NovelTableRecyclerViewAdapter(mContext);
        adapter.setOnItemClickListener(new NovelTableRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, ItemTableRecyclerBinding binding) {
                NovelTableRecyclerViewAdapter clickAdapter = (NovelTableRecyclerViewAdapter) mRecyclerView.getAdapter();
                NovelBody body = clickAdapter.getList().get(position);
                Log.d(TAG, "NovelTableRecyclerView: list size -> " + clickAdapter.getList().size());
                Log.d(TAG, "onItemClick: position -> " + position + "\n" + body.toString());
                mListener.onSelect(body.getNcode(), totalPage, body.getPage(), title, writer, body.getTitle());
           }
        });
        mRecyclerView.setAdapter(adapter);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int bookmark = loadBookmark();
                if (bookmark == 0) {
                    OkCancelDialogFragment dialogFragment
                            = new OkCancelDialogFragment("ブックマーク", "この小説にはしおりをはさんでいません。", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    dialogFragment.show(getFragmentManager(), "okcansel");
                }
                else {
                    mListener.onSelect(ncode, totalPage, bookmark, title, writer, bodyTitles.get(bookmark -1));
                }
            }
        });

        boolean isLoadTable = loadTable();

        if (!isLoadTable) {
            Observable.zip(Observable.create(new Observable.OnSubscribe<Novel>() {
                @Override
                public void call(Subscriber<? super Novel> subscriber) {
                    Narou narou = new Narou();
                    subscriber.onNext(narou.getNovel(ncode));
                }
            }), Observable.create(new Observable.OnSubscribe<List<NovelBody>>() {
                @Override
                public void call(Subscriber<? super List<NovelBody>> subscriber) {
                    Narou narou = new Narou();
                    List<NovelBody> bodies = narou.getNovelTable(ncode);
                    subscriber.onNext(bodies);
                }
            }), new Func2<Novel, List<NovelBody>, Novel>() {
                @Override
                public Novel call(Novel novel, List<NovelBody> novelBodies) {
                    novel.setBodies(novelBodies);
                    return novel;
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Novel>() {
                        @Override
                        public void onCompleted() { }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: ", e.fillInStackTrace());
                        }

                        @Override
                        public void onNext(Novel novel) {
                            binding.title.setText(novel.getTitle());
                            binding.writer.setText(novel.getWriter());
                            binding.story.setText(novel.getStory());

                            Log.d(TAG, "onNext: " + novel.toString());
                            NovelTableRecyclerViewAdapter rxAdapter = (NovelTableRecyclerViewAdapter) mRecyclerView.getAdapter();
                            rxAdapter.clearData();
                            rxAdapter.addDataOf(novel.getBodies());

                            binding.progressBar.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            binding.title.setVisibility(View.VISIBLE);
                            binding.writer.setVisibility(View.VISIBLE);
                            binding.story.setVisibility(View.VISIBLE);

                            writer = novel.getWriter();
                            title = novel.getTitle();
                            totalPage = novel.getAllNumberOfNovel();
                            bodyTitles = new ArrayList<>();
                            for (NovelBody body : novel.getBodies()) {
                                if (!body.isChapter()) {
                                    bodyTitles.add(body.getTitle());
                                }
                            }
                        }
                    });
        }

        setFabMargin();
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean loadTable() {
        realm = RealmUtils.getRealm(mContext);
        RealmResults<NovelTable4Realm> tableResult = realm.where(NovelTable4Realm.class).equalTo("ncode", ncode).findAll().sort("tableNumber");
        Novel4Realm novel4Realm = realm.where(Novel4Realm.class).equalTo("ncode", ncode).findFirst();

        if (novel4Realm == null) {
            return false;
        }

        if (tableResult.size() <= 0 || !novel4Realm.isDownload()) {
            return false;
        }

        ArrayList<NovelBody> table = new ArrayList<>();
        for (NovelTable4Realm novelTable4Realm : tableResult) {
            NovelBody tableItem = new NovelBody();
            tableItem.setNcode(novelTable4Realm.getNcode());
            tableItem.setTitle(novelTable4Realm.getTitle());
            tableItem.setChapter(novelTable4Realm.isChapter());
            tableItem.setPage(novelTable4Realm.getPage());
            table.add(tableItem);
        }

        binding.title.setText(novel4Realm.getTitle());
        binding.writer.setText(novel4Realm.getWriter());
        binding.story.setText(novel4Realm.getStory());

        NovelTableRecyclerViewAdapter rxAdapter = (NovelTableRecyclerViewAdapter) mRecyclerView.getAdapter();
        rxAdapter.clearData();
        rxAdapter.addDataOf(table);

        binding.progressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        binding.title.setVisibility(View.VISIBLE);
        binding.writer.setVisibility(View.VISIBLE);
        binding.story.setVisibility(View.VISIBLE);

        writer = novel4Realm.getWriter();
        title = novel4Realm.getTitle();
        totalPage = novel4Realm.getTotalPage();
        bodyTitles = new ArrayList<>();
        for (NovelBody body : table) {
            if (!body.isChapter()) {
                bodyTitles.add(body.getTitle());
            }
        }

        realm.close();

        return true;
    }

    private void setFabMargin() {
        globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int margin = binding.fab.getHeight() /2 * -1;
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) binding.fab.getLayoutParams();
                mlp.setMargins(mlp.leftMargin, margin, mlp.rightMargin, mlp.bottomMargin);
                binding.fab.setLayoutParams(mlp);

                binding.topContainer.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
            }
        };

        binding.topContainer.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    private int loadBookmark() {
        Realm realm = RealmUtils.getRealm(mContext);

        Log.d(TAG, "loadBookmark: " + ncode);

        RealmQuery<Novel4Realm> query = realm.where(Novel4Realm.class);
        query.equalTo("ncode", ncode);
        RealmResults<Novel4Realm> results = query.findAll();

        Log.d(TAG, "loadBookmark: " + results.size());

        if (results.size() == 0) {
            return 0;
        }
        else {
            Novel4Realm novel4Realm = results.get(0);
            return novel4Realm.getBookmark();
        }
    }

    public interface OnNovelSelectionListener {
        public void onSelect(String ncode, int totalPage, int page, String title, String writer, String bodyTitle);
    }
}
