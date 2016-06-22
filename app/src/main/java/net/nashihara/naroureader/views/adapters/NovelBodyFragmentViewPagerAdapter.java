package net.nashihara.naroureader.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import net.nashihara.naroureader.fragments.NovelBodyFragment;

public class NovelBodyFragmentViewPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = NovelBodyFragmentViewPagerAdapter.class.getSimpleName();

    private String ncode;
    private String title;
    private int totalPage;

    public NovelBodyFragmentViewPagerAdapter(FragmentManager fm, String ncode, String title, int totalPage) {
        super(fm);
        this.ncode = ncode;
        this.title = title;
        this.totalPage = totalPage;
    }

    @Override
    public Fragment getItem(int position) {
        return NovelBodyFragment.newInstance(ncode, title, "", position +1, totalPage);
    }

    @Override
    public int getCount() {
        return totalPage;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title;
    }
}
