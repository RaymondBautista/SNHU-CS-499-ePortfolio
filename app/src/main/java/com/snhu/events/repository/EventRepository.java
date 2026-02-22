/**
 * Events Mobile Application
 *
 * File: EventRepository.java
 *
 * Abstract the database calls, acting as
 * a bridge between ViewModels and the database
 *
 * Last Modified: 2026-02-21
 * Version: 1.0.0
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventRepository {
    private final EventDao eventDao;
    private final ExecutorService executorService;

    // Get database and DAO
    // Execute the service in a single background to prevent UI freezing
    public EventRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        eventDao = db.eventDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Event event) {
        executorService.execute(() -> eventDao.insert(event));
    }

    public void update(Event event) {
        executorService.execute(() -> eventDao.update(event));
    }

    public void delete(Event event) {
        executorService.execute(() -> eventDao.delete(event));
    }

    public LiveData<List<Event>> getEventsForUser(int userId) {
        return eventDao.getEventsByUserId(userId);
    }
}
