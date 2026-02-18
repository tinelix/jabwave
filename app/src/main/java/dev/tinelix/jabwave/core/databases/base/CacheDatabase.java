package dev.tinelix.jabwave.core.databases.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class CacheDatabase {
    public static String prefix = "";

    public static class CacheOpenHelper extends SQLiteOpenHelper {

        public CacheOpenHelper(Context ctx, String dbName) {
            super(ctx, dbName, null, 1);

        }

        public CacheOpenHelper(
                @Nullable Context context,
                @Nullable String name,
                @Nullable SQLiteDatabase.CursorFactory factory,
                int version
        ) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
