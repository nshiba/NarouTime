package net.nashihara.naroureader.models.entities;

import io.realm.RealmObject;

public class NovelTable4Realm extends RealmObject {
    private int tableNumber;
    private String ncode;
    private int page;
    private String title;
    private boolean isChapter;

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getNcode() {
        return ncode;
    }

    public void setNcode(String ncode) {
        if (ncode != null) {
            ncode = ncode.toLowerCase();
        }
        this.ncode = ncode;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChapter() {
        return isChapter;
    }

    public void setChapter(boolean chapter) {
        isChapter = chapter;
    }
}
