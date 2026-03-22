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
 * Last Modified: 2026-03-22
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

import java.util.ArrayList;
import java.util.List;

public class EventViewModel extends AndroidViewModel {
    private final EventRepository repository;
    private final SharedPreferences prefs;

    public EventViewModel(Application application) {
        super(application);
        repository = new EventRepository(application);
        prefs = application.getSharedPreferences("EventPrefs", Context.MODE_PRIVATE);
    }

    // Livedata to retrieve all events in real time
    public LiveData<List<Event>> getEvents(int userId) {
        return repository.getEventsForUser(userId);
    }

    // Filter events by a specific date
    public List<Event> filterEventsByDate(List<Event> allEvents, String targetDate) {
        List<Event> filteredResults = new ArrayList<>();

        // Safety check: if the list is null or no date is selected, return empty
        if (allEvents == null || targetDate == null) {
            return filteredResults;
        }

        for (Event event : allEvents) {
            if (event.date.equals(targetDate)) {
                filteredResults.add(event);
            }
        }
        return filteredResults;
    }

    public void deleteEvent(Event event) {
        repository.delete(event);
    }

    // List Name Preference logic
    public String getListName() { return prefs.getString("list_name", "My Event List"); }
    public void saveListName(String name) { prefs.edit().putString("list_name", name).apply(); }
}
