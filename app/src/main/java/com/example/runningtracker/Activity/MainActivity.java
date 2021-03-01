package com.example.runningtracker.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.runningtracker.Broadcast.LowBatteryBroadcastReceiver;
import com.example.runningtracker.Service.LocationService;
import com.example.runningtracker.R;

import java.lang.reflect.Type;


public class MainActivity extends AppCompatActivity {

    LowBatteryBroadcastReceiver receiver;
    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // apply different App Theme based on previous choice before setting up activity view
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.DarkTheme);
        else
            setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Switch btnSwitch = findViewById(R.id.btnSwitch);

        receiver = new LowBatteryBroadcastReceiver();
        intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, intentFilter);


        // Check the App theme, if it's Dark Mode then no need to alert again...
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
            // prompt an alert when the battery life is below 20
            if (receiver.getBattery() <= 20) {
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Your Battery life is currently low !");
                alertDialog.setMessage("Would you like to turn on Dark Mode to save battery?");     // Set an alert message which ask users to turn Dark Mode on when device battery is low
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                                btnSwitch.setChecked(true);
                                Toast.makeText(MainActivity.this, "Dark Mode on !!!", Toast.LENGTH_SHORT).show();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No thanks",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }

        // ask user for location permission before this application started
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            System.exit(0);
        }

        // set the Switch button on when Dark Mode is selected
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            btnSwitch.setChecked(true);

        // apply different Theme Mode and refresh the App when user clicked the switch button
        btnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    refreshApp();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    refreshApp();
                }
            }
        });
    }

    // function for refreshing the app view
    public void refreshApp() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }


    public void onClickExercise(View view) {
        Button button = findViewById(R.id.goButton);

        // create the animation when user click " Go " button
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        // Use bounce interpolator with amplitude 0.2 and frequency 20
        BounceInterpolator interpolator = new BounceInterpolator(0.3, 30);
        myAnim.setInterpolator(interpolator);
        button.startAnimation(myAnim);

        // delay the system a bit to show the animation before jump into next activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, MovementActivity.class);
                startActivity(intent);
            }
        }, 400);
    }

    public void onClickRecord(View view) {
        Intent intent = new Intent(MainActivity.this, RecordActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, LocationService.class));
        unregisterReceiver(receiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void recreate() {
        super.recreate();
    }
}
