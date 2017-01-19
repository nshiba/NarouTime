package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.adapters.NovelTableRecyclerViewAdapter;
import net.nashihara.naroureader.controller.NovelTableRecyclerViewController;
import net.nashihara.naroureader.databinding.FragmentNovelTableViewBinding;
import net.nashihara.naroureader.utils.RealmUtils;
import net.nashihara.naroureader.views.NovelTableRecyclerView;
import net.nashihara.naroureader.widgets.OkCancelDialogFragment;

import java.util.ArrayList;

import io.realm.Realm;
import narou4j.entities.Novel;
import narou4j.entities.NovelBody;

public class NovelTableRecyclerViewFragment extends Fragment implements NovelTableRecyclerView {

    private static final String TAG = NovelTableRecyclerViewFragment.class.getSimpleName();

    private static final String PARAM_NCODE = "ncode";

    private Realm realm;

    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    private ArrayList<String> bodyTitles;

    private String title;

    private String writer;

    private String ncode;

    private int totalPage;

    private Context context;

    private OnNovelSelectionListener listener;

    private FragmentNovelTableViewBinding binding;

    private NovelTableRecyclerViewController controller;

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
        this.context = context;
        listener = (OnNovelSelectionListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ncode = getArguments().getString(PARAM_NCODE);
        }

        realm = RealmUtils.getRealm(context);
        controller = new NovelTableRecyclerViewController(this, realm);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_novel_table_view, container, false);

        setupRecyclerView();
        setFabMargin();
        binding.fab.setOnClickListener(v -> controller.fetchBookmark(ncode));

        return binding.getRoot();
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

    private void setupRecyclerView() {
        final LinearLayoutManager manager = new LinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        manager.setAutoMeasureEnabled(true);
        binding.recycler.setLayoutManager(manager);
        NovelTableRecyclerViewAdapter adapter = new NovelTableRecyclerViewAdapter(context);
        adapter.setOnItemClickListener((view, position, binding1) -> {
            NovelTableRecyclerViewAdapter clickAdapter = (NovelTableRecyclerViewAdapter) binding.recycler.getAdapter();
            NovelBody body = clickAdapter.getList().get(position);
            listener.onSelect(body.getNcode(), totalPage, body.getPage(), title, writer, body.getTitle());
        });
        binding.recycler.setAdapter(adapter);

        setRecyclerViewLayoutParams();
    }

    // recycler view の WRAP_CONTENT が正常に動作しない対処
    private void setRecyclerViewLayoutParams() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        NovelTableRecyclerViewAdapter adapter
            = (NovelTableRecyclerViewAdapter) binding.recycler.getAdapter();
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

        LinearLayout.LayoutParams params =
            (LinearLayout.LayoutParams) binding.recycler.getLayoutParams();
        params.height = height;
        binding.recycler.setLayoutParams(params);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        controller.fetchNovel(ncode);
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

    private void visibleNovelTable() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recycler.setVisibility(View.VISIBLE);
        binding.title.setVisibility(View.VISIBLE);
        binding.ncode.setVisibility(View.VISIBLE);
        binding.writer.setVisibility(View.VISIBLE);
        binding.story.setVisibility(View.VISIBLE);
    }

    @Override
    public void showBookmark(int bookmark) {
        if (bookmark == 0) {
            OkCancelDialogFragment dialogFragment
                = OkCancelDialogFragment.newInstance("ブックマーク", "この小説にはしおりをはさんでいません。", (dialog, which) -> {});
            dialogFragment.show(getFragmentManager(), "okcansel");
        }
        else {
            listener.onSelect(ncode, totalPage, bookmark, title, writer, bodyTitles.get(bookmark -1));
        }
    }

    @Override
    public void showNovelTable(Novel novel) {
        Log.i(TAG, "showNovelTable: " + novel.toString());

        binding.title.setText(novel.getTitle());
        binding.ncode.setText(String.format("Nコード : %s", novel.getNcode()));
        binding.writer.setText(String.format("作者 : %s", novel.getWriter()));
        binding.story.setText(novel.getStory());

        NovelTableRecyclerViewAdapter rxAdapter
            = (NovelTableRecyclerViewAdapter) binding.recycler.getAdapter();
        rxAdapter.clearData();
        rxAdapter.addDataOf(novel.getBodies());

        setRecyclerViewLayoutParams();
        visibleNovelTable();
        updateNovelInfo(novel);
    }

    private void updateNovelInfo(Novel novel) {
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

    @Override
    public void showError() {
        onLoadError();
    }

    public interface OnNovelSelectionListener {
        void onSelect(String ncode, int totalPage, int page, String title, String writer, String bodyTitle);
    }
}
