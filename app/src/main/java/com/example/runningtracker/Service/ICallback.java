package com.example.runningtracker.Service;

import android.location.Location;

public interface ICallback {
    void TrackerHandler(double distance, long time, Location location , boolean running);
}
