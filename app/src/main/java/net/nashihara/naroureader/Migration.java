package net.nashihara.naroureader;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
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

         // add new model
         class {@link net.nashihara.naroureader.entities.NovelTable4Realm}
         private int tableNumber;
         private String ncode;
         private int page;
         private String title;
         private boolean isChapter;
         ************************************************/
        // Migrate from version 0 to version 1
        if (oldVersion == 2) {
            // Novel4Realm に totalPage field を追加
            schema.get("Novel4Realm").addField("story", String.class);

            // add new realm object
            // Create a new class
            RealmObjectSchema novelTable = schema.create("Pet")
                    .addField("tableNumber", int.class)
                    .addField("ncode", String.class)
                    .addField("page", int.class)
                    .addField("title", String.class)
                    .addField("isChapter", boolean.class);

//                    .addField("name", String.class, FieldAttribute.REQUIRED)
//                    .addField("type", String.class, FieldAttribute.REQUIRED);

            oldVersion++;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Migration;
    }
}
