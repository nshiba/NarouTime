package net.nashihara.naroureader.adapters

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.nashihara.naroureader.R
import net.nashihara.naroureader.databinding.ItemTableRecyclerBinding

import java.util.ArrayList

import narou4j.entities.NovelBody

class NovelTableRecyclerViewAdapter(context: Context) : RecyclerView.Adapter<NovelTableRecyclerViewAdapter.BindingHolder>() {

    private val inflater = LayoutInflater.from(context)

    val list = ArrayList<NovelBody>()

    private var listener: OnItemClickListener? = null

    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder {
        val v = inflater.inflate(R.layout.item_table_recycler, parent, false)
        return BindingHolder(v, listener)
    }

    override fun onBindViewHolder(holder: BindingHolder, position: Int) {
        if (list.size > position) {
            val binding = holder.binding

            val body = list[position]
            if (body.isChapter) {
                binding.chapter.text = body.title
                binding.chapter.visibility = View.VISIBLE
                binding.pageTitle.visibility = View.GONE
            } else {
                binding.pageTitle.text = body.title
                binding.pageTitle.visibility = View.VISIBLE
                binding.chapter.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addDataOf(dataList: List<NovelBody>) {
        val startPos = dataList.size
        list.addAll(dataList)
        notifyItemRangeInserted(startPos, dataList.size)
    }

    fun clearData() {
        list.clear()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int, binding: ItemTableRecyclerBinding)
    }

    class BindingHolder(itemView: View, private val mListener: OnItemClickListener?) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val binding: ItemTableRecyclerBinding = DataBindingUtil.bind<ItemTableRecyclerBinding>(itemView)

        init {
            binding.pageTitle.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            mListener?.onItemClick(v, layoutPosition, binding)
        }
    }
}
