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
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
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
import android.view.Gravity;
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
import android.widget.Toast;

import com.abhinavjhanwar.android.egg.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NekoLand extends AppCompatActivity implements PrefState.PrefsListener {
    public static final boolean DEBUG_NOTIFICATIONS = false;

    private static final int STORAGE_PERM_REQUEST = 123;

    public static int DIALOG_THEME;

    private static final boolean CAT_GEN = false;
    private PrefState mPrefs;
    private CatAdapter mAdapter;
    private Cat mPendingShareCat;
    public static ImageView imageView;
    public static TextView textView, closeAppTextView;

    public static final int SHORTCUT_ACTION_SET_FOOD = 0xf001;
    public static final int SHORTCUT_ACTION_OPEN_SELECTOR = 0xf002;

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

        handleShortcutIntent(getIntent());
        new NekoShortcuts(this).updateShortcuts();
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
        List<Cat> cats;
        if (CAT_GEN) {
            cats = new ArrayList<>(50);
            for (int i = 0; i < cats.size(); i++) {
                cats.add(Cat.create(this));
            }
        } else {
            cats = new ArrayList<>(Arrays.asList(mPrefs.getCats().toArray(new Cat[0])));
        }
        Collections.sort(cats, new Comparator<Cat>() {
            @Override
            public int compare(Cat cat, Cat t1) {
                return  cat.getName().compareTo(t1.getName());
            }
        });
        mAdapter.setCats(cats);

        if (mPrefs.getFoodState() == 0) {
            textView.setText(getResources().getString(R.string.empty_dish));
            imageView.setImageResource(R.drawable.food_dish);
            closeAppTextView.setVisibility(View.GONE);
            new NekoShortcuts(this).updateShortcuts();
        }
    }

    private void handleShortcutIntent(Intent intent) {
        int intentAction = intent.getIntExtra("action", 0);

        if (intentAction == SHORTCUT_ACTION_OPEN_SELECTOR) {
            NekoDialog dialog = new NekoDialog(this);
            dialog.show();
        }
        else if (intentAction == SHORTCUT_ACTION_SET_FOOD) {
            final Food food = new Food(intent.getIntExtra("food", 0));
            NekoDialog dialog = new NekoDialog(this);
            dialog.selectFood(food);
            new NekoShortcuts(this).updateShortcuts();

            imageView.setImageResource(food.getIcon(this));
            textView.setText(food.getName(this));
            closeAppTextView.setVisibility(View.VISIBLE);
            closeAppTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }

    private void onCatClick(Cat cat) {
        if (CAT_GEN) {
            mPrefs.addCat(cat);
            new AlertDialog.Builder(NekoLand.this)
                    .setTitle(getString(R.string.add_cat))
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

        private List<Cat> mCats = new ArrayList<>();

        public void setCats(List<Cat> cats) {
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
                holder.imageView.setImageIcon(mCats.get(position).createLargeIcon(context));
            } else {
                holder.imageView.setImageBitmap(mCats.get(position).createLargeBitmap(context));
            }
            holder.textView.setText(mCats.get(position).getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCatClick(mCats.get(holder.getAdapterPosition()));
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
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    holder.contextGroup.setVisibility(View.INVISIBLE);
                                    holder.contextGroup.removeCallbacks((Runnable) holder.contextGroup.getTag());
                                    onCatRemove(mCats.get(holder.getAdapterPosition()));
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(NekoLand.this);
                    builder.setMessage(getString(R.string.remove_cat)).setPositiveButton(android.R.string.yes, dialogClickListener)
                            .setNegativeButton(android.R.string.no, dialogClickListener).show();
                }
            });
            holder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Cat cat = mCats.get(holder.getAdapterPosition());
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
            return mCats.size();
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
        if (mPrefs.getFoodState() == 0) {
            textView.setText(getResources().getString(R.string.empty_dish));
            imageView.setImageResource(R.drawable.food_dish);
            closeAppTextView.setVisibility(View.GONE);
        }
        handleShortcutIntent(intent);
    }

    private void getReturnDialog(Context context) {
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
                .setTitle(getString(R.string.cat_dialog_return_title))
                .setMessage(getString(R.string.cat_dialog_return_message))
                .setPositiveButton(getString(android.R.string.ok), null)
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
