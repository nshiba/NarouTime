package net.nashihara.naroureader.views;

import narou4j.entities.Novel;

public interface NovelTableRecyclerView extends BaseView {

    void showBookmark(int bookmark);

    void showNovelTable(Novel novel);

    void showError();
}
