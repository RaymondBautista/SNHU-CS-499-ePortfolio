/**
 * Events Mobile Application
 *
 * File: EventRepository.java
 *
 * Abstract the database calls, acting as
 * a bridge between ViewModels and the database
 *
 * Last Modified: 2026-04-04
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.snhu.events.data.AppDatabase;
import com.snhu.events.data.EventDao;
import com.snhu.events.model.Event;
import java.util.List;

public class EventRepository {
    private final EventDao eventDao;

    // Included Firestore Instance
    private final FirebaseFirestore firestore;

    public EventRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        eventDao = db.eventDao();
        firestore = FirebaseFirestore.getInstance(); // Initialize Firestore
    }

    // Hybrid-Cloud Sync: listens to the Firestore and updates local Room automatically
    public void startRealtimeSync(String userId) {
        firestore.collection("events")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<Event> cloudEvents = value.toObjects(Event.class);
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        eventDao.deleteAllForUser(userId); // Clear old local cache
                        eventDao.insertAll(cloudEvents);   // Save fresh cloud data
                    });
                });
    }

    // Retrieves the events of an specific user
    // CHANGED: int userId -> String userId
    public LiveData<List<Event>> getEventsForUser(String userId) {
        return eventDao.getEventsForUser(userId);
    }

    // Call DAO instance to retrieve specific event by ID
    // CHANGED: int userId -> String userId
    public LiveData<Event> getEventById(String id) {
        return eventDao.getEventById(id);
    }

    // Perform a single insertion on the cloud
    public void insert(Event event) {
        if (event.id == null || event.id.isEmpty()) {
            event.id = firestore.collection("events").document().getId();
        }
        firestore.collection("events").document(event.id).set(event);
    }

    // Perform massive insertions using Firestore WriteBatch
    public void insertAll(List<Event> events) {
        WriteBatch batch = firestore.batch(); // Create a batch operation

        for (Event event : events) {
            // Generate IDs for new events
            if (event.id == null || event.id.isEmpty()) {
                event.id = firestore.collection("events").document().getId();
            }

            DocumentReference docRef = firestore.collection("events").document(event.id);
            batch.set(docRef, event); // Add to batch
        }

        // Commit the batch to the Cloud.
        // Once successful, the SnapshotListener will update the local Room DB automatically.
        batch.commit();
    }

    // Push updates of a single event to Cloud and local Room DB
    // Cloud First
    public void update(Event event) {
        firestore.collection("events").document(event.id).set(event);
        AppDatabase.databaseWriteExecutor.execute(() -> eventDao.update(event));
    }

    // Delete a single event from Cloud and local Room DB
    // Cloud First
    public void delete(Event event) {
        firestore.collection("events").document(event.id).delete();
        AppDatabase.databaseWriteExecutor.execute(() -> eventDao.delete(event));
    }
}
