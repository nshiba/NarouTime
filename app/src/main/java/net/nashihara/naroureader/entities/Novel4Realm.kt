package net.nashihara.naroureader.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Novel4Realm(
        _ncode: String? = null,
        var title: String? = null,
        var writer: String? = null,
        var story: String? = null,
        var bookmark: Int = 0,
        var fav: Boolean = false,
        var totalPage: Int = 0,
        var isDownload: Boolean = false) : RealmObject() {

    @PrimaryKey
    var ncode: String? = _ncode

    override fun toString(): String {
        return "Novel4Realm{" +
                "ncode='" + ncode + '\'' +
                ", title='" + title + '\'' +
                ", writer='" + writer + '\'' +
                ", story='" + story + '\'' +
                ", bookmark=" + bookmark +
                ", fav=" + fav +
                ", totalPage=" + totalPage +
                ", isDownload=" + isDownload +
                '}'
    }
}
