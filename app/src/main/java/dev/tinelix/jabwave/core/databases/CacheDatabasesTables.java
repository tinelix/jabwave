package dev.tinelix.jabwave.core.databases;

import android.database.sqlite.SQLiteDatabase;

public class CacheDatabasesTables {
    public static void createUsersTables(
            SQLiteDatabase db,
            boolean allowPhoneNumbers,
            boolean allowHomePages,
            boolean allowLocations,
            boolean allowBio) {
        db.execSQL("DROP TABLE IF EXISTS `contacts`");

        String sql_query =
                "CREATE TABLE `users` (" +
                    "id bigint primary key not null, " +
                    "user_id bigint not null," +
                    "user_name varchar(255)," +
                    "first_name varchar(64) not null," +
                    "last_name varchar(64)," +
                    "sur_name varchar(64)," +
                    "avatar_url varchar(384)," +
                    "avatar_cached_url varchar(384)," +
                    "birthdate bigint";

        if(allowPhoneNumbers)
            sql_query += ", phone varchar(18)";
        if(allowHomePages)
            sql_query += ", homepage varchar(255)";
        if(allowLocations)
            sql_query += ", address varchar(255)";
        if(allowBio)
            sql_query += ", bio varchar(512)";

        sql_query += ")";

        db.execSQL(sql_query);
    }

    public static void createChannelsTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE `channels` (" +
                        "id bigint primary key not null, " +
                        "channel_id bigint not null," +
                        "channel_name varchar(255)," +
                        "title varchar(64) not null," +
                        "avatar_url varchar(384)," +
                        "avatar_cached_url varchar(384)," +
                        "birthdate bigint," +
                        "subscribers_count bigint not null" +
                ")"
        );
    }
}
