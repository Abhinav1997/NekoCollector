package com.abhinavjhanwar.android.egg.neko;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class CatReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NekoService.registerJob(context, 30);
        } else {
            context.startService(new Intent(context, OldService.class));
        }
    }
}