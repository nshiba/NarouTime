package net.nashihara.naroureader.views

import net.nashihara.naroureader.entities.Query

import java.util.ArrayList

interface SearchView {

    fun showResult(query: Query, genreList: List<Int>)

    fun showError()
}
