package net.nashihara.naroureader.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.nashihara.naroureader.R
import net.nashihara.naroureader.adapters.RankingFragmentPagerAdapter
import net.nashihara.naroureader.databinding.FragmentRankingViewpagerBinding

import narou4j.enums.RankingType.DAILY
import narou4j.enums.RankingType.MONTHLY
import narou4j.enums.RankingType.QUARTET
import narou4j.enums.RankingType.WEEKLY

class RankingViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentRankingViewpagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentRankingViewpagerBinding>(inflater!!, R.layout.fragment_ranking_viewpager, container, false)

        val fragments = mutableListOf<Fragment>()
        RANKING_TYPES.forEach { fragments.add(RankingRecyclerViewFragment.newInstance(it)) }
        val adapter = RankingFragmentPagerAdapter(childFragmentManager, fragments.toList(), TITLES)
        binding.pager.adapter = adapter
        binding.pager.offscreenPageLimit = 4
        binding.tabStrip.setupWithViewPager(binding.pager)

        return binding.root
    }

    companion object {
        private val RANKING_TYPES = arrayOf(DAILY.toString(), WEEKLY.toString(), MONTHLY.toString(), QUARTET.toString(), "all")

        private val TITLES = arrayOf("日間", "週間", "月間", "四半期", "累計")

        fun newInstance(): RankingViewPagerFragment {
            return RankingViewPagerFragment()
        }
    }
}
