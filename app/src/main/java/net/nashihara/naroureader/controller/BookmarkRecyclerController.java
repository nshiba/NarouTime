package net.nashihara.naroureader.controller;

import net.nashihara.naroureader.entities.Novel4Realm;
import net.nashihara.naroureader.utils.RealmUtils;
import net.nashihara.naroureader.views.BookmarkRecyclerView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class BookmarkRecyclerController implements Controller<BookmarkRecyclerView> {

    private BookmarkRecyclerView view;

    public BookmarkRecyclerController(BookmarkRecyclerView view) {
        attach(view);
    }

    @Override
    public void attach(BookmarkRecyclerView view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
    }

    public void fetchBookmarkNovels() {
        Realm realm = RealmUtils.getRealm(view.getContext());
        RealmResults<Novel4Realm> results = realm.where(Novel4Realm.class).notEqualTo("bookmark", 0).findAll();

        ArrayList<Novel4Realm> novels = new ArrayList<>();
        for (Novel4Realm novel4Realm : results) {
            novels.add(novel4Realm);
        }

        view.showBookmarks(novels);
    }
}
