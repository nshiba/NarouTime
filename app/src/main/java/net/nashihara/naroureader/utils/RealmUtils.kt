package net.nashihara.naroureader.utils

import android.content.Context

import io.realm.Realm
import io.realm.RealmConfiguration
import okhttp3.internal.io.RealConnection

object RealmUtils {
    private val migration = Migration()
    private var defaultConfig: RealmConfiguration? = null

    private val VERSION = 3

    fun getRealm(context: Context): Realm {
        if (defaultConfig == null) {
            Realm.init(context)
            defaultConfig = buildConfig()
        }

        return Realm.getInstance(defaultConfig)
    }

    private fun buildConfig() : RealmConfiguration? {
        return RealmConfiguration.Builder()
                .schemaVersion(VERSION.toLong())
                .migration(migration)
                .build()
    }
}
