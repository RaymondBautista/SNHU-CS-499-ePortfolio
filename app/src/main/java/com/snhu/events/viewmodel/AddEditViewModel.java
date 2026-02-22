/**
 * Events Mobile Application
 *
 * File: AddEditViewModel.java
 *
 * Handles the logic of the
 * events creation and modification
 * in the database, as well as bulk
 * insertions
 *
 * Last Modified: 2026-02-22
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.snhu.events.model.Event;
import com.snhu.events.repository.EventRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditViewModel extends AndroidViewModel {
    private final EventRepository repository;   // Event table repository instance
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

    public AddEditViewModel(@NonNull Application application) {
        super(application);
        repository = new EventRepository(application);
    }

    public LiveData<Event> getEvent(int id) {
        return repository.getEventById(id);
    }

    public void saveEvents(int userId, String name, String desc, String startD, String endD, String startT, String endT) {

        // Get a list of events to save if the user specify start and end dates

        List<Event> eventsToSave = new ArrayList<>();
        try {
            Date startDate = sdf.parse(startD);
            // If no end date, just use start date
            Date endDate = (endD == null || endD.isEmpty()) ? startDate : sdf.parse(endD);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            while (!calendar.getTime().after(endDate)) {
                String currentDate = sdf.format(calendar.getTime());
                eventsToSave.add(new Event(name, desc, currentDate, startT, endT, userId));
                calendar.add(Calendar.DATE, 1);
            }
            repository.insertAll(eventsToSave);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void updateEvent(Event event) {
        repository.update(event);
    }
}
