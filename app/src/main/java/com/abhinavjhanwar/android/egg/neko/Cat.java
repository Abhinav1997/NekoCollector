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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.abhinavjhanwar.android.egg.R;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Cat extends Drawable {
    private static final long[] PURR = {0, 40, 20, 40, 20, 40, 20, 40, 20, 40, 20, 40};

    private Random mNotSoRandom;
    private Bitmap mBitmap;
    private long mSeed;
    private String mName;
    private int mBodyColor;

    private synchronized Random notSoRandom(long seed) {
        if (mNotSoRandom == null) {
            mNotSoRandom = new Random();
            mNotSoRandom.setSeed(seed);
        }
        return mNotSoRandom;
    }

    private static float frandrange(Random r) {
        float a = 0.5f;
        float b = 1f;
        return (b - a) * r.nextFloat() + a;
    }

    private static Object choose(Random r, Object... l) {
        return l[r.nextInt(l.length)];
    }

    private static int chooseP(Random r, int[] a) {
        int pct = r.nextInt(1000);
        final int stop = a.length - 2;
        int i = 0;
        while (i < stop) {
            pct -= a[i];
            if (pct < 0) break;
            i += 2;
        }
        return a[i + 1];
    }

    private static final int[] P_BODY_COLORS = {
            180, 0xFF212121, // black
            180, 0xFFFFFFFF, // white
            140, 0xFF616161, // gray
            140, 0xFF795548, // brown
            100, 0xFF90A4AE, // steel
            100, 0xFFFFF9C4, // buff
            100, 0xFFFF8F00, // orange
            5, 0xFF29B6F6, // blue..?
            5, 0xFFFFCDD2, // pink!?
            5, 0xFFCE93D8, // purple?!?!?
            4, 0xFF43A047, // yeah, why not green
            1, 0,          // ?!?!?!
    };

    private static final int[] P_COLLAR_COLORS = {
            250, 0xFFFFFFFF,
            250, 0xFF000000,
            250, 0xFFF44336,
            50, 0xFF1976D2,
            50, 0xFFFDD835,
            50, 0xFFFB8C00,
            50, 0xFFF48FB1,
            50, 0xFF4CAF50,
    };

    private static final int[] P_BELLY_COLORS = {
            750, 0,
            250, 0xFFFFFFFF,
    };

    private static final int[] P_DARK_SPOT_COLORS = {
            700, 0,
            250, 0xFF212121,
            50, 0xFF6D4C41,
    };

    private static final int[] P_LIGHT_SPOT_COLORS = {
            700, 0,
            300, 0xFFFFFFFF,
    };

    private final CatParts D;

    private static void tint(int color, Drawable... ds) {
        for (Drawable d : ds) {
            if (d != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    d.mutate().setTint(color);
                } else {
                    DrawableCompat.setTint(DrawableCompat.wrap(d).mutate(), color);
                }
            }
        }
    }

    private static boolean isDark(int color) {
        final int r = (color & 0xFF0000) >> 16;
        final int g = (color & 0x00FF00) >> 8;
        final int b = color & 0x0000FF;
        return (r + g + b) < 0x80;
    }

    public Cat(Context context, long seed, List<Cat> mCats) {
        D = new CatParts(context);
        mSeed = seed;
        long check = seed;
        for (int i = 0; i < mCats.size(); i++) {
            while (context.getString(R.string.default_cat_name,
                    String.valueOf(check).substring(0, 3)).equals(mCats.get(i).getName())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    check = Math.abs(ThreadLocalRandom.current().nextInt());
                } else {
                    check = Math.abs(new Random().nextInt());
                }
            }
        }

        setName(context.getString(R.string.default_cat_name,
                String.valueOf(check).substring(0, 3)));

        final Random nsr = notSoRandom(seed);

        // body color
        mBodyColor = chooseP(nsr, P_BODY_COLORS);
        if (mBodyColor == 0) mBodyColor = Color.HSVToColor(new float[]{
                nsr.nextFloat() * 360f, frandrange(nsr), frandrange(nsr)});

        tint(mBodyColor, D.body, D.head, D.leg1, D.leg2, D.leg3, D.leg4, D.tail,
                D.leftEar, D.rightEar, D.foot1, D.foot2, D.foot3, D.foot4, D.tailCap);
        tint(0x20000000, D.leg2Shadow, D.tailShadow);
        if (isDark(mBodyColor)) {
            tint(0xFFFFFFFF, D.leftEye, D.rightEye, D.mouth, D.nose);
        }
        tint(isDark(mBodyColor) ? 0xFFEF9A9A : 0x20D50000, D.leftEarInside, D.rightEarInside);

        tint(chooseP(nsr, P_BELLY_COLORS), D.belly);
        tint(chooseP(nsr, P_BELLY_COLORS), D.back);
        final int faceColor = chooseP(nsr, P_BELLY_COLORS);
        tint(faceColor, D.faceSpot);
        if (!isDark(faceColor)) {
            tint(0xFF000000, D.mouth, D.nose);
        }

        if (nsr.nextFloat() < 0.25f) {
            tint(0xFFFFFFFF, D.foot1, D.foot2, D.foot3, D.foot4);
        } else {
            if (nsr.nextFloat() < 0.25f) {
                tint(0xFFFFFFFF, D.foot1, D.foot2);
            } else if (nsr.nextFloat() < 0.25f) {
                tint(0xFFFFFFFF, D.foot3, D.foot4);
            } else if (nsr.nextFloat() < 0.1f) {
                tint(0xFFFFFFFF, (Drawable) choose(nsr, D.foot1, D.foot2, D.foot3, D.foot4));
            }
        }

        tint(nsr.nextFloat() < 0.333f ? 0xFFFFFFFF : mBodyColor, D.tailCap);

        final int capColor = chooseP(nsr, isDark(mBodyColor) ? P_LIGHT_SPOT_COLORS : P_DARK_SPOT_COLORS);
        tint(capColor, D.cap);
        //tint(chooseP(nsr, isDark(bodyColor) ? P_LIGHT_SPOT_COLORS : P_DARK_SPOT_COLORS), D.nose);

        final int collarColor = chooseP(nsr, P_COLLAR_COLORS);
        tint(collarColor, D.collar);
        tint((nsr.nextFloat() < 0.1f) ? collarColor : 0, D.bowtie);
    }

    public static Cat create(Context context) {
        PrefState prefs = new PrefState(context);
        List<Cat> cats = prefs.getCats();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new Cat(context, Math.abs(ThreadLocalRandom.current().nextInt()), cats);
        }
        return new Cat(context, Math.abs(new Random().nextInt()), cats);
    }

    public Notification.Builder buildNotification(Context context) {
        final Bundle extras = new Bundle();
        extras.putString("android.substName", context.getString(R.string.app_name));
        final Intent intent = new Intent(Intent.ACTION_MAIN)
                .setClass(context, NekoLand.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new Notification.Builder(context)
                    .setSmallIcon(Icon.createWithResource(context, R.drawable.stat_icon))
                    .setLargeIcon(createLargeIcon(context))
                    .setColor(getBodyColor())
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setContentTitle(NekoService.notificationText)
                    .setShowWhen(true)
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setContentText(getName())
                    .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                    .setAutoCancel(true)
                    .setVibrate(PURR)
                    .addExtras(extras);
        }
        return new Notification.Builder(context)
                .setSmallIcon(R.drawable.stat_icon)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentTitle(OldService.notificationText)
                .setContentText(getName())
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                .setAutoCancel(true)
                .setVibrate(PURR);
    }

    public NotificationCompat.Builder buildNotificationbelowM(Context context) {
        final Bundle extras = new Bundle();
        extras.putString("android.substName", context.getString(R.string.app_name));
        final Intent intent = new Intent(Intent.ACTION_MAIN)
                .setClass(context, NekoLand.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.stat_icon)
                .setContentTitle(OldService.notificationText)
                .setContentText(getName())
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                .setAutoCancel(true)
                .setVibrate(PURR);
    }


    public long getSeed() {
        return mSeed;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final int hw = Math.min(canvas.getWidth(), canvas.getHeight());

        if (mBitmap == null || mBitmap.getWidth() != hw || mBitmap.getHeight() != hw) {
            mBitmap = Bitmap.createBitmap(hw, hw, Bitmap.Config.ARGB_8888);
            final Canvas bitCanvas = new Canvas(mBitmap);
            slowDraw(bitCanvas, 0, 0, hw, hw);
        }
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    private void slowDraw(Canvas canvas, int x, int y, int w, int h) {
        for (int i = 0; i < D.drawingOrder.length; i++) {
            final Drawable d = D.drawingOrder[i];
            if (d != null) {
                d.setBounds(x, y, x + w, y + h);
                d.draw(canvas);
            }
        }

    }

    public Bitmap createBitmap(int w, int h) {
        if (mBitmap != null && mBitmap.getWidth() == w && mBitmap.getHeight() == h) {
            return mBitmap.copy(mBitmap.getConfig(), true);
        }
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        slowDraw(new Canvas(result), 0, 0, w, h);
        return result;
    }

    public Bitmap createLargeBitmap(Context context) {
        final Resources res = context.getResources();
        final int w = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        final int h = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);

        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(result);
        final Paint pt = new Paint();
        float[] hsv = new float[3];
        Color.colorToHSV(mBodyColor, hsv);
        hsv[2] = (hsv[2] > 0.5f)
                ? (hsv[2] - 0.25f)
                : (hsv[2] + 0.25f);
        pt.setColor(Color.HSVToColor(hsv));
        pt.setFlags(Paint.ANTI_ALIAS_FLAG);
        float r = w / 2;
        canvas.drawCircle(r, r, r, pt);
        int m = w / 10;

        slowDraw(canvas, m, m, w - m - m, h - m - m);

        return result;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public Icon createLargeIcon(Context context) {
        return Icon.createWithBitmap(createLargeBitmap(context));
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    private int getBodyColor() {
        return mBodyColor;
    }

    public static class CatParts {
        public final Drawable leftEar;
        public final Drawable rightEar;
        public final Drawable rightEarInside;
        public final Drawable leftEarInside;
        public final Drawable head;
        public final Drawable faceSpot;
        public final Drawable cap;
        public final Drawable mouth;
        public final Drawable body;
        public final Drawable foot1;
        public final Drawable leg1;
        public final Drawable foot2;
        public final Drawable leg2;
        public final Drawable foot3;
        public final Drawable leg3;
        public final Drawable foot4;
        public final Drawable leg4;
        public final Drawable tail;
        public final Drawable leg2Shadow;
        public final Drawable tailShadow;
        public final Drawable tailCap;
        public final Drawable belly;
        public final Drawable back;
        public final Drawable rightEye;
        public final Drawable leftEye;
        public final Drawable nose;
        public final Drawable bowtie;
        public final Drawable collar;
        public final Drawable[] drawingOrder;

        public CatParts(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                body = context.getDrawable(R.drawable.body);
                head = context.getDrawable(R.drawable.head);
                leg1 = context.getDrawable(R.drawable.leg1);
                leg2 = context.getDrawable(R.drawable.leg2);
                leg3 = context.getDrawable(R.drawable.leg3);
                leg4 = context.getDrawable(R.drawable.leg4);
                tail = context.getDrawable(R.drawable.tail);
                leftEar = context.getDrawable(R.drawable.left_ear);
                rightEar = context.getDrawable(R.drawable.right_ear);
                rightEarInside = context.getDrawable(R.drawable.right_ear_inside);
                leftEarInside = context.getDrawable(R.drawable.left_ear_inside);
                faceSpot = context.getDrawable(R.drawable.face_spot);
                cap = context.getDrawable(R.drawable.cap);
                mouth = context.getDrawable(R.drawable.mouth);
                foot4 = context.getDrawable(R.drawable.foot4);
                foot3 = context.getDrawable(R.drawable.foot3);
                foot1 = context.getDrawable(R.drawable.foot1);
                foot2 = context.getDrawable(R.drawable.foot2);
                leg2Shadow = context.getDrawable(R.drawable.leg2_shadow);
                tailShadow = context.getDrawable(R.drawable.tail_shadow);
                tailCap = context.getDrawable(R.drawable.tail_cap);
                belly = context.getDrawable(R.drawable.belly);
                back = context.getDrawable(R.drawable.back);
                rightEye = context.getDrawable(R.drawable.right_eye);
                leftEye = context.getDrawable(R.drawable.left_eye);
                nose = context.getDrawable(R.drawable.nose);
                collar = context.getDrawable(R.drawable.collar);
                bowtie = context.getDrawable(R.drawable.bowtie);
            } else {
                body = context.getResources().getDrawable(R.drawable.body);
                head = context.getResources().getDrawable(R.drawable.head);
                leg1 = context.getResources().getDrawable(R.drawable.leg1);
                leg2 = context.getResources().getDrawable(R.drawable.leg2);
                leg3 = context.getResources().getDrawable(R.drawable.leg3);
                leg4 = context.getResources().getDrawable(R.drawable.leg4);
                tail = context.getResources().getDrawable(R.drawable.tail);
                leftEar = context.getResources().getDrawable(R.drawable.left_ear);
                rightEar = context.getResources().getDrawable(R.drawable.right_ear);
                rightEarInside = context.getResources().getDrawable(R.drawable.right_ear_inside);
                leftEarInside = context.getResources().getDrawable(R.drawable.left_ear_inside);
                faceSpot = context.getResources().getDrawable(R.drawable.face_spot);
                cap = context.getResources().getDrawable(R.drawable.cap);
                mouth = context.getResources().getDrawable(R.drawable.mouth);
                foot4 = context.getResources().getDrawable(R.drawable.foot4);
                foot3 = context.getResources().getDrawable(R.drawable.foot3);
                foot1 = context.getResources().getDrawable(R.drawable.foot1);
                foot2 = context.getResources().getDrawable(R.drawable.foot2);
                leg2Shadow = context.getResources().getDrawable(R.drawable.leg2_shadow);
                tailShadow = context.getResources().getDrawable(R.drawable.tail_shadow);
                tailCap = context.getResources().getDrawable(R.drawable.tail_cap);
                belly = context.getResources().getDrawable(R.drawable.belly);
                back = context.getResources().getDrawable(R.drawable.back);
                rightEye = context.getResources().getDrawable(R.drawable.right_eye);
                leftEye = context.getResources().getDrawable(R.drawable.left_eye);
                nose = context.getResources().getDrawable(R.drawable.nose);
                collar = context.getResources().getDrawable(R.drawable.collar);
                bowtie = context.getResources().getDrawable(R.drawable.bowtie);
            }
            drawingOrder = getDrawingOrder();
        }

        private Drawable[] getDrawingOrder() {
            return new Drawable[]{
                    collar,
                    leftEar, leftEarInside, rightEar, rightEarInside,
                    head,
                    faceSpot,
                    cap,
                    leftEye, rightEye,
                    nose, mouth,
                    tail, tailCap, tailShadow,
                    foot1, leg1,
                    foot2, leg2,
                    foot3, leg3,
                    foot4, leg4,
                    leg2Shadow,
                    body, belly,
                    bowtie
            };
        }
    }
}