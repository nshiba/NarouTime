package net.nashihara.naroureader.controller;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.entities.NovelTable4Realm;
import net.nashihara.naroureader.views.NovelTableRecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import narou4j.Narou;
import narou4j.entities.Novel;
import narou4j.entities.NovelBody;
import rx.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NovelTableRecyclerViewController implements Controller<NovelTableRecyclerView> {

    private static final String TAG = NovelTableRecyclerViewController.class.getSimpleName();

    private NovelTableRecyclerView view;

    private Realm realm;

    public NovelTableRecyclerViewController(NovelTableRecyclerView view, Realm realm) {
        attach(view);
        this.realm = realm;
    }

    @Override
    public void attach(NovelTableRecyclerView view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
    }

    public void fetchBookmark(String ncode) {
        RealmQuery<Novel4Realm> query = realm.where(Novel4Realm.class);
        query.equalTo("ncode", ncode);
        RealmResults<Novel4Realm> results = query.findAll();

        if (results.size() == 0) {
            view.showBookmark(0);
        } else {
            Novel4Realm novel4Realm = results.get(0);
            view.showBookmark(novel4Realm.getBookmark());
        }
    }

    public void fetchNovel(String ncode) {
        Log.d(TAG, "fetchNovel: " + ncode);

        RealmResults<NovelTable4Realm> tableResult = realm.where(NovelTable4Realm.class).equalTo("ncode", ncode).findAll().sort("tableNumber");
        Novel4Realm novel4Realm = realm.where(Novel4Realm.class).equalTo("ncode", ncode).findFirst();

        if (novel4Realm == null) {
            fetchNovelFromApi(ncode);
            return;
        }

        if (tableResult.size() <= 0 || !novel4Realm.isDownload()) {
            fetchNovelFromApi(ncode);
            return;
        }

        ArrayList<NovelBody> table = new ArrayList<>();
        for (NovelTable4Realm novelTable4Realm : tableResult) {
            NovelBody tableItem = new NovelBody();
            tableItem.setNcode(novelTable4Realm.getNcode());
            tableItem.setTitle(novelTable4Realm.getTitle());
            tableItem.setChapter(novelTable4Realm.isChapter());
            tableItem.setPage(novelTable4Realm.getPage());
            table.add(tableItem);
        }

        Novel novel = new Novel();
        novel.setNcode(String.format("Nコード : %s", ncode));
        novel.setWriter(String.format("作者 : %s", novel4Realm.getWriter()));
        novel.setTitle(novel4Realm.getTitle());
        novel.setStory(novel4Realm.getStory());

        view.showNovelTable(novel);

        realm.close();
    }

    public void fetchNovelFromApi(String ncode) {
        Observable.zip(fetchNovelBasicInfo(ncode), fetchNovelTable(ncode), (info, table) -> {
            info.setBodies(table);
            return info;
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(view::showNovelTable, this::showError);
    }

    private Observable<Novel> fetchNovelBasicInfo(String ncode) {
        return Observable.fromEmitter(emitter -> {
            Narou narou = new Narou();
            try {
                emitter.onNext(narou.getNovel(ncode));
            } catch (IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.NONE);
    }

    private Observable<List<NovelBody>> fetchNovelTable(String ncode) {
        return Observable.fromEmitter(emitter -> {
            Narou narou = new Narou();

            try {
                emitter.onNext(narou.getNovelTable(ncode));
            } catch (IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.NONE);
    }

    private void showError(Throwable throwable) {
        Log.e(TAG, "NovelTableRecyclerViewController: ", throwable);
        FirebaseCrash.report(throwable);
        view.showError();
    }
}
