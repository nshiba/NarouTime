package net.nashihara.naroureader.views;

import net.nashihara.naroureader.entities.Query;

import java.util.ArrayList;

public interface SearchView extends BaseView {

    void showResult(Query query, ArrayList<Integer> genreList);

    void showError();
}
