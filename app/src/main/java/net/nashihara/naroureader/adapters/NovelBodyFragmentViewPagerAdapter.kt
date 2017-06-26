package net.nashihara.naroureader.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

import net.nashihara.naroureader.fragments.NovelBodyFragment

class NovelBodyFragmentViewPagerAdapter(
        fm: FragmentManager,
        private val ncode: String,
        private val title: String,
        private val totalPage: Int) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return NovelBodyFragment.newInstance(ncode, title, "", position + 1, totalPage)
    }

    override fun getCount(): Int {
        return totalPage
    }

    override fun getPageTitle(position: Int): CharSequence {
        return title ?: ""
    }
}
