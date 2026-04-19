/**
 * Events Mobile Application
 *
 * File: AppDatabase.java
 *
 * Creates the physical SQLite internal database file
 * on the device using the Singleton pattern.
 *
 * Last Modified: 2026-02-22
 * Version: 2.5.0
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Event.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;
    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract UserDao userDao();
    public abstract EventDao eventDao();

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