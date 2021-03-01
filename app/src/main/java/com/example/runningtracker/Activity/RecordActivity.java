package com.example.runningtracker.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.runningtracker.R;
import com.example.runningtracker.Database.TrackerProviderContract;

import java.text.DecimalFormat;

public class RecordActivity extends AppCompatActivity {

    long recordID;
    double totalDistance;           // this variable calculates the total distance user has run so far
    long totalTime;                 // this variable calculates the total time user has spent so far
    static int count;
    static int dateClick, distanceClick, timeClick = 0;
    String order = TrackerProviderContract.SORT_DATE + " DESC";     // default sorting order by date
    ListView listView;
    TextView totalDistanceTv, totalTimeTv, totalDataTitle, recordCountTv, dateTv, distanceTv, timeTv;
    SimpleCursorAdapter dataAdapter;
    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // apply different App Theme based on previous choice before setting up activity view
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.DarkTheme);
        else
            setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // set the "Up" button for user to go back
        ActionBar backBar = getSupportActionBar();
        backBar.setDisplayHomeAsUpEnabled(true);

        totalDistanceTv = findViewById(R.id.record_totalDistance);
        totalTimeTv = findViewById(R.id.record_totalTime);
        totalDataTitle = findViewById(R.id.totalDataTitle);
        recordCountTv = findViewById(R.id.record_count);
        dateTv = findViewById(R.id.date_title);
        distanceTv = findViewById(R.id.distance_title);
        timeTv = findViewById(R.id.time_title);
        listView = findViewById(R.id.list);


        String[] projection = new String[]{
                TrackerProviderContract._ID,
                TrackerProviderContract.SORT_DISTANCE,
                TrackerProviderContract.SORT_TIME
        };

        Cursor cursor = getContentResolver().query(TrackerProviderContract.RECORD_URI, projection, null, null, order);
        if (cursor.moveToFirst()) {
            do {
                double journeyDistance = cursor.getDouble(1);
                long journeyTime = cursor.getLong(2);
                count = cursor.getCount();
                totalDistance += journeyDistance;
                totalTime += journeyTime;
            } while (cursor.moveToNext());
        }

        // convert the distance with 2 decimal
        DecimalFormat df = new DecimalFormat("0.00");
        if (cursor.getCount() == 0)
            totalDistance = 0;
        totalDistanceTv.setText(df.format(totalDistance));

        if (count == 0)
            recordCountTv.setText("No exercises");
        else
            recordCountTv.setText(count + " exercises");
        cursor.close();


        totalDistanceTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalTimeTv.setVisibility(View.VISIBLE);
                totalDistanceTv.setVisibility(View.GONE);
                totalTimeTv.setText(totalTime / 1000 + "");
                totalDataTitle.setText("TOTAL TIME (sec)");
            }
        });

        totalTimeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalTimeTv.setVisibility(View.GONE);
                totalDistanceTv.setVisibility(View.VISIBLE);
                DecimalFormat df = new DecimalFormat("0.00");
                totalDistanceTv.setText(df.format(totalDistance));
                totalDataTitle.setText("TOTAL DISTANCE (m)");
            }
        });

        dataSorting();
        listViewListener();
        queryRecord(order);

        getContentResolver().
                registerContentObserver(
                        TrackerProviderContract.ALL_URI,
                        true,
                        new ChangeObserver(handler));
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

    class ChangeObserver extends ContentObserver {

        public ChangeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            queryRecord(order);
        }
    }

    public void dataSorting() {
        dateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateClick++;
                if (dateClick % 2 != 0) {
                    order = TrackerProviderContract.SORT_DATE + " DESC";
                } else {
                    order = TrackerProviderContract.SORT_DATE + " ASC";
                }
                queryRecord(order);
            }
        });

        distanceTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                distanceClick++;
                if (distanceClick % 2 != 0) {
                    order = TrackerProviderContract.SORT_DISTANCE + " DESC";
                } else {
                    order = TrackerProviderContract.SORT_DISTANCE + " ASC";
                }
                queryRecord(order);
            }
        });

        timeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeClick++;
                if (timeClick % 2 != 0) {
                    order = TrackerProviderContract.SORT_TIME + " DESC";
                } else {
                    order = TrackerProviderContract.SORT_TIME + " ASC";
                }
                queryRecord(order);
            }
        });
    }

    public void listViewListener() {

        // Allow user to view details of each journey when short click the list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent data = new Intent(RecordActivity.this, DetailRecordActivity.class);
                data.putExtra("id", id);
                startActivity(data);
            }
        });

        // prompt an alert message when users long click the list view
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                recordID = id;
                AlertDialog alertDialog = new AlertDialog.Builder(RecordActivity.this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Do you want to delete this Journey ?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteRecord(recordID);
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;
            }
        });
    }

    public void queryRecord(String order) {

        String[] projection = new String[]{
                TrackerProviderContract._ID,
                TrackerProviderContract.DATE,
                TrackerProviderContract.DISTANCE,
                TrackerProviderContract.TIME,
                TrackerProviderContract.SPEED
        };

        String[] display = new String[]{
                TrackerProviderContract.DATE,
                TrackerProviderContract.DISTANCE,
                TrackerProviderContract.TIME,
                TrackerProviderContract.SPEED
        };

        int[] colResIds = new int[]{
                R.id.show_date,
                R.id.show_distance,
                R.id.show_time
        };

        // query and show data from database
        Cursor cursor = getContentResolver().query(TrackerProviderContract.RECORD_URI, projection, null, null, order);
        dataAdapter = new SimpleCursorAdapter(
                this,
                R.layout.record_layout,
                cursor,
                display,
                colResIds,
                0);
        listView.setAdapter(dataAdapter);
    }

    // delete the record in database based on input record id
    public void deleteRecord(long id) {
        String[] ID = new String[]{String.valueOf(id)};

        String[] journeyID = new String[]{
                TrackerProviderContract._ID,
                TrackerProviderContract.SORT_DISTANCE
        };

        Cursor cursor = getContentResolver().query(TrackerProviderContract.RECORD_URI, journeyID, null, null, order);
        if (cursor.moveToFirst()) {
            double deletingDistance = cursor.getDouble(1);
            totalDistance -= deletingDistance;
            count--;
            getContentResolver().delete(TrackerProviderContract.RECORD_URI, TrackerProviderContract._ID + "=?", ID);
        }
        cursor.close();

        DecimalFormat df = new DecimalFormat("0.00");
        totalDistanceTv.setText(df.format(totalDistance));
        recordCountTv.setText(count + " exercises");
    }

}
