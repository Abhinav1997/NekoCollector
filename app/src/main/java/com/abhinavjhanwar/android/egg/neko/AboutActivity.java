package com.abhinavjhanwar.android.egg.neko;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.abhinavjhanwar.android.egg.R;

import java.util.Random;

public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        final ImageView imageView = (ImageView) findViewById(R.id.aboutCat);
        final ImageView githubImage = (ImageView) findViewById(R.id.githubIcon);
        final ImageView twitterImage = (ImageView) findViewById(R.id.twitterIcon);
        final ImageView gPlusImage = (ImageView) findViewById(R.id.gPlusIcon);
        final ImageView facebookImage = (ImageView) findViewById(R.id.facebookIcon);
        Cat cat = Cat.create(this);
        Drawable catIcon = new BitmapDrawable(getResources(), cat.createLargeIcon(this));
        imageView.setImageDrawable(catIcon);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Toast.makeText(getApplicationContext(), getCatEmoji(), Toast.LENGTH_SHORT).show();
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
