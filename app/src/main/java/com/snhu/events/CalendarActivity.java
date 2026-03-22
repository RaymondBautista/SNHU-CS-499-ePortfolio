/**
 * Events Mobile Application
 *
 * File: CalendarActivity.java
 *
 * Connects with the shared EventViewModel
 * of the MainActivity to retrieve the events filtered by day
 * showing them in a list below a calendar to provide users
 * a more intuitive perspective of their events
 *
 * Last Modified: 2026-03-22
 *
 * Author: Raymond Bautista
 */

package com.snhu.events;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.snhu.events.model.Event;
import com.snhu.events.ui.EventAdapter;
import com.snhu.events.ui.ListItem;
import com.snhu.events.viewmodel.EventViewModel;
import android.widget.CalendarView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    // Shared event view model with main activity
    private EventViewModel viewModel;
    // Event adapter instance to show them in the correct format in the recycler view
    private EventAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Retrieve user ID to show the correct elements
        int currentUserId = getSharedPreferences("EventPrefs", MODE_PRIVATE).getInt("USER_ID", -1);

        // Initialize UI
        RecyclerView rv = findViewById(R.id.rvDayEvents);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this);
        rv.setAdapter(adapter);

        // Get events from the ViewModel
        // Ensure Activity View only renders the elements and the ViewModel retrieves them from the DB
        viewModel = new ViewModelProvider(this).get(EventViewModel.class);
        viewModel.getEvents(currentUserId).observe(this, events -> {
            this.allEvents = events;
            updateListForDate(selectedDate); // Refresh if data changes
        });

        // Calendar Listener
        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Format to match DB string: yyyy/MM/dd
            selectedDate = String.format(Locale.getDefault(), "%04d/%02d/%02d", year, month + 1, dayOfMonth);
            updateListForDate(selectedDate);
        });

        setupNavigation();
    }

    private void updateListForDate(String date) {
        if (date == null) return;
        List<ListItem> dayItems = new ArrayList<>();
        for (Event e : allEvents) {
            if (e.date.equals(date)) {
                dayItems.add(new ListItem(e));
            }
        }
        // Using the adapter in a simplified mode (just one list)
        adapter.setData(dayItems, new ArrayList<>());
    }

    // Setup navigation bar interaction
    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_calendar);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddEditEventActivity.class));
                return true;
            }
            return true;
        });
    }

    @Override public void onEdit(Event event) { /* Reuse logic from MainActivity */ }
    @Override public void onDelete(Event event) { viewModel.deleteEvent(event); }
}