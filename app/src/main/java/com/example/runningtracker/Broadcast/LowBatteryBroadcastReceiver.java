package com.example.runningtracker.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

public class LowBatteryBroadcastReceiver extends BroadcastReceiver {

    int percentage;
    @Override
    public void onReceive(Context context, Intent intent) {
        int currLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);    //Current Battery
        int total = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);        //Total Battery
        percentage = currLevel * 100 / total;

        Log.d("battery", "battery: " + percentage);
    }

    public int getBattery() {
        return percentage;
    }
}
