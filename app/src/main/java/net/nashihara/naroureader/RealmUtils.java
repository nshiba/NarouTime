package net.nashihara.naroureader;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmUtils {
    private static Migration migration = new Migration();
    private static RealmConfiguration defaultConfig;

    public static Realm getRealm(Context contenxt, int version) {
        if (defaultConfig == null) {
            defaultConfig = getConfig(contenxt, version);
        }

        return Realm.getInstance(defaultConfig);
    }

    private static RealmConfiguration getConfig(Context context, int version) {
        defaultConfig = new RealmConfiguration.Builder(context)
                .schemaVersion(version)
//                .migration(migration)
                .build();
        return defaultConfig;
    }

}
