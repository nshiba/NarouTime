package net.nashihara.naroureader.utils;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmUtils {
    private static Migration migration = new Migration();
    private static RealmConfiguration defaultConfig;

    private static int VERSION = 3;

    public static Realm getRealm(Context context) {
        if (defaultConfig == null) {
            Realm.init(context);
            defaultConfig = getConfig();
        }

        return Realm.getInstance(defaultConfig);
    }

    private static RealmConfiguration getConfig() {
        defaultConfig = new RealmConfiguration.Builder()
            .schemaVersion(VERSION)
            .migration(migration)
            .build();
        return defaultConfig;
    }

}
