package net.nashihara.naroureader.fragments

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.nashihara.naroureader.R
import net.nashihara.naroureader.adapters.SimpleRecyclerViewAdapter
import net.nashihara.naroureader.presenter.DownloadedRecyclerPresenter
import net.nashihara.naroureader.databinding.FragmentSimpleRecycerViewBinding
import net.nashihara.naroureader.listeners.FragmentTransactionListener
import net.nashihara.naroureader.entities.Novel4Realm
import net.nashihara.naroureader.entities.NovelItem
import net.nashihara.naroureader.views.DownloadedRecyclerView

import java.util.ArrayList

import narou4j.entities.Novel
import net.nashihara.naroureader.listeners.OnItemClickListener

class DownloadedRecyclerViewFragment : Fragment(), DownloadedRecyclerView {

    private lateinit var adapter: SimpleRecyclerViewAdapter

    private lateinit var binding: FragmentSimpleRecycerViewBinding

    private lateinit var controller: DownloadedRecyclerPresenter

    private var listener: FragmentTransactionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments == null) {
            return
        }

        controller = DownloadedRecyclerPresenter(this)

        adapter = SimpleRecyclerViewAdapter(context)
        adapter.setOnItemClickListener(OnItemClickListener { _, position -> replaceFragment(position) })

        controller.fetchDownloadedNovels()
    }

    private fun replaceFragment(position: Int) {
        val novel = adapter.list[position]

        val novelDetail = Novel()
        novelDetail.ncode = novel.ncode
        novelDetail.title = novel.title
        novelDetail.story = novel.story
        novelDetail.writer = novel.writer
        novelDetail.allNumberOfNovel = novel.totalPage

        val item = NovelItem()
        item.novelDetail = novelDetail
        listener?.replaceFragment(NovelTableRecyclerViewFragment.newInstance(novel.ncode), novel.title, item)
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

        context ?: return

        if (context is FragmentTransactionListener) {
            listener = context as FragmentTransactionListener?
        } else {
            throw RuntimeException(context.toString() + " must implement context instanceof OnFragmentReplaceListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun showDownloadedNovels(novels: ArrayList<Novel4Realm>) {
        adapter.clearData()
        adapter.addDataOf(novels)
    }

    companion object {

        fun newInstance(): DownloadedRecyclerViewFragment {
            val fragment = DownloadedRecyclerViewFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
