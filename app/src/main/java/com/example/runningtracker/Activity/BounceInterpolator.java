package com.example.runningtracker.Activity;

import android.view.animation.Interpolator;

public class BounceInterpolator implements Interpolator {
    private double mAmplitude;
    private double mFrequency;

    // allow to set the amplitude and frequency of the function
    BounceInterpolator(double amplitude, double frequency) {
        mAmplitude = amplitude;
        mFrequency = frequency;
    }

    // function formula for interpolation
    public float getInterpolation(float time) {
        return (float) (-1 * Math.pow(Math.E, -time / mAmplitude) *
                Math.cos(mFrequency * time) + 1);
    }
}
