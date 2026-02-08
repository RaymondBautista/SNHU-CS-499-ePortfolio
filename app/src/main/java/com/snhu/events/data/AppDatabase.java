/**
 * Events Mobile Application
 *
 * File: AppDatabase.java
 *
 * Creates the physical SQLite internal database file
 * on the device using the Singleton pattern.
 *
 * Last Modified: 2026-02-08
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.data;
import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.snhu.events.model.User;

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "events_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
