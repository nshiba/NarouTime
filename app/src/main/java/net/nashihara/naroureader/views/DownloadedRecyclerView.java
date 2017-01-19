package net.nashihara.naroureader.views;

import net.nashihara.naroureader.models.entities.Novel4Realm;

import java.util.ArrayList;

public interface DownloadedRecyclerView extends BaseView {

    void showDownloadedNovels(ArrayList<Novel4Realm> novels);
}
