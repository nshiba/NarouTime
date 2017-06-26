package net.nashihara.naroureader.fragments

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.nashihara.naroureader.R
import net.nashihara.naroureader.adapters.NovelDetailRecyclerViewAdapter
import net.nashihara.naroureader.databinding.FragmentSearchRecyclerBinding
import net.nashihara.naroureader.databinding.ItemRankingRecyclerBinding
import net.nashihara.naroureader.entities.NovelItem
import net.nashihara.naroureader.entities.Query
import net.nashihara.naroureader.listeners.FragmentTransactionListener
import net.nashihara.naroureader.presenter.SearchRecyclerPresenter
import net.nashihara.naroureader.views.SearchRecyclerView

import java.util.ArrayList

class SearchRecyclerViewFragment : Fragment(), SearchRecyclerView {

    private var query: Query? = null

    private var genreList: ArrayList<Int>? = null

    private lateinit var binding: FragmentSearchRecyclerBinding

    private var replaceListener: FragmentTransactionListener? = null

    private var controller: SearchRecyclerPresenter? = null

    private var allItems: List<NovelItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args == null) {
            Log.d(TAG, "onCreate: args null")
            return
        }

        query = args.getParcelable<Query>(ARG_QUERY)
        genreList = args.getIntegerArrayList(ARG_GENRE_LIST)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentSearchRecyclerBinding>(inflater!!, R.layout.fragment_search_recycler, container, false)

        controller = SearchRecyclerPresenter(this)

        binding.recycler.layoutManager = LinearLayoutManager(context)

        val adapter = NovelDetailRecyclerViewAdapter(context, true)
        binding.recycler.adapter = adapter

        controller?.searchNovel(query, genreList)

        return binding.root
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.replaceListener = context as FragmentTransactionListener?

        controller?.attach(this)
    }

    override fun onDetach() {
        super.onDetach()
        controller?.detach()
    }

    private fun reload() {
        fragmentManager.beginTransaction().detach(this).attach(this).commit()
    }

    private fun onLoadError() {
        binding.progressBar.visibility = View.GONE
        binding.btnReload.visibility = View.VISIBLE
        binding.btnReload.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnReload.visibility = View.GONE
            reload()
        }
    }

    override fun showRecyclerView(novelItems: List<NovelItem>?) {
        if (novelItems == null) {
            binding.noResultNovel.visibility = View.VISIBLE
            return
        }

        val adapter = binding.recycler.adapter as NovelDetailRecyclerViewAdapter
        adapter.clearData()
        adapter.addDataOf(novelItems)
        allItems = ArrayList(novelItems)
        binding.progressBar.visibility = View.GONE
        binding.recycler.visibility = View.VISIBLE

        adapter.setOnItemClickListener(object : NovelDetailRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int, itemBinding: ItemRankingRecyclerBinding) {
                if (view.id == R.id.btn_expand) {
                    if (itemBinding.allStory.visibility == View.GONE) {
                        itemBinding.allStory.visibility = View.VISIBLE
                        itemBinding.keyword.visibility = View.VISIBLE
                        itemBinding.btnExpand.setImageResource(R.drawable.ic_expand_less_black_24dp)
                    } else {
                        itemBinding.allStory.visibility = View.GONE
                        itemBinding.keyword.visibility = View.GONE
                        itemBinding.btnExpand.setImageResource(R.drawable.ic_expand_more_black_24dp)
                    }
                } else {
                    val item = (binding.recycler.adapter as NovelDetailRecyclerViewAdapter).list.get(position)
                    replaceListener!!.replaceFragment(NovelTableRecyclerViewFragment.newInstance(item.novelDetail.ncode), item.novelDetail.title, item)
                }
            }

            override fun onItemLongClick(view: View, position: Int, binding: ItemRankingRecyclerBinding) {

            }
        })
    }

    override fun showError() {
        onLoadError()
    }

    companion object {
        private val TAG = SearchRecyclerViewFragment::class.java.simpleName

        private val ARG_QUERY = "query"

        private val ARG_GENRE_LIST = "genre_list"

        fun newInstance(query: Query?, genreList: List<Int>?): SearchRecyclerViewFragment {

            val fragment = SearchRecyclerViewFragment()
            val args = Bundle()
            args.putParcelable(ARG_QUERY, query)
            args.putIntegerArrayList(ARG_GENRE_LIST, ArrayList(genreList))
            fragment.arguments = args
            return fragment
        }
    }
}
