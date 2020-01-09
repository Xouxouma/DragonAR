package com.google.ar.sceneform.samples.dragonAR;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "DRAGON_DATABASE";

    DatabaseHandler(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Dragons(id INTEGER PRIMARY KEY, name TEXT, gender INTEGER, level INTEGER, " +
                "satiety INTEGER, happiness INTEGER, energy INTEGER, color INTEGER) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Dragons");
        onCreate(db);
    }

    public void insertDragon(Dragon dragon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", dragon.getName());
        values.put("gender", dragon.getIntGender());
        values.put("level", dragon.getLevel());
        values.put("satiety", dragon.getSatiety());
        values.put("happiness", dragon.getHappiness());
        values.put("energy", dragon.getEnergy());
        values.put("color", dragon.getIntColor());
        long dragonId = db.insert("Dragon", null, values);
    }

    public int numberOfDragons(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, "Dragons");
        return numRows;
    }
}
