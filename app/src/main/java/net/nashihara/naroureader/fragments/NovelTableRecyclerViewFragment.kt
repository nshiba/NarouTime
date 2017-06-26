package net.nashihara.naroureader.fragments

import android.content.Context
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout

import net.nashihara.naroureader.R
import net.nashihara.naroureader.adapters.NovelTableRecyclerViewAdapter
import net.nashihara.naroureader.presenter.NovelTableRecyclerViewPresenter
import net.nashihara.naroureader.databinding.FragmentNovelTableViewBinding
import net.nashihara.naroureader.utils.RealmUtils
import net.nashihara.naroureader.views.NovelTableRecyclerView
import net.nashihara.naroureader.widgets.OkCancelDialogFragment

import java.util.ArrayList

import io.realm.Realm
import narou4j.entities.Novel
import net.nashihara.naroureader.databinding.ItemTableRecyclerBinding

class NovelTableRecyclerViewFragment : Fragment(), NovelTableRecyclerView {

    private lateinit var realm: Realm

    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    private var bodyTitles: ArrayList<String>? = null

    private var title: String? = null

    private var writer: String? = null

    private var ncode: String? = null

    private var totalPage: Int = 0

    private var listener: OnNovelSelectionListener? = null

    private lateinit var binding: FragmentNovelTableViewBinding

    private lateinit var controller: NovelTableRecyclerViewPresenter

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as OnNovelSelectionListener?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            ncode = arguments.getString(PARAM_NCODE)
        }
        realm = RealmUtils.getRealm(context)
        controller = NovelTableRecyclerViewPresenter(this, realm)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentNovelTableViewBinding>(inflater!!, R.layout.fragment_novel_table_view, container, false)

        setupRecyclerView()
        setFabMargin()
        binding.fab.setOnClickListener { controller.fetchBookmark(ncode) }

        return binding.root
    }

    private fun setFabMargin() {
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val margin = binding.fab.height / 2 * -1
            val mlp = binding.fab.layoutParams as ViewGroup.MarginLayoutParams
            mlp.setMargins(mlp.leftMargin, margin, mlp.rightMargin, mlp.bottomMargin)
            binding.fab.layoutParams = mlp

            binding.topContainer.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }

        binding.topContainer.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    private fun setupRecyclerView() {
        val manager = object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        manager.isAutoMeasureEnabled = true
        binding.recycler.layoutManager = manager
        val adapter = NovelTableRecyclerViewAdapter(context!!)
        adapter.setOnItemClickListener(object : NovelTableRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int, itemBinding: ItemTableRecyclerBinding) {
                val clickAdapter = binding.recycler.adapter as NovelTableRecyclerViewAdapter
                val body = clickAdapter.list[position]
                listener?.onSelect(body.ncode, totalPage, body.page, title?:"", writer?:"", body.title)
            }
        })
        binding.recycler.adapter = adapter

        setRecyclerViewLayoutParams()
    }

    // recycler view の WRAP_CONTENT が正常に動作しない対処
    private fun setRecyclerViewLayoutParams() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        val adapter = binding.recycler.adapter as NovelTableRecyclerViewAdapter
        val bodies = adapter.list

        var height = 0
        for (body in bodies) {
            if (body.isChapter) {
                height += 148
            } else {
                height += 135
            }
        }

        val params = binding.recycler.layoutParams as LinearLayout.LayoutParams
        params.height = height
        binding.recycler.layoutParams = params

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        controller.fetchNovel(ncode)
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

    private fun visibleNovelTable() {
        binding.progressBar.visibility = View.GONE
        binding.recycler.visibility = View.VISIBLE
        binding.title.visibility = View.VISIBLE
        binding.ncode.visibility = View.VISIBLE
        binding.writer.visibility = View.VISIBLE
        binding.story.visibility = View.VISIBLE
    }

    override fun showBookmark(bookmark: Int) {
        if (bookmark == 0) {
            val dialogFragment = OkCancelDialogFragment.newInstance(
                    "ブックマーク", "この小説にはしおりをはさんでいません。"
                    , DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
            dialogFragment.show(fragmentManager, "okcansel")
        } else {
            listener?.onSelect(ncode?:"", totalPage, bookmark, title?:"", writer?:"", bodyTitles!![bookmark - 1])
        }
    }

    override fun showNovelTable(novel: Novel) {
        Log.i(TAG, "showNovelTable: " + novel.toString())

        binding.title.text = novel.title
        binding.ncode.text = String.format("Nコード : %s", novel.ncode)
        binding.writer.text = String.format("作者 : %s", novel.writer)
        binding.story.text = novel.story

        val rxAdapter = binding.recycler.adapter as NovelTableRecyclerViewAdapter
        rxAdapter.clearData()
        rxAdapter.addDataOf(novel.bodies)

        setRecyclerViewLayoutParams()
        visibleNovelTable()
        updateNovelInfo(novel)
    }

    private fun updateNovelInfo(novel: Novel) {
        writer = novel.writer
        title = novel.title
        totalPage = novel.allNumberOfNovel
        bodyTitles = ArrayList<String>()
        novel.bodies
                .filterNot { it.isChapter }
                .forEach { bodyTitles?.add(it.title) }
    }

    override fun showError() {
        onLoadError()
    }

    interface OnNovelSelectionListener {
        fun onSelect(ncode: String, totalPage: Int, page: Int, title: String, writer: String, bodyTitle: String)
    }

    companion object {

        private val TAG = NovelTableRecyclerViewFragment::class.java.simpleName

        private val PARAM_NCODE = "ncode"

        fun newInstance(ncode: String): NovelTableRecyclerViewFragment {
            val fragment = NovelTableRecyclerViewFragment()
            val args = Bundle()
            args.putString(PARAM_NCODE, ncode)
            fragment.arguments = args
            return fragment
        }
    }
}
