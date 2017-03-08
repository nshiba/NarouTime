package net.nashihara.naroureader.presenter;

import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import net.nashihara.naroureader.entities.NovelItem;
import net.nashihara.naroureader.views.RankingRecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import narou4j.Narou;
import narou4j.Ranking;
import narou4j.entities.Novel;
import narou4j.entities.NovelRank;
import narou4j.enums.NovelGenre;
import narou4j.enums.OutputOrder;
import narou4j.enums.RankingType;
import rx.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RankingRecyclerPresenter implements Presenter<RankingRecyclerView> {

    private static final String TAG = RankingRecyclerPresenter.class.getSimpleName();

    private RankingRecyclerView view;

    public RankingRecyclerPresenter(RankingRecyclerView view) {
        attach(view);
    }

    @Override
    public void attach(RankingRecyclerView view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
    }

    private void error(Throwable throwable) {
        view.showError();

        if (throwable == null) {
            return;
        }

        Log.e(TAG, "RankingRecyclerController: ", throwable.fillInStackTrace());
        FirebaseCrash.report(throwable);
    }

    public void fetchRanking(String rankingType) {
        if (TextUtils.isEmpty(rankingType)) {
            return;
        }

        if (rankingType.equals("all")) {
            fetchTotalRanking();
        } else {
            fetchEachRanking(RankingType.valueOf(rankingType));
        }
    }

    private void fetchTotalRanking() {
        fetchNovelsFromTotalRanking()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::showRanking, this::error);
    }

    private Observable<List<NovelItem>> fetchNovelsFromTotalRanking() {
        return Observable.fromEmitter(emitter -> {
            Narou narou = new Narou();
            narou.setOrder(OutputOrder.TOTAL_POINT);
            narou.setLim(301);

            List<Novel> novels = null;
            try {
                novels = narou.getNovels();
            } catch (IOException e) {
                emitter.onError(e);
            }

            if (novels == null) {
                emitter.onError(null);
                emitter.onCompleted();
                return;
            }

            emitter.onNext(novelToNovelItem(novels));
            emitter.onCompleted();
        }, Emitter.BackpressureMode.NONE);
    }

    private List<NovelItem> novelToNovelItem(List<Novel> novels) {
        novels.remove(0);

        List<NovelItem> items = new ArrayList<>();
        NovelItem item;
        for (Novel novel : novels) {
            // ncodeが大文字と小文字が混在しているので小文字に統一
            novel.setNcode(novel.getNcode().toLowerCase());

            item = new NovelItem();
            NovelRank rank = new NovelRank();
            rank.setPt(novel.getGlobalPoint());
            item.setNovelDetail(novel);
            item.setRank(rank);
            items.add(item);
        }

        return items;
    }

    private void fetchEachRanking(RankingType rankingType) {
        fetchNovelRank(rankingType)
            .flatMap(map -> fetchPrevNovelRank(map, rankingType))
            .flatMap(this::setupNovelRanking)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::showRanking, this::error);
    }

    private Calendar setupCalendarFromRankingType(RankingType rankingType) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        switch (rankingType) {
            case DAILY: {
                cal.add(Calendar.DAY_OF_MONTH, -2);
                break;
            }
            case WEEKLY: {
                cal.add(Calendar.DAY_OF_MONTH, -7);
                break;
            }
            case MONTHLY: {
                cal.add(Calendar.DAY_OF_MONTH, -31);
                break;
            }
            case QUARTET: {
                cal.add(Calendar.DAY_OF_MONTH, -31);
                break;
            }
        }

        return cal;
    }

    private Observable<HashMap<String, NovelItem>> fetchNovelRank(RankingType rankingType) {
        return Observable.fromEmitter(emitter -> {
            Ranking ranking = new Ranking();
            ArrayList<NovelRank> ranks = null;
            try {
                ranks = (ArrayList<NovelRank>) ranking.getRanking(rankingType);
            } catch (IOException e) {
                emitter.onError(e);
            }

            if (ranks == null) {
                emitter.onError(null);
                emitter.onCompleted();
                return;
            }

            emitter.onNext(novelRankToNovelMap(ranks, rankingType));
            emitter.onCompleted();
        }, Emitter.BackpressureMode.NONE);
    }

    private HashMap<String, NovelItem> novelRankToNovelMap(List<NovelRank> rankList, RankingType rankingType) {
        HashMap<String, NovelItem> map = new HashMap<>();
        for (NovelRank rank : rankList) {
            NovelItem novelItem = new NovelItem();
            rank.setRankingType(rankingType);
            novelItem.setRank(rank);
            map.put(rank.getNcode(), novelItem);
        }

        return map;
    }

    private Observable<HashMap<String, NovelItem>> fetchPrevNovelRank(HashMap<String, NovelItem> map, RankingType rankingType) {
        return Observable.fromEmitter(emitter -> {
            Ranking ranking = new Ranking();
            Calendar cal = setupCalendarFromRankingType(rankingType);

            List<NovelRank> rankList = null;
            try {
                rankList = ranking.getRanking(rankingType, cal.getTime());
            } catch (IOException e) {
                emitter.onError(e);
                return;
            }

            if (rankList == null) {
                emitter.onError(null);
                return;
            }

            setupRank(rankList, map);

            emitter.onNext(map);
            emitter.onCompleted();
        }, Emitter.BackpressureMode.NONE);
    }

    private void setupRank(List<NovelRank> rankList, HashMap<String, NovelItem> map) {
        for (NovelRank rank : rankList) {
            NovelItem item = map.get(rank.getNcode());

            if (item == null) {
                continue;
            }

            item.setPrevRank(rank);
            map.put(rank.getNcode(), item);
        }
    }

    private Observable<List<NovelItem>> setupNovelRanking(HashMap<String, NovelItem> map) {
        return Observable.fromEmitter(emitter -> {
            Pair<List<Novel>, Throwable> pair = fetchNovelFromNcode(map);

            if (pair.first == null) {
                error(pair.second);
                return;
            }

            List<NovelItem> items = validateNovels(pair.first, map);
            emitter.onNext(items);
            emitter.onCompleted();
        }, Emitter.BackpressureMode.NONE);
    }

    private Pair<List<Novel>, Throwable> fetchNovelFromNcode(HashMap<String, NovelItem> map) {
        Narou narou = new Narou();
        Set set = map.keySet();
        String[] array = new String[set.size()];
        set.toArray(array);

        narou.setNCode(array);
        narou.setLim(300);
        List<Novel> novels;
        try {
            novels = narou.getNovels();
        } catch (IOException e) {
            return Pair.create(null, e);
        }

        return Pair.create(novels, null);
    }

    private List<NovelItem> validateNovels(List<Novel> novelList, HashMap<String, NovelItem> map) {
        novelList.remove(0);

        ArrayList<NovelItem> items = new ArrayList<>();
        for (Novel novel : novelList) {
            NovelItem item = map.get(novel.getNcode());
            if (item != null) {
                // 大文字 → 小文字
                novel.setNcode(novel.getNcode().toLowerCase());

                item.setNovelDetail(novel);
                items.add(item);
            }
        }

        return items;
    }

    public void filterNovelRanking(List<NovelItem> novelItemList, NovelGenre[] filterIds,
                                   boolean[] itemChecked, String min, String max) {
        Set<NovelGenre> trueSet = new HashSet<>();
        List<NovelItem> filterList = new ArrayList<>();
        List<NovelItem> resultList = new ArrayList<>();

        int maxLength = validateCharLength(min);
        int minLength = validateCharLength(max);

        for (int i = 0; i < itemChecked.length; i++) {
            genreAndEndCheck(i, filterIds, itemChecked, novelItemList, trueSet, filterList);
        }

        for (int i = 0; i < filterList.size(); i++) {
            NovelItem target = filterList.get(i);

            if (!charLengthCheck(target, trueSet, maxLength, minLength)) {
                continue;
            }

            resultList.add(target);
        }

        view.showRanking(resultList);
    }

    private int validateCharLength(String length) {
        return length.equals("") ? 0 : Integer.parseInt(length);
    }

    private void genreAndEndCheck(int itemIndex, NovelGenre[] filterIds, boolean[] itemChecked,
                                  List<NovelItem> novelItemList, Set<NovelGenre> trueSet, List<NovelItem> filterList) {
        if (itemIndex == 0) {
            // 完結済チェック
            if (itemChecked[itemIndex]) {
                for (int j = 0; j < novelItemList.size(); j++) {
                    if (novelItemList.get(j).getNovelDetail().getIsNovelContinue() == 0) {
                        filterList.add(novelItemList.get(j));
                    }
                }
            }
            else {
                filterList.addAll(novelItemList);
            }

            return;
        }

        // ジャンルチェック
        if (itemChecked[itemIndex]) {
            trueSet.add(filterIds[itemIndex -1]);
        }
    }

    private boolean charLengthCheck(NovelItem target, Set<NovelGenre> trueSet, int max, int min) {
        if (!trueSet.contains(target.getNovelDetail().getGenre())) {
            return false;
        }

        // 文字数チェック
        if (max <= 0) {
            if (min > target.getNovelDetail().getNumberOfChar()) {
                return false;
            }
        } else {
            if (min > target.getNovelDetail().getNumberOfChar() || target.getNovelDetail().getNumberOfChar() > max) {
                return false;
            }
        }

        return true;
    }
}
