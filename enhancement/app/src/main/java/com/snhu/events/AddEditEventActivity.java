/**
 * Events Mobile Application
 *
 * File: AddEditEventActivity.java
 *
 * Connects the add / edit
 * event activity layout to
 * the ViewModel to handle
 * user activity effectively
 *
 * Last Modified: 2026-04-04
 *
 * Author: Raymond Bautista
 */

package com.snhu.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.snhu.events.model.Event;
import com.snhu.events.viewmodel.AddEditViewModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditEventActivity extends AppCompatActivity {

    // Activity ViewModel instance to communicate with model data layer
    private AddEditViewModel viewModel;

    // Form text input fields
    private TextInputEditText editName, editDesc, editStartD, editEndD, editStartT, editEndT;

    // View elements and action buttons
    private View sectionEndDate;
    private TextView txtHeader;
    private Button btnAction;

    // Changed from Int to String
    private String userId, eventId;

    // Flag to determine if the form should be on add or edit mode
    private boolean isEditMode = false;
    private Event currentEvent;
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);

        viewModel = new ViewModelProvider(this).get(AddEditViewModel.class);

        // Load User ID from Prefs and Event ID from Intent
        userId = getSharedPreferences("EventPrefs", MODE_PRIVATE).getString("USER_ID", null);
        eventId = getIntent().getStringExtra("EVENT_ID");
        isEditMode = (eventId != null);

        initViews();    // Initialize UI elements
        setupPickers(); // Initialize date and time pickers

        // Select which elements should be on screen based on the mode flag
        if (isEditMode) prepareEditMode();
        else prepareAddMode();

        // Action buttons listeners
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        btnAction.setOnClickListener(v -> handleAction());
    }

    // Initialize UI elements
    private void initViews() {
        editName = findViewById(R.id.editEventName);
        editDesc = findViewById(R.id.editEventDesc);
        editStartD = findViewById(R.id.editStartDate);
        editEndD = findViewById(R.id.editEndDate);
        editStartT = findViewById(R.id.editStartTime);
        editEndT = findViewById(R.id.editEndTime);
        sectionEndDate = findViewById(R.id.sectionEndDate);
        txtHeader = findViewById(R.id.txtPageHeader);
        btnAction = findViewById(R.id.btnAction);
    }

    // Prepare the screen to show only the necessary elements to edit
    private void prepareEditMode() {
        txtHeader.setText(R.string.edit_event_title);
        btnAction.setText(R.string.update_button);
        sectionEndDate.setVisibility(View.GONE);    // Hide the end date field

        // Get selected event data by its id and display it on each field
        viewModel.getEvent(eventId).observe(this, event -> {
            if (event != null) {
                currentEvent = event;
                editName.setText(event.name);
                editDesc.setText(event.description);
                editStartD.setText(event.date);
                editStartT.setText(event.startTime);
                editEndT.setText(event.endTime);
            }
        });
    }

    // Prepare the screen to show only the necessary elements to add
    private void prepareAddMode() {
        txtHeader.setText(R.string.add_event_title);
        btnAction.setText(R.string.save_button);    // Change button text to "SAVE"
        sectionEndDate.setVisibility(View.VISIBLE); // Ensure end date field is visible
    }

    // Setup date and time picker UI elements and their listeners
    private void setupPickers() {
        editStartD.setOnClickListener(v -> showDatePicker(editStartD));
        editEndD.setOnClickListener(v -> showDatePicker(editEndD));
        editStartT.setOnClickListener(v -> showTimePicker(editStartT));
        editEndT.setOnClickListener(v -> showTimePicker(editEndT));
    }

    // Shows a calendar date picker modern view and parse text to required format
    private void showDatePicker(TextInputEditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            target.setText(String.format(Locale.getDefault(), "%d/%02d/%02d", y, m + 1, d));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Shows a clock time picker modern view and parse text to required format
    private void showTimePicker(TextInputEditText target) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, h, m) -> {
            Calendar temp = Calendar.getInstance();
            temp.set(Calendar.HOUR_OF_DAY, h);
            temp.set(Calendar.MINUTE, m);
            target.setText(timeFmt.format(temp.getTime()));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
    }

    // Handle user input data elements for correct insertion and update
    private void handleAction() {
        String name = editName.getText().toString().trim();
        String sD = editStartD.getText().toString().trim();
        String eD = editEndD.getText().toString().trim();
        String sT = editStartT.getText().toString().trim();
        String eT = editEndT.getText().toString().trim();

        // Basic Null Validation. Ask the user to fill all required elements
        if (name.isEmpty() || sD.isEmpty() || sT.isEmpty() || eT.isEmpty()) {
            Toast.makeText(this, "Required fields are empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Logic Validation (Date and Time)
        try {
            Date startDate = dateFmt.parse(sD);
            Date startTime = timeFmt.parse(sT);
            Date endTime = timeFmt.parse(eT);

            if (!isEditMode && !eD.isEmpty()) {
                Date endDate = dateFmt.parse(eD);
                if (endDate.before(startDate)) {
                    Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // If same day, check time
            if (endTime.before(startTime) || endTime.equals(startTime)) {
                Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (ParseException e) {
            Toast.makeText(this, "Invalid Date/Time format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve data and call correct viewModel method based on current status flag
        if (userId == null) {
            Toast.makeText(this, "Error: User session lost. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        if (isEditMode) {
            currentEvent.name = name;
            currentEvent.description = editDesc.getText().toString();
            currentEvent.date = sD;
            currentEvent.startTime = sT;
            currentEvent.endTime = eT;
            viewModel.updateEvent(currentEvent);
        } else {
            viewModel.saveEvents(userId, name, editDesc.getText().toString(), sD, eD, sT, eT);
        }

        finish(); // Returns to MainActivity
    }
}