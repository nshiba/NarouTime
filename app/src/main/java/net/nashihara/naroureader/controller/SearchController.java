package net.nashihara.naroureader.controller;

import net.nashihara.naroureader.views.SearchView;

import java.util.ArrayList;

import narou4j.enums.NovelGenre;

public class SearchController implements Controller<SearchView> {

    private SearchView view;

    public SearchController(SearchView view) {
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

        int limit;
        if (limitStr.equals("")) {
            limit = 0;
        } else {
            limit = Integer.parseInt(limitStr);
        }

        if (limit > 500) {
            view.showError();
            return;
        }

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

        NovelGenre[] genreIds = NovelGenre.values();
        ArrayList<Integer> genres = new ArrayList<>();
        for (int i = 0; i < genreChecked.length; i++) {
            if (genreChecked[i]) {
                genres.add(genreIds[i].getId());
            }
        }

        view.showResult(ncode, limit, sortOrder, search, notSearch, targetTitle, targetStory,
            targetKeyword, targetWriter, time, max, min, end, stop, pickup, genres);
    }
}
