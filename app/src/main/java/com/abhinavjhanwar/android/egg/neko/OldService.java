package com.abhinavjhanwar.android.egg.neko;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.abhinavjhanwar.android.egg.R;

import java.util.List;
import java.util.Random;

public class OldService extends Service {

    private static final String TAG = "NekoService";

    public static int CAT_NOTIFICATION = 1;

    public static float CAT_CAPTURE_PROB = 1.0f; // generous

    public static long SECONDS = 1000;
    public static long MINUTES = 60 * SECONDS;

    public static float INTERVAL_JITTER_FRAC = 0.25f;

    public static String notificationText = "A cat is here.";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Context context = this;
        final PrefState prefs = new PrefState(this);
        long intervalMinutes = prefs.getTimeInterval();
        long interval = intervalMinutes * MINUTES;
        long jitter = (long) (INTERVAL_JITTER_FRAC * interval);
        interval += (long) (Math.random() * (2 * jitter)) - jitter;
        Log.d(TAG, "A cat will visit in " + interval + "ms: ");
        Runnable mRunnable;
        Handler mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                NotificationManager noman = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                int food = prefs.getFoodState();
                if (food != 0) {
                    prefs.setFoodState(0); // nom
                    final Random rng = new Random();
                    if (rng.nextFloat() <= CAT_CAPTURE_PROB) {
                        Cat cat;
                        List<Cat> cats = prefs.getCats();
                        final int[] probs = getResources().getIntArray(R.array.food_new_cat_prob);
                        final float new_cat_prob = (float) ((food < probs.length) ? probs[food] : 50) / 100f;

                        if (cats.size() == 0 || rng.nextFloat() <= new_cat_prob) {
                            cat = Cat.create(context);
                            prefs.addCat(cat);
                            notificationText = "A new cat is here.";
                            prefs.setCatReturns(false);
                            Log.v(TAG, "A new cat is here: " + cat.getName());
                        } else {
                            cat = cats.get(rng.nextInt(cats.size()));
                            notificationText = "A cat has returned.";
                            prefs.setCatReturns(true);
                            Log.v(TAG, "A cat has returned: " + cat.getName());
                        }

                        final NotificationCompat.Builder builder = cat.buildNotificationbelowM(context);
                        Log.v(TAG, "Creating notification");
                        noman.notify(CAT_NOTIFICATION, builder.build());
                    }
                }
            }
        };
        mHandler.postDelayed(mRunnable, interval);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved()");
        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(),
                1,
                restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);
    }
}