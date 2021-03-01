package com.example.runningtracker.Database;

import android.net.Uri;

public class TrackerProviderContract {

    public static final String AUTHORITY = "com.example.runningtracker";
    public static final Uri RECORD_URI = Uri.parse("content://" + AUTHORITY + "/record");
    public static final Uri ALL_URI = Uri.parse("content://" + AUTHORITY + "/");

    public static final String _ID = "_id";
    public static final String DATE = "date";
    public static final String DISTANCE = "distance";
    public static final String TIME = "time";
    public static final String SPEED = "speed";
    public static final String SORT_DATE = "sort_date";
    public static final String SORT_DISTANCE = "sort_distance";
    public static final String SORT_TIME = "sort_time";
    public static final String SORT_SPEED = "sort_speed";
    public static final String REVIEW = "review";
    public static final String COMMENT = "comment";

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/TrackerContentProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/TrackerContentProvider.data.text";

}
