package net.nashihara.naroureader.models

import narou4j.entities.NovelRank
import narou4j.enums.RankingType

class RankingManager(private val novelRank: NovelRank, private val prevNovelRank: NovelRank?) {

    fun hasPrevNovelRank(): Boolean {
        return prevNovelRank != null
    }

    fun buildPositionMessage(position: Int): String {
        if (novelRank.rankingType == null) {
            return (position + 1).toString() + "位"
        }
        return buildPositionMessage()
    }

    fun buildPositionMessage(): String {
        return novelRank.rank.toString() + "位"
    }

    fun buildPrevRankingMessage(): String {
        val defaultMessage = "前回：ー"
        if (prevNovelRank == null) {
            return defaultMessage
        }

        val prev = prevNovelRank.rank.toString() + "位"
        return when (novelRank.rankingType) {
            RankingType.DAILY -> "前日：" + prev
            RankingType.WEEKLY -> "前週：" + prev
            RankingType.MONTHLY -> "前月：" + prev
            RankingType.QUARTET -> "前月：" + prev
            else -> defaultMessage
        }
    }

    fun isRankUp(): Boolean = novelRank.rank < prevNovelRank?.rank ?: 300

    fun isRankDown(): Boolean = novelRank.rank > prevNovelRank?.rank ?: 300

    fun isEqual(): Boolean = novelRank.rank == prevNovelRank?.rank ?: -1
}
