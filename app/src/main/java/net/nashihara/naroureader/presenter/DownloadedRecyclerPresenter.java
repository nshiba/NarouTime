package net.nashihara.naroureader.presenter;

import android.support.v7.widget.RecyclerView;

import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.utils.RealmUtils;
import net.nashihara.naroureader.views.DownloadedRecyclerView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class DownloadedRecyclerPresenter implements Presenter<DownloadedRecyclerView> {

    private DownloadedRecyclerView view;

    public DownloadedRecyclerPresenter(DownloadedRecyclerView view) {
        attach(view);
    }

    @Override
    public void attach(DownloadedRecyclerView view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
    }

    public void fetchDownloadedNovels() {
        Realm realm = RealmUtils.getRealm(view.getContext());
        RealmResults<Novel4Realm> results = realm.where(Novel4Realm.class).equalTo("isDownload", true).findAll();

        ArrayList<Novel4Realm> novels = new ArrayList<>();
        for (Novel4Realm novel4Realm : results) {
            novels.add(novel4Realm);
        }

        view.showDownloadedNovels(novels);
    }
}
