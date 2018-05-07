package com.example.android.ctastatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by MissTank on 2/5/18.
 */

public class ServiceReceiver extends BroadcastReceiver{


    @Override
    public void onReceive(Context context, Intent intent) {


        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i("Restarting ","Device");
            Intent serviceIntent = new Intent(context, ForegroundService.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startService(serviceIntent);
        }

    }



}






