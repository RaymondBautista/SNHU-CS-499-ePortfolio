/**
 * Events Mobile Application
 *
 * File: AppDatabase.java
 *
 * Creates the physical SQLite internal database file
 * on the device using the Singleton pattern.
 *
 * Last Modified: 2026-02-21
 * Version: 2.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.data;
import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.snhu.events.model.Event;
import com.snhu.events.model.User;

@Database(entities = {User.class, Event.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract EventDao eventDao(); // Add this line

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "events_database")
                    .fallbackToDestructiveMigration() // Note: This clears DB on version change
                    .build();
        }
        return instance;
    }
}