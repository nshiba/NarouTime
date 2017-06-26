package net.nashihara.naroureader.views

import net.nashihara.naroureader.entities.Novel4Realm

import java.util.ArrayList

interface DownloadedRecyclerView {

    fun showDownloadedNovels(novels: List<Novel4Realm>)
}
