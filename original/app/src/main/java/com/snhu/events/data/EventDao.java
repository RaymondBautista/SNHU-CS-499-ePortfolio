/**
 * Events Mobile Application
 *
 * File: EventDao.java
 *
 * Data Access Object defines
 * the SQL queries for event
 * retrieving and handling.
 *
 * Last Modified: 2026-02-22
 * Version: 1.5.0
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

    // Create DAO Methods
    @Insert
    void insert(Event event);

    @Insert
    void insertAll(List<Event> events); // For range duplication

    // Update DAO Method
    @Update
    void update(Event event);

    // Delete DAO Method
    @Delete
    void delete(Event event);

    // Read DAO methods

    // Return events related to user in ascending time order
    @Query("SELECT * FROM events WHERE userId = :userId ORDER BY date ASC, startTime ASC")
    LiveData<List<Event>> getEventsForUser(int userId);

    // Return a single event by its id
    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    LiveData<Event> getEventById(int eventId);

    // Sync method for the SMS Worker
    @Query("SELECT * FROM events WHERE userId = :userId AND date = :date")
    List<Event> getEventsByUserIdSync(int userId, String date);
}
