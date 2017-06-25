package net.nashihara.naroureader.presenter

import android.util.Log

import net.nashihara.naroureader.entities.Novel4Realm
import net.nashihara.naroureader.entities.NovelBody4Realm
import net.nashihara.naroureader.views.NovelBodyView

import java.io.IOException

import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults
import narou4j.Narou
import narou4j.entities.NovelBody
import rx.Emitter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers

class NovelBodyPresenter(view: NovelBodyView, private val realm: Realm) : Presenter<NovelBodyView> {

    private var view: NovelBodyView? = null

    init {
        attach(view)
    }

    override fun attach(view: NovelBodyView) {
        this.view = view
    }

    override fun detach() {
        view = null
    }

    fun setupNovelPage(ncode: String, title: String, body: String, page: Int, autoDownload: Boolean, autoSync: Boolean) {
        val query = realm.where(Novel4Realm::class.java)
        query.equalTo("ncode", ncode)
        val results = query.findAll()

        val isExistNovel = results.size > 0
        if (body == "") {
            val targetBody = getNovelBody(ncode, page)
            if (isExistNovel && targetBody.size > 0) {
                val body4Realm = targetBody[0]

                val novelBody = NovelBody()
                novelBody.body = body4Realm.body
                novelBody.ncode = body4Realm.ncode
                novelBody.title = body4Realm.title
                novelBody.page = body4Realm.page
                view?.showNovelBody(novelBody)

                return
            }

            fetchBody(ncode, page, autoDownload)
        } else {
            updateBody(ncode, page, body, title, autoDownload)
        }
    }

    private fun updateBody(ncode: String, page: Int, body: String, title: String, autoDownload: Boolean) {
        if (!autoDownload) {
            return
        }

        storeNovelBody(ncode, page, title, body)
    }

    private fun fetchBody(ncode: String, page: Int, autoDownload: Boolean) {
        Observable.fromEmitter<NovelBody>({
            val narou = Narou()
            try {
                it.onNext(narou.getNovelBody(ncode, page))
            } catch (e: IOException) {
                it.onError(e)
            }
        }, Emitter.BackpressureMode.NONE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ disposeNovelBody(it, autoDownload) }, { this.showError(it) })
    }

    private fun disposeNovelBody(body: NovelBody, autoDownload: Boolean) {
        if (autoDownload) {
            storeNovelBody(body)
        }

        view?.showNovelBody(body)
    }

    private fun showError(throwable: Throwable) {
        view?.showError()
        Log.e(TAG, "NovelBodyController: ", throwable)
    }

    private fun storeNovelBody(ncode: String, page: Int, title: String, body: String) {
        val novelBody = NovelBody()
        novelBody.ncode = ncode
        novelBody.page = page
        novelBody.title = title
        novelBody.body = body

        storeNovelBody(novelBody)
    }

    private fun storeNovelBody(body: NovelBody) {
        realm.beginTransaction()

        val results = getNovelBody(body.ncode, body.page)

        val body4Realm: NovelBody4Realm
        if (results.size <= 0) {
            body4Realm = realm.createObject(NovelBody4Realm::class.java)
        } else {
            body4Realm = results[0]
        }

        body4Realm.ncode = body.ncode
        body4Realm.title = body.title
        body4Realm.body = body.body
        body4Realm.page = body.page

        realm.commitTransaction()
    }

    private fun getNovelBody(ncode: String, page: Int): RealmResults<NovelBody4Realm> {
        val ncodeResults = realm.where(NovelBody4Realm::class.java).equalTo("ncode", ncode).findAll()
        return ncodeResults.where().equalTo("page", page).findAll()
    }

    companion object {

        private val TAG = NovelBodyPresenter::class.java.simpleName
    }
}
