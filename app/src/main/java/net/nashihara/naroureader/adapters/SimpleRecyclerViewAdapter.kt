package net.nashihara.naroureader.adapters

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.nashihara.naroureader.R
import net.nashihara.naroureader.databinding.ItemSimpleRecyclerBinding
import net.nashihara.naroureader.entities.Novel4Realm

import java.util.ArrayList

class SimpleRecyclerViewAdapter(context: Context) : RecyclerView.Adapter<SimpleRecyclerViewAdapter.BindingHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    val list = ArrayList<Novel4Realm>()

    private var listener: ((View, Int) -> Unit)? = null

    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        var recyclerView = recyclerView
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder {
        val v = inflater.inflate(R.layout.item_simple_recycler, parent, false)
        return BindingHolder(v, listener)
    }

    override fun onBindViewHolder(holder: BindingHolder, position: Int) {
        if (list.size > position) {
            val binding = holder.binding

            binding.title.text = list[position].title
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addDataOf(dataList: List<Novel4Realm>) {
        val beforePos = list.size
        list.addAll(dataList)
        notifyItemRangeInserted(beforePos, dataList.size)
    }

    fun clearData() {
        list.clear()
    }

    fun setOnItemClickListener(listener: (View, Int) -> Unit){
      this.listener = listener
    }

    class BindingHolder(itemView: View, private val listener: ((View, Int) -> Unit)?) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val binding: ItemSimpleRecyclerBinding = DataBindingUtil.bind<ItemSimpleRecyclerBinding>(itemView)

        init {
            binding.itemContainer.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            listener?.invoke(v, layoutPosition)
        }
    }
}
