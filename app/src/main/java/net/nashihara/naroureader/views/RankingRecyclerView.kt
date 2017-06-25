package net.nashihara.naroureader.views

import net.nashihara.naroureader.entities.NovelItem

interface RankingRecyclerView {

    fun showRanking(novelItems: List<NovelItem>)

    fun showError()
}
