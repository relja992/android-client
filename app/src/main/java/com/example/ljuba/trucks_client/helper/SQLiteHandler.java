package com.example.ljuba.trucks_client.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_USER = "user";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_CREATED_AT = "created_at";

    // Location table name
    private static final String TABLE_LOCATION = "location";

    private static final String KEY_LOCATION_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_TRAVEL_ORDER_ID = "travel_order_id";
    private static final String KEY_DRIVER_ID = "driver_id";
    private static final String KEY_SUCCESSFULLY_SENT_TO_SERVER= "successfully";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_LOCATION_TABLE = "CREATE TABLE " + TABLE_LOCATION + "("
                + KEY_LOCATION_ID + " INTEGER PRIMARY KEY," + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT," + KEY_TRAVEL_ORDER_ID + " INTEGER,"
                + KEY_DRIVER_ID + " INTEGER," + KEY_SUCCESSFULLY_SENT_TO_SERVER + " INTEGER" + ")";
        db.execSQL(CREATE_LOCATION_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(String name, String email, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_UID, uid); // Email
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
            user.put("created_at", cursor.getString(4));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * Storing location details in database
     * */
    public void logLocation(String latitude, String longitude, int travelOrderID, int driverID, int successfully) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, latitude); // Geografska duzina
        values.put(KEY_LONGITUDE, longitude); // Geografska sirina
        values.put(KEY_TRAVEL_ORDER_ID, travelOrderID); // ID putnog naloga
        values.put(KEY_DRIVER_ID, driverID); // ID vozaca
        values.put(KEY_SUCCESSFULLY_SENT_TO_SERVER, successfully); // Uspesnot 1/0 (ok/not ok)

        // Inserting Row
        long id = db.insert(TABLE_LOCATION, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New location inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getLastLocation() {
        HashMap<String, String> location = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_LOCATION + " ORDER BY id DESC LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            location.put("latitude", cursor.getString(1));
            location.put("longitude", cursor.getString(2));
            location.put("travelOrderID", cursor.getString(3));
            location.put("driverID", cursor.getString(4));
            location.put("successfully", cursor.getString(5));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + location.toString());

        return location;
    }

    public List getUnsentLocations() {
        ArrayList<String[]> neposlateLokacije = new ArrayList();
        String selectQuery = "SELECT * FROM " + TABLE_LOCATION + " WHERE successfully = 0";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false){

            String[] lokacija = new String[5];

            lokacija[0] = cursor.getString(0);
            lokacija[1] = cursor.getString(1);
            lokacija[2] = cursor.getString(2);
            lokacija[3] = cursor.getString(3);
            lokacija[4] = cursor.getString(4);

            neposlateLokacije.add(lokacija);

            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        // return lokacije
        Log.d(TAG, "Fetching locations from Sqlite: " + neposlateLokacije.toString());

        return neposlateLokacije;
    }

    public void setLocationSent(String[] location) {

        String selectQuery = "UPDATE " + TABLE_LOCATION + " SET successfully = 1 WHERE " + location[0];

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Move to first row
        cursor.moveToFirst();
        cursor.close();
        db.close();

        // return user
        Log.d(TAG, "Setting to sent unsent location with id from Sqlite: " + location[0]);
    }

    public int countUnsentLocations() {
        int counter = 0;
        String selectQuery = "SELECT count(*) FROM " + TABLE_LOCATION + " WHERE successfully = 0";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        counter = cursor.getInt(0);
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching number of unsent locations from Sqlite: " + counter);

        return counter;
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }


}
