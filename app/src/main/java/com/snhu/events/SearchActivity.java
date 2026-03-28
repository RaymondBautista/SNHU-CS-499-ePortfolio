/**
 * Events Mobile Application
 *
 * File: SearchActivity.java
 *
 * Connects with the shared EventViewModel
 * of the MainActivity to retrieve the events raw data
 * and the SearchViewModel of this screen to show the filtered
 * events in ascending chronological order based on user
 * search input
 *
 * Last Modified: 2026-03-28
 *
 * Author: Raymond Bautista
 */

package com.snhu.events;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.snhu.events.model.Event;
import com.snhu.events.ui.EventAdapter;
import com.snhu.events.ui.ListItem;
import com.snhu.events.viewmodel.EventViewModel;
import com.snhu.events.viewmodel.SearchViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private SearchViewModel searchViewModel;

    /**
     * Reuse this ViewModel to connect to the database and retrieve
     * the raw data to be filtered in this class ViewModel
     */
    private EventViewModel eventViewModel;
    private EventAdapter adapter;
    private TextView txtNoResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        txtNoResults = findViewById(R.id.txtNoResults);

        // Setup ViewModels
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        // Setup Event List RecyclerView
        RecyclerView rv = findViewById(R.id.rvSearchResults);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this);
        rv.setAdapter(adapter);

        // Observe the raw data from DB using EventViewModel
        // Pass events to SearchViewModel's internal Map
        int userId = getSharedPreferences("EventPrefs", MODE_PRIVATE).getInt("USER_ID", -1);
        eventViewModel.getEvents(userId).observe(this, events -> {
            // Refactored: We send data to the search cache instead of keeping a local list
            searchViewModel.updateRawData(events);
        });

        // Observe the search results from SearchViewModel
        searchViewModel.getSearchResults().observe(this, filteredEvents -> {
            if (filteredEvents.isEmpty()) {
                // Show message if there are not search results
                txtNoResults.setVisibility(View.VISIBLE);
                adapter.setData(new ArrayList<>(), new ArrayList<>());
            } else {
                txtNoResults.setVisibility(View.GONE);
                // Convert to ListItems with Date Headers
                adapter.setData(processEventsForDisplay(filteredEvents), new ArrayList<>());
            }
        });

        // Search Bar Input Listener
        // Re-trigger the search immediately so the UI removes the deleted item
        EditText editSearch = findViewById(R.id.editSearchQuery);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Only pass the query string and the ViewModel handles the rest
                searchViewModel.performSearch(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        setupNavigation();
    }

    // Setup navigation bar interaction
    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_search);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish(); // Go back to Main
            } else if (id == R.id.nav_add) {
                // Redirect to Add Screen
                startActivity(new Intent(this, AddEditEventActivity.class));
            } else if (id == R.id.nav_calendar) {
                // Redirect to Calendar Screen
                startActivity(new Intent(this, CalendarActivity.class));
            } else if (id == R.id.nav_sms) {
                // Redirect: Tell MainActivity which dialog to open
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("TRIGGER_ACTION", id);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            return true;
        });
    }

    // Callbacks for the Adapter
    @Override
    public void onEdit(Event event) {
        Intent intent = new Intent(this, AddEditEventActivity.class);
        intent.putExtra("EVENT_ID", event.id);
        startActivity(intent);
    }

    // Reset the navbar once this screen is resumed
    @Override
    protected void onResume() {
        super.onResume();
        /* Reset the navigation bar to highlight this activity icon,
         * whenever the user returns to this screen from the Add/Edit screen.
         */
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        if (nav != null) {
            nav.setSelectedItemId(R.id.nav_search);
        }
    }

    // Allow user to delete a searched event from this screen
    @Override
    public void onDelete(Event event) {
        // Delete the event from the database in a background thread
        eventViewModel.deleteEvent(event);

        // Optimistic UI Approach: Instant O(1) removal in UI (Search Cache)
        searchViewModel.deleteEventOptimistically(event.id);
    }

    // Helper to add Date Headers (same logic as MainActivity)
    private List<ListItem> processEventsForDisplay(List<Event> events) {
        List<ListItem> items = new ArrayList<>();
        String lastDate = "";
        for (Event e : events) {
            if (!e.date.equals(lastDate)) {
                items.add(new ListItem(formatDate(e.date)));
                lastDate = e.date;
            }
            items.add(new ListItem(e));
        }
        return items;
    }

    private String formatDate(String raw) {
        try {
            Date d = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(raw);
            return new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(d);
        } catch (Exception ex) { return raw; }
    }
}