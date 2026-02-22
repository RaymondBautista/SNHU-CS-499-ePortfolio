/**
 * Events Mobile Application
 *
 * File: EventRepository.java
 *
 * Abstract the database calls, acting as
 * a bridge between ViewModels and the database
 *
 * Last Modified: 2026-02-22
 * Version: 1.5.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.snhu.events.data.AppDatabase;
import com.snhu.events.data.EventDao;
import com.snhu.events.model.Event;
import java.util.List;

public class EventRepository {
    private final EventDao eventDao;

    public EventRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        eventDao = db.eventDao();
    }

    public LiveData<List<Event>> getEventsForUser(int userId) {
        return eventDao.getEventsForUser(userId);
    }

    public LiveData<Event> getEventById(int id) {
        return eventDao.getEventById(id);
    }

    public void insertAll(List<Event> events) {
        AppDatabase.databaseWriteExecutor.execute(() -> eventDao.insertAll(events));
    }

    public void update(Event event) {
        AppDatabase.databaseWriteExecutor.execute(() -> eventDao.update(event));
    }

    public void delete(Event event) {
        AppDatabase.databaseWriteExecutor.execute(() -> eventDao.delete(event));
    }
}
