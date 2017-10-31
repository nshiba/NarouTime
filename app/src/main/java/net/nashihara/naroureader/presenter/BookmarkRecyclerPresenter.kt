package net.nashihara.naroureader.presenter

import android.content.Context
import net.nashihara.naroureader.entities.Novel4Realm
import net.nashihara.naroureader.utils.RealmUtils
import net.nashihara.naroureader.views.BookmarkRecyclerView

class BookmarkRecyclerPresenter(view: BookmarkRecyclerView) : Presenter<BookmarkRecyclerView> {

    private var view: BookmarkRecyclerView? = null

    init {
        attach(view)
    }

    override fun attach(view: BookmarkRecyclerView) {
        this.view = view
    }

    override fun detach() {
        view = null
    }

    fun fetchBookmarkNovels(context: Context) {
        val realm = RealmUtils.getRealm()
        val results = realm.where(Novel4Realm::class.java).notEqualTo("bookmark", 0).findAll()
        results?.let { view?.showBookmarks(it.toList()) }
    }
}
