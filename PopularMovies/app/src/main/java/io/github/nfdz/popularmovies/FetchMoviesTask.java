/*
 * Copyright (C) 2017 Noe Fernandez
 */

package io.github.nfdz.popularmovies;


import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;
import java.util.List;

import io.github.nfdz.popularmovies.types.MovieInfo;
import io.github.nfdz.popularmovies.types.AsyncTaskListener;
import io.github.nfdz.popularmovies.utilities.TMDbException;
import io.github.nfdz.popularmovies.utilities.TMDbJsonUtils;
import io.github.nfdz.popularmovies.utilities.TMDbNetworkUtils;

/**
 * This task retrieves a list of movies from internet servers in an asynchronous way.
 */
public class FetchMoviesTask  extends AsyncTask<Integer, Void, List<MovieInfo>> {

    private static final String TAG = FetchMoviesTask.class.getSimpleName();

    /** Log error message used if there is an error retrieving movies data */
    private static final String ERROR_MOVIES = "There was an error retrieving movies data. ";

    private final AsyncTaskListener<List<MovieInfo>> mListener;

    private final int mMinPosterWidth;

    public FetchMoviesTask(int minPosterWidth, AsyncTaskListener<List<MovieInfo>> listener) {
        mListener = listener;
        mMinPosterWidth = minPosterWidth;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onPreTaskExecution();
    }

    @Override
    protected void onPostExecute(List<MovieInfo> moviesData) {
        mListener.onTaskComplete(moviesData);
    }

    @Override
    protected List<MovieInfo> doInBackground(Integer... params) {

        /* If there is no order criteria it cannot fetch movies info */
        if (params.length == 0) {
            return null;
        }
        int criteria = params[0];

        try {
            URL configRequestUrl = TMDbNetworkUtils.buildConfigURL();
            String jsonConfigResponse = TMDbNetworkUtils
                    .getResponseFromHttpUrl(configRequestUrl);
            String posterBasePath = TMDbJsonUtils
                    .getPosterBasePathFromJson(jsonConfigResponse, mMinPosterWidth);
            URL moviesRequestUrl = TMDbNetworkUtils.buildMoviesURL(criteria);
            String moviesJsonResponse = TMDbNetworkUtils
                    .getResponseFromHttpUrl(moviesRequestUrl);
            List<MovieInfo> movies = TMDbJsonUtils
                    .getMoviesFromJson(moviesJsonResponse, posterBasePath);
            return movies;
        } catch (TMDbException e) {
            Log.d(TAG, ERROR_MOVIES, e);
            return null;
        }
    }
}
