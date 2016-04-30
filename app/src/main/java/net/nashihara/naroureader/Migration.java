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
         class {@link net.nashihara.naroureader.entities.Novel4Realm}
         private String ncode;
         private String title;
         private String writer;
         private int bookmark;
         private boolean fav;

         // Version 1
         class {@link net.nashihara.naroureader.entities.Novel4Realm}
         private String ncode;
         private String title;
         private String writer;
         private int totalPage;
         private int bookmark;
         private boolean fav;
         ************************************************/
        // Migrate from version 0 to version 1
        if (oldVersion == 0) {
            // Novel4Realm に totalPage field を追加
            schema.get("Novel4Realm").addField("totalPage", int.class);

            oldVersion++;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Migration;
    }
}
