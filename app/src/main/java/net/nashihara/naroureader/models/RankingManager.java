package net.nashihara.naroureader.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import narou4j.entities.NovelRank;

public class RankingManager {
    private NovelRank novelRank;
    private NovelRank prevNovelRank;

    public RankingManager(@NonNull NovelRank novelRank, @Nullable NovelRank prevNovelRank) {
        this.novelRank = novelRank;
        this.prevNovelRank = prevNovelRank;
    }

    public boolean hasPrevNovelRank() {
        return prevNovelRank != null;
    }

    public String buildPositionMessage(int position) {
        if (novelRank.getRankingType() == null) {
            return String.valueOf(position +1) + "位";
        }
        return buildPositionMessage();
    }

    public String buildPositionMessage() {
        return novelRank.getRank() + "位";
    }

    public String buildPrevRankingMessage() {
        String defaultMessage = "前回：ー";
        if (!hasPrevNovelRank()) {
            return defaultMessage;
        }

        String prev = String.valueOf(prevNovelRank.getRank()) + "位";
        switch (novelRank.getRankingType()) {
            case DAILY:
                return "前日：" + prev;
            case WEEKLY:
                return "前週：" + prev;
            case MONTHLY:
                return "前月：" + prev;
            case QUARTET:
                return "前月：" + prev;
            default:
                return defaultMessage;
        }
    }

    public boolean isRankUp() {
        return novelRank.getRank() < prevNovelRank.getRank();
    }

    public boolean isRankDown() {
        return novelRank.getRank() > prevNovelRank.getRank();
    }

    public boolean isEqual() {
        return novelRank.getRank() == prevNovelRank.getRank();
    }
}
