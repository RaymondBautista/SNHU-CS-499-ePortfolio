/**
 * Events Mobile Application
 *
 * File: MainActivity.java
 *
 * Connects the main event list
 * activity layout to the
 * ViewModel to manage view
 * elements effectively
 *
 * Last Modified: 2026-02-22
 * Version: 2.5.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.snhu.events.model.Event;
import com.snhu.events.ui.EventAdapter;
import com.snhu.events.ui.ListItem;
import com.snhu.events.viewmodel.EventViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {
    private EventViewModel viewModel;
    private EventAdapter adapter;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve ID from SharedPreferences for persistence
        SharedPreferences prefs = getSharedPreferences("EventPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("USER_ID", -1);

        // Security Check: If no user found, kick back to login
        if (currentUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize view model
        viewModel = new ViewModelProvider(this).get(EventViewModel.class);

        // Setup Editable Header
        EditText editListName = findViewById(R.id.editListName);
        editListName.setText(viewModel.getListName());
        editListName.setOnEditorActionListener((v, actionId, event) -> {
            viewModel.saveListName(v.getText().toString());
            return false;
        });

        // Setup event list RecyclerView
        RecyclerView rv = findViewById(R.id.rvEvents);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this);
        rv.setAdapter(adapter);

        // Observe Data - The UI updates automatically when DB changes
        viewModel.getEvents(currentUserId).observe(this, events -> {
            adapter.setItems(processEventsForDisplay(events));
        });

        setupNavigation();
    }

    // Get the navigation bar currently selected button to display the correct screen
    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_sms) handleSmsAction();
            else if (id == R.id.nav_add) navigateToAddEvent();
            else if (id == R.id.nav_logout) showLogoutDialog();
            return true;
        });
    }

    // Implements on-screen dialog logic

    // SMS Dialog
    private void handleSmsAction() {

        // Read Android Manifest to check for permission
        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;

        // Create an alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layout = getLayoutInflater().inflate(R.layout.dialog_sms, null);
        builder.setView(layout);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView title = layout.findViewById(R.id.dialogTitle);
        TextView message = layout.findViewById(R.id.dialogMessage);
        Button btnPos = layout.findViewById(R.id.btnPositive);
        Button btnNeg = layout.findViewById(R.id.btnNegative);

        if (!hasPermission) {
            // If the user has not granted permission, then ask for it
            title.setText(R.string.sms_permission);
            btnPos.setText(R.string.allow_button);
            btnPos.setOnClickListener(v -> {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
                dialog.dismiss();
            });
        } else {
            // If the app already has permission, ask the user if wants to disable
            title.setText(R.string.sms_disable);
            message.setText(R.string.sms_disable_description);
            btnPos.setText(R.string.YES_button);
            btnNeg.setText(R.string.NO_button);
            btnPos.setOnClickListener(v -> {
                // Disable the SMS work manager task
                dialog.dismiss();
            });
        }
        btnNeg.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // Logout Dialog
    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layout = getLayoutInflater().inflate(R.layout.dialog_logout, null);
        builder.setView(layout);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        layout.findViewById(R.id.btnPositive).setOnClickListener(v -> {
            // Clear the session on logout
            getSharedPreferences("EventPrefs", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        layout.findViewById(R.id.btnNegative).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // Navigation and adapters callbacks

    // Navigate to the edit form if the user press over an activity
    @Override
    public void onEdit(Event event) {
        Intent intent = new Intent(this, AddEditEventActivity.class);
        intent.putExtra("EVENT_ID", event.id);
        startActivity(intent);
    }


    // Connects with ViewModel delete event functionality
    @Override
    public void onDelete(Event event) {
        viewModel.deleteEvent(event); // Triggers LiveData to refresh UI
    }

    // Set the navbar to events (home) after finishing a secondary screen interaction
    @Override
    protected void onResume() {
        super.onResume();
        // Ensure navbar stays on 'Home' when returning
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_home);
    }

    // Sends the user to Add Event form if the user press the button on the navigation bar
    private void navigateToAddEvent() {
        Intent intent = new Intent(this, AddEditEventActivity.class);
        startActivity(intent);
    }

    // Helper to group events by date
    private List<ListItem> processEventsForDisplay(List<Event> events) {
        List<ListItem> items = new ArrayList<>();
        String lastDate = "";
        String today = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());

        for (Event e : events) {
            if (!e.date.equals(lastDate)) {
                String header = e.date.equals(today) ? "Today" : formatDate(e.date);
                items.add(new ListItem(header));
                lastDate = e.date;
            }
            items.add(new ListItem(e));
        }
        return items;
    }

    private String formatDate(String raw) {
        try {
            Date d = new SimpleDateFormat("yyyy/MM/dd").parse(raw);
            return new SimpleDateFormat("EEE, MMM d").format(d);
        } catch (Exception ex) { return raw; }
    }
}