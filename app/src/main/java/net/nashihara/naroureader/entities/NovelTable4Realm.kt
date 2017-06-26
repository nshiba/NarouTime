package net.nashihara.naroureader.entities

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass
open class NovelTable4Realm(
        var tableNumber: Int = 0,
        _ncode: String? = "",
        var page: Int = 0,
        var title: String = "",
        var isChapter: Boolean = false) : RealmObject() {

    var ncode: String? = _ncode
        set(value) {
            field = value?.toLowerCase()
        }
}
