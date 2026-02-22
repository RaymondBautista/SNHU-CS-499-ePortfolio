/**
 * Events Mobile Application
 *
 * File: EventDao.java
 *
 * Data Access Object defines
 * the SQL queries for event
 * retrieving and handling.
 *
 * Last Modified: 2026-02-21
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.snhu.events.model.Event;
import java.util.List;

@Dao
public interface EventDao {

    // Events CRUD implementation

    // Create DAO Method
    @Insert
    void insert(Event event);

    // Update DAO Method
    @Update
    void update(Event event);

    // Delete DAO Method
    @Delete
    void delete(Event event);

    // Read DAO methods

    // Get all events for a specific user, ordered by date and time
    @Query("SELECT * FROM events WHERE userId = :userId ORDER BY date ASC, startTime ASC")
    LiveData<List<Event>> getEventsByUserId(int userId);

    // Specifically for the "Modify" feature: find a single event by ID
    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    Event getEventById(int eventId);

    // SMS Worker: Synchronous fetch for a specific user and date
    @Query("SELECT * FROM events WHERE userId = :userId AND date = :date ORDER BY startTime ASC")
    List<Event> getEventsByUserIdSync(int userId, String date);
}
