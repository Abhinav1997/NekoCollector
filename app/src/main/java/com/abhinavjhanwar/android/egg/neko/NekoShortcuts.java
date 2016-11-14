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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.abhinavjhanwar.android.egg.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergey Sova <i.am@lestad.net> on 06.11.2016.
 */

public class NekoShortcuts {
    private final Context mContext;

    public NekoShortcuts(Context context) {
        mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    public void updateShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = mContext.getSystemService(ShortcutManager.class);

            final PrefState prefs = new PrefState(mContext);
            int currentFoodState = prefs.getFoodState();
            final List<ShortcutInfo> shortcuts = new ArrayList<>();
            final int mFoodCount = mContext.getResources().getStringArray(R.array.food_names).length;

            if (currentFoodState == 0) {
                for (int i = 1; i < mFoodCount; i++) {
                    final Food food = new Food(i);

                    final Intent action = new Intent(mContext, NekoLand.class)
                            .setAction(Intent.ACTION_VIEW)
                            .putExtra("action", NekoLand.SHORTCUT_ACTION_SET_FOOD)
                            .putExtra("food", i);

                    final ShortcutInfo shortcut = new ShortcutInfo.Builder(mContext, "food" + i)
                            .setShortLabel(food.getName(mContext))
                            .setLongLabel(food.getName(mContext))
                            .setIcon(Icon.createWithBitmap(getDarkIcon(mContext, food.getIcon(mContext))))
                            .setIntent(action)
                            .build();
                    shortcuts.add(shortcut);
                }
            } else {
                // Add current
                final Food currentFood = new Food(currentFoodState);
                final Intent currentActionIntent = new Intent(mContext, NekoLand.class)
                        .setAction(Intent.ACTION_VIEW)
                        .putExtra("action", NekoLand.SHORTCUT_ACTION_OPEN_SELECTOR);
                final ShortcutInfo currentFoodShortcut = new ShortcutInfo.Builder(mContext, "current")
                        .setShortLabel(currentFood.getName(mContext))
                        .setLongLabel(mContext.getString(R.string.current_dish).replace("%s", currentFood.getName(mContext)))
                        .setIcon(Icon.createWithBitmap(getDarkIcon(mContext, currentFood.getIcon(mContext))))
                        .setIntent(currentActionIntent)
                        .build();
                final Intent emptyActionIntent = new Intent(mContext, NekoLand.class)
                        .setAction(Intent.ACTION_VIEW)
                        .putExtra("action", NekoLand.SHORTCUT_ACTION_SET_FOOD_EMPTY);
                final ShortcutInfo emptyFoodShortcut = new ShortcutInfo.Builder(mContext, "empty")
                        .setShortLabel(mContext.getResources().getString(R.string.empty_dish))
                        .setLongLabel(mContext.getResources().getString(R.string.empty_dish))
                        .setIcon(Icon.createWithBitmap(getDarkIcon(mContext, R.drawable.food_dish)))
                        .setIntent(emptyActionIntent)
                        .build();
                shortcuts.add(emptyFoodShortcut);
                shortcuts.add(currentFoodShortcut);
            }

            shortcutManager.setDynamicShortcuts(shortcuts);
        }
    }

    //Credits: http://stackoverflow.com/a/35347960
    private static Bitmap getDarkIcon(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable != null) {
            drawable.setColorFilter(0xFF616161, PorterDuff.Mode.MULTIPLY);
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }
}