package net.nashihara.naroureader.presenter

import net.nashihara.naroureader.entities.Query
import net.nashihara.naroureader.views.SearchView

import java.util.ArrayList

import narou4j.enums.NovelGenre

class SearchPresenter(view: SearchView) : Presenter<SearchView> {

    private var view: SearchView? = null

    init {
        attach(view)
    }

    override fun attach(view: SearchView) {
        this.view = view
    }

    override fun detach() {
        view = null
    }

    private fun validateGenreIds(genreChecked: BooleanArray?): ArrayList<Int> {
        val genreIds = NovelGenre.values()
        val genres = ArrayList<Int>()

        genreChecked?.indices
                ?.filter { genreChecked[it] }
                ?.mapTo(genres) { genreIds[it].id }

        return genres
    }

    fun shapeSearchQuery(query: Query?, genreChecked: BooleanArray?) {
        if (query?.limit ?: 0 > 500) {
            view?.showError()
            return
        }

        val genres = validateGenreIds(genreChecked)

        view?.showResult(query!!, genres)
    }
}
