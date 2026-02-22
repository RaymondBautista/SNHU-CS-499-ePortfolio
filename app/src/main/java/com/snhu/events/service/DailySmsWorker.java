/**
 * Events Mobile Application
 *
 * File: DailySmsWorker.java
 *
 * Background service to send
 * SMS reminders even if the
 * app is closed
 *
 * Last Modified: 2026-02-21
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.service;

import android.content.Context;
import android.telephony.SmsManager;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.snhu.events.data.AppDatabase;
import com.snhu.events.model.Event;
import com.snhu.events.model.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailySmsWorker extends Worker {

    public DailySmsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get User ID from the task data
        int userId = getInputData().getInt("USER_ID", -1);
        if (userId == -1) return Result.failure();

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        String today = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());

        // Fetch data synchronously (Blocking calls)
        List<Event> todayEvents = db.eventDao().getEventsByUserIdSync(userId, today);
        User user = db.userDao().getUserByIdSync(userId);

        // Logic to send SMS if events exist and user has a phone number
        if (user != null && user.phone != null && !todayEvents.isEmpty()) {
            StringBuilder sb = new StringBuilder("Today's Events:\n");
            for (Event e : todayEvents) {
                sb.append("- ").append(e.name).append(" at ").append(e.startTime).append("\n");
            }

            try {
                // Using the modern (2026 style) SmsManager retrieval
                SmsManager smsManager = getApplicationContext().getSystemService(SmsManager.class);
                smsManager.sendTextMessage(user.phone, null, sb.toString(), null, null);
            } catch (Exception e) {
                return Result.retry(); // Try again later if network is down
            }
        }

        return Result.success();
    }
}
