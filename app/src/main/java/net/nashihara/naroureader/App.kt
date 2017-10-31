package net.nashihara.naroureader

import android.app.Application
import net.nashihara.naroureader.utils.RealmUtils

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        RealmUtils.init(context = applicationContext)
    }
}
