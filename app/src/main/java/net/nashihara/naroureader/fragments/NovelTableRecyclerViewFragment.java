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

import net.nashihara.naroureader.OnFragmentReplaceListener;
import net.nashihara.naroureader.R;
import net.nashihara.naroureader.adapters.NovelTableRecyclerViewAdapter;
import net.nashihara.naroureader.databinding.FragmentNovelTableViewBinding;
import net.nashihara.naroureader.databinding.TableListItemBinding;

import java.util.List;

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

    private String mNcode;
    private Context mContext;
    private OnFragmentReplaceListener mReplaceListener;
    private RecyclerView mRecyclerView;
    private FragmentNovelTableViewBinding binding;

    public NovelTableRecyclerViewFragment() {}

    public static NovelTableRecyclerViewFragment newInstance(String param1) {
        NovelTableRecyclerViewFragment fragment = new NovelTableRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_NCODE, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mReplaceListener = (OnFragmentReplaceListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNcode = getArguments().getString(PARAM_NCODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_novel_table_view, container, false);

        // TODO: RecyclerView
        mRecyclerView = binding.recycler;
        LinearLayoutManager manager = new LinearLayoutManager(mContext) {
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
            public void onItemClick(View view, int position, TableListItemBinding binding) {
                NovelTableRecyclerViewAdapter clickAdapter = (NovelTableRecyclerViewAdapter) mRecyclerView.getAdapter();
                NovelBody body = clickAdapter.getList().get(position);
                Log.d(TAG, "NovelTableRecyclerView: list size -> " + clickAdapter.getList().size());
                Log.d(TAG, "onItemClick: position -> " + position + "\n" + body.toString());
                mReplaceListener.onFragmentReplaceAction(null, "");
            }
        });
        mRecyclerView.setAdapter(adapter);

        Bundle args = getArguments();
        if (args != null) {
            Observable.zip(Observable.create(new Observable.OnSubscribe<Novel>() {
                @Override
                public void call(Subscriber<? super Novel> subscriber) {
                    Narou narou = new Narou();
                    subscriber.onNext(narou.getNovel(mNcode));
                }
            }), Observable.create(new Observable.OnSubscribe<List<NovelBody>>() {
                @Override
                public void call(Subscriber<? super List<NovelBody>> subscriber) {
                    Narou narou = new Narou();
                    List<NovelBody> bodies = narou.getNovelTable(mNcode);
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
                }
            });
        }

        return binding.getRoot();
    }
}
