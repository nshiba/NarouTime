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
         private int bookmark;
         private boolean fav;
         private int totalPage;
         ************************************************/
        // Migrate from version 0 to version 1
        if (oldVersion == 0) {
            // Novel4Realm に totalPage field を追加
            schema.get("Novel4Realm").addField("totalPage", int.class);

            oldVersion++;
        }

        /************************************************
         // Version 1
         class {@link net.nashihara.naroureader.entities.Novel4Realm}
         private String ncode;
         private String title;
         private String writer;
         private int bookmark;
         private boolean fav;
         private int totalPage;

         // Version 2
         class {@link net.nashihara.naroureader.entities.Novel4Realm}
         private String ncode;
         private String title;
         private String writer;
         private int bookmark;
         private boolean fav;
         private int totalPage;
         private boolean isDownload;
         ************************************************/
        // Migrate from version 0 to version 1
        if (oldVersion == 1) {
            // Novel4Realm に totalPage field を追加
            schema.get("Novel4Realm").addField("isDownload", boolean.class);

            oldVersion++;
        }


        /************************************************
         // Version 2
         class {@link net.nashihara.naroureader.entities.Novel4Realm}
         private String ncode;
         private String title;
         private String writer;
         private int bookmark;
         private boolean fav;
         private int totalPage;
         private boolean isDownload;

         // Version 3
         class {@link net.nashihara.naroureader.entities.Novel4Realm}
         private String ncode;
         private String title;
         private String writer;
         private String story;
         private int bookmark;
         private boolean fav;
         private int totalPage;
         private boolean isDownload = false;
         ************************************************/
        // Migrate from version 0 to version 1
        if (oldVersion == 2) {
            // Novel4Realm に totalPage field を追加
            schema.get("Novel4Realm").addField("story", String.class);

            oldVersion++;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Migration;
    }
}
