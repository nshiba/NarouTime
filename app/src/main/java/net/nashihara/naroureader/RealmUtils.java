package net.nashihara.naroureader;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmUtils {
    private static Migration migration = new Migration();
    private static RealmConfiguration defaultConfig;

    private static int VERSION = 0;

    public static Realm getRealm(Context context) {
        if (defaultConfig == null) {
            defaultConfig = getConfig(context);
        }

        return Realm.getInstance(defaultConfig);
    }

    private static RealmConfiguration getConfig(Context context) {
        defaultConfig = new RealmConfiguration.Builder(context)
                .schemaVersion(VERSION)
//                .migration(migration)
                .build();
        return defaultConfig;
    }

}
