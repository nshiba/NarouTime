package net.nashihara.naroureader.presenter

import android.support.v4.util.Pair
import android.text.TextUtils
import android.util.Log

import com.google.firebase.crash.FirebaseCrash

import net.nashihara.naroureader.entities.NovelItem
import net.nashihara.naroureader.views.RankingRecyclerView

import java.io.IOException
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
import rx.Emitter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.functions.Func1
import rx.schedulers.Schedulers

class RankingRecyclerPresenter(view: RankingRecyclerView) : Presenter<RankingRecyclerView> {

    private var view: RankingRecyclerView? = null

    init {
        attach(view)
    }

    override fun attach(view: RankingRecyclerView) {
        this.view = view
    }

    override fun detach() {
        view = null
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
        if (TextUtils.isEmpty(rankingType)) {
            return
        }

        if (rankingType == "all") {
            fetchTotalRanking()
        } else {
            fetchEachRanking(RankingType.valueOf(rankingType))
        }
    }

    private fun fetchTotalRanking() {
        fetchNovelsFromTotalRanking()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view?.showRanking(it) }, { this.error(it) })
    }

    private fun fetchNovelsFromTotalRanking(): Observable<List<NovelItem>> {
        return Observable.fromEmitter<List<NovelItem>>({ emitter ->
            val narou = Narou()
            narou.setOrder(OutputOrder.TOTAL_POINT)
            narou.setLim(301)

            var novels: MutableList<Novel>? = null
            try {
                novels = narou.novels
            } catch (e: IOException) {
                emitter.onError(e)
            }

            if (novels == null) {
                emitter.onError(null)
                emitter.onCompleted()
                return@fromEmitter
            }

            emitter.onNext(novelToNovelItem(novels))
            emitter.onCompleted()
        }, Emitter.BackpressureMode.NONE)
    }

    private fun novelToNovelItem(novels: MutableList<Novel>): List<NovelItem> {
        novels.removeAt(0)

        val items = ArrayList<NovelItem>()
        var item: NovelItem
        for (novel in novels) {
            // ncodeが大文字と小文字が混在しているので小文字に統一
            novel.ncode = novel.ncode.toLowerCase()

            item = NovelItem()
            val rank = NovelRank()
            rank.pt = novel.globalPoint
            item.novelDetail = novel
            item.rank = rank
            items.add(item)
        }

        return items
    }

    private fun fetchEachRanking(rankingType: RankingType) {
        fetchNovelRank(rankingType)
                .flatMap { map -> fetchPrevNovelRank(map, rankingType) }
                .flatMap<List<NovelItem>>({ this.setupNovelRanking(it) })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view?.showRanking(it) }, { this.error(it) })
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

    private fun fetchNovelRank(rankingType: RankingType): Observable<HashMap<String, NovelItem>> {
        return Observable.fromEmitter<HashMap<String, NovelItem>>({ emitter ->
            val ranking = Ranking()
            var ranks: ArrayList<NovelRank>? = null
            try {
                ranks = ranking.getRanking(rankingType) as ArrayList<NovelRank>
            } catch (e: IOException) {
                emitter.onError(e)
            }

            if (ranks == null) {
                emitter.onError(Throwable("error: ranks is null"))
                emitter.onCompleted()
                return@fromEmitter
            }

            emitter.onNext(novelRankToNovelMap(ranks, rankingType))
            emitter.onCompleted()
        }, Emitter.BackpressureMode.NONE)
    }

    private fun novelRankToNovelMap(rankList: List<NovelRank>, rankingType: RankingType): HashMap<String, NovelItem> {
        val map = HashMap<String, NovelItem>()
        for (rank in rankList) {
            val novelItem = NovelItem()
            rank.rankingType = rankingType
            novelItem.rank = rank
            map.put(rank.ncode, novelItem)
        }

        return map
    }

    private fun fetchPrevNovelRank(map: HashMap<String, NovelItem>, rankingType: RankingType): Observable<HashMap<String, NovelItem>> {
        return Observable.fromEmitter<HashMap<String, NovelItem>>({ emitter ->
            val ranking = Ranking()
            val cal = setupCalendarFromRankingType(rankingType)

            val rankList: List<NovelRank>
            try {
                rankList = ranking.getRanking(rankingType, cal.time)
            } catch (e: IOException) {
                emitter.onError(e)
                return@fromEmitter
            }

            if (rankList == null) {
                emitter.onError(null)
                return@fromEmitter
            }

            setupRank(rankList, map)

            emitter.onNext(map)
            emitter.onCompleted()
        }, Emitter.BackpressureMode.NONE)
    }

    private fun setupRank(rankList: List<NovelRank>, map: HashMap<String, NovelItem>) {
        for (rank in rankList) {
            val item = map[rank.ncode] ?: continue

            item.prevRank = rank
            map.put(rank.ncode, item)
        }
    }

    private fun setupNovelRanking(map: HashMap<String, NovelItem>): Observable<List<NovelItem>> {
        return Observable.fromEmitter<List<NovelItem>>({ emitter ->
            val pair = fetchNovelFromNcode(map)

            if (pair.first == null) {
                error(pair.second)
                return@fromEmitter
            }

            val items = validateNovels(pair.first, map)
            emitter.onNext(items)
            emitter.onCompleted()
        }, Emitter.BackpressureMode.NONE)
    }

    private fun fetchNovelFromNcode(map: HashMap<String, NovelItem>): Pair<MutableList<Novel>, Throwable> {
        val narou = Narou()
        val set = map.keys

        narou.setNCode(set.toTypedArray())
        narou.setLim(300)
        val novels: List<Novel>
        try {
            novels = narou.novels
        } catch (e: IOException) {
            return Pair.create<MutableList<Novel>, Throwable>(null, e)
        }

        return Pair.create<MutableList<Novel>, Throwable>(novels, null)
    }

    private fun validateNovels(novelList: MutableList<Novel>, map: HashMap<String, NovelItem>): List<NovelItem> {
        novelList.removeAt(0)

        val items = ArrayList<NovelItem>()
        for (novel in novelList) {
            val item = map[novel.ncode]
            if (item != null) {
                // 大文字 → 小文字
                novel.ncode = novel.ncode.toLowerCase()

                item.novelDetail = novel
                items.add(item)
            }
        }

        return items
    }

    fun filterNovelRanking(novelItemList: List<NovelItem>, filterIds: Array<NovelGenre>,
                           itemChecked: BooleanArray, min: String, max: String) {
        val trueSet = HashSet<NovelGenre>()
        val filterList = ArrayList<NovelItem>()
        val resultList = ArrayList<NovelItem>()

        val maxLength = validateCharLength(min)
        val minLength = validateCharLength(max)

        for (i in itemChecked.indices) {
            genreAndEndCheck(i, filterIds, itemChecked, novelItemList, trueSet, filterList)
        }

        for (i in filterList.indices) {
            val target = filterList[i]

            if (!charLengthCheck(target, trueSet, maxLength, minLength)) {
                continue
            }

            resultList.add(target)
        }

        view?.showRanking(resultList)
    }

    private fun validateCharLength(length: String): Int {
        return if (length == "") 0 else Integer.parseInt(length)
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
