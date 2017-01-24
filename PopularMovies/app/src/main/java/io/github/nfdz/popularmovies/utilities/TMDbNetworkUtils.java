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

public class TMDbNetworkUtils {

    public static enum OrderCriteria { MOST_POPULAR, HIGHEST_RATED };

    private static final String TAG = TMDbNetworkUtils.class.getSimpleName();

    private static final String ERROR_URL = "There was an error building URL. ";
    private static final String ERROR_CONN = "There was an error with network connection. ";
    private static final String ERROR_NO_DATA = "There was an error with server response. It is empty. ";

    // TODO: Insert your TMDb API key here
    private static final String TMDB_API_KEY = "";

    private static final String POPULAR_MOVIES_BASE_URL = "https://api.themoviedb.org/3/movie/popular?";
    private static final String TOP_RATED_MOVIES_BASE_URL = "https://api.themoviedb.org/3/movie/top_rated?";
    private static final String CONFIGURATION_BASE_URL = " https://api.themoviedb.org/3/configuration?";

    static final String PAGE_PARAM = "page";
    static final String LANGUAGE_PARAM = "language";
    static final String API_KEY_PARAM = "api_key";

    static final String DEFAULT_LANG = "en-US";
    static final String DEFAULT_PAGE = "1";

    // https://developers.themoviedb.org/3/movies/get-popular-movies
    public static URL buildMoviesURL(OrderCriteria order) throws TMDbException {
        String baseUrl = order.equals(OrderCriteria.MOST_POPULAR) ? POPULAR_MOVIES_BASE_URL
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

    // https://developers.themoviedb.org/3/configuration/get-api-configuration
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
