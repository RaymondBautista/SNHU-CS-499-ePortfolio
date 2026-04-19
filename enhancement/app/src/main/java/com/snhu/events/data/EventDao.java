/**
 * Events Mobile Application
 *
 * File: EventDao.java
 *
 * Data Access Object defines
 * the SQL queries for event
 * retrieving and handling.
 *
 * Last Modified: 2026-04-04
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.snhu.events.model.Event;
import java.util.List;

@Dao
public interface EventDao {

    // Events CRUD implementation

    // Create DAO Methods

    // Inserts a single event
    // ADDED: onConflict = OnConflictStrategy.REPLACE so Cloud syncs can safely overwrite local cache
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Event event);

    // Insert all events provided
    // ADDED: onConflict = OnConflictStrategy.REPLACE so Cloud syncs can safely overwrite local cache
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Event> events); // For range duplication and cloud sync

    // Update DAO Method
    @Update
    void update(Event event);

    // Delete DAO Methods
    @Delete
    void delete(Event event);

    // Required for the Repository to clear old local data before saving fresh Cloud data
    @Query("DELETE FROM events WHERE userId = :userId")
    void deleteAllForUser(String userId);

    // Read DAO methods

    // Return events related to user in ascending time order
    // CHANGED: int userId -> String userId
    @Query("SELECT * FROM events WHERE userId = :userId ORDER BY date ASC, startTime ASC")
    LiveData<List<Event>> getEventsForUser(String userId);

    // Return a single event by its id
    // CHANGED: int eventId -> String eventId
    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    LiveData<Event> getEventById(String eventId);

    // Sync method for the SMS Worker
    // CHANGED: int eventId -> String eventId
    @Query("SELECT * FROM events WHERE userId = :userId AND date = :date")
    List<Event> getEventsByUserIdSync(String userId, String date);
}
