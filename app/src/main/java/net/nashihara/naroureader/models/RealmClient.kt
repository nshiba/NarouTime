package net.nashihara.naroureader.models

import android.content.Context
import io.realm.Realm
import net.nashihara.naroureader.utils.RealmUtils

class RealmClient(context: Context) {

    private val realm: Realm = RealmUtils.getRealm()

//    private val realm: Realm = Realm.getDefaultInstance()

}
