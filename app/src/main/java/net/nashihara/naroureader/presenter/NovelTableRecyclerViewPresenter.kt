package net.nashihara.naroureader.presenter

import android.util.Log

import com.google.firebase.crash.FirebaseCrash

import net.nashihara.naroureader.entities.Novel4Realm
import net.nashihara.naroureader.entities.NovelTable4Realm
import net.nashihara.naroureader.views.NovelTableRecyclerView

import java.util.ArrayList

import io.realm.Realm
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import narou4j.Narou
import narou4j.entities.Novel
import narou4j.entities.NovelBody
import net.nashihara.naroureader.async
import net.nashihara.naroureader.ui

class NovelTableRecyclerViewPresenter(view: NovelTableRecyclerView, private val realm: Realm) : Presenter<NovelTableRecyclerView> {

    private var view: NovelTableRecyclerView? = null

    private var job = Job()

    init {
        attach(view)
    }

    override fun attach(view: NovelTableRecyclerView) {
        this.view = view
    }

    override fun detach() {
        view = null
        job.cancel()
    }

    fun fetchBookmark(ncode: String?) {
        val query = realm.where(Novel4Realm::class.java)
        query.equalTo("ncode", ncode ?: "")
        val results = query.findAll()

        if (results.size == 0) {
            view?.showBookmark(0)
        } else {
            val novel4Realm = results[0]
            view?.showBookmark(novel4Realm.bookmark)
        }
    }

    fun fetchNovel(ncode: String?) {
        if (ncode == null) {
            view?.showError()
            return
        }
        Log.d(TAG, "fetchNovel: " + ncode)

        val tableResult = realm.where(NovelTable4Realm::class.java).equalTo("ncode", ncode).findAll().sort("tableNumber")
        val novel4Realm = realm.where(Novel4Realm::class.java).equalTo("ncode", ncode).findFirst()

        if (novel4Realm == null) {
            fetchNovelFromApi(ncode)
            return
        }

        if (tableResult.size <= 0 || !novel4Realm.isDownload) {
            fetchNovelFromApi(ncode)
            return
        }

        val table = ArrayList<NovelBody>()
        for (novelTable4Realm in tableResult) {
            val tableItem = NovelBody()
            tableItem.ncode = novelTable4Realm.ncode
            tableItem.title = novelTable4Realm.title
            tableItem.isChapter = novelTable4Realm.isChapter
            tableItem.page = novelTable4Realm.page
            table.add(tableItem)
        }

        val novel = Novel()
        novel.ncode = String.format("Nコード : %s", ncode)
        novel.writer = String.format("作者 : %s", novel4Realm.writer)
        novel.title = novel4Realm.title
        novel.story = novel4Realm.story
        novel.bodies = table

        view?.showNovelTable(novel)

        realm.close()
    }

    fun fetchNovelFromApi(ncode: String) {
        job.cancel()
        job = ui {
            try {
                val info = fetchNovelBasicInfo(ncode).await()
                val table = fetchNovelTable(ncode).await()
                info.bodies = table
                view?.showNovelTable(info)
            } catch (e: Exception) {
                showError(e)
            }
        }
    }

    private fun fetchNovelBasicInfo(ncode: String): Deferred<Novel> {
        return async { Narou().getNovel(ncode) }
    }

    private fun fetchNovelTable(ncode: String): Deferred<MutableList<NovelBody>> {
        return async { Narou().getNovelTable(ncode) }
    }

    private fun showError(throwable: Throwable) {
        Log.e(TAG, "NovelTableRecyclerViewController: ", throwable)
        FirebaseCrash.report(throwable)
        view?.showError()
    }

    companion object {

        private val TAG = NovelTableRecyclerViewPresenter::class.java.simpleName
    }
}
