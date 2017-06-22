package net.nashihara.naroureader.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class RankingFragmentPagerAdapter(
        fm: FragmentManager,
        private val fragments: Array<Fragment>,
        private val titles: Array<String>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        if (position >= fragments.size) {
            return null
        } else {
            return fragments[position]
        }
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        if (position >= titles.size) {
            return null
        } else {
            return titles[position]
        }
    }
}
