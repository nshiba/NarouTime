package net.nashihara.naroureader.presenter

import android.support.v4.util.Pair
import android.util.Log

import com.google.firebase.crash.FirebaseCrash

import net.nashihara.naroureader.entities.NovelItem
import net.nashihara.naroureader.entities.Query
import net.nashihara.naroureader.views.SearchRecyclerView

import java.io.IOException
import java.util.ArrayList

import narou4j.Narou
import narou4j.entities.Novel
import narou4j.entities.NovelRank
import narou4j.enums.NovelGenre
import narou4j.enums.NovelType
import narou4j.enums.OutputOrder
import narou4j.enums.SearchWordTarget
import rx.Emitter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers

class SearchRecyclerPresenter(view: SearchRecyclerView) : Presenter<SearchRecyclerView> {

    private var view: SearchRecyclerView? = null

    init {
        attach(view)
    }

    override fun attach(view: SearchRecyclerView) {
        this.view = view
    }

    override fun detach() {
        view = null
    }

    private fun error(throwable: Throwable) {
        Log.e(TAG, "error: ", throwable)
        FirebaseCrash.report(throwable)
        view?.showError()
    }

    fun searchNovel(query: Query?, genreList: ArrayList<Int>?) {
        if (query == null || genreList == null) {
            error(Throwable("query or genreList is null"))
            return
        }
        buildSearchObservable(query, genreList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ novels ->
                    if (novels == null) {
                        error(Throwable("onNext: novels are null"))
                        return@subscribe
                    }

                    if (novels.isEmpty()) {
                        error(Throwable("onNext: novels size is zero"))
                        return@subscribe
                    }

                    val novelItemList = setupNovelItems(novels)
                    view?.showRecyclerView(novelItemList)
                }, { this.error(it) })
    }

    private fun setupNovelItems(novelList: List<Novel>): List<NovelItem> {
        val novelItemList = ArrayList<NovelItem>()
        for (i in novelList.indices) {
            val novel = novelList[i]
            val rank = NovelRank()

            novel.ncode = novel.ncode.toLowerCase()

            rank.ncode = novel.ncode
            rank.rank = i

            novelItemList.add(NovelItem(novelDetail = novel, rank = rank))
        }

        return novelItemList
    }

    private fun buildSearchObservable(query: Query, genreList: ArrayList<Int>): Observable<List<Novel>> {
        return Observable.fromEmitter<List<Novel>>({ emitter ->
            val narou = Narou()

            emitSearchObservable(emitter, narou, query, genreList)

            emitter.onCompleted()
        }, Emitter.BackpressureMode.NONE)
    }

    private fun emitSearchObservable(emitter: Emitter<List<Novel>>, narou: Narou, query: Query, genreList: ArrayList<Int>) {
        if (query.ncode == "") {
            emitSearchObservableFromSearchQuery(emitter, narou, query, genreList)
        } else {
            emitSearchObservableFromNcode(emitter, narou, query.ncode)
        }
    }

    private fun emitSearchObservableFromNcode(emitter: Emitter<List<Novel>>, narou: Narou, ncode: String) {
        var novel: Novel? = null
        try {
            novel = narou.getNovel(ncode)
        } catch (e: IOException) {
            emitter.onError(e)
        }

        if (novel == null) {
            emitter.onNext(null)
        } else {
            val list = ArrayList<Novel>()
            list.add(novel)
            emitter.onNext(list)
        }
    }

    private fun emitSearchObservableFromSearchQuery(
            emitter: Emitter<List<Novel>>, narou: Narou, query: Query, genreList: ArrayList<Int>) {
        setTextParam(narou, query.search, query.notSearch)
        setPageLimit(narou, query.limit)
        setSortOrder(narou, query.sortOrder)
        setTime(narou, query.time)
        setSummary(narou, query.isTargetTitle, query.isTargetStory, query.isTargetKeyword, query.isTargetWriter)
        setCharLength(narou, query.minLength, query.maxLength)
        setSerializationInfo(narou, query.isEnd, query.isStop, query.isPickup)
        setGenre(narou, genreList)

        val result = fetchNovel(narou)
        if (result.second != null) {
            emitter.onError(result.second)
        } else {
            emitter.onNext(result.first)
        }
    }

    private fun setTextParam(narou: Narou, search: String, notSearch: String) {
        if (search != "") {
            narou.setSearchWord(search)
        }

        if (notSearch != "") {
            narou.setNotWord(notSearch)
        }
    }

    private fun setPageLimit(narou: Narou, limit: Int) {
        if (limit == 0) {
            narou.setLim(50)
        } else {
            narou.setLim(limit)
        }
    }

    private fun setSummary(narou: Narou, title: Boolean, story: Boolean, keyword: Boolean, writer: Boolean) {
        if (title) {
            narou.setSearchTarget(SearchWordTarget.TITLE)
        }
        if (story) {
            narou.setSearchTarget(SearchWordTarget.SYNOPSIS)
        }
        if (keyword) {
            narou.setSearchTarget(SearchWordTarget.KEYWORD)
        }
        if (writer) {
            narou.setSearchTarget(SearchWordTarget.WRITER)
        }
    }

    private fun setCharLength(narou: Narou, min: Int, max: Int) {
        if (min != 0 || max != 0) {
            narou.setCharacterLength(min, max)
        }
    }

    private fun setSerializationInfo(narou: Narou, end: Boolean, stop: Boolean, pickup: Boolean) {
        if (end) {
            narou.setNovelType(NovelType.ALL_NOVEL)
        } else {
            narou.setNovelType(NovelType.ALL_SERIES)
        }

        if (stop) {
            narou.setExcludeStop(true)
        }

        if (pickup) {
            narou.setPickup(true)
        }
    }

    private fun setGenre(narou: Narou, genreList: List<Int>) {
        if (genreList.size > 0) {
            for (genre in genreList) {
                narou.setGenre(NovelGenre.valueOf(genre))
            }
        }
    }

    private fun fetchNovel(narou: Narou): Pair<List<Novel>, Throwable> {
        var novels: MutableList<Novel>? = null
        var throwable: Throwable? = null
        try {
            novels = narou.novels
        } catch (e: IOException) {
            throwable = e
        }

        if (novels != null) {
            novels.removeAt(0)
        }

        return Pair.create<List<Novel>, Throwable>(novels, throwable)
    }

    private fun setSortOrder(narou: Narou, order: Int) {
        val index = order - 1
        val orders = OutputOrder.values()
        if (index >= 0 && index < orders.size) {
            narou.setOrder(orders[index])
        }
    }

    private fun setTime(narou: Narou, time: Int) {
        val index = time - 1
        val times = arrayOf(intArrayOf(0, 5), intArrayOf(5, 10), intArrayOf(10, 30), intArrayOf(30, 60), intArrayOf(60, 120), intArrayOf(120, 180), intArrayOf(180, 240), intArrayOf(240, 300), intArrayOf(300, 360), intArrayOf(360, 420), intArrayOf(420, 480), intArrayOf(480, 540), intArrayOf(540, 600), intArrayOf(600, 0))

        if (index >= 0 && index < times.size) {
            narou.setReadTime(times[index][0], times[index][1])
        }
    }

    companion object {

        private val TAG = SearchRecyclerPresenter::class.java.simpleName
    }
}
