package com.example.runningtracker.Database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class TrackerContentProvider extends ContentProvider {
    private DBHelper dbHelper = null;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(TrackerProviderContract.AUTHORITY, "record", 1);
    }

    @Override
    public boolean onCreate() {
        Log.d("cw2 provider", "TrackerProviderContract oncreate");
        this.dbHelper = new DBHelper(this.getContext(), "mydb", null, 8);
        return true;
    }

    @Override
    public String getType(Uri uri) {

        String contentType;

        if (uri.getLastPathSegment() == null) {
            contentType = TrackerProviderContract.CONTENT_TYPE_MULTIPLE;
        } else {
            contentType = TrackerProviderContract.CONTENT_TYPE_SINGLE;
        }
        return contentType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName;

        switch (uriMatcher.match(uri)) {
            default:
                tableName = "record";
                break;
        }

        long id = db.insert(tableName, null, values);
        db.close();
        Uri nu = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(nu, null);
        return nu;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Log.d("cw2 provider query", uri.toString() + " " + uriMatcher.match(uri));

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case 1:
                return db.query("record", projection, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName;
        switch (uriMatcher.match(uri)) {
            default:
                tableName = "record";
                break;
        }
        int id = db.update(tableName, values, selection, selectionArgs);
        db.close();

        getContext().getContentResolver().notifyChange(uri, null);
        return id;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName;
        switch (uriMatcher.match(uri)) {
            default:
                tableName = "record";
                break;
        }

        int id = db.delete(tableName, selection, selectionArgs);
        db.close();
        Uri nu = ContentUris.withAppendedId(uri, id);
        Log.d("cw2 provider", nu.toString());
        getContext().getContentResolver().notifyChange(nu, null);
        return id;
    }
}
