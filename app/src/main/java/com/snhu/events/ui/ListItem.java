/**
 * Events Mobile Application
 *
 * File: ListItem.java
 *
 * Wrapper helper class to
 * transform the database list
 *
 * Last Modified: 2026-03-22
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.ui;

import com.snhu.events.model.Event;

public class ListItem {

    // Header type constants
    public static final int TYPE_HEADER = 0;        // Dates or today
    public static final int TYPE_EVENT = 1;         // Event header
    public static final int TYPE_COLLAPSIBLE = 2;   // Dropdown row

    public int type;
    public String headerDate;
    public Event event;

    public ListItem(String date) { this.type = TYPE_HEADER; this.headerDate = date; }
    public ListItem(Event event) { this.type = TYPE_EVENT; this.event = event; }
    public ListItem(int type) { this.type = type; } // For the collapsible row9
}
