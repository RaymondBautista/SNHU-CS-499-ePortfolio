/**
 * Events Mobile Application
 *
 * File: MfaSmsWorker.java
 *
 * Background service to send
 * SMS MFA codes on the login
 *
 * Last Modified: 2026-03-21
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.service;

import android.content.Context;
import android.telephony.SmsManager;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MfaSmsWorker extends Worker {
    public MfaSmsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        // Retrieve user's phon and MFA code
        String phone = getInputData().getString("PHONE");
        String code = getInputData().getString("MFA_CODE");

        // Fail if not available
        if (phone == null || code == null) return Result.failure();

        // Send MFA SMS to the user with an expiration of 5 minutes
        try {
            SmsManager smsManager = getApplicationContext().getSystemService(SmsManager.class);
            if (smsManager != null) {
                String message = "Your Events App security code is: " + code + ". It expires in 5 minutes.";
                smsManager.sendTextMessage(phone, null, message, null, null);
                return Result.success();
            }
        } catch (Exception e) {
            return Result.retry();
        }
        return Result.failure();
    }
}
