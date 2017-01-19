package net.nashihara.naroureader.views;

import net.nashihara.naroureader.entities.Novel4Realm;

import java.util.ArrayList;

public interface DownloadedRecyclerView extends BaseView {

    void showDownloadedNovels(ArrayList<Novel4Realm> novels);
}
