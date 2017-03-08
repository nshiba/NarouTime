package net.nashihara.naroureader.presenter;

import android.util.Log;

import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.entities.NovelBody4Realm;
import net.nashihara.naroureader.views.NovelBodyView;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import narou4j.Narou;
import narou4j.entities.NovelBody;
import rx.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class NovelBodyPresenter implements Presenter<NovelBodyView> {

    private final static String TAG = NovelBodyPresenter.class.getSimpleName();

    private NovelBodyView view;

    private Realm realm;

    public NovelBodyPresenter(NovelBodyView view, Realm realm) {
        attach(view);
        this.realm = realm;
    }

    @Override
    public void attach(NovelBodyView view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
    }

    public void setupNovelPage(String ncode, String title, String body, int page, boolean autoDownload, boolean autoSync) {
        RealmQuery<Novel4Realm> query = realm.where(Novel4Realm.class);
        query.equalTo("ncode", ncode);
        RealmResults<Novel4Realm> results = query.findAll();

        boolean isExistNovel = results.size() > 0;
        if (body.equals("")) {
            RealmResults<NovelBody4Realm> targetBody = getNovelBody(ncode, page);
            if (isExistNovel && targetBody.size() > 0) {
                NovelBody4Realm body4Realm = targetBody.get(0);

                NovelBody novelBody = new NovelBody();
                novelBody.setBody(body4Realm.getBody());
                novelBody.setNcode(body4Realm.getNcode());
                novelBody.setTitle(body4Realm.getTitle());
                novelBody.setPage(body4Realm.getPage());
                view.showNovelBody(novelBody);

                return;
            }

            fetchBody(ncode, page, autoDownload);
        } else {
            updateBody(ncode, page, body, title, autoDownload);
        }
    }

    private void updateBody(String ncode, int page, String body, String title, boolean autoDownload) {
        if (!autoDownload) {
            return;
        }

        storeNovelBody(ncode, page, title, body);
    }

    private void fetchBody(String ncode, int page, boolean autoDownload) {
        Observable.fromEmitter((Action1<Emitter<NovelBody>>) emitter -> {
            Narou narou = new Narou();
            try {
                emitter.onNext(narou.getNovelBody(ncode, page));
            } catch (IOException e) {
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.NONE)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(novelBody -> disposeNovelBody(novelBody, autoDownload), this::showError);
    }

    private void disposeNovelBody(NovelBody body, boolean autoDownload) {
        if (autoDownload) {
            storeNovelBody(body);
        }

        view.showNovelBody(body);
    }

    private void showError(Throwable throwable) {
        view.showError();
        Log.e(TAG, "NovelBodyController: ", throwable);
    }

    private void storeNovelBody(String ncode, int page, String title, String body) {
        NovelBody novelBody = new NovelBody();
        novelBody.setNcode(ncode);
        novelBody.setPage(page);
        novelBody.setTitle(title);
        novelBody.setBody(body);

        storeNovelBody(novelBody);
    }

    private void storeNovelBody(NovelBody body) {
        realm.beginTransaction();

        RealmResults<NovelBody4Realm> results = getNovelBody(body.getNcode(), body.getPage());

        NovelBody4Realm body4Realm;
        if (results.size() <= 0) {
            body4Realm = realm.createObject(NovelBody4Realm.class);
        }
        else {
            body4Realm = results.get(0);
        }

        body4Realm.setNcode(body.getNcode());
        body4Realm.setTitle(body.getTitle());
        body4Realm.setBody(body.getBody());
        body4Realm.setPage(body.getPage());

        realm.commitTransaction();
    }

    private RealmResults<NovelBody4Realm> getNovelBody(String ncode, int page) {
        RealmResults<NovelBody4Realm> ncodeResults = realm.where(NovelBody4Realm.class).equalTo("ncode", ncode).findAll();
        return ncodeResults.where().equalTo("page", page).findAll();
    }
}
