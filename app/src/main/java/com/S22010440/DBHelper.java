package com.S22010440;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

// This class helps manage the SQLite database for user info
public class DBHelper extends SQLiteOpenHelper {

    // Name of the database and version
    private static final String DB_NAME = "UserDB";
    private static final int DB_VERSION = 1;

    // Constructor
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // This method is called only once when the database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table with username as the primary key
        db.execSQL("CREATE TABLE users(username TEXT PRIMARY KEY, password TEXT)");
    }

    // This method is called if the database version is updated
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old table and recreate it
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // Insert a new user to the database
    public boolean insertUser(String username, String password) {
        // If the user already exists, don't insert again
        if (checkUserExists(username)) {
            return false;
        }

        // Get writable access and insert user data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        // Try to insert the row, if success returns a positive value
        long result = db.insert("users", null, values);
        return result != -1;
    }

    // Check if a username already exists in the database
    public boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});

        // Check if cursor has any results
        boolean exists = cursor.getCount() > 0;

        cursor.close(); // Always close cursor
        return exists;
    }
}
