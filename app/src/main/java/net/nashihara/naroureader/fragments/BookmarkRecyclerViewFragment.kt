package net.nashihara.naroureader.fragments

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.nashihara.naroureader.R
import net.nashihara.naroureader.activities.NovelViewActivity
import net.nashihara.naroureader.adapters.SimpleRecyclerViewAdapter
import net.nashihara.naroureader.presenter.BookmarkRecyclerPresenter
import net.nashihara.naroureader.databinding.FragmentSimpleRecycerViewBinding
import net.nashihara.naroureader.entities.Novel4Realm
import net.nashihara.naroureader.listeners.OnItemClickListener
import net.nashihara.naroureader.views.BookmarkRecyclerView

import java.util.ArrayList

class BookmarkRecyclerViewFragment : Fragment(), BookmarkRecyclerView {

    private lateinit var adapter: SimpleRecyclerViewAdapter

    private lateinit var binding: FragmentSimpleRecycerViewBinding

    private var controller: BookmarkRecyclerPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments == null) {
            return
        }

        controller = BookmarkRecyclerPresenter(this)

        adapter = SimpleRecyclerViewAdapter(context)
        adapter.setOnItemClickListener(OnItemClickListener { _, position -> startNovelActivity(position) })

        controller!!.fetchBookmarkNovels()
    }

    private fun startNovelActivity(position: Int) {
        val novel = adapter.list[position]

        val intent = Intent(context, NovelViewActivity::class.java)
        intent.putExtra("ncode", novel.ncode)
        intent.putExtra("page", novel.bookmark)
        intent.putExtra("title", novel.title)
        intent.putExtra("writer", novel.writer)
        intent.putExtra("totalPage", novel.totalPage)

        startActivity(intent)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentSimpleRecycerViewBinding>(inflater!!, R.layout.fragment_simple_recycer_view, container, false)

        binding.recycler.layoutManager = LinearLayoutManager(context)
        binding.recycler.adapter = adapter

        binding.progressBar.visibility = View.GONE
        binding.recycler.visibility = View.VISIBLE

        return binding.root
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        controller!!.detach()
    }

    override fun showBookmarks(novels: ArrayList<Novel4Realm>) {
        adapter.clearData()
        adapter.addDataOf(novels)
    }

    companion object {

        fun newInstance(): BookmarkRecyclerViewFragment {
            val fragment = BookmarkRecyclerViewFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
