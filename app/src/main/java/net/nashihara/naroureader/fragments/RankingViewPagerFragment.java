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

import static narou4j.enums.RankingType.DAILY;
import static narou4j.enums.RankingType.MONTHLY;
import static narou4j.enums.RankingType.QUARTET;
import static narou4j.enums.RankingType.WEEKLY;

public class RankingViewPagerFragment extends Fragment {
    private static final String TAG = RankingViewPagerFragment.class.getSimpleName();

    private static final String PARAM_TYPE = "type";
    private static final String PARAM_TITLE = "title";

    private static final String[] RANKING_TYPES = new String[]{
      DAILY.toString(),
      WEEKLY.toString(),
      MONTHLY.toString(),
      QUARTET.toString(),
      "all"
    };

    private static final String[] TITLES = new String[]{"日間", "週間", "月間", "四半期", "累計"};

    private FragmentRankingViewpagerBinding binding;

    public static RankingViewPagerFragment newInstance() {
        return new RankingViewPagerFragment();
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

        Fragment fragments[] = new Fragment[TITLES.length];
        for (int i = 0; i < fragments.length; i++) {
            fragments[i] = RankingRecyclerViewFragment.newInstance(RANKING_TYPES[i]);
        }
        RankingFragmentPagerAdapter adapter = new RankingFragmentPagerAdapter(getChildFragmentManager(), fragments, TITLES);
        binding.pager.setAdapter(adapter);
        binding.pager.setOffscreenPageLimit(4);
        binding.tabStrip.setupWithViewPager(binding.pager);

        return binding.getRoot();
    }
}
