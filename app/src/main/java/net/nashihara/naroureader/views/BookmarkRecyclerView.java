package net.nashihara.naroureader.views;

import net.nashihara.naroureader.entities.Novel4Realm;

import java.util.ArrayList;

public interface BookmarkRecyclerView extends BaseView {

    void showBookmarks(ArrayList<Novel4Realm> novels);
}
