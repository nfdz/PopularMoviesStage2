/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.utilities;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.nfdz.popularmovies.types.MovieInfo;

/**
 * These utilities will be used to TMDb JSON data.
 */
public class TMDbJsonUtils {

    private static final String TAG = TMDbJsonUtils.class.getSimpleName();

    // Error message strings
    private static final String ERROR_JSON = "There was an error processing JSON. ";
    private static final String ERROR_NO_DATA = "There is no required data in JSON.";

    // Movies JSON object nodes
    private static final String RESULTS_NODE = "results";
    private static final String POSTER_NODE = "poster_path";
    private static final String SYNOPSIS_NODE = "overview";
    private static final String TITLE_NODE = "title";
    private static final String RELEASE_NODE = "release_date";
    private static final String VOTE_AVG_NODE = "vote_average";

    // Configuration JSON object nodes
    private static final String IMAGES_NODE = "images";
    private static final String URL_NODE = "base_url";
    private static final String SIZES_NODE = "poster_sizes";

    /**
     * This method parses JSON from a movies request response and returns a List of MovieInfo
     * object that contains all information in application data model way.
     *
     * @param moviesJsonStr JSON response from server for any kind of movies request.
     * @param postersBasePath Configured base path for movie poster images.
     * @return List of MovieInfo objects.
     * @throws TMDbException If JSON data cannot be properly parsed.
     */
    public static List<MovieInfo> getMoviesFromJson(String moviesJsonStr,
                                                    String postersBasePath) throws TMDbException {
        try {
            List<MovieInfo> result = new ArrayList<MovieInfo>();
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsNode = moviesJson.getJSONArray(RESULTS_NODE);

            for (int i = 0; i < resultsNode.length(); i++) {
                JSONObject movieNode = resultsNode.getJSONObject(i);
                String posterPath = postersBasePath + movieNode.getString(POSTER_NODE);
                String synopsis = movieNode.getString(SYNOPSIS_NODE);
                String title = movieNode.getString(TITLE_NODE);
                String releaseDate = movieNode.getString(RELEASE_NODE);
                double rating = movieNode.getDouble(VOTE_AVG_NODE);

                MovieInfo movie = new MovieInfo(title, releaseDate, rating, synopsis, posterPath);
                result.add(movie);
            }
            return result;
        } catch(JSONException e) {
            throw new TMDbException(ERROR_JSON, e);
        }
    }

    /**
     * This method parse JSON from a configuration request response and returns the base path
     * for all movie poster images.
     *
     * @param configJsonStr JSON response from server for configuration request.
     * @param minPosterWidth Desired minimum width of the poster images in pixels.
     * @return Base path URL for all movie poster images.
     * @throws TMDbException If JSON data cannot be properly parsed.
     */
    public static String getPosterBasePathFromJson(String configJsonStr,
                                                   int minPosterWidth) throws TMDbException {
        try {
            JSONObject configJson = new JSONObject(configJsonStr);
            JSONObject imagesConfJson = configJson.getJSONObject(IMAGES_NODE);
            String baseUrl = imagesConfJson.getString(URL_NODE);
            JSONArray sizesArray = imagesConfJson.getJSONArray(SIZES_NODE);

            int length = sizesArray.length();
            if (length <= 0) {
                throw new TMDbException(ERROR_NO_DATA);
            }
            List<String> sizes = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                sizes.add(sizesArray.getString(i));
            }
            String size = resolveSize(sizes, minPosterWidth);

            return baseUrl + size;
        } catch(JSONException e) {
            throw new TMDbException(ERROR_JSON, e);
        }
    }

    /**
     * This method process a list of available sizes for images of TMDb and resolve what
     * of them is the most suitable for the app needs.
     *
     * @param sizes List of available sizes.
     * @param minPosterWidth Desired minimum width of the poster images in pixels.
     * @return The most suitable size for given requirement.
     */
    private static String resolveSize(List<String> sizes, int minPosterWidth) {
        // All sizes are like 'w154' excepts 'original'
        // Try to get the first size with width equal to or greater than half the screen
        String size = "original";
        for (String availableSize : sizes) {
            size = availableSize;
            if (size.startsWith("w")) {
                String widthStr = size.substring(1, size.length());
                int width = Integer.parseInt(widthStr);
                if (minPosterWidth < width) {
                    break;
                }
            }
        }
        Log.v(TAG, "Poster image width size is: " + size + " (Min width=" + minPosterWidth + ")");
        return size;
    }
}
