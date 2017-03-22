/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.utilities;


import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the TMDb servers.
 */
public class TMDbNetworkUtils {

    /** Sort criteria most popular flag */
    public static final int MOST_POPULAR_FLAG = 0;
    /** Sort criteria highest rated flag */
    public static final int HIGHEST_RATED_FLAG = 1;

    private static final String TAG = TMDbNetworkUtils.class.getSimpleName();

    // Error message strings
    private static final String ERROR_URL = "There was an error building URL. ";
    private static final String ERROR_CONN = "There was an error with network connection. ";
    private static final String ERROR_NO_DATA = "There was an error with server response. It is empty. ";

    // TODO: Insert your TMDb API key here
    private static final String TMDB_API_KEY = "";

    // TMDb base URLs
    private static final String POPULAR_MOVIES_BASE_URL = "https://api.themoviedb.org/3/movie/popular?";
    private static final String TOP_RATED_MOVIES_BASE_URL = "https://api.themoviedb.org/3/movie/top_rated?";
    private static final String CONFIGURATION_BASE_URL = "https://api.themoviedb.org/3/configuration?";
    private static final String MOVIE_VIDEOS_BASE_FORMAT_URL = "https://api.themoviedb.org/3/movie/%d/videos?";
    private static final String MOVIE_REVIEWS_BASE_FORMAT_URL = "https://api.themoviedb.org/3/movie/%d/reviews?";

    // URL movies params
    static final String PAGE_PARAM = "page";
    static final String LANGUAGE_PARAM = "language";
    static final String API_KEY_PARAM = "api_key";

    // Default values for URL movies params
    static final String DEFAULT_LANG = "en-US";
    static final String DEFAULT_PAGE = "1";


    /**
     * This method builds the URL needed to request movies list. This list depends on
     * given sort criteria.
     * For more information: https://developers.themoviedb.org/3/movies/get-popular-movies
     *
     * @param criteria flag to know what URL has to request.
     * @return The URL to use to query the movies server.
     * @throws TMDbException Exception building URL.
     */
    public static URL buildMoviesURL(int criteria) throws TMDbException {
        String baseUrl = criteria == MOST_POPULAR_FLAG ? POPULAR_MOVIES_BASE_URL
                                                       : TOP_RATED_MOVIES_BASE_URL;
        Uri builtUri = Uri.parse(baseUrl).buildUpon()
                                         .appendQueryParameter(API_KEY_PARAM, TMDB_API_KEY)
                                         .appendQueryParameter(LANGUAGE_PARAM, DEFAULT_LANG)
                                         .appendQueryParameter(PAGE_PARAM, DEFAULT_PAGE)
                                         .build();

        try {
            URL url = new URL(builtUri.toString());
            Log.v(TAG, "Built movies URL: " + url);
            return url;
        } catch (MalformedURLException e) {
            throw new TMDbException(ERROR_URL, e);
        }
    }


    /**
     * This method builds the URL needed to request TMDb configuration.
     * For more information: https://developers.themoviedb.org/3/configuration/get-api-configuration
     *
     * @return The URL to use to query the movies server.
     * @throws TMDbException TMDbException Exception building URL.
     */
    public static URL buildConfigURL() throws TMDbException {
        Uri builtUri = Uri.parse(CONFIGURATION_BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, TMDB_API_KEY)
                .build();

        try {
            URL url = new URL(builtUri.toString());
            Log.v(TAG, "Built configuration URL: " + url);
            return url;
        } catch (MalformedURLException e) {
            throw new TMDbException(ERROR_URL, e);
        }
    }

    /**
     * This method builds the URL needed to request TMDb movie videos.
     * For more information: https://developers.themoviedb.org/3/movies/get-movie-videos
     *
     * @param movieId
     * @return The URL to use to query the movies videos server.
     * @throws TMDbException TMDbException Exception building URL.
     */
    public static URL buildMovieVideosURL(int movieId) throws TMDbException {

        String movieVideosBasePath = String.format(MOVIE_VIDEOS_BASE_FORMAT_URL, movieId);
        Uri builtUri = Uri.parse(movieVideosBasePath).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, TMDB_API_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, DEFAULT_LANG)
                .build();

        try {
            URL url = new URL(builtUri.toString());
            Log.v(TAG, "Built movie videos URL: " + url);
            return url;
        } catch (MalformedURLException e) {
            throw new TMDbException(ERROR_URL, e);
        }
    }

    /**
     * This method builds the URL needed to request TMDb movie reviews.
     * For more information: https://developers.themoviedb.org/3/movies/get-movie-reviews
     *
     * @param movieId
     * @return The URL to use to query the movies reviews server.
     * @throws TMDbException TMDbException Exception building URL.
     */
    public static URL buildMovieReviewsURL(int movieId) throws TMDbException {

        String movieReviewsBasePath = String.format(MOVIE_REVIEWS_BASE_FORMAT_URL, movieId);
        Uri builtUri = Uri.parse(movieReviewsBasePath).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, TMDB_API_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, DEFAULT_LANG)
                .build();

        try {
            URL url = new URL(builtUri.toString());
            Log.v(TAG, "Built movie reviews URL: " + url);
            return url;
        } catch (MalformedURLException e) {
            throw new TMDbException(ERROR_URL, e);
        }
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws TMDbException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws TMDbException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                throw new TMDbException(ERROR_NO_DATA);
            }
        } catch (IOException e) {
            throw new TMDbException(ERROR_CONN, e);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

}
