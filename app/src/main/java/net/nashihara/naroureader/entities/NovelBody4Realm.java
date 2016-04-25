package net.nashihara.naroureader.entities;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class NovelBody4Realm extends RealmObject {
    @PrimaryKey
    private int page;
    private String title;
    private String body;

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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
