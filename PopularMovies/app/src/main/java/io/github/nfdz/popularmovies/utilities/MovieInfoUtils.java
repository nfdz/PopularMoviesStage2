/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.utilities;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.nfdz.popularmovies.data.MovieContract;
import io.github.nfdz.popularmovies.types.MovieInfo;

public class MovieInfoUtils {

    public static final String[] MOVIES_PROJECTION = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_SYNOPSIS,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_POSTER_PATHS
    };

    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_TITLE = 1;
    public static final int INDEX_MOVIE_RELEASE_DATE = 2;
    public static final int INDEX_MOVIE_RATING = 3;
    public static final int INDEX_MOVIE_SYNOPSIS = 4;
    public static final int INDEX_MOVIE_POSTER_PATHS = 5;

    public static final String PATHS_SEPARATOR = ";";

    public static Map<Integer, MovieInfo> getMoviesFromCursor(Cursor cursor) {
        Map<Integer, MovieInfo> movies = new HashMap<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(INDEX_MOVIE_ID);
            String title = cursor.getString(INDEX_MOVIE_TITLE);
            String releaseDate = cursor.getString(INDEX_MOVIE_RELEASE_DATE);
            double rating = cursor.getDouble(INDEX_MOVIE_RATING);
            String synopsis = cursor.getString(INDEX_MOVIE_SYNOPSIS);
            String mergedPaths = cursor.getString(INDEX_MOVIE_POSTER_PATHS);
            String[] posterPaths = splitPosterPaths(mergedPaths);
            movies.put(id, new MovieInfo(title, releaseDate, rating, synopsis, posterPaths));
        }
        return movies;
    }

    public static String[] splitPosterPaths(String mergedPaths) {
        return mergedPaths.split(PATHS_SEPARATOR);
    }

    public static String mergePosterPaths(String[] paths) {
        StringBuilder mergedPaths = new StringBuilder();
        for (String path : paths) {
            mergedPaths.append(path);
            mergedPaths.append(PATHS_SEPARATOR);
        }
        return mergedPaths.toString();
    }

    public static ContentValues getContentValuesFor(MovieInfo movie) {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        values.put(MovieContract.MovieEntry.COLUMN_RATING, movie.getRating());
        values.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, movie.getSynopsis());
        values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATHS,
                mergePosterPaths(movie.getPosterPaths()));
        return values;
    }
}
