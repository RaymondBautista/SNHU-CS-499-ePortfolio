/**
 * Events Mobile Application
 *
 * File: ListItem.java
 *
 * Wrapper helper class to
 * transform the database list
 *
 * Last Modified: 2026-02-21
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.ui;

import com.snhu.events.model.Event;

public class ListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_EVENT = 1;

    public int type;
    public String headerDate;
    public Event event;

    public ListItem(String date) { this.type = TYPE_HEADER; this.headerDate = date; }
    public ListItem(Event event) { this.type = TYPE_EVENT; this.event = event; }
}
