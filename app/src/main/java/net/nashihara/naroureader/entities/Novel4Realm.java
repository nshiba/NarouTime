package net.nashihara.naroureader.entities;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Novel4Realm extends RealmObject {
    @PrimaryKey
    private String ncode;
    private String title;
    private String writer;
    private int bookmark;
    private boolean fav;

    public String getNcode() {
        return ncode;
    }

    public void setNcode(String ncode) {
        this.ncode = ncode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public int getBookmark() {
        return bookmark;
    }

    public void setBookmark(int bookmark) {
        this.bookmark = bookmark;
    }

    public boolean isFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }
}
