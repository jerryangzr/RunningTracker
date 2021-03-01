package com.example.runningtracker.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.runningtracker.R;
import com.example.runningtracker.Database.TrackerProviderContract;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class ResultActivity extends AppCompatActivity {

    double sortDistance, sortSpeed;
    long sortDate, sortTime;
    String type;
    String showDate, showDistance, showTime, showSpeed;
    String journeyReview, journeyComment;
    TextView distanceTv, timeTv, speedTv;
    TextView resultTypeTv;
    ImageView resultTypeImg;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // apply different App Theme based on previous choice before setting up activity view
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.DarkTheme);
        else
            setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        distanceTv = findViewById(R.id.totalDistance);
        timeTv = findViewById(R.id.totalTime);
        speedTv = findViewById(R.id.avgSpeed);
        resultTypeTv = findViewById(R.id.result_type);
        resultTypeImg = findViewById(R.id.result_imgType);

        Intent data = getIntent();      // get the data sent from MovementActivity
        sortDistance = data.getDoubleExtra("distance", 0.00);
        sortTime = data.getLongExtra("time", 0);

        sortSpeed = sortDistance / TimeUnit.MILLISECONDS.toSeconds(sortTime);
        showDate = data.getStringExtra("date");
        sortDate = data.getLongExtra("sortDate", 0);

        DecimalFormat df = new DecimalFormat("0.00");
        showDistance = df.format(sortDistance) + " m";
        distanceTv.setText(showDistance);

        //Convert result time format into hh,mm,ss
        showTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(sortTime),   // Convert milliseconds to hour,miniute,second format
                TimeUnit.MILLISECONDS.toMinutes(sortTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(sortTime)),
                TimeUnit.MILLISECONDS.toSeconds(sortTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sortTime)));
        timeTv.setText(showTime);

        // show only 3 decimal of the given speed
        if ((sortDistance == 0) && sortTime == 0)
            speedTv.setText("0.00 m/s");

        if (sortTime < 1)
            speedTv.setText("0.00 m/s");
        else {
            DecimalFormat speedDF = new DecimalFormat("0.00");
            showSpeed = speedDF.format(sortSpeed) + " m/s";
            speedTv.setText(showSpeed);
        }


        if (sortSpeed >= 6) {                       // speed beyond 6 m/s , evaluated as running exercise
            type = "Run";
            resultTypeTv.setText(type);
            resultTypeTv.setTextColor(Color.parseColor("#FF1900"));
            resultTypeImg.setBackgroundResource(R.drawable.run_type);
        } else if (sortSpeed >= 3) {                                // speed between 3-6 m/s , evaluated as jogging exercise
            type = "Jog";
            resultTypeTv.setText(type);
            resultTypeTv.setTextColor(Color.parseColor("#03D1EC"));
            resultTypeImg.setBackgroundResource(R.drawable.jog_type);
        } else {                                               // speed below 3 m/s , evaluated as walking exercise
            type = "Walk";
            resultTypeTv.setText(type);
            resultTypeTv.setTextColor(Color.parseColor("#380E75"));
            resultTypeImg.setBackgroundResource(R.drawable.walk_type);
        }

    }

    public void onClickMark(View view) {
        Intent intent = new Intent(ResultActivity.this, MarkActivity.class);
        startActivityForResult(intent, 100);
    }

    // get the result data from MarkActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null) {
                journeyReview = data.getStringExtra("review");
                journeyComment = data.getStringExtra("comment");
            }
        }
    }

    // save the user record into database and go back to menu
    public void onClickMenu(View view) {
        // store user data into database
        ContentValues userData = new ContentValues();
        userData.put(TrackerProviderContract.DATE, showDate);
        userData.put(TrackerProviderContract.DISTANCE, showDistance);
        userData.put(TrackerProviderContract.TIME, showTime);
        userData.put(TrackerProviderContract.SPEED, showSpeed);
        userData.put(TrackerProviderContract.SORT_DATE, sortDate);
        userData.put(TrackerProviderContract.SORT_DISTANCE, sortDistance);
        userData.put(TrackerProviderContract.SORT_TIME, sortTime);
        userData.put(TrackerProviderContract.SORT_SPEED, sortSpeed);
        userData.put(TrackerProviderContract.REVIEW, journeyReview);
        userData.put(TrackerProviderContract.COMMENT, journeyComment);
        getContentResolver().insert(TrackerProviderContract.RECORD_URI, userData);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
