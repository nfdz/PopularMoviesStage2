/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.github.nfdz.popularmovies.data.MovieContract.PopularMovieEntry;
import io.github.nfdz.popularmovies.data.MovieContract.HighestRatedMovieEntry;
import io.github.nfdz.popularmovies.data.MovieContract.FavoriteMovieEntry;
import io.github.nfdz.popularmovies.data.MovieContract.MovieEntry;

public class MovieDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MovieEntry.SQL_CREATE_TABLE);
        db.execSQL(PopularMovieEntry.SQL_CREATE_TABLE);
        db.execSQL(HighestRatedMovieEntry.SQL_CREATE_TABLE);
        db.execSQL(FavoriteMovieEntry.SQL_CREATE_TABLE);
    }

    /**
     * This method discards the old table of data and calls onCreate to recreate a new one.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PopularMovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HighestRatedMovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteMovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
