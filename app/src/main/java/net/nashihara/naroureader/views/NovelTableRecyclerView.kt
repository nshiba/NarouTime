package net.nashihara.naroureader.views

import narou4j.entities.Novel

interface NovelTableRecyclerView {

    fun showBookmark(bookmark: Int)

    fun showNovelTable(novel: Novel)

    fun showError()
}
