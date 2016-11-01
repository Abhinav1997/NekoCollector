package com.abhinavjhanwar.android.egg.neko;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abhinavjhanwar.android.egg.R;

import java.util.Random;

public class AboutActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final ImageView imageView = (ImageView) findViewById(R.id.aboutCat);
        final ImageView githubImage = (ImageView) findViewById(R.id.githubIcon);
        final ImageView twitterImage = (ImageView) findViewById(R.id.twitterIcon);
        final ImageView gPlusImage = (ImageView) findViewById(R.id.gPlusIcon);
        final ImageView facebookImage = (ImageView) findViewById(R.id.facebookIcon);
        final TextView creditsText = (TextView) findViewById(R.id.credits);
        creditsText.setText(getString(R.string.credits) + ": " + getString(R.string.aosp));
        final Cat[] cat = {Cat.create(this)};
        final Drawable[] catIcon = {new BitmapDrawable(getResources(), cat[0].createLargeBitmap(this))};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            imageView.setImageIcon(cat[0].createLargeIcon(this));
        } else {
            imageView.setImageDrawable(catIcon[0]);
        }
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 1000, 0};
                    Toast toast = Toast.makeText(getApplicationContext(), getCatEmoji(), Toast.LENGTH_SHORT);
                    View toastView = toast.getView();
                    toastView.setBackgroundColor(0x00000000);
                    vb.vibrate(pattern, 0);
                    toast.show();
                    vb.cancel();
                }
                return true;
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cat[0] = Cat.create(getApplicationContext());
                catIcon[0] = new BitmapDrawable(getResources(), cat[0].createLargeBitmap(getApplicationContext()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    imageView.setImageIcon(cat[0].createLargeIcon(getApplicationContext()));
                } else {
                    imageView.setImageDrawable(catIcon[0]);
                }
            }
        });

        githubImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://github.com/Abhinav1997/NekoCollector");
            }
        });

        twitterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://twitter.com/AbhinavJhanwar");
            }
        });

        gPlusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://plus.google.com/u/0/+AbhinavJhanwar1997");
            }
        });

        facebookImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink("https://www.facebook.com/AbhinavJhanwar97");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getCatEmoji() {
        String[] catEmojiList = {new String(Character.toChars(0x1F638)), new String(Character.toChars(0x1F639)), new String(Character.toChars(0x1F640)), new String(Character.toChars(0x1F63A)), new String(Character.toChars(0x1F63B)), new String(Character.toChars(0x1F63C)), new String(Character.toChars(0x1F63D)), new String(Character.toChars(0x1F63E)), new String(Character.toChars(0x1F63F))};
        Random r = new Random();
        return catEmojiList[r.nextInt(catEmojiList.length)];
    }

    public void openLink(String URL) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
        startActivity(intent);
    }
}