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
 * Last Modified: 2026-04-04
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

    // Kicks off the background Firestore listener to sync Cloud data into Room
    public void startCloudSync(String userId) {
        repository.startRealtimeSync(userId);
    }

    // Retrieves all events in real time from the Local Room DB (which is kept fresh by startCloudSync)
    public LiveData<List<Event>> getEvents(String userId) {
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
