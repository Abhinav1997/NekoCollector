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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PrefState implements OnSharedPreferenceChangeListener {

    private static final String FILE_NAME = "mPrefs";

    private static final String FOOD_STATE = "food";

    private static final String TIME_INTERVAL = "interval";

    private static final String CHECKBOX_SHOW_STATE = "donotshow";

    private static final String CAT_RETURN_STATE = "cat_returns";

    private static final String CAT_KEY_PREFIX = "cat:";

    private final Context mContext;
    private final SharedPreferences mPrefs;
    private PrefsListener mListener;

    public PrefState(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(FILE_NAME, 0);
    }

    // Can also be used for renaming.
    public void addCat(Cat cat) {
        mPrefs.edit()
                .putString(CAT_KEY_PREFIX + String.valueOf(cat.getSeed()), cat.getName())
                .apply();
    }

    public void removeCat(Cat cat) {
        mPrefs.edit()
                .remove(CAT_KEY_PREFIX + String.valueOf(cat.getSeed()))
                .apply();
    }

    public List<Cat> getCats() {
        ArrayList<Cat> cats = new ArrayList<>();
        Map<String, ?> map = mPrefs.getAll();
        for (String key : map.keySet()) {
            if (key.startsWith(CAT_KEY_PREFIX)) {
                long seed = Long.parseLong(key.substring(CAT_KEY_PREFIX.length()));
                Cat cat = new Cat(mContext, seed, cats);
                cat.setName(String.valueOf(map.get(key)));
                cats.add(cat);
            }
        }
        return cats;
    }

    public int getFoodState() {
        return mPrefs.getInt(FOOD_STATE, 0);
    }

    public void setFoodState(int foodState) {
        mPrefs.edit().putInt(FOOD_STATE, foodState).apply();
    }

    public void setDoNotShow(boolean doNotShow) {
        mPrefs.edit().putBoolean(CHECKBOX_SHOW_STATE, doNotShow).apply();
    }

    public boolean getDoNotShow() {
        return mPrefs.getBoolean(CHECKBOX_SHOW_STATE, false);
    }

    public void setCatReturns(boolean catReturns) {
        mPrefs.edit().putBoolean(CAT_RETURN_STATE, catReturns).apply();
    }

    public boolean getCatReturns() {
        return mPrefs.getBoolean(CAT_RETURN_STATE, false);
    }

    public long getTimeInterval() {
        return mPrefs.getLong(TIME_INTERVAL, 30);
    }

    public void setTimeInterval(long timeInterval) {
        mPrefs.edit().putLong(TIME_INTERVAL, timeInterval).apply();
    }

    public void setListener(PrefsListener listener) {
        mListener = listener;
        if (mListener != null) {
            mPrefs.registerOnSharedPreferenceChangeListener(this);
        } else {
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mListener.onPrefsChanged();
    }

    public interface PrefsListener {
        void onPrefsChanged();
    }
}
