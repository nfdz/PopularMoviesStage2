/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MovieContentProvider extends ContentProvider {

    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIES_WITH_ID = 101;
    public static final int CODE_POPULAR_MOVIES = 200;
    public static final int CODE_HIGHEST_RATED_MOVIES = 300;
    public static final int CODE_FAVORITE_MOVIES = 400;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private MovieDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIES, CODE_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#", CODE_MOVIES_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/" +
                MovieContract.PATH_POPULAR_MOVIES, CODE_POPULAR_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/" +
                MovieContract.PATH_HIGHEST_RATED_MOVIES, CODE_HIGHEST_RATED_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/" +
                MovieContract.PATH_FAVORITE_MOVIES, CODE_FAVORITE_MOVIES);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                return bulkInsertForTable(MovieContract.MovieEntry.TABLE_NAME, uri, values);
            case CODE_POPULAR_MOVIES:
                return bulkInsertForTable(MovieContract.PopularMovieEntry.TABLE_NAME, uri, values);
            case CODE_HIGHEST_RATED_MOVIES:
                return bulkInsertForTable(MovieContract.HighestRatedMovieEntry.TABLE_NAME, uri, values);
            case CODE_FAVORITE_MOVIES:
                return bulkInsertForTable(MovieContract.FavoriteMovieEntry.TABLE_NAME, uri, values);
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private int bulkInsertForTable(String tableName, Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        int rowsInserted = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    rowsInserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (rowsInserted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsInserted;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_MOVIES_WITH_ID:
                String idString = uri.getLastPathSegment();
                selection = MovieContract.MovieEntry._ID + " = ? ";
                selectionArgs = new String[]{idString};
                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_POPULAR_MOVIES:
                cursor = queryMoviesFromReferencedTable(MovieContract.PopularMovieEntry.TABLE_NAME,
                        MovieContract.PopularMovieEntry.COLUMN_MOVIE_ID,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder);
                break;
            case CODE_HIGHEST_RATED_MOVIES:
                cursor = queryMoviesFromReferencedTable(MovieContract.HighestRatedMovieEntry.TABLE_NAME,
                        MovieContract.HighestRatedMovieEntry.COLUMN_MOVIE_ID,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder);
                break;
            case CODE_FAVORITE_MOVIES:
                cursor = queryMoviesFromReferencedTable(MovieContract.FavoriteMovieEntry.TABLE_NAME,
                        MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor queryMoviesFromReferencedTable(String tableName,
                                                  String column,
                                                  String[] projection,
                                                  String selection,
                                                  String[] selectionArgs,
                                                  String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        sqLiteQueryBuilder.setTables(tableName + " INNER JOIN " + MovieContract.MovieEntry.TABLE_NAME +
                " ON " + tableName + "." + column + " = " + MovieContract.MovieEntry.TABLE_NAME +
                "." + MovieContract.MovieEntry._ID
        );
        return sqLiteQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        int numRowsDeleted;
        if (selection == null) selection = "1";

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;
            case CODE_POPULAR_MOVIES:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.PopularMovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case CODE_HIGHEST_RATED_MOVIES:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.HighestRatedMovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case CODE_FAVORITE_MOVIES:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MovieContract.FavoriteMovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Get type is not implemented in PopularMovies");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        String tableName;
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                returnUri = MovieContract.MovieEntry.CONTENT_URI;
                tableName = MovieContract.MovieEntry.TABLE_NAME;
                break;
            case CODE_POPULAR_MOVIES:
                returnUri = MovieContract.PopularMovieEntry.CONTENT_URI;
                tableName = MovieContract.PopularMovieEntry.TABLE_NAME;
                break;
            case CODE_HIGHEST_RATED_MOVIES:
                returnUri = MovieContract.HighestRatedMovieEntry.CONTENT_URI;
                tableName = MovieContract.HighestRatedMovieEntry.TABLE_NAME;
                break;
            case CODE_FAVORITE_MOVIES:
                returnUri = MovieContract.FavoriteMovieEntry.CONTENT_URI;
                tableName = MovieContract.FavoriteMovieEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id = db.insert(tableName, null, contentValues);
        if (_id != -1) {
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            throw new android.database.SQLException("Can not insert a row in: " + uri);
        }
        return returnUri;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        throw new UnsupportedOperationException("Update is not implemented in PopularMovies");
    }
}
