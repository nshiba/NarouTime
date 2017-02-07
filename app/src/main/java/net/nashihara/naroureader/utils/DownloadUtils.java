package net.nashihara.naroureader.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import net.nashihara.naroureader.widgets.NovelDownloadDialogFragment;
import net.nashihara.naroureader.widgets.OkCancelDialogFragment;
import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.entities.NovelBody4Realm;
import net.nashihara.naroureader.entities.NovelTable4Realm;

import java.io.IOException;
import java.util.List;

import io.realm.Realm;
import narou4j.Narou;
import narou4j.entities.Novel;
import narou4j.entities.NovelBody;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public abstract class DownloadUtils {
    private static final String TAG = DownloadUtils.class.getSimpleName();
    private NovelDownloadDialogFragment downloadDialog;
    private FragmentManager manager;
    private Realm realm;
    private Context mContext;
    private Novel novel;

    public void novelDownlaod(final Novel novel, final FragmentManager manager, Context context) {
        mContext = context;
        this.novel = novel;
        this.manager = manager;

        OkCancelDialogFragment okCancelDialog = OkCancelDialogFragment.newInstance("小説ダウンロード", novel.getTitle() + "をダウンロードしますか？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (OkCancelDialogFragment.CANSEL == which) {
                    return;
                }
                dialog.dismiss();

                checkNovel();
            }
        });
        okCancelDialog.show(manager, "okcansel");
    }

    private void checkNovel() {
        realm = RealmUtils.getRealm(mContext);
        Novel4Realm novel4Realm = realm.where(Novel4Realm.class).equalTo("ncode", novel.getNcode().toLowerCase()).findFirst();
        if (novel4Realm != null) {
            if (novel4Realm.isDownload()) {
                downloaded(manager);
                return;
            }
        }
        else {
            realm.beginTransaction();
            novel4Realm = realm.createObject(Novel4Realm.class);
            novel4Realm.setNcode(novel.getNcode().toLowerCase());
            novel4Realm.setTitle(novel.getTitle());
            novel4Realm.setWriter(novel.getWriter());
            novel4Realm.setTotalPage(novel.getAllNumberOfNovel());
            novel4Realm.setStory(novel.getStory());
            realm.commitTransaction();
        }
        realm.close();

        downloadDialog = NovelDownloadDialogFragment.newInstance(novel.getAllNumberOfNovel(), "小説ダウンロード", novel.getTitle() + "をダウンロード中");
        downloadDialog.show(manager, "download");
        downloadTable();
    }

    private void downloaded(FragmentManager manager) {
        OkCancelDialogFragment okCancelDialog = OkCancelDialogFragment.newInstance(
            "小説ダウンロード", "すでにこの小説はダウンロード済みです", (dialog, which) -> dialog.dismiss());

        okCancelDialog.show(manager, "okcancel");
    }

    private void downloadTable() {
        Observable.create(new Observable.OnSubscribe<List<NovelBody>>() {
            @Override
            public void call(Subscriber<? super List<NovelBody>> subscriber) {
                Narou narou = new Narou();
                try {
                    subscriber.onNext(narou.getNovelTable(novel.getNcode().toLowerCase()));
                } catch (IOException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Subscriber<List<NovelBody>>() {
            @Override
            public void onCompleted() {
                downloadBody(novel.getAllNumberOfNovel());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: ", e.fillInStackTrace());
                FirebaseCrash.report(e);
                onDownloadError(downloadDialog);
            }

            @Override
            public void onNext(List<NovelBody> novelBodies) {
                storeTable(novelBodies);
                downloadDialog.setProgress(1);
            }
        });
    }

    private void downloadBody(final int totalPage) {
        Observable.create(new Observable.OnSubscribe<NovelBody>() {
            @Override
            public void call(Subscriber<? super NovelBody> subscriber) {
                Narou narou = new Narou();

                try {
                    realm = RealmUtils.getRealm(mContext);
                    for (int i = 1; i <= totalPage; i++) {
                        subscriber.onNext(narou.getNovelBody(novel.getNcode().toLowerCase(), i));
                    }
                    realm.close();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribe(new Subscriber<NovelBody>() {
            @Override
            public void onCompleted() {
                updateIsDownload();
                onDownloadSuccess(downloadDialog, novel);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: ", e.fillInStackTrace());
                FirebaseCrash.report(e);
                onDownloadError(downloadDialog);
                realm.close();
            }

            @Override
            public void onNext(NovelBody novelBody) {
                realm.beginTransaction();
                NovelBody4Realm body4Realm = realm.createObject(NovelBody4Realm.class);
                body4Realm.setNcode(novelBody.getNcode());
                body4Realm.setPage(novelBody.getPage());
                body4Realm.setTitle(novelBody.getTitle());
                body4Realm.setBody(novelBody.getBody());
                realm.commitTransaction();
                downloadDialog.setProgress(novelBody.getPage() +1);
            }
        });
    }

    private void updateIsDownload() {
        realm = RealmUtils.getRealm(mContext);
        Novel4Realm novel4Realm = realm.where(Novel4Realm.class).equalTo("ncode", novel.getNcode().toLowerCase()).findFirst();
        realm.beginTransaction();
        novel4Realm.setDownload(true);
        realm.commitTransaction();
        realm.close();
    }

    private void storeTable(List<NovelBody> novelBodies) {
        realm = RealmUtils.getRealm(mContext);
        realm.beginTransaction();
        for (int i = 0; i < novelBodies.size(); i++) {
            NovelBody targetTable = novelBodies.get(i);
            NovelTable4Realm storeTable = realm.createObject(NovelTable4Realm.class);

            storeTable.setTableNumber(i);

            if (targetTable.isChapter()) {
                storeTable.setChapter(true);
                storeTable.setTitle(targetTable.getTitle());
                storeTable.setNcode(targetTable.getNcode());
            }
            else {
                storeTable.setChapter(false);
                storeTable.setTitle(targetTable.getTitle());
                storeTable.setNcode(targetTable.getNcode());
                storeTable.setPage(targetTable.getPage());
            }
        }
        realm.commitTransaction();
        realm.close();
    }

    public abstract void onDownloadSuccess(NovelDownloadDialogFragment dialog, Novel novel);
    public abstract void onDownloadError(NovelDownloadDialogFragment dialog);
}