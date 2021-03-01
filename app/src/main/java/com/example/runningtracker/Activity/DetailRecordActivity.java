package com.example.runningtracker.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.runningtracker.Database.TrackerProviderContract;
import com.example.runningtracker.R;

import java.text.SimpleDateFormat;

public class DetailRecordActivity extends AppCompatActivity {

    long id;
    TextView detailDistance, detailDate, detailTime, detailAvgSpeed, detailReview, detailType;
    EditText detailComment;
    ImageView imgType;
    String type, distance, time, avgSpeed, review, comment;
    double date;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // apply different App Theme based on previous choice before setting up activity view
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.DarkTheme);
        else
            setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar backBar = getSupportActionBar();
        backBar.setDisplayHomeAsUpEnabled(true);

        Intent data = getIntent();
        id = data.getLongExtra("id", 0);
        Log.d("id", "id: " + id);
        detailDistance = findViewById(R.id.detail_distance);
        detailDate = findViewById(R.id.detail_date);
        detailTime = findViewById(R.id.detail_time);
        detailAvgSpeed = findViewById(R.id.detail_avgSpeed);
        detailReview = findViewById(R.id.detail_review);
        detailComment = findViewById(R.id.detail_comment);
        detailType = findViewById(R.id.detail_type);
        imgType = findViewById(R.id.img_type);

        showDetails();

        SimpleDateFormat SDF = new SimpleDateFormat("dd MMMM yyyy");

        detailDate.setText(SDF.format(date)); // show the chosen journey date with format day,month,year
        detailDistance.setText(distance);            // show the chosen journey distance
        detailTime.setText(time);            // show chosen journey used time
        detailAvgSpeed.setText(avgSpeed);            // show the chosen journey average speed
        detailReview.setText(review);            // set the chosen journey distance record
        detailComment.setText(comment);            // show the chosen journey comment
        detailType.setText(type);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDetails() {
        String[] ID = new String[]{String.valueOf(id)};
        String[] projection = new String[]{
                TrackerProviderContract.SORT_DATE,
                TrackerProviderContract.DISTANCE,
                TrackerProviderContract.TIME,
                TrackerProviderContract.SPEED,
                TrackerProviderContract.SORT_SPEED,         // get the double version to for exercise evaluation
                TrackerProviderContract.REVIEW,
                TrackerProviderContract.COMMENT
        };

        // get and show all the required data from data base
        Cursor cursor = getContentResolver().query(TrackerProviderContract.RECORD_URI, projection, TrackerProviderContract._ID + "=?", ID, null);
        if (cursor.moveToFirst()) {
            do {
                date = cursor.getDouble(0);
                distance = cursor.getString(1);
                time = cursor.getString(2);
                avgSpeed = cursor.getString(3);
                review = cursor.getString(5);
                comment = cursor.getString(6);

                if (review == null)
                    review = "Empty";

                if (comment == null)
                    comment = "No Comment";

                double speed = cursor.getDouble(4);
                if (speed >= 6) {                       // speed beyond 6 m/s , evaluated as running exercise
                    type = "Run";
                    detailType.setTextColor(Color.parseColor("#FF1900"));
                    imgType.setBackgroundResource(R.drawable.run_type);
                } else if (speed >= 3) {                                // speed between 3-6 m/s , evaluated as jogging exercise
                    type = "Jog";
                    detailType.setTextColor(Color.parseColor("#03D1EC"));
                    imgType.setBackgroundResource(R.drawable.jog_type);
                } else {                                               // speed below 3 m/s , evaluated as walking exercise
                    type = "Walk";
                    detailType.setTextColor(Color.parseColor("#380E75"));
                    imgType.setBackgroundResource(R.drawable.walk_type);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public void onClickEdit(View view) {
        String[] ID = new String[]{String.valueOf(id)};
        String text = detailComment.getText().toString();
        ContentValues newComment = new ContentValues();
        newComment.put(TrackerProviderContract.COMMENT, text);
        getContentResolver().update(TrackerProviderContract.RECORD_URI, newComment, TrackerProviderContract._ID + "=?", ID);
        Toast.makeText(this,"Successfully updated the comment !",Toast.LENGTH_SHORT).show();
        finish();
    }
}
