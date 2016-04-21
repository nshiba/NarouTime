package net.nashihara.naroureader.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.adapters.RankingFragmentPagerAdapter;
import net.nashihara.naroureader.databinding.FragmentRankingViewpagerBinding;

public class RankingViewPagerFragment extends Fragment {
    private static final String TAG = RankingViewPagerFragment.class.getSimpleName();

    private static final String PARAM_TYPE = "type";
    private static final String PARAM_TITLE = "title";

    private FragmentRankingViewpagerBinding binding;

    public static RankingViewPagerFragment newInstance(String[] types, String[] titles) {
        RankingViewPagerFragment fragment = new RankingViewPagerFragment();
        Bundle args = new Bundle();
        args.putStringArray(PARAM_TYPE, types);
        args.putStringArray(PARAM_TITLE, titles);
        fragment.setArguments(args);
        return fragment;
    }

    public RankingViewPagerFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ranking_viewpager, container, false);

        return binding.getRoot();
    }

    @Override
    public void onResume() {

        Bundle args = getArguments();
        if (args != null) {
            String[] titles = args.getStringArray(PARAM_TITLE);
            String[] types = args.getStringArray(PARAM_TYPE);
            Fragment fragments[] = new Fragment[titles.length];
            for (int i = 0; i < fragments.length; i++) {
                fragments[i] = RankingRecyclerViewFragment.newInstance(types[i]);
            }
            RankingFragmentPagerAdapter adapter = new RankingFragmentPagerAdapter(getChildFragmentManager(), fragments, titles);
            binding.pager.setAdapter(adapter);
            binding.pager.setOffscreenPageLimit(4);
            binding.tabStrip.setupWithViewPager(binding.pager);
        }
        super.onResume();
    }
}
