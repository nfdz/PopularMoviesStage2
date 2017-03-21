/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.nfdz.popularmovies.data.MovieContract;
import io.github.nfdz.popularmovies.types.MovieInfo;
import io.github.nfdz.popularmovies.utilities.MovieInfoUtils;
import io.github.nfdz.popularmovies.utilities.TMDbException;
import io.github.nfdz.popularmovies.utilities.TMDbJsonUtils;
import io.github.nfdz.popularmovies.utilities.TMDbNetworkUtils;

public class MoviesTasks {

    private static final String TAG = MoviesTasks.class.getSimpleName();
    private static final String ERROR_FETCH_MOVIES = "There was an error retrieving movies data. Criteria: ";

    public static final String ACTION_SYNC_MOVIES = "increment-water-count";

    public static void executeTask(Context context, String action, Uri data) {
        if (ACTION_SYNC_MOVIES.equals(action)) {
            syncMovies(context);
        }
    }

    /**
     * This method sync all local movies data with last internet data.
     * @param context
     */
    synchronized public static void syncMovies(Context context) {
        // retrieve most popular movies
        List<MovieInfo> popularMovies;
        try {
            popularMovies = fetchMoviesWithCriteria(TMDbNetworkUtils.MOST_POPULAR_FLAG);
        } catch (TMDbException e) {
            Log.d(TAG, ERROR_FETCH_MOVIES + "most popular.", e);
            return;
        }

        // retrieve highest rated movies
        List<MovieInfo> highestRatedMovies;
        try {
            highestRatedMovies = fetchMoviesWithCriteria(TMDbNetworkUtils.HIGHEST_RATED_FLAG);
        } catch (TMDbException e) {
            Log.d(TAG, ERROR_FETCH_MOVIES + "highest rated.", e);
            return;
        }

        // get favorite movies from local provider
        ContentResolver contentResolver = context.getContentResolver();
        Cursor favoritesCursor = contentResolver.query(MovieContract.FavoriteMovieEntry.CONTENT_URI,
                MovieInfoUtils.MOVIES_PROJECTION,
                null,
                null,
                null);
        Map<Integer, MovieInfo> favoriteMovies = MovieInfoUtils.getMoviesFromCursor(favoritesCursor);

        // delete all information about popular and highest rated movies
        contentResolver.delete(
                MovieContract.PopularMovieEntry.CONTENT_URI,
                null,
                null);
        contentResolver.delete(
                MovieContract.HighestRatedMovieEntry.CONTENT_URI,
                null,
                null);
        // delete all movies that are not saved as favorites
        String where = buildDeleteSqlWhereNotIds(favoriteMovies.keySet());
        contentResolver.delete(
                MovieContract.PopularMovieEntry.CONTENT_URI,
                where,
                null);

        // insert popular movies (avoid insert already contained movies)
        Map<Integer, MovieInfo> insertedPopularMovies = new HashMap<>();
        for (MovieInfo movie : popularMovies) {
            // use stored id if exists
            if (!favoriteMovies.containsKey(movie.getMovieId())) {
                // insert movie
                ContentValues movieValues = MovieInfoUtils.getContentValuesFor(movie);
                contentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);
                insertedPopularMovies.put(movie.getMovieId(), movie);
            }
            // insert popular
            ContentValues popularValues = new ContentValues();
            popularValues.put(MovieContract.PopularMovieEntry.COLUMN_MOVIE_ID, movie.getMovieId());
            contentResolver.insert(MovieContract.PopularMovieEntry.CONTENT_URI, popularValues);
        }

        // insert highest rated movies (avoid insert already contained movies)
        for (MovieInfo movie : highestRatedMovies) {
            // use stored id if exists, look for in favorite movies and inserted popular movies
            if (!favoriteMovies.containsKey(movie.getMovieId()) &&
                    !insertedPopularMovies.containsKey(movie.getMovieId())) {
                // insert movie
                ContentValues movieValues = MovieInfoUtils.getContentValuesFor(movie);
                contentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);
            }
            // insert highest rated
            ContentValues popularValues = new ContentValues();
            popularValues.put(MovieContract.HighestRatedMovieEntry.COLUMN_MOVIE_ID, movie.getMovieId());
            contentResolver.insert(MovieContract.HighestRatedMovieEntry.CONTENT_URI, popularValues);
        }
    }

    private static String buildDeleteSqlWhereNotIds(Set<Integer> ids) {
        StringBuilder whereBuilder = new StringBuilder();
        Iterator<Integer> it = ids.iterator();
        while (it.hasNext()) {
            whereBuilder.append("(");
            whereBuilder.append(MovieContract.MovieEntry._ID);
            whereBuilder.append(" <> ");
            whereBuilder.append(it.next());
            whereBuilder.append(")");
            if (it.hasNext()) whereBuilder.append(" AND ");
        }
        return whereBuilder.toString();
    }

    private static List<MovieInfo> fetchMoviesWithCriteria(int criteria) throws TMDbException {
        URL configRequestUrl = TMDbNetworkUtils.buildConfigURL();
        String jsonConfigResponse = TMDbNetworkUtils.getResponseFromHttpUrl(configRequestUrl);
        String[] posterBasePaths = TMDbJsonUtils.getPosterBasePathsFromJson(jsonConfigResponse);
        String[] backdropBasePaths = TMDbJsonUtils.getBackdropBasePathsFromJson(jsonConfigResponse);
        URL moviesRequestUrl = TMDbNetworkUtils.buildMoviesURL(criteria);
        String moviesJsonResponse = TMDbNetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
        return TMDbJsonUtils.getMoviesFromJson(moviesJsonResponse, posterBasePaths, backdropBasePaths);
    }
}