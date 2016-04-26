package net.nashihara.naroureader;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class Migration implements RealmMigration {
    private static final String TAG = Migration.class.getSimpleName();

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        /************************************************
         // Version 0
         class {@link net.nashihara.naroureader.entities.NovelBody4Realm}
         private int page;
         private String ncode;
         private String title;
         private String body;
         ************************************************/
        // Migrate from version 0 to version 1
        if (oldVersion == 0) {
            oldVersion++;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Migration;
    }
}
