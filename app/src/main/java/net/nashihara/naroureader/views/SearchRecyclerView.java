package net.nashihara.naroureader.views;

import net.nashihara.naroureader.entities.NovelItem;

import java.util.List;

public interface SearchRecyclerView extends BaseView {

    void showRecyclerView(List<NovelItem> novelItems);

    void showError();
}
