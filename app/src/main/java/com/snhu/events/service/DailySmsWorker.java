/**
 * Events Mobile Application
 *
 * File: DailySmsWorker.java
 *
 * Background service to send
 * SMS reminders even if the
 * app is closed
 *
 * Last Modified: 2026-02-22
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.service;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
        Context context = getApplicationContext();

        // Get User ID
        int userId = getInputData().getInt("USER_ID", -1);
        if (userId == -1) return Result.failure();

        // CHECK PREFERENCE: Did the user turn SMS OFF in the app?
        SharedPreferences prefs = context.getSharedPreferences("EventPrefs", Context.MODE_PRIVATE);
        boolean isSmsEnabledInApp = prefs.getBoolean("SMS_ENABLED_" + userId, false);

        if (!isSmsEnabledInApp) {
            // User disabled alerts; we "succeed" by doing nothing.
            return Result.success();
        }

        // CHECK SYSTEM PERMISSION: Does Android still allow us to send SMS?
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Have the preference ON, but no system permission.
            // Stop here to avoid a SecurityException.
            return Result.failure();
        }

        // Proceed with Database logic
        AppDatabase db = AppDatabase.getInstance(context);
        String today = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());

        List<Event> todayEvents = db.eventDao().getEventsByUserIdSync(userId, today);
        User user = db.userDao().getUserByIdSync(userId);

        if (user != null && user.phone != null && !todayEvents.isEmpty()) {
            StringBuilder sb = new StringBuilder("Today's Events:\n");
            for (Event e : todayEvents) {
                sb.append("- ").append(e.name).append(" at ").append(e.startTime).append("\n");
            }

            try {
                SmsManager smsManager = context.getSystemService(SmsManager.class);
                if (smsManager != null) {
                    smsManager.sendTextMessage(user.phone, null, sb.toString(), null, null);
                }
            } catch (Exception e) {
                // Networking/Signal issues might trigger a retry
                return Result.retry();
            }
        }

        return Result.success();
    }
}
