package net.nashihara.naroureader.adapters

import android.content.Context
import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.nashihara.naroureader.R
import net.nashihara.naroureader.databinding.ItemRankingRecyclerBinding
import net.nashihara.naroureader.entities.NovelItem
import net.nashihara.naroureader.models.RankingManager

import narou4j.entities.Novel
import narou4j.entities.NovelRank

class NovelDetailRecyclerViewAdapter(context: Context, private val isSearch: Boolean) : RecyclerView.Adapter<NovelDetailRecyclerViewAdapter.BindingHolder>() {

    private var mInflater = LayoutInflater.from(context)
    val list = SortedList(NovelItem::class.java, SortedListCallback(this, isSearch))
    private var mListener: OnItemClickListener? = null
    private var mRecyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        mRecyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder {
        val v = mInflater.inflate(R.layout.item_ranking_recycler, parent, false)
        return BindingHolder(v, mListener)
    }

    override fun onBindViewHolder(holder: BindingHolder, position: Int) {
        if (list.size() > position && list.get(position) != null) {
            val binding = holder.binding

            val novelItem = list.get(position)
            val novel = novelItem.novelDetail

            if (isSearch) {
                setView4Search(binding, novel)
            } else {
                val rank = novelItem.rank
                val prevRank = novelItem.prevRank
                setView4Ranking(binding, novel, rank, prevRank, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size()
    }

    private fun setView4Search(binding: ItemRankingRecyclerBinding, novel: Novel) {
        binding.ranking.visibility = View.GONE
        binding.rankNew.visibility = View.GONE
        binding.rankDiffKigou.visibility = View.GONE
        binding.prevRankText.visibility = View.GONE
        binding.rankNew.visibility = View.GONE
        binding.rankingPoint.visibility = View.GONE

        binding.title.text = novel.title
        binding.ncode.text = novel.ncode
        binding.writer.text = novel.writer
        binding.genre.text = novel.genre.text
        binding.allStory.text = novel.story
        binding.allStory.visibility = View.GONE
        binding.keyword.text = "キーワード：" + novel.keyword
        binding.keyword.visibility = View.GONE
        binding.btnExpand.alpha = 0.7f

        if (novel.isNovelContinue == 1) {
            val res = mRecyclerView!!.resources
            binding.isContinue.setTextColor(res.getColor(android.support.v7.appcompat.R.color.secondary_text_default_material_light))
            binding.isContinue.text = "連載中"
        } else {
            val res = mRecyclerView!!.resources
            binding.isContinue.setTextColor(res.getColor(R.color.colorAccent))
            binding.isContinue.text = "完結済"
        }
        binding.page.text = "全" + novel.allNumberOfNovel.toString() + "部分"
        binding.length.text = int2String(novel.numberOfChar) + " 文字"
    }

    private fun setView4Ranking(binding: ItemRankingRecyclerBinding, novel: Novel, rank: NovelRank, prevRank: NovelRank?, position: Int) {
        val rankingManager = RankingManager(rank, prevRank)

        binding.ranking.text = rankingManager.buildPositionMessage(position)
        binding.prevRankText.text = rankingManager.buildPrevRankingMessage()

        if (rankingManager.hasPrevNovelRank()) {
            binding.rankNew.visibility = View.GONE
            binding.rankDiffKigou.visibility = View.VISIBLE

            if (rankingManager.isRankUp) {
                binding.rankDiffKigou.setImageResource(R.drawable.ic_up)
            } else if (rankingManager.isRankDown) {
                binding.rankDiffKigou.setImageResource(R.drawable.ic_down)
            } else if (rankingManager.isEqual) {
                binding.rankDiffKigou.setImageResource(R.drawable.ic_sonomama)
            }
        } else {
            binding.rankDiffKigou.visibility = View.GONE
            binding.rankNew.visibility = View.VISIBLE
            binding.rankNew.setTextColor(Color.RED)
        }

        binding.title.text = novel.title
        binding.rankingPoint.text = int2String(rank.pt) + "pt"
        binding.ncode.text = novel.ncode
        binding.writer.text = novel.writer
        binding.genre.text = novel.genre.text
        binding.allStory.text = novel.story
        binding.allStory.visibility = View.GONE
        binding.keyword.text = "キーワード：" + novel.keyword
        binding.keyword.visibility = View.GONE
        binding.btnExpand.alpha = 0.7f

        if (novel.isNovelContinue == 1) {
            val res = mRecyclerView!!.resources
            binding.isContinue.setTextColor(res.getColor(android.support.v7.appcompat.R.color.secondary_text_default_material_light))
            binding.isContinue.text = "連載中"
        } else {
            val res = mRecyclerView!!.resources
            binding.isContinue.setTextColor(res.getColor(R.color.colorAccent))
            binding.isContinue.text = "完結済"
        }
        binding.page.text = "全" + novel.allNumberOfNovel.toString() + "部分"
        binding.length.text = int2String(novel.numberOfChar) + " 文字"
    }

    private fun int2String(number: Int): String {
        val builder = StringBuilder()
        val pageString = number.toString()
        builder.append(pageString)
        var c = 0
        for (i in pageString.length downTo 1) {
            if (c % 3 == 0 && c != 0) {
                builder.insert(i, ",")
            }
            c++
        }
        return builder.toString()
    }

    fun addDataOf(dataList: List<NovelItem>) {
        list!!.addAll(dataList)
    }

    fun removeDataOf(dataList: List<NovelItem>) {
        list!!.beginBatchedUpdates()
        for (item in dataList) {
            list.remove(item)
        }
        list.endBatchedUpdates()
    }

    fun clearData() {
        list!!.clear()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int, binding: ItemRankingRecyclerBinding)
        fun onItemLongClick(view: View, position: Int, binding: ItemRankingRecyclerBinding)
    }

    class BindingHolder(itemView: View, private val mListener: OnItemClickListener?) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        val binding: ItemRankingRecyclerBinding = DataBindingUtil.bind<ItemRankingRecyclerBinding>(itemView)

        init {
            //            binding.btnStory.setOnClickListener(this);
            binding.btnExpand.setOnClickListener(this)
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            mListener?.onItemClick(v, layoutPosition, binding)
        }

        override fun onLongClick(v: View): Boolean {
            if (mListener == null) {
                return false
            }

            mListener.onItemLongClick(v, layoutPosition, binding)
            return true
        }
    }

    private class SortedListCallback(private val adapter: NovelDetailRecyclerViewAdapter, private val isSearch: Boolean) : SortedList.Callback<NovelItem>() {

        override fun compare(o1: NovelItem, o2: NovelItem): Int {
            if (o2.rank.rank > o1.rank.rank) {
                return -1
            }
            if (o1.rank.rank == o2.rank.rank) {
                return 0
            }
            return 1
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyItemRangeRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            adapter.notifyItemChanged(position, count)
        }

        override fun areContentsTheSame(oldItem: NovelItem, newItem: NovelItem): Boolean {
            return oldItem.rank.rank == newItem.rank.rank
        }

        override fun areItemsTheSame(item1: NovelItem, item2: NovelItem): Boolean {
            if (item1.novelDetail == null) {
                return item2.novelDetail == null
            }
            return item1.novelDetail.ncode === item2.novelDetail.ncode
        }
    }

    companion object {
        private val TAG = NovelDetailRecyclerViewAdapter::class.java.simpleName
    }
}
