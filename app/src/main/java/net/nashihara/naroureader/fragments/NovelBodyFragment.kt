package net.nashihara.naroureader.fragments

import android.content.Context
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.nashihara.naroureader.R
import net.nashihara.naroureader.presenter.NovelBodyPresenter
import net.nashihara.naroureader.databinding.FragmentNovelBodyBinding
import net.nashihara.naroureader.entities.Novel4Realm
import net.nashihara.naroureader.utils.RealmUtils
import net.nashihara.naroureader.views.NovelBodyView

import io.realm.Realm
import narou4j.entities.NovelBody

class NovelBodyFragment : Fragment(), NovelBodyView {

    private val pref: SharedPreferences
            by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    private val realm: Realm = RealmUtils.getRealm()

    private var page: Int = 0

    private var totalPage: Int = 0

    private var title: String? = null

    private var body: String? = null

    private var ncode: String? = null

    private val nextBody = ""

    private val prevBody = ""

    private var listener: OnNovelBodyInteraction? = null

    private lateinit var binding: FragmentNovelBodyBinding

    private lateinit var controller: NovelBodyPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = NovelBodyPresenter(this, realm)

        arguments?.let {
            title = it.getString(ARG_TITLE)
            body = it.getString(ARG_BODY)
            ncode = it.getString(ARG_NCODE)
            totalPage = it.getInt(ARG_TOTAL_PAGE)
            page = it.getInt(ARG_PAGE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentNovelBodyBinding>(inflater!!, R.layout.fragment_novel_body, container, false)

        if (body == "") {
            goneBody()
        } else {
            binding.body.text = body
            binding.title.text = title
            visibleBody()
        }

        setupPageButton()

        return binding.getRoot()
    }

    private fun setupPageButton() {
        binding.page.text = "$page/$totalPage"
        binding.btnNext.setOnClickListener {
            if (page >= totalPage) {
                return@setOnClickListener
            }

            realm.close()
            listener?.onNovelBodyLoadAction(nextBody, page + 1, "")
        }

        binding.btnPrev.setOnClickListener {
            if (page <= 1) {
                return@setOnClickListener
            }

            realm.close()
            listener?.onNovelBodyLoadAction(prevBody, page - 1, "")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val autoDownload = pref.getBoolean(getString(R.string.auto_download), false)
        val autoSync = pref.getBoolean(getString(R.string.auto_sync), false)
        controller.setupNovelPage(ncode?:"", title?:"", body?:"", page, autoDownload, autoSync)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as OnNovelBodyInteraction?
        controller = NovelBodyPresenter(this, realm)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        realm.close()
        controller.detach()
    }

    override fun onResume() {
        super.onResume()
        setupPageColor()
    }

    private fun setupPageColor() {
        val text = pref.getInt(getString(R.string.body_text), 0)
        val background = pref.getInt(getString(R.string.body_background), 0)

        if (text != 0) {
            binding.page.setTextColor(text)
            binding.title.setTextColor(text)
            binding.body.setTextColor(text)
            binding.btnNext.setTextColor(text)
            binding.btnPrev.setTextColor(text)
        }

        if (background != 0) {
            binding.root.setBackgroundColor(background)
        }
    }

    private fun visibleBody() {
        binding.body.visibility = View.VISIBLE
        binding.title.visibility = View.VISIBLE
        binding.btnPrev.visibility = View.VISIBLE
        binding.page.visibility = View.VISIBLE

        if (page == totalPage) {
            binding.btnNext.visibility = View.GONE
            binding.readFinish.visibility = View.VISIBLE
        } else {
            binding.btnNext.visibility = View.VISIBLE
            binding.readFinish.visibility = View.GONE
        }

        binding.progressBar.visibility = View.GONE
    }

    private fun goneBody() {
        binding.body.visibility = View.GONE
        binding.title.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
        binding.btnPrev.visibility = View.GONE
        binding.page.visibility = View.GONE
        binding.readFinish.visibility = View.GONE

        binding.progressBar.visibility = View.VISIBLE
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

    override fun showNovelBody(novelBody: NovelBody) {
        binding.body.text = novelBody.body
        binding.title.text = novelBody.title
        visibleBody()
    }

    override fun showError() {
        onLoadError()
    }

    interface OnNovelBodyInteraction {
        fun onNovelBodyLoadAction(body: String, nextPage: Int, bodyTitle: String)
        fun getNovel4RealmInstance(): Novel4Realm
    }

    companion object {

        private val ARG_NCODE = "ncode"

        private val ARG_TITLE = "title"

        private val ARG_BODY = "body"

        private val ARG_PAGE = "page"

        private val ARG_TOTAL_PAGE = "total_page"

        fun newInstance(ncode: String, title: String, body: String, page: Int, totalPage: Int): NovelBodyFragment {
            val fragment = NovelBodyFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_BODY, body)
            args.putString(ARG_NCODE, ncode)
            args.putInt(ARG_PAGE, page)
            args.putInt(ARG_TOTAL_PAGE, totalPage)
            fragment.arguments = args
            return fragment
        }
    }
}
