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

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.abhinavjhanwar.android.egg.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NekoLand extends AppCompatActivity implements PrefState.PrefsListener {
    public static boolean DEBUG_NOTIFICATIONS = false;

    private static final int STORAGE_PERM_REQUEST = 123;

    public static int DIALOG_THEME;

    private static boolean CAT_GEN = false;
    private PrefState mPrefs;
    private CatAdapter mAdapter;
    private Cat mPendingShareCat;
    public static ImageView imageView;
    public static TextView textView, closeAppTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DIALOG_THEME = android.R.style.Theme_Material_Dialog_NoActionBar;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            DIALOG_THEME = android.R.style.Theme_Holo_Dialog_NoActionBar;
        } else {
            DIALOG_THEME = android.R.style.Theme_Dialog;
        }
        setContentView(R.layout.neko_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPrefs = new PrefState(this);
        mPrefs.setListener(this);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.holder);
        imageView = (ImageView) findViewById(R.id.food_icon);
        textView = (TextView) findViewById(R.id.food);
        closeAppTextView = (TextView) findViewById(R.id.close_app);
        recyclerView.setNestedScrollingEnabled(false);
        final NekoDialog nekoDialog = new NekoDialog(this);
        final int[] foodState = {mPrefs.getFoodState()};
        Food food = new Food(foodState[0]);
        textView.setText(food.getName(this));
        imageView.setImageResource(food.getIcon(this));
        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                foodState[0] = mPrefs.getFoodState();
                if (foodState[0] == 0) {
                    nekoDialog.show();
                } else {
                    mPrefs.setFoodState(0);
                    textView.setText(getResources().getString(R.string.empty_dish));
                    imageView.setImageResource(R.drawable.food_dish);
                    closeAppTextView.setVisibility(View.GONE);
                }
            }
        });
        mAdapter = new CatAdapter();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        updateCats();
        recyclerView.setFocusable(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrefs.setListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater findMenuItems = getMenuInflater();
        findMenuItems.inflate(R.menu.about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent aboutIntent = new Intent(NekoLand.this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateCats() {
        Cat[] cats;
        if (CAT_GEN) {
            cats = new Cat[50];
            for (int i = 0; i < cats.length; i++) {
                cats[i] = Cat.create(this);
            }
        } else {
            cats = mPrefs.getCats().toArray(new Cat[0]);
        }
        mAdapter.setCats(cats);

        if (mPrefs.getFoodState() == 0) {
            textView.setText(getResources().getString(R.string.empty_dish));
            imageView.setImageResource(R.drawable.food_dish);
            closeAppTextView.setVisibility(View.GONE);
        }
    }

    private void onCatClick(Cat cat) {
        if (CAT_GEN) {
            mPrefs.addCat(cat);
            new AlertDialog.Builder(NekoLand.this)
                    .setTitle("Cat added")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } else {
            showNameDialog(cat);
        }
//      noman.notify(1, cat.buildNotification(NekoLand.this).build());
    }

    private void onCatRemove(Cat cat) {
        mPrefs.removeCat(cat);
    }

    private void showNameDialog(final Cat cat) {
        Context context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context = new ContextThemeWrapper(this,
                    android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                context = new ContextThemeWrapper(this,
                        android.R.style.Theme_Holo_Dialog_NoActionBar);
        } else {
            context = new ContextThemeWrapper(this,
                    android.R.style.Theme_Dialog);
        }
        // TODO: Move to XML, add correct margins.
        final ViewGroup nullParent = null;
        View view = LayoutInflater.from(context).inflate(R.layout.edit_text, nullParent);
        final EditText text = (EditText) view.findViewById(android.R.id.edit);
        text.setText(cat.getName());
        text.setSelection(cat.getName().length());
        Drawable catIcon;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            catIcon = cat.createLargeIcon(this).loadDrawable(this);
        } else {
            catIcon = new BitmapDrawable(getResources(), cat.createLargeBitmap(this));
        }
        //Drawable catIcon = cat.createLargeIcon(this).loadDrawable(this);
        new AlertDialog.Builder(context)
                .setTitle(" ")
                .setIcon(catIcon)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cat.setName(text.getText().toString().trim());
                        mPrefs.addCat(cat);
                    }
                }).show();
    }

    @Override
    public void onPrefsChanged() {
        updateCats();
    }

    private class CatAdapter extends RecyclerView.Adapter<CatHolder> {

        private Cat[] mCats;

        public void setCats(Cat[] cats) {
            mCats = cats;
            notifyDataSetChanged();
        }

        @Override
        public CatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CatHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cat_view, parent, false));
        }

        @Override
        public void onBindViewHolder(final CatHolder holder, int position) {
            Context context = holder.itemView.getContext();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.imageView.setImageIcon(mCats[position].createLargeIcon(context));
            } else {
                holder.imageView.setImageBitmap(mCats[position].createLargeBitmap(context));
            }
            holder.textView.setText(mCats[position].getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCatClick(mCats[holder.getAdapterPosition()]);
                }
            });
            holder.itemView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    holder.contextGroup.removeCallbacks((Runnable) holder.contextGroup.getTag());
                    holder.contextGroup.setVisibility(View.VISIBLE);
                    Runnable hideAction = new Runnable() {
                        @Override
                        public void run() {
                            holder.contextGroup.setVisibility(View.INVISIBLE);
                        }
                    };
                    holder.contextGroup.setTag(hideAction);
                    holder.contextGroup.postDelayed(hideAction, 5000);
                    return true;
                }
            });
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.contextGroup.setVisibility(View.INVISIBLE);
                    holder.contextGroup.removeCallbacks((Runnable) holder.contextGroup.getTag());
                    onCatRemove(mCats[holder.getAdapterPosition()]);
                }
            });
            holder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Cat cat = mCats[holder.getAdapterPosition()];
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            mPendingShareCat = cat;
                            requestPermissions(
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    STORAGE_PERM_REQUEST);
                            return;
                        }
                    }
                    shareCat(cat);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mCats.length;
        }
    }

    private void shareCat(Cat cat) {
        final File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                getString(R.string.directory_name));
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e("NekoLand", "save: error: can't create Pictures directory");
            return;
        }
        final File png = new File(dir, cat.getName().replaceAll("[/ #:]+", "_") + ".png");
        Bitmap bitmap = cat.createBitmap(512, 512);
        if (bitmap != null) {
            try {
                OutputStream os = new FileOutputStream(png);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
                os.close();
                MediaScannerConnection.scanFile(
                        this,
                        new String[] {png.toString()},
                        new String[] {"image/png"},
                        null);
                Uri uri = Uri.fromFile(png);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, cat.getName());
                intent.setType("image/png");
                startActivity(Intent.createChooser(intent, null));
            } catch (IOException e) {
                Log.e("NekoLand", "save: error: " + e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERM_REQUEST) {
            if (mPendingShareCat != null) {
                shareCat(mPendingShareCat);
                mPendingShareCat = null;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mPrefs.getCatReturns() && !mPrefs.getDoNotShow()) {
            getReturnDialog(this);
        }
        if(mPrefs.getFoodState() == 0) {
            textView.setText(getResources().getString(R.string.empty_dish));
            imageView.setImageResource(R.drawable.food_dish);
            closeAppTextView.setVisibility(View.GONE);
        }
    }

    public void getReturnDialog(Context context) {
        View checkBoxView = View.inflate(this, R.layout.checkbox, null);
        CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.skip_dialog);
        float dpi = context.getResources().getDisplayMetrics().density;
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mPrefs.setDoNotShow(true);
                } else {
                    mPrefs.setDoNotShow(false);
                }
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("A cat has returned")
                .setMessage("Looks like an old cat has returned. Try different food for getting unique cats.")
                .setPositiveButton("OK", null)
                .create();
        dialog.setView(checkBoxView, (int)(19*dpi), (int)(5*dpi), (int)(14*dpi), (int)(5*dpi));
        dialog.show();
    }

    private static class CatHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textView;
        private final View contextGroup;
        private final View delete;
        private final View share;

        public CatHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(android.R.id.icon);
            textView = (TextView) itemView.findViewById(android.R.id.title);
            contextGroup = itemView.findViewById(R.id.contextGroup);
            delete = itemView.findViewById(android.R.id.closeButton);
            share = itemView.findViewById(R.id.shareText);
        }
    }
}
