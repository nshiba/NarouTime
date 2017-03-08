package net.nashihara.naroureader.presenter;

import android.renderscript.Int2;

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

    private int validateLimit(String limitStr) {
        int limit;
        if (limitStr.equals("")) {
            limit = 0;
        } else {
            limit = Integer.parseInt(limitStr);
        }

        return limit;
    }

    private Int2 validateMinMax(String minLength, String maxLength) {
        int min, max;

        if (minLength.equals("")) {
            min = 0;
        } else {
            min = Integer.parseInt(minLength);
        }

        if (maxLength.equals("")) {
            max = 0;
        } else {
            max = Integer.parseInt(maxLength);
        }

        return new Int2(max, min);
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

    public void shapeSearchQuery(
        String ncode,
        String limitStr,
        int sortOrder,
        String search,
        String notSearch,
        boolean targetTitle,
        boolean targetStory,
        boolean targetKeyword,
        boolean targetWriter,
        int time,
        String maxLength,
        String minLength,
        boolean end,
        boolean stop,
        boolean pickup,
        boolean[] genreChecked) {

        int limit = validateLimit(limitStr);
        if (limit > 500) {
            view.showError();
            return;
        }

        Int2 minMax = validateMinMax(minLength, maxLength);
        ArrayList<Integer> genres = validateGenreIds(genreChecked);

        view.showResult(ncode, limit, sortOrder, search, notSearch, targetTitle, targetStory,
            targetKeyword, targetWriter, time, minMax.x, minMax.y, end, stop, pickup, genres);
    }
}
