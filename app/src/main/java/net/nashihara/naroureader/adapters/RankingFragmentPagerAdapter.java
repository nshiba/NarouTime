package net.nashihara.naroureader.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class RankingFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = RankingFragmentPagerAdapter.class.getSimpleName();

    private Fragment[] fragments;
    private String[] titles;

    public RankingFragmentPagerAdapter(FragmentManager fm, Fragment[] fragments, String[] titles) {
        super(fm);
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        if (position >= fragments.length) {
            return null;
        } else {
            return fragments[position];
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position >= titles.length) {
            return null;
        } else {
            return titles[position];
        }
    }
}
