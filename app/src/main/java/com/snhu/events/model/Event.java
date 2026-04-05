/**
 * Events Mobile Application
 *
 * File: Event.java
 *
 * Defines the structure of the events table.
 *
 * Last Modified: 2026-04-04
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.model;

import androidx.annotation.NonNull;
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

    // Change from auto-incremental int ID to String Firestore ID
    @PrimaryKey
    @NonNull
    public String id;

    public String name;        // Obligatory
    public String description; // Optional
    public String date;        // YYYY/MM/DD - Obligatory
    public String startTime;   // HH:MM - Obligatory
    public String endTime;     // HH:MM - Obligatory

    // Changed to String ID to match Firestore UID
    @NonNull
    public String userId;         // Foreign Key

    // Empty constructor for Firestore mapping
    public Event() {}

    public Event(@NonNull String id, @NonNull String userId, String name, String description, String date, String startTime, String endTime) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}