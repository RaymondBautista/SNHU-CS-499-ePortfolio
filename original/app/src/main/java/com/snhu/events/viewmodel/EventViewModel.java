/**
 * Events Mobile Application
 *
 * File: EventViewModel.java
 *
 * Handles the logic of the
 * user's list name and event
 * deletion process on the main
 * event list screen.
 *
 * Last Modified: 2026-02-21
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.snhu.events.model.Event;
import com.snhu.events.repository.EventRepository;
import java.util.List;

public class EventViewModel extends AndroidViewModel {
    private final EventRepository repository;
    private final SharedPreferences prefs;

    public EventViewModel(Application application) {
        super(application);
        repository = new EventRepository(application);
        prefs = application.getSharedPreferences("EventPrefs", Context.MODE_PRIVATE);
    }

    public LiveData<List<Event>> getEvents(int userId) {
        return repository.getEventsForUser(userId);
    }

    public void deleteEvent(Event event) {
        repository.delete(event);
    }

    // List Name Preference logic
    public String getListName() { return prefs.getString("list_name", "My Event List"); }
    public void saveListName(String name) { prefs.edit().putString("list_name", name).apply(); }
}
