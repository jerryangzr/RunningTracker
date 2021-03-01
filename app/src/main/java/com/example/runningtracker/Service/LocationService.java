package com.example.runningtracker.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.runningtracker.Activity.MovementActivity;
import com.example.runningtracker.R;

public class LocationService extends Service {
    RemoteCallbackList<MyTrackerBinder> remoteCallbackList = new RemoteCallbackList<MyTrackerBinder>();

    LocationManager locationManager;
    MyLocationListener locationListener;
    Boolean running = false;
    Boolean finished = false;
    Location lastLocation;
    double totalDistance;
    long startTime, spentTime, pauseTime, waitingTime;


    public void onCreate() {
        super.onCreate();
        Log.d("service", "Service onCreate");

        int NOTIFICATION_ID = 1;
        final String CHANNEL_ID = "100";

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel name";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(LocationService.this, MovementActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background).setContentTitle("Running Tracker").setContentText("Click here to View").setContentIntent(pendingIntent).setPriority(NotificationCompat.PRIORITY_DEFAULT);
        startForeground(NOTIFICATION_ID, mBuilder.build());


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(LocationService.this);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1, // minimum time interval between updates
                    0, // minimum distance between updates, in metres
                    locationListener);

            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.d("location", e.toString());
        }
    }

    public void doCallbacks(double distance, long time, Location location, boolean running) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.TrackerHandler(distance, time, location, running);
        }
        remoteCallbackList.finishBroadcast();
    }

    public class MyTrackerBinder extends Binder implements IInterface {
        @Override
        public IBinder asBinder() {
            return this;
        }

        public void registerCallback(ICallback callback) {
            this.callback = callback;
            remoteCallbackList.register(MyTrackerBinder.this);
        }

        public void unregisterCallback(ICallback callback) {
            remoteCallbackList.unregister(MyTrackerBinder.this);
        }

        ICallback callback;

        public void startTracking() {
            startTime = System.currentTimeMillis();        // get the current time
            running = true;
        }

        public void pauseTracking() {
            running = false;
            pauseTime = System.currentTimeMillis();   // mark the pausing time
        }

        public void continueTracking() {
            running = true;
            waitingTime = System.currentTimeMillis() - pauseTime;  // calculate the waiting time
            startTime += waitingTime;           // update the starting time
        }

        public void finishTracking() {     // function allows Movement activity to call
            running = false;
            finished = true;
        }

        public Location getLastLocation() {
            return lastLocation;
        }
    }


    public class MyLocationListener implements LocationListener {
        private Context myContext;

        public MyLocationListener(Context myContext) {
            this.myContext = myContext;
        }

        @Override
        public void onLocationChanged(Location currentLocation) {

//            Intent intent = new Intent("myBroadCast");
//            intent.putExtra("broadcast", location);   //!!!
//            LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent);
            Log.d("listener", currentLocation.getLatitude() + " " + currentLocation.getLongitude());
            try {
                if (running) {
                    double tmpDistance = currentLocation.distanceTo(lastLocation);
                    totalDistance += tmpDistance;
                    spentTime = System.currentTimeMillis() - startTime;   // calculate the spent time
                } else if (finished) {
                    totalDistance = 0;
                    spentTime = 0;
                    finished = false;
                }
            } catch (Exception e) {
            }
            doCallbacks(totalDistance, spentTime, currentLocation, running);
            lastLocation = currentLocation;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // information about the signal, i.e. number of satellites
            Log.d("listener", "onStatusChanged: " + provider + " " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            // the user enabled (for example) the GPS
            Log.d("listener", "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // the user disabled (for example) the GPS
            Log.d("listener", "onProviderDisabled: " + provider);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return new MyTrackerBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (finished)
            stopForeground(true);
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        if (finished) {
            stopSelf();
        }
        return true;
    }
}
