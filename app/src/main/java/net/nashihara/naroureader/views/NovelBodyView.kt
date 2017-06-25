package net.nashihara.naroureader.views

import narou4j.entities.NovelBody

interface NovelBodyView {

    fun showNovelBody(novelBody: NovelBody)

    fun showError()
}
