package net.nashihara.naroureader.presenter;

import android.support.annotation.Nullable;

import net.nashihara.naroureader.entities.Query;
import net.nashihara.naroureader.views.SearchView;

import java.util.ArrayList;

import narou4j.enums.NovelGenre;

public class SearchPresenter implements Presenter<SearchView> {

    private SearchView view;

    public SearchPresenter(SearchView view) {
        attach(view);
    }

    @Override
    public void attach(SearchView view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
    }

    private ArrayList<Integer> validateGenreIds(boolean[] genreChecked) {
        NovelGenre[] genreIds = NovelGenre.values();
        ArrayList<Integer> genres = new ArrayList<>();
        for (int i = 0; i < genreChecked.length; i++) {
            if (genreChecked[i]) {
                genres.add(genreIds[i].getId());
            }
        }

        return genres;
    }

    public void shapeSearchQuery(@Nullable Query query, @Nullable boolean[] genreChecked) {
        if ((query != null ? query.getLimit() : 0) > 500) {
            view.showError();
            return;
        }

        ArrayList<Integer> genres = validateGenreIds(genreChecked);

        view.showResult(query , genres);
    }
}
