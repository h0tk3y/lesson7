package com.ifmomd.igushkin.rss_reader;
/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FeedsDBAdapter {

    private static final String LOG_TAG = "FeedsDBAdapter";

    public static final String KEY_ID = "_id";
    public static final String CHANNELS_TABLE_NAME = "channels";
    public static final String ITEMS_TABLE_NAME = "items";

    //Items table
    public static final String KEY_TITLE        = "title";
    public static final String KEY_DESCRIPTION  = "description";
    public static final String KEY_LINK         = "link";
    public static final String KEY_DATE_TIME    = "date_time";
    public static final String KEY_CHANNEL_ID   = "channel_id";

    public static final String ITEMS_TABLE_CREATE_QUERY = "CREATE TABLE " + ITEMS_TABLE_NAME + " (" +
                                                          KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                          KEY_TITLE + " TEXT, " +
                                                          KEY_DESCRIPTION + " TEXT, " +
                                                          KEY_LINK + " TEXT, " +
                                                          KEY_DATE_TIME + " INTEGER, " +
                                                          KEY_CHANNEL_ID + " INTEGER, " +
                                                          "FOREIGN KEY (" + KEY_CHANNEL_ID + ") REFERENCES "+
                                                          CHANNELS_TABLE_NAME+ "(" + KEY_ID + ") ON DELETE CASCADE, "+
                                                          "UNIQUE ("+KEY_LINK+") ON CONFLICT IGNORE)";

    //Channels table
    public static final String KEY_NAME            = "name";
    //KEY_LINK
    public static final String KEY_LAST_UPDATE     = "last_update";

    public static final String CHANNELS_TABLE_CREATE_QUERY = "CREATE TABLE " + CHANNELS_TABLE_NAME + " (" +
                                                             KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                             KEY_NAME + " TEXT, " +
                                                             KEY_LINK + " TEXT, " +
                                                             KEY_LAST_UPDATE + " INTEGER)";


    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME    = "feeds_data";
    private static final int    DATABASE_VERSION = 10;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        @Override
        public void onOpen(SQLiteDatabase db) {
            db.execSQL("PRAGMA foreign_keys=ON");
        }

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(ITEMS_TABLE_CREATE_QUERY);
            db.execSQL(CHANNELS_TABLE_CREATE_QUERY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                           + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + ITEMS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + CHANNELS_TABLE_NAME);
            onCreate(db);
        }
    }

    public FeedsDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public FeedsDBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createItem(String title, String description, String link, long time, long channelID) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_DESCRIPTION, description);
        initialValues.put(KEY_LINK, link);
        initialValues.put(KEY_DATE_TIME, time);
        initialValues.put(KEY_CHANNEL_ID, channelID);

        return mDb.insert(ITEMS_TABLE_NAME, null, initialValues);
    }

    public long createChannel(String name, String link, long time) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_LINK, link);
        initialValues.put(KEY_LAST_UPDATE, time);

        return mDb.insert(CHANNELS_TABLE_NAME, null, initialValues);
    }

    public boolean deleteChannel(long id) {
        return mDb.delete(CHANNELS_TABLE_NAME, KEY_ID + "=" + id, null) > 0;
    }

    public Cursor fetchAllItems() {
        return mDb.query(ITEMS_TABLE_NAME, new String[]{KEY_ID, KEY_TITLE,
                KEY_DESCRIPTION, KEY_LINK, KEY_DATE_TIME, KEY_CHANNEL_ID}, null, null, null, null, KEY_DATE_TIME+" DESC");
    }

    public Cursor fetchItemsByChannel(long channel_id) {
        return mDb.query(ITEMS_TABLE_NAME, new String[]{KEY_ID, KEY_TITLE,
                KEY_DESCRIPTION, KEY_LINK, KEY_DATE_TIME, KEY_CHANNEL_ID}, KEY_CHANNEL_ID + "=" + channel_id, null, null, null, KEY_DATE_TIME+" DESC");
    }

    public Cursor fetchAllChannels() {
        return mDb.query(CHANNELS_TABLE_NAME, new String[]{KEY_ID, KEY_NAME,
                KEY_LINK, KEY_LAST_UPDATE}, null, null, null, null, null);
    }

    public Cursor fetchChannelById(long id) {
        return mDb.query(CHANNELS_TABLE_NAME, new String[]{KEY_ID, KEY_NAME,
                KEY_LINK, KEY_LAST_UPDATE}, KEY_ID + "=" + id, null, null, null, null);
    }

    public RSSChannel getChannelById(long id) {
        Cursor c = fetchChannelById(id);
        if (c.getCount() > 0)
            c.moveToFirst();
        else
            return null;
        RSSChannel result = new RSSChannel();
        result.id = id;
        result.title = c.getString(c.getColumnIndex(KEY_NAME));
        result.link = c.getString(c.getColumnIndex(KEY_LINK));
        result.lasUpdated = c.getLong(c.getColumnIndex(KEY_LAST_UPDATE));
        return result;
    }

    public boolean updateChannelInfo(long id, String name, String link) {
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_NAME, name);
        newValues.put(KEY_LINK, link);
        return mDb.update(CHANNELS_TABLE_NAME, newValues, KEY_ID + "=" +id, null) > 0;
    }

    public boolean deleteChannelById(long id) {
        return mDb.delete(CHANNELS_TABLE_NAME, KEY_ID + "=" +id, null) > 0;
    }

    public Cursor fetchItem(long id) throws SQLException {
        Cursor mCursor =

                mDb.query(true, ITEMS_TABLE_NAME, new String[]{KEY_ID,
                        KEY_TITLE, KEY_DESCRIPTION, KEY_LINK, KEY_DATE_TIME, KEY_CHANNEL_ID}, KEY_ID + "=" + id, null,
                          null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}
