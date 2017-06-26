package net.nashihara.naroureader.entities

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass
open class NovelBody4Realm(
        var page: Int = 0,
        _ncode: String? = null,
        var title: String? = null,
        var body: String? = null) : RealmObject() {

    var ncode: String? = _ncode
        set(value) {
            field = value?.toLowerCase()
        }
}
