package net.nashihara.naroureader.entities;

import narou4j.entities.Novel;
import narou4j.entities.NovelRank;

public class NovelItem {
    private static final String TAG = NovelItem.class.getSimpleName();
    private int rankingPoint;
    private int ranking;
    private Novel novelDetail;
    private NovelRank prevRank;

    public Novel getNovelDetail() {
        return novelDetail;
    }

    public void setNovelDetail(Novel novelDetail) {
        this.novelDetail = novelDetail;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public int getRankingPoint() {
        return rankingPoint;
    }

    public void setRankingPoint(int rankingPoint) {
        this.rankingPoint = rankingPoint;
    }

    public NovelRank getPrevRank() {
        return prevRank;
    }

    public void setPrevRank(NovelRank prevRank) {
        this.prevRank = prevRank;
    }

    @Override
    public String toString() {
        return "NovelItem{" + "\n" +
                "rankingPoint=" + rankingPoint +"\n" +
                ", ranking=" + ranking +"\n" +
                ", novelDetail=" + novelDetail +"\n" +
                ", prevRank=" + prevRank +"\n" +
                '}';
    }
}
