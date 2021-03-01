package com.example.runningtracker.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                    int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
        Log.d("cw2", "DBHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("cw2 db", "DBHelper onCreate");
        // TODO Auto-generated method stub
        db.execSQL("CREATE TABLE record (" +
                "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT ," +
                "date VARCHAR(128) NOT NULL," +         // date (String type)
                "distance VARCHAR(128) NOT NULL," +     // distance (String type)
                "speed VARCHAR(128) NOT NULL," +       // speed (String type)
                "time VARCHAR(128) NOT NULL," +        //time (String type)
                "sort_date INTEGER NOT NULL," +      // sort_date for date sorting
                "sort_distance REAL NOT NULL," +    // sort_distance for distance sorting
                "sort_time INTEGER NOT NULL," +     // sort_time for time sorting
                "sort_speed REAL NOT NULL," +       // sort_speed for speed sorting
                "review VARCHAR(128)," +            // review (String type)
                "comment VARCHAR(128)" +            // comment (String type)
                ");");

        // add default data
        db.execSQL("INSERT INTO record (date,distance,speed,time,sort_date,sort_distance,sort_time,sort_speed,review,comment) " +
                "VALUES ('07 January 2020 22:51','372.63 m','2.96 m/s','00:02:06',1578437509552,372.6253172159195,126604,2.9573437874279325,'Good','Super Fun!!!');");
        db.execSQL("INSERT INTO record (date,distance,speed,time,sort_date,sort_distance,sort_time,sort_speed,review,comment) " +
                "VALUES ('09 January 2020 19:24','65.23 m','0.15 m/s','00:07:07',1578597853001,65.22838798165321,427014,0.15275969082354382,'Normal','Not a bad run..');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS record");
        onCreate(db);
    }
}
