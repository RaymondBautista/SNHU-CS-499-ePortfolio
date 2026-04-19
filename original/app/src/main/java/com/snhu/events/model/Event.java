/**
 * Events Mobile Application
 *
 * File: Event.java
 *
 * Defines the structure of the events table.
 *
 * Last Modified: 2026-02-21
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Set the User table id as a foreign key for this model
// Apply cascading to delete the associated events if an user is deleted
@Entity(tableName = "events",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")})
public class Event {

    // Event table fields

    // Set id as auto-incremental primary key
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;        // Obligatory
    public String description; // Optional
    public String date;        // YYYY/MM/DD - Obligatory
    public String startTime;   // HH:MM - Obligatory
    public String endTime;     // HH:MM - Obligatory
    public int userId;         // Foreign Key

    public Event(String name, String description, String date, String startTime, String endTime, int userId) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userId = userId;
    }
}