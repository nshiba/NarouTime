package net.nashihara.naroureader.views;

import net.nashihara.naroureader.entities.Query;

import java.util.ArrayList;

public interface SearchView  {

    void showResult(Query query, ArrayList<Integer> genreList);

    void showError();
}
