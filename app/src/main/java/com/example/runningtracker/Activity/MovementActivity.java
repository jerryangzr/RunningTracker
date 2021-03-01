package com.example.runningtracker.Activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.runningtracker.Broadcast.LowBatteryBroadcastReceiver;
import com.example.runningtracker.R;
import com.example.runningtracker.Service.ICallback;
import com.example.runningtracker.Service.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MovementActivity extends FragmentActivity implements OnMapReadyCallback {

    String status, date;
    Boolean start = false;
    Boolean isRunning = false;
    double totalDistance;
    long totalTime, sortDate;
    Button btnFinish;
    ImageButton btnStart, btnPause, btnStop;
    TextView title, dateTv, distanceTv, speedTv, timeTv;
    GoogleMap mMap;
    SupportMapFragment mapFragment;
    LowBatteryBroadcastReceiver receiver;
    IntentFilter intentFilter;
    LocationService.MyTrackerBinder locationService = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // apply different app theme before activity content view set up
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.DarkTheme);
        else
            setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement);

        receiver = new LowBatteryBroadcastReceiver();
        intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, intentFilter);

        title = findViewById(R.id.movement_title);
        btnStart = findViewById(R.id.start);
        btnPause = findViewById(R.id.pause);
        btnStop = findViewById(R.id.stop);
        btnFinish = findViewById(R.id.finish);

        dateTv = findViewById(R.id.date);
        distanceTv = findViewById(R.id.distance);
        speedTv = findViewById(R.id.speed);
        timeTv = findViewById(R.id.time);

        // prompt an alert when the battery life is below 20
        if (receiver.getBattery() <= 20) {
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(MovementActivity.this).create();
            alertDialog.setTitle("Your Battery life is currently low !");
            alertDialog.setMessage("Please charge your phone ASAP to keep the app running...");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        SimpleDateFormat SDF = new SimpleDateFormat("dd MMMM yyyy HH:mm");   // show the current Date with format day, month, year, hour : minute
        Date D = new Date();
        sortDate = D.getTime();      // convert Date to long version for sorting mechanism in RecordActivity.
        date = SDF.format(D);

        SimpleDateFormat showingDate = new SimpleDateFormat("dd MMMM yyyy");   // show the current Date with format day, month, year, hour : minute
        dateTv.setText(showingDate.format(sortDate));

        btnFinish.setEnabled(false);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.startService(new Intent(this, LocationService.class));  //add
        this.bindService(new Intent(MovementActivity.this, LocationService.class), serviceConnection, Context.BIND_AUTO_CREATE); // bind the service
    }

    public void onClickStart(View view) {
        btnStart.setVisibility(View.GONE);      // hide the start button
        btnPause.setVisibility(View.VISIBLE);   // show the pause button
        btnStop.setVisibility(View.VISIBLE);    // show the stop button
        if (!start) {
            locationService.startTracking();
        } else {
            locationService.continueTracking();
        }
        start = true;
        status = "Running";
        title.setText(status);
        title.setTextColor(Color.parseColor("#3AC819"));
        btnFinish.setEnabled(true);
    }

    public void onClickPause(View view) {
        btnStart.setEnabled(true);
        btnStart.setVisibility(View.VISIBLE);      // show the start button
        btnPause.setVisibility(View.GONE);         // hide the pause button
        locationService.pauseTracking();
        status = "Paused";
        title.setText(status);
        title.setTextColor(Color.parseColor("#ED281C"));
    }

    public void onClickStop(View view) {
        //  prompt an alert message when user click the stop button
        AlertDialog alertDialog = new AlertDialog.Builder(MovementActivity.this).create();
        alertDialog.setTitle("Are you sure you want to give up ?");
        alertDialog.setMessage("This exercise will not be recorded if you quit right now...");

        // called when user click the "Confirm" button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isRunning = false;
                        finish();
                    }
                });
        // called when user click the "Cancel" button
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void onClickFinish(View view) {      // When user click Finish button
        locationService.finishTracking();
        Intent intent = new Intent(MovementActivity.this, ResultActivity.class);
        intent.putExtra("date", date);
        intent.putExtra("distance", totalDistance);
        intent.putExtra("time", totalTime);
        intent.putExtra("sortDate", sortDate);            // send the long version of current date for further sorting mechanism
        startActivity(intent);
        finish();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            locationService = (LocationService.MyTrackerBinder) service;
            locationService.registerCallback(callback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService.unregisterCallback(callback);
            locationService = null;
        }
    };


    ICallback callback = new ICallback() {
        @Override
        public void TrackerHandler(final double distance, final long time, final Location location, final boolean running) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isRunning = running;
                    if (!start) {
                        status = "Status";
                        title.setText(status);
                        btnStop.setEnabled(false);
                        btnStop.setClickable(false);
                        btnFinish.setEnabled(false);
                    } else if (running) {
                        status = "Running";
                        title.setText(status);
                        title.setTextColor(Color.parseColor("#3AC819"));
                        btnStop.setVisibility(View.VISIBLE);
                        btnPause.setVisibility(View.VISIBLE);
                        btnPause.setClickable(true);
                        btnStop.setClickable(true);
                        btnStop.setEnabled(true);
                        btnFinish.setEnabled(true);
                    } else {
                        status = "Paused";
                        title.setText(status);
                        title.setTextColor(Color.parseColor("#ED281C"));
                        btnStart.setVisibility(View.VISIBLE);
                        btnStop.setVisibility(View.VISIBLE);
                        btnStop.setClickable(true);
                        btnStop.setEnabled(true);
                        btnStart.setClickable(true);
                        btnFinish.setEnabled(true);
                    }

                    // set run Distance with format with only 2 decimal
                    totalDistance = distance;
                    DecimalFormat df = new DecimalFormat("0.00");
                    distanceTv.setText(df.format(distance) + " m");

                    // set spent Time with format 00:00:00
                    String formatTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(time),   // Convert milliseconds to hour,miniute,second format
                            TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                            TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
                    timeTv.setText(formatTime);
                    totalTime = time;

                    // set current running Speed with 3 decimal
                    double formatSpeed = location.getSpeed() * 0.5144444;   // convert speed unit "knots" to "m/s"
                    DecimalFormat speedDF = new DecimalFormat("0.000");
                    speedTv.setText(speedDF.format(formatSpeed) + " m/s");


                    // get the recent location for route drawing
                    LatLng lastLocation = new LatLng(locationService.getLastLocation().getLatitude(), locationService.getLastLocation().getLongitude());
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    // draw the user running route by last two location data point
                    mMap.addPolyline(new PolylineOptions()
                            .add(lastLocation)
                            .add(currentLocation)
                            .width(4f)
                            .color(Color.BLACK));
                    //add circle
//                    mMap.addCircle(new CircleOptions().center(currentLocation).radius(10.0).strokeWidth(2f).fillColor(Color.BLUE));

                    // move the camera to the user's location and zoom in
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
                }
            });
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String title = "Nottingham";
        // Add a starting marker in Nottingham and move the camera
        LatLng nottingham = new LatLng(52.953198, -1.184100);

        // check and request the location permission before locating users location
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    1
            );
        }

        // enable google map to track users current location
        mMap.setMyLocationEnabled(true);

        MarkerOptions options = new MarkerOptions().position(nottingham).title(title).snippet("Jubilee Campus");
        Marker locationMarker = mMap.addMarker(options);
        locationMarker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationMarker.getPosition()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
        if (!isRunning) {
            stopService(new Intent(MovementActivity.this, LocationService.class));
        }
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }
}
