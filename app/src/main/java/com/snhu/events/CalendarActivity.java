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

import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    // Shared event view model with main activity
    private EventViewModel viewModel;
    // Event adapter instance to show them in the correct format in the recycler view
    private EventAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private String selectedDate;
    // Shows when there are not events scheduled on a specific day
    private TextView txtEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialize selected date as Today by default
        selectedDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
        txtEmptyState = findViewById(R.id.txtEmptyState);
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
            updateListForDate(); // Refresh if data changes
        });

        // Calendar Listener
        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Format to match DB string: yyyy/MM/dd
            selectedDate = String.format(Locale.getDefault(), "%04d/%02d/%02d", year, month + 1, dayOfMonth);
            updateListForDate();
        });

        setupNavigation();
    }

    private void updateListForDate() {
        List<Event> filtered = viewModel.filterEventsByDate(allEvents, selectedDate);

        if (filtered.isEmpty()) {
            txtEmptyState.setVisibility(View.VISIBLE);
            adapter.setData(new ArrayList<>(), new ArrayList<>()); // Clear adapter
        } else {
            txtEmptyState.setVisibility(View.GONE);
            // By passing filtered to 'upcoming' and an empty list to 'past',
            // the adapter never renders the collapsible toggle.
            List<ListItem> items = new ArrayList<>();
            for (Event e : filtered) items.add(new ListItem(e));
            adapter.setData(items, new ArrayList<>());
        }
    }

    // Setup navigation bar interaction
    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_calendar);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                finish(); // Go back to Main
            } else if (id == R.id.nav_add) {
                // Redirect to the Add screen
                startActivity(new Intent(this, AddEditEventActivity.class));
            } else if (id == R.id.nav_search) {
                // Redirect to the Search screen
                startActivity(new Intent(this, SearchActivity.class));
            }
            else if (id == R.id.nav_sms || id == R.id.btnLogout) {
                // Redirect: Tell MainActivity which dialog to open
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("TRIGGER_ACTION", id);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            return true;
        });
    }

    @Override public void onEdit(Event e) {
        Intent intent = new Intent(this, AddEditEventActivity.class);
        intent.putExtra("EVENT_ID", e.id);
        startActivity(intent);
    }

    @Override public void onDelete(Event e) { viewModel.deleteEvent(e); }
}