package net.nashihara.naroureader.views;

import net.nashihara.naroureader.entities.NovelItem;

import java.util.List;

public interface RankingRecyclerView extends BaseView {

    void showRanking(List<NovelItem> novelItems);

    void showError();
}
