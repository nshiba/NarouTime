package net.nashihara.naroureader.presenter

import android.util.Log

import com.google.firebase.crash.FirebaseCrash
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job

import net.nashihara.naroureader.entities.NovelItem
import net.nashihara.naroureader.views.RankingRecyclerView

import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.HashSet

import narou4j.Narou
import narou4j.Ranking
import narou4j.entities.Novel
import narou4j.entities.NovelRank
import narou4j.enums.NovelGenre
import narou4j.enums.OutputOrder
import narou4j.enums.RankingType
import net.nashihara.naroureader.addTo
import net.nashihara.naroureader.async
import net.nashihara.naroureader.ui

class RankingRecyclerPresenter(view: RankingRecyclerView) : Presenter<RankingRecyclerView> {

    private var view: RankingRecyclerView? = null

    private val jobList = mutableListOf<Job>()

    init {
        attach(view)
    }

    override fun attach(view: RankingRecyclerView) {
        this.view = view
    }

    override fun detach() {
        view = null
        jobList.forEach { it.cancel() }
    }

    private fun error(throwable: Throwable?) {
        view?.showError()

        if (throwable == null) {
            return
        }

        Log.e(TAG, "RankingRecyclerController: ", throwable.fillInStackTrace())
        FirebaseCrash.report(throwable)
    }

    fun fetchRanking(rankingType: String) {
        if (rankingType.isNullOrEmpty()) {
            return
        }

        if (rankingType == "all") {
            fetchTotalRanking()
        } else {
            fetchEachRanking(RankingType.valueOf(rankingType))
        }
    }

    private fun fetchTotalRanking() {
        ui {
            try {
                val novelList = fetchNovelsFromTotalRanking()
                val novelItemList = novelToNovelItem(novelList.await())
                view?.showRanking(novelItemList)
            } catch (e: Exception) {
                error(e)
            }
        }.addTo(jobList)
    }

    private fun fetchNovelsFromTotalRanking(): Deferred<MutableList<Novel>> {
        return async {
            val narou = Narou()
            narou.setOrder(OutputOrder.TOTAL_POINT)
            narou.setLim(301)
            return@async narou.novels
        }
    }

    private fun novelToNovelItem(novels: MutableList<Novel>): List<NovelItem> {
        novels.removeAt(0)

        val items = ArrayList<NovelItem>()
        var item: NovelItem
        for (novel in novels) {
            // ncodeが大文字と小文字が混在しているので小文字に統一
            novel.ncode = novel.ncode.toLowerCase()

            val rank = NovelRank()
            rank.pt = novel.globalPoint
            item = NovelItem(novelDetail = novel, rank = rank)
            items.add(item)
        }

        return items
    }

    private fun fetchEachRanking(rankingType: RankingType) {
        ui {
            try {
                val ranking = async { fetchNovelRank(rankingType) }
                val rankingWithNovelInfo = async { fetchRankingNovelItem(ranking.await()) }
                val setupedNovelRanking = async { setupPrevNovelRank(rankingWithNovelInfo.await(), rankingType) }.await()
                view?.showRanking(setupedNovelRanking)
            } catch (e: Exception) {
                error(e)
            }
        }.addTo(jobList)
    }

    private fun setupCalendarFromRankingType(rankingType: RankingType): Calendar {
        val cal = Calendar.getInstance()
        cal.time = Date()

        when (rankingType) {
            RankingType.DAILY -> {
                cal.add(Calendar.DAY_OF_MONTH, -2)
            }
            RankingType.WEEKLY -> {
                cal.add(Calendar.DAY_OF_MONTH, -7)
            }
            RankingType.MONTHLY -> {
                cal.add(Calendar.DAY_OF_MONTH, -31)
            }
            RankingType.QUARTET -> {
                cal.add(Calendar.DAY_OF_MONTH, -31)
            }
        }

        return cal
    }

    private fun fetchNovelRank(rankingType: RankingType): HashMap<String, NovelRank> {
        val ranks = Ranking().getRanking(rankingType)
        return novelRankToNovelMap(ranks, rankingType)
    }

    private fun novelRankToNovelMap(rankList: List<NovelRank>, rankingType: RankingType): HashMap<String, NovelRank> {
        val map = HashMap<String, NovelRank>()
        rankList.forEach {
            it.rankingType = rankingType
            map.put(it.ncode, it)
        }

        return map
    }

    private fun setupPrevNovelRank(novelItems: List<NovelItem>, rankingType: RankingType): List<NovelItem> {
        val cal = setupCalendarFromRankingType(rankingType)
        val rankList: List<NovelRank> = Ranking().getRanking(rankingType, cal.time)
        return setupPrevRank(rankList, novelItems)
    }

    private fun setupPrevRank(rankList: List<NovelRank>, novelItems: List<NovelItem>): List<NovelItem> {
        novelItems.map { novelItem ->
            novelItem.prevRank = rankList.find { rankItem ->
                rankItem.ncode.toLowerCase() == novelItem.novelDetail.ncode.toLowerCase() }
        }
        return novelItems
    }

    private fun fetchRankingNovelItem(map: HashMap<String, NovelRank>): List<NovelItem> {
        val narou = Narou()
        narou.setNCode(map.keys.toTypedArray())
        narou.setLim(300)

        val novels = narou.novels
        val novelItems = mutableListOf<NovelItem>()
        novels.map { novelItems.add(NovelItem(novelDetail = it, rank = map[it.ncode])) }

        return validateNovels(novels, map)
    }
    private fun validateNovels(novelList: List<Novel>, map: HashMap<String, NovelRank>): List<NovelItem> {
        val novelMutableList = novelList.toMutableList()
        novelMutableList.removeAt(0)

        val items = mutableListOf<NovelItem>()
        for (novel in novelMutableList) {
            val rank = map[novel.ncode]
            rank?.let {
                novel.ncode = novel.ncode.toLowerCase()
                items.add(NovelItem(novelDetail = novel, rank = it))
            }
        }

        return items
    }

    fun filterNovelRanking(novelItemList: List<NovelItem>, filterIds: Array<NovelGenre>,
                           itemChecked: BooleanArray, min: String, max: String) {
        val trueSet = HashSet<NovelGenre>()
        val filterList = ArrayList<NovelItem>()

        val maxLength = validateCharLength(min)
        val minLength = validateCharLength(max)

        for (i in itemChecked.indices) {
            genreAndEndCheck(i, filterIds, itemChecked, novelItemList, trueSet, filterList)
        }

        val resultList = filterList.indices
                .map { filterList[it] }
                .filter { charLengthCheck(it, trueSet, maxLength, minLength) }

        view?.showRanking(resultList)
    }

    private fun validateCharLength(length: String): Int {
        return when (length) {
            "" -> 0
            else -> Integer.parseInt(length)
        }
    }

    private fun genreAndEndCheck(itemIndex: Int, filterIds: Array<NovelGenre>, itemChecked: BooleanArray,
                                 novelItemList: List<NovelItem>, trueSet: MutableSet<NovelGenre>, filterList: MutableList<NovelItem>) {
        if (itemIndex == 0) {
            // 完結済チェック
            if (itemChecked[itemIndex]) {
                for (j in novelItemList.indices) {
                    if (novelItemList[j].novelDetail.isNovelContinue == 0) {
                        filterList.add(novelItemList[j])
                    }
                }
            } else {
                filterList.addAll(novelItemList)
            }

            return
        }

        // ジャンルチェック
        if (itemChecked[itemIndex]) {
            trueSet.add(filterIds[itemIndex - 1])
        }
    }

    private fun charLengthCheck(target: NovelItem, trueSet: Set<NovelGenre>, max: Int, min: Int): Boolean {
        if (!trueSet.contains(target.novelDetail.genre)) {
            return false
        }

        // 文字数チェック
        if (max <= 0) {
            if (min > target.novelDetail.numberOfChar) {
                return false
            }
        } else {
            if (min > target.novelDetail.numberOfChar || target.novelDetail.numberOfChar > max) {
                return false
            }
        }

        return true
    }

    companion object {

        private val TAG = RankingRecyclerPresenter::class.java.simpleName
    }
}
