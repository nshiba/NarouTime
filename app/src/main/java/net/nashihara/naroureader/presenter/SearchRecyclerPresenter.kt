package net.nashihara.naroureader.presenter

import android.util.Log

import com.google.firebase.crash.FirebaseCrash
import kotlinx.coroutines.experimental.Job

import net.nashihara.naroureader.entities.NovelItem
import net.nashihara.naroureader.entities.Query
import net.nashihara.naroureader.views.SearchRecyclerView

import java.util.ArrayList

import narou4j.Narou
import narou4j.entities.Novel
import narou4j.entities.NovelRank
import narou4j.enums.NovelGenre
import narou4j.enums.NovelType
import narou4j.enums.OutputOrder
import narou4j.enums.SearchWordTarget
import net.nashihara.naroureader.async
import net.nashihara.naroureader.ui

class SearchRecyclerPresenter(view: SearchRecyclerView) : Presenter<SearchRecyclerView> {

    private var view: SearchRecyclerView? = null

    private val jobList = mutableListOf<Job>()

    init {
        attach(view)
    }

    override fun attach(view: SearchRecyclerView) {
        this.view = view
    }

    override fun detach() {
        view = null
        jobList.forEach { it.cancel() }
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

        ui {
            try {
                val novels = async { fetchNovelList(query, genreList) }.await()
                val novelItemList = setupNovelItems(novels)
                view?.showRecyclerView(novelItemList)
            } catch (e: Exception) {
                error(e)
            }
        }
    }

    private fun setupNovelItems(novelList: List<Novel>): List<NovelItem> {
        val novelItemList = ArrayList<NovelItem>()
        for (i in novelList.indices) {
            val novel = novelList[i]
            val rank = NovelRank()

            novel.ncode ?: continue

            novel.ncode = novel.ncode.toLowerCase()
            rank.ncode = novel.ncode
            rank.rank = i

            novelItemList.add(NovelItem(novelDetail = novel, rank = rank))
        }

        return novelItemList
    }

    private fun fetchNovelList(query: Query, genreList: ArrayList<Int>): List<Novel> {
        val narou = Narou()
        return if (query.ncode == "") {
            fetchNovelListFromSearchQuery(narou, query, genreList)
        } else {
            fetchNovelListFromNcode(narou, query.ncode)
        }
    }

    private fun fetchNovelListFromNcode(narou: Narou, ncode: String): List<Novel> {
        val novel: Novel = narou.getNovel(ncode)
        val list = ArrayList<Novel>()
        list.add(novel)
        return list
    }

    private fun fetchNovelListFromSearchQuery(
            narou: Narou, query: Query, genreList: ArrayList<Int>): List<Novel> {
        narou.setTextParam(query.search, query.notSearch)
        narou.setPageLimit(query.limit)
        narou.setSortOrder(query.sortOrder)
        narou.setTime(query.time)
        narou.setSummary(query.isTargetTitle, query.isTargetStory, query.isTargetKeyword, query.isTargetWriter)
        narou.setCharLength(query.minLength, query.maxLength)
        narou.setSerializationInfo(query.isEnd, query.isStop, query.isPickup)
        narou.setGenre(genreList)

        return narou.novels
    }

    companion object {

        private val TAG = SearchRecyclerPresenter::class.java.simpleName
    }
}

private fun Narou.setGenre(genreList: ArrayList<Int>) {
    if (genreList.size <= 0) {
        return
    }

    for (genre in genreList) {
        this.setGenre(NovelGenre.valueOf(genre))
    }
}

private fun Narou.setSerializationInfo(end: Boolean, stop: Boolean, pickup: Boolean) {
    if (end) {
        this.setNovelType(NovelType.ALL_NOVEL)
    } else {
        this.setNovelType(NovelType.ALL_SERIES)
    }

    if (stop) {
        this.setExcludeStop(true)
    }

    if (pickup) {
        this.setPickup(true)
    }
}

private fun Narou.setCharLength(min: Int, max: Int) {
    if (min != 0 || max != 0) {
        this.setCharacterLength(min, max)
    }
}

private fun Narou.setSummary(title: Boolean, story: Boolean, keyword: Boolean, writer: Boolean) {
    if (title) {
        this.setSearchTarget(SearchWordTarget.TITLE)
    }
    if (story) {
        this.setSearchTarget(SearchWordTarget.SYNOPSIS)
    }
    if (keyword) {
        this.setSearchTarget(SearchWordTarget.KEYWORD)
    }
    if (writer) {
        this.setSearchTarget(SearchWordTarget.WRITER)
    }
}

private fun Narou.setTime(time: Int) {
    val index = time - 1
    val times = arrayOf(intArrayOf(0, 5), intArrayOf(5, 10), intArrayOf(10, 30), intArrayOf(30, 60), intArrayOf(60, 120), intArrayOf(120, 180), intArrayOf(180, 240), intArrayOf(240, 300), intArrayOf(300, 360), intArrayOf(360, 420), intArrayOf(420, 480), intArrayOf(480, 540), intArrayOf(540, 600), intArrayOf(600, 0))

    if (index >= 0 && index < times.size) {
        this.setReadTime(times[index][0], times[index][1])
    }
}

private fun Narou.setSortOrder(order: Int) {
    val index = order - 1
    val orders = OutputOrder.values()
    if (index >= 0 && index < orders.size) {
        this.setOrder(orders[index])
    }
}

private fun Narou.setPageLimit(limit: Int) {
    if (limit == 0) {
        this.setLim(50)
    } else {
        this.setLim(limit)
    }
}

private fun Narou.setTextParam(search: String, notSearch: String) {
    if (search != "") {
        this.setSearchWord(search)
    }

    if (notSearch != "") {
        this.setNotWord(notSearch)
    }
}
