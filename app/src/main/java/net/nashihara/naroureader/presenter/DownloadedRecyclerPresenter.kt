package net.nashihara.naroureader.presenter

import android.content.Context
import android.support.v7.widget.RecyclerView

import net.nashihara.naroureader.entities.Novel4Realm
import net.nashihara.naroureader.utils.RealmUtils
import net.nashihara.naroureader.views.DownloadedRecyclerView

import java.util.ArrayList

import io.realm.Realm
import io.realm.RealmResults

class DownloadedRecyclerPresenter(view: DownloadedRecyclerView) : Presenter<DownloadedRecyclerView> {

    private var view: DownloadedRecyclerView? = null

    init {
        attach(view)
    }

    override fun attach(view: DownloadedRecyclerView) {
        this.view = view
    }

    override fun detach() {
        view = null
    }

    fun fetchDownloadedNovels(context: Context) {
        val realm = RealmUtils.getRealm()
        val results = realm.where(Novel4Realm::class.java).equalTo("isDownload", true).findAll()
        view?.showDownloadedNovels(results.toList())
    }
}
