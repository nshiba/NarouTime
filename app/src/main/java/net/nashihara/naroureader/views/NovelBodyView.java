package net.nashihara.naroureader.views;

import narou4j.entities.NovelBody;

public interface NovelBodyView extends BaseView {

    void showNovelBody(NovelBody novelBody);

    void showError();
}
