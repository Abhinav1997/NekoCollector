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

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abhinavjhanwar.android.egg.R;

import java.util.ArrayList;

public class NekoDialog extends Dialog {

    public NekoDialog(@NonNull Context context) {
        super(context, NekoLand.DIALOG_THEME);
        RecyclerView view = new RecyclerView(getContext());
        Adapter mAdapter = new Adapter(getContext());
        view.setLayoutManager(new GridLayoutManager(getContext(), 2));
        view.setAdapter(mAdapter);
        final float dp = context.getResources().getDisplayMetrics().density;
        final int pad = (int) (16 * dp);
        view.setPadding(pad, pad, pad, pad);
        setContentView(view);
    }

    public void selectFood(Food food) {
        PrefState prefs = new PrefState(getContext());
        int currentState = prefs.getFoodState();

        if (currentState == 0 && food.getType() != 0) {
            prefs.setTimeInterval(food.getInterval(getContext()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                NekoService.registerJob(getContext(), food.getInterval(getContext()));
            } else {
                getContext().startService(new Intent(getContext(), OldService.class));
            }
        }
        prefs.setFoodState(food.getType());
    }

    private void onFoodSelected(Food food, int position) {
        selectFood(food);
        Intent intent = new Intent(getContext().getApplicationContext(), NekoLand.class)
                .putExtra("action", NekoLand.SHORTCUT_ACTION_SET_FOOD)
                .putExtra("food", position + 1);
        getContext().startActivity(intent);
        dismiss();
        new NekoShortcuts(getContext()).updateShortcuts();
    }

    private class Adapter extends RecyclerView.Adapter<Holder> {

        private final Context mContext;
        private final ArrayList<Food> mFoods = new ArrayList<>();

        public Adapter(Context context) {
            mContext = context;
            int[] foods = context.getResources().getIntArray(R.array.food_names);
            // skip food 0, you can't choose it
            for (int i = 1; i < foods.length; i++) {
                mFoods.add(new Food(i));
            }
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.food_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(final Holder holder, int position) {
            final Food food = mFoods.get(position);
            ((ImageView) holder.itemView.findViewById(R.id.icon))
                    .setImageResource(food.getIcon(mContext));
            ((TextView) holder.itemView.findViewById(R.id.text))
                    .setText(food.getName(mContext));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFoodSelected(mFoods.get(holder.getAdapterPosition()), holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFoods.size();
        }
    }

    public static class Holder extends RecyclerView.ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }
    }
}