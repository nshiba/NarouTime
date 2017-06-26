package net.nashihara.naroureader.entities

import narou4j.entities.Novel
import narou4j.entities.NovelRank

data class NovelItem(
        var novelDetail: Novel,
        var rank: NovelRank? = null,
        var prevRank: NovelRank? = null)
