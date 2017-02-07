package net.nashihara.naroureader.views;

import java.util.ArrayList;

public interface SearchView extends BaseView {

    void showResult(
        String ncode,
        int limit,
        int sortOrder,
        String search,
        String notSearch,
        boolean targetTitle,
        boolean targetStory,
        boolean targetKeyword,
        boolean targetWriter,
        int time,
        int maxLength,
        int minLength,
        boolean end,
        boolean stop,
        boolean pickup,
        ArrayList<Integer> genreList);

    void showError();
}
