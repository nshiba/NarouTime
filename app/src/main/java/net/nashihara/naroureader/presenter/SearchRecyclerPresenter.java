package net.nashihara.naroureader.presenter;

import android.support.v4.util.Pair;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import net.nashihara.naroureader.entities.NovelItem;
import net.nashihara.naroureader.entities.Query;
import net.nashihara.naroureader.views.SearchRecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import narou4j.Narou;
import narou4j.entities.Novel;
import narou4j.entities.NovelRank;
import narou4j.enums.NovelGenre;
import narou4j.enums.NovelType;
import narou4j.enums.OutputOrder;
import narou4j.enums.SearchWordTarget;
import rx.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static narou4j.enums.OutputOrder.BOOKMARK_COUNT;
import static narou4j.enums.OutputOrder.CHARACTER_LENGTH_ASC;
import static narou4j.enums.OutputOrder.CHARACTER_LENGTH_DESC;
import static narou4j.enums.OutputOrder.HYOKA_COUNT;
import static narou4j.enums.OutputOrder.HYOKA_COUNT_ASC;
import static narou4j.enums.OutputOrder.IMPRESSION_COUNT;
import static narou4j.enums.OutputOrder.NCODE_DESC;
import static narou4j.enums.OutputOrder.OLD;
import static narou4j.enums.OutputOrder.REVIEW_COUNT;
import static narou4j.enums.OutputOrder.TOTAL_POINT;
import static narou4j.enums.OutputOrder.TOTAL_POINT_ASC;
import static narou4j.enums.OutputOrder.WEEKLY_UU;

public class SearchRecyclerPresenter implements Presenter<SearchRecyclerView> {

    private final static String TAG = SearchRecyclerPresenter.class.getSimpleName();

    private SearchRecyclerView view;

    public SearchRecyclerPresenter(SearchRecyclerView view) {
        attach(view);
    }

    @Override
    public void attach(SearchRecyclerView view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
    }

    private void error(Throwable throwable) {
        Log.e(TAG, "error: ", throwable);
        FirebaseCrash.report(throwable);
        view.showError();
    }

    public void searchNovel(Query query, ArrayList<Integer> genreList) {
        buildSearchObservable(query, genreList)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(novels -> {
                if (novels == null) {
                    Log.d(TAG, "onNext: novels are null");
                    view.showRecyclerView(null);
                    return;
                }

                if (novels.size() == 0) {
                    Log.d(TAG, "onNext: novels size is zero");
                    view.showRecyclerView(null);
                    return;
                }

                List<NovelItem> novelItemList = setupNovelItems(novels);
                view.showRecyclerView(novelItemList);
            }, this::error);
    }

    private List<NovelItem> setupNovelItems(List<Novel> novelList) {
        List<NovelItem> novelItemList = new ArrayList<>();
        for (int i = 0; i < novelList.size(); i++) {
            Novel novel = novelList.get(i);
            NovelItem item = new NovelItem();
            NovelRank rank = new NovelRank();

            novel.setNcode(novel.getNcode().toLowerCase());

            rank.setNcode(novel.getNcode());
            rank.setRank(i);

            item.setNovelDetail(novel);
            item.setRank(rank);

            novelItemList.add(item);
        }

        return novelItemList;
    }

    private Observable<List<Novel>> buildSearchObservable(Query query, ArrayList<Integer> genreList) {

        return Observable.fromEmitter(emitter -> {
            Narou narou = new Narou();

            emitSearchObservable(emitter, narou, query, genreList);

            emitter.onCompleted();
        }, Emitter.BackpressureMode.NONE);
    }

    private void emitSearchObservable(Emitter<List<Novel>> emitter, Narou narou, Query query, ArrayList<Integer> genreList) {
        if (query.getNcode().equals("")) {
            emitSearchObservableFromSearchQuery(emitter, narou, query, genreList);
        } else {
            emitSearchObservableFromNcode(emitter, narou, query.getNcode());
        }
    }

    private void emitSearchObservableFromNcode(Emitter<List<Novel>> emitter, Narou narou, String ncode) {
        Novel novel = null;
        try {
            novel = narou.getNovel(ncode);
        } catch (IOException e) {
            emitter.onError(e);
        }

        if (novel == null) {
            emitter.onNext(null);
        } else {
            List<Novel> list = new ArrayList<>();
            list.add(novel);
            emitter.onNext(list);
        }
    }

    private void emitSearchObservableFromSearchQuery(
      Emitter<List<Novel>> emitter, Narou narou, Query query, ArrayList<Integer> genreList) {
        setTextParam(narou, query.getSearch(), query.getNotSearch());
        setPageLimit(narou, query.getLimit());
        setSortOrder(narou, query.getSortOrder());
        setTime(narou, query.getTime());
        setSummary(narou, query.isTargetTitle(), query.isTargetStory(), query.isTargetKeyword(), query.isTargetWriter());
        setCharLength(narou, query.getMinLength(), query.getMaxLength());
        setSerializationInfo(narou, query.isEnd(), query.isStop(), query.isPickup());
        setGenre(narou, genreList);

        Pair<List<Novel>, Throwable> result = fetchNovel(narou);
        if (result.second != null) {
            emitter.onError(result.second);
        } else {
            emitter.onNext(result.first);
        }
    }

    private void setTextParam(Narou narou, String search, String notSearch) {
        if (!search.equals("")) {
            narou.setSearchWord(search);
        }

        if (!notSearch.equals("")) {
            narou.setNotWord(notSearch);
        }
    }

    private void setPageLimit(Narou narou, int limit) {
        if (limit == 0) {
            narou.setLim(50);
        } else {
            narou.setLim(limit);
        }
    }

    private void setSummary(Narou narou, boolean title, boolean story, boolean keyword, boolean writer) {
        if (title) {
            narou.setSearchTarget(SearchWordTarget.TITLE);
        }
        if (story) {
            narou.setSearchTarget(SearchWordTarget.SYNOPSIS);
        }
        if (keyword) {
            narou.setSearchTarget(SearchWordTarget.KEYWORD);
        }
        if (writer) {
            narou.setSearchTarget(SearchWordTarget.WRITER);
        }
    }

    private void setCharLength(Narou narou, int min, int max) {
        if (min != 0 || max != 0) {
            narou.setCharacterLength(min, max);
        }
    }

    private void setSerializationInfo(Narou narou, boolean end, boolean stop, boolean pickup) {
        if (end) {
            narou.setNovelType(NovelType.ALL_NOVEL);
        } else {
            narou.setNovelType(NovelType.ALL_SERIES);
        }

        if (stop) {
            narou.setExcludeStop(true);
        }

        if (pickup) {
            narou.setPickup(true);
        }
    }

    private void setGenre(Narou narou, List<Integer> genreList) {
        if (genreList.size() > 0) {
            for (Integer genre : genreList) {
                narou.setGenre(NovelGenre.valueOf(genre));
            }
        }
    }

    private Pair<List<Novel>, Throwable> fetchNovel(Narou narou) {
        List<Novel> novels = null;
        Throwable throwable = null;
        try {
            novels = narou.getNovels();
        } catch (IOException e) {
            throwable = e;
        }

        if (novels != null) {
            novels.remove(0);
        }

        return Pair.create(novels, throwable);
    }

    private void setSortOrder(Narou narou, int order) {
        int index = order - 1;
        OutputOrder[] orders = {
          BOOKMARK_COUNT,
          REVIEW_COUNT,
          TOTAL_POINT,
          TOTAL_POINT_ASC,
          IMPRESSION_COUNT,
          HYOKA_COUNT,
          HYOKA_COUNT_ASC,
          WEEKLY_UU,
          CHARACTER_LENGTH_ASC,
          CHARACTER_LENGTH_DESC,
          NCODE_DESC,
          OLD,
        };

        if (index < orders.length) {
            narou.setOrder(orders[index]);
        }
    }

    private void setTime(Narou narou, int time) {
        int index = time - 1;
        int times[][] = {
          {0, 5},
          {5, 10},
          {10, 30},
          {30, 60},
          {60, 120},
          {120, 180},
          {180, 240},
          {240, 300},
          {300, 360},
          {360, 420},
          {420, 480},
          {480, 540},
          {540, 600},
          {600, 0},
        };

        if (index < times.length) {
            narou.setReadTime(times[index][0], times[index][1]);
        }
    }
}
