/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.abhinavjhanwar.android.egg.neko;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.abhinavjhanwar.android.egg.R;

import java.util.List;
import java.util.Locale;
import java.util.Random;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NekoService extends JobService {

    private static final String TAG = "NekoService";

    public static int JOB_ID = 43;

    public static int CAT_NOTIFICATION = 1;

    public static float CAT_CAPTURE_PROB = 1.0f; // generous

    public static long SECONDS = 1000;
    public static long MINUTES = 60 * SECONDS;

    public static float INTERVAL_JITTER_FRAC = 0.25f;

    public static String notificationText;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.v(TAG, "Starting job: " + String.valueOf(params));

        notificationText = getString(R.string.cat_notif_default);
        NotificationManager noman;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            noman = getSystemService(NotificationManager.class);
        } else {
            noman = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (NekoLand.DEBUG_NOTIFICATIONS) {
            final Bundle extras = new Bundle();
            extras.putString("android.substName", getString(R.string.app_name));
            final Cat cat = Cat.create(this);
            final Notification.Builder builder
                    = cat.buildNotification(this)
                        .setContentTitle("DEBUG")
                        .setContentText("Ran job: " + params);
            noman.notify(1, builder.build());
        }

        final PrefState prefs = new PrefState(this);
        int food = prefs.getFoodState();
        if (food != 0) {
            prefs.setFoodState(0); // nom
            final Random rng = new Random();
            if (rng.nextFloat() <= CAT_CAPTURE_PROB) {
                Cat cat;
                List<Cat> cats = prefs.getCats();
                final int[] probs = getResources().getIntArray(R.array.food_new_cat_prob);
                final float new_cat_prob = (float)((food < probs.length) ? probs[food] : 50) / 100f;

                if (cats.size() == 0 || rng.nextFloat() <= new_cat_prob) {
                    cat = Cat.create(this);
                    prefs.addCat(cat);
                    notificationText = getString(R.string.cat_notif_new);
                    prefs.setCatReturns(false);
                    Log.v(TAG, "A new cat is here: " + cat.getName());
                } else {
                    cat = cats.get(rng.nextInt(cats.size()));
                    notificationText = getString(R.string.cat_notif_return);
                    prefs.setCatReturns(true);
                    Log.v(TAG, "A cat has returned: " + cat.getName());
                }

                final Notification.Builder builder = cat.buildNotification(this);
                noman.notify(CAT_NOTIFICATION, builder.build());
            }
        }
        cancelJob(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    public static void registerJob(Context context, long intervalMinutes) {
        JobScheduler jss;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jss = context.getSystemService(JobScheduler.class);
        } else {
            jss = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        jss.cancel(JOB_ID);
        long interval = intervalMinutes * MINUTES;
        long jitter = (long)(INTERVAL_JITTER_FRAC * interval);
        interval += (long)(Math.random() * (2 * jitter)) - jitter;

        final JobInfo jobInfo = new JobInfo.Builder(JOB_ID,
                new ComponentName(context, NekoService.class))
                .setMinimumLatency(interval)
                .build();

        Log.v(TAG, "A cat will visit in " + interval + "ms: " + String.valueOf(jobInfo));
        jss.schedule(jobInfo);

        if (NekoLand.DEBUG_NOTIFICATIONS) {
            NotificationManager noman;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                noman = context.getSystemService(NotificationManager.class);
            } else {
                noman = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            }
            noman.notify(500, new Notification.Builder(context)
                    .setSmallIcon(R.drawable.stat_icon)
                    .setContentTitle(String.format(Locale.US, "Job scheduled in %d min", (interval / MINUTES)))
                    .setContentText(String.valueOf(jobInfo))
                    .setPriority(Notification.PRIORITY_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setShowWhen(true)
                    .build());
        }
    }

    public static void cancelJob(Context context) {
        JobScheduler jss;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jss = context.getSystemService(JobScheduler.class);
        } else {
            jss = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        Log.v(TAG, "Canceling job");
        jss.cancel(JOB_ID);
    }
}
