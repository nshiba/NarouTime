package net.nashihara.naroureader.presenter

import android.util.Log

import com.google.firebase.crash.FirebaseCrash

import net.nashihara.naroureader.entities.Novel4Realm
import net.nashihara.naroureader.entities.NovelTable4Realm
import net.nashihara.naroureader.views.NovelTableRecyclerView

import java.io.IOException
import java.util.ArrayList

import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults
import narou4j.Narou
import narou4j.entities.Novel
import narou4j.entities.NovelBody
import rx.Emitter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers

class NovelTableRecyclerViewPresenter(view: NovelTableRecyclerView, private val realm: Realm) : Presenter<NovelTableRecyclerView> {

    private var view: NovelTableRecyclerView? = null

    init {
        attach(view)
    }

    override fun attach(view: NovelTableRecyclerView) {
        this.view = view
    }

    override fun detach() {
        view = null
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
        Observable.zip(fetchNovelBasicInfo(ncode), fetchNovelTable(ncode)) { info, table ->
            info.bodies = table
            info
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view?.showNovelTable(it) }, { this.showError(it) })
    }

    private fun fetchNovelBasicInfo(ncode: String): Observable<Novel> {
        return Observable.fromEmitter<Novel>({ emitter ->
            val narou = Narou()
            try {
                emitter.onNext(narou.getNovel(ncode))
            } catch (e: IOException) {
                emitter.onError(e)
            }
        }, Emitter.BackpressureMode.NONE)
    }

    private fun fetchNovelTable(ncode: String): Observable<List<NovelBody>> {
        return Observable.fromEmitter<List<NovelBody>>({ emitter ->
            val narou = Narou()

            try {
                emitter.onNext(narou.getNovelTable(ncode))
            } catch (e: IOException) {
                emitter.onError(e)
            }
        }, Emitter.BackpressureMode.NONE)
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
