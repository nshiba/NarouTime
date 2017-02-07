package net.nashihara.naroureader.controller;

import android.support.v4.util.Pair;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import net.nashihara.naroureader.entities.NovelItem;
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

public class SearchRecyclerController implements Controller<SearchRecyclerView> {

    private final static String TAG = SearchRecyclerController.class.getSimpleName();

    private SearchRecyclerView view;

    public SearchRecyclerController(SearchRecyclerView view) {
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

    public void searchNovel(String ncode, int limit, int sortOrder, String search, String notSearch,
                            boolean targetTitle, boolean targetStory, boolean targetKeyword,
                            boolean targetWriter, int time, int maxLength, int minLength,
                            boolean end, boolean stop, boolean pickup, ArrayList<Integer> genreList) {

        buildSearchObservable(ncode, limit, sortOrder, search, notSearch, targetTitle, targetStory,
            targetKeyword, targetWriter, time, maxLength, minLength, end, stop, pickup, genreList)
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

    private Observable<List<Novel>> buildSearchObservable(String ncode, int limit, int sortOrder,
                                                          String search, String notSearch,
                                                          boolean targetTitle, boolean targetStory,
                                                          boolean targetKeyword, boolean targetWriter,
                                                          int time, int maxLength, int minLength,
                                                          boolean end, boolean stop, boolean pickup,
                                                          ArrayList<Integer> genreList) {

        return Observable.fromEmitter(emitter -> {
            Narou narou = new Narou();

            if (!ncode.equals("")) {
                emitSearchObservableFromNcode(emitter, narou, ncode);
            } else {
                emitSearchObservableFromSearchQuery(emitter, narou, limit, sortOrder, search,
                    notSearch, targetTitle, targetStory, targetKeyword, targetWriter, time,
                    maxLength, minLength, end, stop, pickup, genreList);
            }

            emitter.onCompleted();
        }, Emitter.BackpressureMode.NONE);
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

    private void emitSearchObservableFromSearchQuery(Emitter<List<Novel>> emitter, Narou narou,
                                                     int limit, int sortOrder, String search,
                                                     String notSearch, boolean targetTitle,
                                                     boolean targetStory, boolean targetKeyword,
                                                     boolean targetWriter, int time, int maxLength, int minLength,
                                                     boolean end, boolean stop, boolean pickup,
                                                     ArrayList<Integer> genreList) {
        setTextParam(narou, search, notSearch);
        setPageLimit(narou, limit);
        setSortOrder(narou, sortOrder);
        setTime(narou, time);
        setSummary(narou, targetTitle, targetStory, targetKeyword, targetWriter);
        setCharLength(narou, minLength, maxLength);
        setSerializationInfo(narou, end, stop, pickup);
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
        switch (order) {
            case 1: {
                narou.setOrder(OutputOrder.BOOKMARK_COUNT);
                break;
            }
            case 2: {
                narou.setOrder(OutputOrder.REVIEW_COUNT);
                break;
            }
            case 3: {
                narou.setOrder(OutputOrder.TOTAL_POINT);
                break;
            }
            case 4: {
                narou.setOrder(OutputOrder.TOTAL_POINT_ASC);
                break;
            }
            case 5: {
                narou.setOrder(OutputOrder.IMPRESSION_COUNT);
                break;
            }
            case 6: {
                narou.setOrder(OutputOrder.HYOKA_COUNT);
                break;
            }
            case 7: {
                narou.setOrder(OutputOrder.HYOKA_COUNT_ASC);
                break;
            }
            case 8: {
                narou.setOrder(OutputOrder.WEEKLY_UU);
                break;
            }
            case 9: {
                narou.setOrder(OutputOrder.CHARACTER_LENGTH_ASC);
                break;
            }
            case 10: {
                narou.setOrder(OutputOrder.CHARACTER_LENGTH_DESC);
                break;
            }
            case 11: {
                narou.setOrder(OutputOrder.NCODE_DESC);
                break;
            }
            case 12: {
                narou.setOrder(OutputOrder.OLD);
                break;
            }
        }
    }

    private void setTime(Narou narou, int time) {
        switch (time) {
            case 1: {
                narou.setReadTime(0, 5);
                break;
            }
            case 2: {
                narou.setReadTime(5, 10);
                break;
            }
            case 3: {
                narou.setReadTime(10, 30);
                break;
            }
            case 4: {
                narou.setReadTime(30, 60);
                break;
            }
            case 5: {
                narou.setReadTime(60, 120);
                break;
            }
            case 6: {
                narou.setReadTime(120, 180);
                break;
            }
            case 7: {
                narou.setReadTime(180, 240);
                break;
            }
            case 8: {
                // 異世界
                narou.setReadTime(240, 300);
                break;
            }
            case 9: {
                narou.setReadTime(300, 360);
                break;
            }
            case 10: {
                narou.setReadTime(360, 420);
                break;
            }
            case 11: {
                narou.setReadTime(420, 480);
                break;
            }
            case 12: {
                narou.setReadTime(480, 540);
                break;
            }
            case 13: {
                narou.setReadTime(540, 600);
                break;
            }
            case 14: {
                narou.setReadTime(600, 0);
                break;
            }
        }
    }
}
