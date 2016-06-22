package net.nashihara.naroureader.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.utils.RealmUtils;
import net.nashihara.naroureader.views.adapters.SimpleRecyclerViewAdapter;
import net.nashihara.naroureader.databinding.FragmentSimpleRecycerViewBinding;
import net.nashihara.naroureader.models.entities.Novel4Realm;
import net.nashihara.naroureader.models.entities.NovelItem;
import net.nashihara.naroureader.utils.OnFragmentReplaceListener;
import net.nashihara.naroureader.utils.OnItemClickListener;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import narou4j.entities.Novel;

public class DownloadedRecyclerViewFragment extends Fragment {
    private static final String TAG = DownloadedRecyclerViewFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private SimpleRecyclerViewAdapter adapter;
    private OnFragmentReplaceListener mListener;
    private Context mContext;
    private FragmentSimpleRecycerViewBinding binding;
    private ArrayList<Novel4Realm> novels = new ArrayList<>();

    public DownloadedRecyclerViewFragment() {}

    public static DownloadedRecyclerViewFragment newInstance() {
        DownloadedRecyclerViewFragment fragment = new DownloadedRecyclerViewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            Realm realm = RealmUtils.getRealm(mContext);
            RealmResults<Novel4Realm> results = realm.where(Novel4Realm.class).equalTo("isDownload", true).findAll();

            for (Novel4Realm novel4Realm : results) {
                novels.add(novel4Realm);
            }

            adapter = new SimpleRecyclerViewAdapter(mContext);
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    final Novel4Realm novel = novels.get(position);

                    Novel novelDetail = new Novel();
                    novelDetail.setNcode(novel.getNcode());
                    novelDetail.setTitle(novel.getTitle());
                    novelDetail.setStory(novel.getStory());
                    novelDetail.setWriter(novel.getWriter());
                    novelDetail.setAllNumberOfNovel(novel.getTotalPage());

                    NovelItem item = new NovelItem();
                    item.setNovelDetail(novelDetail);
                    mListener.onFragmentReplaceAction(NovelTableRecyclerViewFragment.newInstance(novel.getNcode()), novel.getTitle(), item);
                }
            });

            adapter.clearData();
            adapter.addDataOf(novels);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_simple_recycer_view, container, false);

        mRecyclerView = binding.recycler;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(adapter);

        binding.progressBar.setVisibility(View.GONE);
        binding.recycler.setVisibility(View.VISIBLE);

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof OnFragmentReplaceListener) {
            mListener = (OnFragmentReplaceListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement context instanceof OnFragmentReplaceListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
