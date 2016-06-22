package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.google.firebase.crash.FirebaseCrash;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.FragmentNovelTableViewBinding;
import net.nashihara.naroureader.models.entities.Novel4Realm;
import net.nashihara.naroureader.models.entities.NovelTable4Realm;
import net.nashihara.naroureader.utils.RealmUtils;
import net.nashihara.naroureader.views.adapters.NovelTableRecyclerViewAdapter;
import net.nashihara.naroureader.views.widgets.OkCancelDialogFragment;

import java.io.IOException;
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
        adapter.setOnItemClickListener((view, position, binding1) -> {
            NovelTableRecyclerViewAdapter clickAdapter = (NovelTableRecyclerViewAdapter) mRecyclerView.getAdapter();
            NovelBody body = clickAdapter.getList().get(position);
            mListener.onSelect(body.getNcode(), totalPage, body.getPage(), title, writer, body.getTitle());
        });
        mRecyclerView.setAdapter(adapter);

        binding.fab.setOnClickListener(v -> {
            int bookmark = loadBookmark();
            if (bookmark == 0) {
                OkCancelDialogFragment dialogFragment
                    = OkCancelDialogFragment.newInstance("ブックマーク", "この小説にはしおりをはさんでいません。", (dialog, which) -> {});
                dialogFragment.show(getFragmentManager(), "okcansel");
            }
            else {
                mListener.onSelect(ncode, totalPage, bookmark, title, writer, bodyTitles.get(bookmark -1));
            }
        });

        boolean isLoadTable = loadTable();

        if (!isLoadTable) {
            Observable.zip(Observable.create(new Observable.OnSubscribe<Novel>() {
                @Override
                public void call(Subscriber<? super Novel> subscriber) {
                    Narou narou = new Narou();
                    try {
                        subscriber.onNext(narou.getNovel(ncode));
                    } catch (IOException e) {
                        subscriber.onError(e);
                    }
                }
            }), Observable.create(new Observable.OnSubscribe<List<NovelBody>>() {
                @Override
                public void call(Subscriber<? super List<NovelBody>> subscriber) {
                    Narou narou = new Narou();
                    try {
                        subscriber.onNext(narou.getNovelTable(ncode));
                    } catch (IOException e) {
                        subscriber.onError(e);
                    }
                }
            }), (novel, novelBodies) -> {
                novel.setBodies(novelBodies);
                return novel;
            })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Novel>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        onLoadError();
                        Log.e(TAG, "onError: ", e.fillInStackTrace());
                        FirebaseCrash.report(e);
                    }

                    @Override
                    public void onNext(Novel novel) {
                        binding.title.setText(novel.getTitle());
                        binding.ncode.setText("Nコード : " + ncode);
                        binding.writer.setText("作者 : " + novel.getWriter());
                        binding.story.setText(novel.getStory());

                        NovelTableRecyclerViewAdapter rxAdapter = (NovelTableRecyclerViewAdapter) mRecyclerView.getAdapter();
                        rxAdapter.clearData();
                        rxAdapter.addDataOf(novel.getBodies());

                        setRecyclerViewLayoutParams();

                        binding.progressBar.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        binding.title.setVisibility(View.VISIBLE);
                        binding.ncode.setVisibility(View.VISIBLE);
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

    private void setRecyclerViewLayoutParams() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        NovelTableRecyclerViewAdapter adapter = (NovelTableRecyclerViewAdapter) mRecyclerView.getAdapter();
        ArrayList<NovelBody> bodies = adapter.getList();

        int height = 0;
        for (NovelBody body : bodies) {
            if (body.isChapter()) {
                height += 148;
            }
            else {
                height += 135;
            }
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mRecyclerView.getLayoutParams();
        Log.d(TAG, "setRecyclerViewLayoutParams: before height -> " + params.height);
        params.height = height;
        Log.d(TAG, "setRecyclerViewLayoutParams: after height -> " + params.height);
        mRecyclerView.setLayoutParams(params);

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
        binding.ncode.setText("Nコード : " + ncode);
        binding.writer.setText("作者 : " + novel4Realm.getWriter());
        binding.story.setText(novel4Realm.getStory());

        NovelTableRecyclerViewAdapter rxAdapter = (NovelTableRecyclerViewAdapter) mRecyclerView.getAdapter();
        rxAdapter.clearData();
        rxAdapter.addDataOf(table);

        setRecyclerViewLayoutParams();

        binding.progressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        binding.title.setVisibility(View.VISIBLE);
        binding.ncode.setVisibility(View.VISIBLE);
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
        globalLayoutListener = () -> {
            int margin = binding.fab.getHeight() /2 * -1;
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) binding.fab.getLayoutParams();
            mlp.setMargins(mlp.leftMargin, margin, mlp.rightMargin, mlp.bottomMargin);
            binding.fab.setLayoutParams(mlp);

            binding.topContainer.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        };

        binding.topContainer.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    private int loadBookmark() {
        Realm realm = RealmUtils.getRealm(mContext);

        RealmQuery<Novel4Realm> query = realm.where(Novel4Realm.class);
        query.equalTo("ncode", ncode);
        RealmResults<Novel4Realm> results = query.findAll();


        if (results.size() == 0) {
            return 0;
        }
        else {
            Novel4Realm novel4Realm = results.get(0);
            return novel4Realm.getBookmark();
        }
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

    public interface OnNovelSelectionListener {
        public void onSelect(String ncode, int totalPage, int page, String title, String writer, String bodyTitle);
    }
}
