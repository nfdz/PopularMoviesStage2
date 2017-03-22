/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.utilities;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.HashMap;
import java.util.Map;

import io.github.nfdz.popularmovies.data.MovieContract;
import io.github.nfdz.popularmovies.types.MovieInfo;

/**
 * This class has useful methods to work with movie data contract and java movie types.
 */
public class MovieInfoUtils {

    /**
     * Projection needed to build MovieInfo java object.
     */
    public static final String[] MOVIES_PROJECTION = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_SYNOPSIS,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_POSTER_PATHS,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_BACKDROP_PATHS
    };

    // Indexes for MOVIES_PROJECTION
    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_TITLE = 1;
    public static final int INDEX_MOVIE_RELEASE_DATE = 2;
    public static final int INDEX_MOVIE_RATING = 3;
    public static final int INDEX_MOVIE_SYNOPSIS = 4;
    public static final int INDEX_MOVIE_POSTER_PATHS = 5;
    public static final int INDEX_MOVIE_BACKDROP_PATHS = 6;

    public static final String PATHS_SEPARATOR = ";";

    /**
     * Extract MovieInfo objects contained in given cursor. It is important that cursor has
     * expected projection MOVIES_PROJECTION.
     * @param cursor
     * @return map with movie id as key and movie info as value.
     */
    public static Map<Integer, MovieInfo> getMoviesFromCursor(Cursor cursor) {
        Map<Integer, MovieInfo> movies = new HashMap<>();
        cursor.moveToFirst();
        cursor.moveToPrevious();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(INDEX_MOVIE_ID);
            String title = cursor.getString(INDEX_MOVIE_TITLE);
            String releaseDate = cursor.getString(INDEX_MOVIE_RELEASE_DATE);
            double rating = cursor.getDouble(INDEX_MOVIE_RATING);
            String synopsis = cursor.getString(INDEX_MOVIE_SYNOPSIS);
            String mergedPosterPaths = cursor.getString(INDEX_MOVIE_POSTER_PATHS);
            String[] posterPaths = splitPaths(mergedPosterPaths);
            String mergedBackdropPaths = cursor.getString(INDEX_MOVIE_BACKDROP_PATHS);
            String[] backdropPaths = splitPaths(mergedBackdropPaths);
            movies.put(id, new MovieInfo(id,
                                         title,
                                         releaseDate,
                                         rating,
                                         synopsis,
                                         posterPaths,
                                         backdropPaths));
        }
        return movies;
    }

    /**
     * This method split an array of path joined in one string to store to accomplish with
     * data contract.
     * @param mergedPaths
     * @return An array with paths.
     */
    public static String[] splitPaths(String mergedPaths) {
        return mergedPaths.split(PATHS_SEPARATOR);
    }

    /**
     * This method join an array of paths in one string to store to accomplish with data contract.
     * @param paths
     * @return string with joined paths.
     */
    public static String mergePaths(String[] paths) {
        StringBuilder mergedPaths = new StringBuilder();
        for (String path : paths) {
            mergedPaths.append(path);
            mergedPaths.append(PATHS_SEPARATOR);
        }
        return mergedPaths.toString();
    }

    /**
     * This method builds and fills a ContentValues object for given movie info java object.
     * @param movie
     * @return ContentValues
     */
    public static ContentValues getContentValuesFor(MovieInfo movie) {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry._ID, movie.getMovieId());
        values.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        values.put(MovieContract.MovieEntry.COLUMN_RATING, movie.getRating());
        values.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, movie.getSynopsis());
        values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATHS, mergePaths(movie.getPosterPaths()));
        values.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATHS, mergePaths(movie.getBackdropPaths()));
        return values;
    }
}
