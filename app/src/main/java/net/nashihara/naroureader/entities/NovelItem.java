package net.nashihara.naroureader.entities;

import narou4j.Novel;

public class NovelItem {
    private static final String TAG = NovelItem.class.getSimpleName();
    private int rankingPoint;

    private Novel novelDetail;

    public Novel getNovelDetail() {
        return novelDetail;
    }

    public void setNovelDetail(Novel novelDetail) {
        this.novelDetail = novelDetail;
    }

    public int getRankingPoint() {
        return rankingPoint;
    }

    public void setRankingPoint(int rankingPoint) {
        this.rankingPoint = rankingPoint;
    }

    @Override
    public String toString() {
        return "NovelItem{" + "\n" +
                "rankingPoint=" + rankingPoint + "\n" +
                ", novelDetail=" + novelDetail.toString() + "\n" +
                '}';
    }
}
