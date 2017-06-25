package net.nashihara.naroureader.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.nashihara.naroureader.R
import net.nashihara.naroureader.adapters.NovelDetailRecyclerViewAdapter
import net.nashihara.naroureader.presenter.RankingRecyclerPresenter
import net.nashihara.naroureader.databinding.FragmentRankingRecyclerBinding
import net.nashihara.naroureader.databinding.ItemRankingRecyclerBinding
import net.nashihara.naroureader.entities.NovelItem
import net.nashihara.naroureader.listeners.FragmentTransactionListener
import net.nashihara.naroureader.utils.DownloadUtils
import net.nashihara.naroureader.views.RankingRecyclerView
import net.nashihara.naroureader.widgets.FilterDialogFragment
import net.nashihara.naroureader.widgets.ListDialogFragment
import net.nashihara.naroureader.widgets.NovelDownloadDialogFragment
import net.nashihara.naroureader.widgets.OkCancelDialogFragment

import java.util.ArrayList
import java.util.Arrays

import narou4j.entities.Novel
import narou4j.enums.NovelGenre

class RankingRecyclerViewFragment : Fragment(), RankingRecyclerView {

    private lateinit var binding: FragmentRankingRecyclerBinding

    private var replaceListener: FragmentTransactionListener? = null

    private var allItems = ArrayList<NovelItem>()

    private lateinit var controller: RankingRecyclerPresenter

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        replaceListener = context as FragmentTransactionListener?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentRankingRecyclerBinding>(inflater!!, R.layout.fragment_ranking_recycler, container, false)

        controller = RankingRecyclerPresenter(this)

        binding.fab.setOnClickListener { setupFab() }

        binding.recycler.layoutManager = LinearLayoutManager(context)

        val adapter = setupRecyclerView()
        binding.recycler.adapter = adapter

        arguments?.let {
            val typeStr = it.getString(PARAM_TYPE, "")
            controller.fetchRanking(typeStr)
        }
        return binding.root
    }

    private fun setupFab() {
        val filters = ArrayList(Arrays.asList(*resources.getStringArray(R.array.genres)))
        filters.add(0, "完結済み")

        val filterIds = NovelGenre.values()


        val checked = BooleanArray(filters.size)
        checked[0] = false
        for (i in 1..checked.size - 1) {
            checked[i] = true
        }
        val checkBoxDialog = FilterDialogFragment.newInstance("小説絞込み", filters.toTypedArray(), checked, true,
                object : FilterDialogFragment.OnDialogButtonClickListener {
                    override fun onPositiveButton(which: Int, itemChecked: BooleanArray, min: String, max: String) {
                        controller.filterNovelRanking(allItems, filterIds, itemChecked, min, max)
                    }

                    override fun onNeutralButton(which: Int) {
                        val adapter = binding.recycler.adapter as NovelDetailRecyclerViewAdapter
                        adapter.list.clear()
                        adapter.list.addAll(allItems)
                    }
                })
        checkBoxDialog.show(fragmentManager, "multiple")
    }

    private fun setupRecyclerView(): NovelDetailRecyclerViewAdapter {
        val adapter = NovelDetailRecyclerViewAdapter(context, false)
        adapter.setOnItemClickListener(object : NovelDetailRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int, itemBinding: ItemRankingRecyclerBinding) {
                if (view.id != R.id.btn_expand) {
                    val item = (binding.recycler.adapter as NovelDetailRecyclerViewAdapter).list.get(position)
                    replaceListener?.replaceFragment(NovelTableRecyclerViewFragment
                            .newInstance(item.novelDetail.ncode), item.novelDetail.title, item)
                    return
                }

                if (itemBinding.allStory.visibility == View.GONE) {
                    itemBinding.allStory.visibility = View.VISIBLE
                    itemBinding.keyword.visibility = View.VISIBLE
                    itemBinding.btnExpand.setImageResource(R.drawable.ic_expand_less_black_24dp)
                } else {
                    itemBinding.allStory.visibility = View.GONE
                    itemBinding.keyword.visibility = View.GONE
                    itemBinding.btnExpand.setImageResource(R.drawable.ic_expand_more_black_24dp)
                }
            }

            override fun onItemLongClick(view: View, position: Int, itemBinding: ItemRankingRecyclerBinding) {

                val adapter = binding.recycler.adapter as NovelDetailRecyclerViewAdapter

                val item = adapter.list.get(position)
                val strings = arrayOf("小説を読む", "ダウンロード", "ブラウザで小説ページを開く", "ブラウザで作者ページを開く")
                val listDialog = ListDialogFragment.newInstance(item.novelDetail.title, strings,
                        DialogInterface.OnClickListener { _, which ->  longClickListDialogListener(which, position, item)})
                listDialog.show(fragmentManager, "list_dialog")
            }
        })

        return adapter
    }

    private fun longClickListDialogListener(which: Int, position: Int, item: NovelItem) {
        when (which) {
            0 -> {
                val item1 = (binding.recycler.adapter as NovelDetailRecyclerViewAdapter).list.get(position)
                replaceListener?.replaceFragment(NovelTableRecyclerViewFragment
                        .newInstance(item1.novelDetail.ncode), item1.novelDetail.title, item1)
            }
            1 -> {
                val downloadUtils = object : DownloadUtils() {
                    override fun onDownloadSuccess(dialog: NovelDownloadDialogFragment, novel: Novel) {
                        dialog.dismiss()

                        val okCancelDialog = OkCancelDialogFragment.newInstance("ダウンロード完了", "ダウンロードした小説を開きますか？") { dialog1, which1 ->
                            dialog1.dismiss()
                            if (OkCancelDialogFragment.OK == which1) {
                                val novelItem = NovelItem()
                                novelItem.novelDetail = novel
                                replaceListener!!.replaceFragment(
                                        NovelTableRecyclerViewFragment.newInstance(novel.ncode),
                                        novelItem.novelDetail.title, novelItem)
                            }
                        }
                        okCancelDialog.show(fragmentManager, "okcansel")
                    }

                    override fun onDownloadError(dialog: NovelDownloadDialogFragment) {
                        Log.d(TAG, "onDownloadError: ")
                        dialog.dismiss()
                    }
                }
                downloadUtils.novelDownlaod(item.novelDetail, fragmentManager, context)
            }
            2 -> {
                val url = "http://ncode.syosetu.com/" + item.novelDetail.ncode + "/"
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            3 -> {
                val url = "http://mypage.syosetu.com/" + item.novelDetail.userId + "/"
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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

    override fun showRanking(novelItems: List<NovelItem>) {
        if (allItems.isEmpty()) {
            allItems = ArrayList(novelItems)
        }

        val adapter = binding.recycler.adapter as NovelDetailRecyclerViewAdapter
        adapter.clearData()
        adapter.addDataOf(novelItems)
        binding.progressBar.visibility = View.GONE
        binding.recycler.visibility = View.VISIBLE
    }

    override fun showError() {
        onLoadError()
    }

    companion object {

        private val TAG = RankingRecyclerViewFragment::class.java.simpleName

        private val PARAM_TYPE = "rankingType"

        fun newInstance(type: String): RankingRecyclerViewFragment {
            val fragment = RankingRecyclerViewFragment()
            val args = Bundle()
            args.putString(PARAM_TYPE, type)
            fragment.arguments = args
            return fragment
        }
    }
}
