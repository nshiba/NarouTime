package net.nashihara.naroureader.fragments

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter

import net.nashihara.naroureader.R
import net.nashihara.naroureader.databinding.FragmentSearchBinding
import net.nashihara.naroureader.entities.Query
import net.nashihara.naroureader.listeners.FragmentTransactionListener
import net.nashihara.naroureader.presenter.SearchPresenter
import net.nashihara.naroureader.views.SearchView
import net.nashihara.naroureader.widgets.FilterDialogFragment
import net.nashihara.naroureader.widgets.OkCancelDialogFragment

import java.util.ArrayList

class SearchFragment : Fragment(), SearchView {

    private lateinit var binding: FragmentSearchBinding
    private var replaceListener: FragmentTransactionListener? = null

    private lateinit var mQuery: Query

    private var genreChecked: BooleanArray? = null
    private var genreStrings: Array<String>? = null

    private var controller: SearchPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments == null) {
            return
        }

        controller = SearchPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentSearchBinding>(inflater!!, R.layout.fragment_search, container, false)

        mQuery = Query()
        setupSort()
        setupGenre()
        setupReadTime()
        setupSearchButton()

        return binding.root
    }

    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            mQuery.ncode = binding.editNcode.text.toString()
            mQuery.setLimit(binding.editLimit.text.toString())
            mQuery.search = binding.editSearch.text.toString()
            mQuery.notSearch = binding.editNotSearch.text.toString()
            mQuery.isTargetTitle = binding.keywordTitle.isChecked
            mQuery.isTargetStory = binding.keywordStory.isChecked
            mQuery.isTargetKeyword = binding.keywordKeyword.isChecked
            mQuery.isTargetKeyword = binding.keywordWriter.isChecked
            mQuery.setMaxLength(binding.maxLength.text.toString())
            mQuery.setMinLength(binding.minLength.text.toString())
            mQuery.isEnd = binding.end.isChecked
            mQuery.isStop = binding.stop.isChecked
            mQuery.isPickup = binding.pickup.isChecked
            controller?.shapeSearchQuery(mQuery, genreChecked)
        }
    }

    private fun setupReadTime() {
        // 読了目安時間
        val adapter = ArrayAdapter.createFromResource(context, R.array.time_spinner, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.timeSpinner.adapter = adapter
        binding.timeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mQuery.time = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupGenre() {
        genreStrings = resources.getStringArray(R.array.genres)
        genreStrings?.let { genreChecked = BooleanArray(it.size) }

        for (i in genreChecked!!.indices) {
            genreChecked!![i] = false
        }

        binding.btnGenre.setOnClickListener { v ->
            FilterDialogFragment.newInstance("ジャンル選択", genreStrings, genreChecked, false,
                    object : FilterDialogFragment.OnDialogButtonClickListener {
                        override fun onPositiveButton(which: Int, itemChecked: BooleanArray, min: String, max: String) {
                            val builder = StringBuilder()
                            itemChecked.indices
                                    .filter { itemChecked[it] }
                                    .forEach { builder.append(genreStrings!![it]).append(" ") }
                            binding.genreText.text = builder.toString()
                        }

                        override fun onNeutralButton(which: Int) {
                            for (i in genreChecked!!.indices) {
                                genreChecked!![i] = false
                            }
                            binding.genreText.text = "\n\n指定なし\n\n"
                        }
                    }).show(fragmentManager, "filter")
        }
    }

    private fun setupSort() {
        // 並び順
        val adapter = ArrayAdapter.createFromResource(
                context, R.array.sort_spinner, android.R.layout.simple_spinner_item)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sortSpinner.adapter = adapter
        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mQuery.sortOrder = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.replaceListener = context as FragmentTransactionListener?
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun reload() {
        fragmentManager.beginTransaction().detach(this).attach(this).commit()
    }

    override fun showResult(query: Query?, genreList: ArrayList<Int>?) {
        val fragment = SearchRecyclerViewFragment.newInstance(query, genreList)
        replaceListener!!.replaceFragment(fragment, "検索結果", null)
    }

    override fun showError() {
        OkCancelDialogFragment.newInstance("エラー", "最大取得件数は500件です。") { dialog, which ->
            dialog.dismiss()
            reload()
        }.show(fragmentManager, "okcancel")
    }

    companion object {

        fun newInstance(): SearchFragment {
            val fragment = SearchFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
