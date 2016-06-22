package net.nashihara.naroureader.models.entities;

import narou4j.entities.Novel;
import narou4j.entities.NovelRank;

public class NovelItem {
    private static final String TAG = NovelItem.class.getSimpleName();
    private Novel novelDetail;
    private NovelRank rank;
    private NovelRank prevRank;

    public Novel getNovelDetail() {
        return novelDetail;
    }

    public void setNovelDetail(Novel novelDetail) {
        this.novelDetail = novelDetail;
    }

    public NovelRank getRank() {
        return rank;
    }

    public void setRank(NovelRank rank) {
        this.rank = rank;
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
                ", novelDetail=" + novelDetail + "\n" +
                ", rank=" + rank + "\n" +
                ", prevRank=" + prevRank + "\n" +
                '}';
    }
}
