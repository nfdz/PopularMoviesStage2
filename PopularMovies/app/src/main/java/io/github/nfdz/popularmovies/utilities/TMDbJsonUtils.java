/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.utilities;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.nfdz.popularmovies.types.MovieInfo;

/**
 * These utilities will be used to TMDb JSON data.
 */
public class TMDbJsonUtils {

    private static final String TAG = TMDbJsonUtils.class.getSimpleName();

    // Error message strings
    private static final String ERROR_JSON = "There was an error processing JSON. ";
    private static final String ERROR_NO_DATA = "There is no required data in JSON.";
    private static final String ERROR_API = "There is a problem with API key.";
    private static final String ERROR_RESOURCE = "There is a problem with request resource (could not be found).";

    // Movies JSON object nodes
    private static final String RESULTS_NODE = "results";
    private static final String ID_NODE = "id";
    private static final String POSTER_NODE = "poster_path";
    private static final String SYNOPSIS_NODE = "overview";
    private static final String TITLE_NODE = "title";
    private static final String RELEASE_NODE = "release_date";
    private static final String VOTE_AVG_NODE = "vote_average";
    private static final String BACKDROP_AVG_NODE = "backdrop_path";


    // Configuration JSON object nodes
    private static final String IMAGES_NODE = "images";
    private static final String URL_NODE = "base_url";
    private static final String POSTER_SIZES_NODE = "poster_sizes";
    private static final String BACKDROP_SIZES_NODE = "backdrop_sizes";

    // Movie videos JSON object nodes
    private static final String VIDEO_RESULTS_NODE = "results";
    private static final String VIDEO_KEY_NODE = "key";
    private static final String VIDEO_SITE_NODE = "site";
    private static final String VIDEO_NAME_NODE = "name";

    private static final String YOUTUBE_SITE = "YouTube";
    private static final String YOUTUBE_BASE_PATH = "https://www.youtube.com/watch?v=";

    private static final String ERROR_CODE_NODE = "status_code";
    private static final int ERROR_CODE_API = 7;
    private static final int ERROR_CODE_RES = 34;

    /**
     * This method parses JSON from a movies request response and returns a List of MovieInfo
     * object that contains all information in application data model way.
     *
     * @param moviesJsonStr JSON response from server for any kind of movies request.
     * @param posterBasePaths Configured base paths for movie images.
     * @param backdropBasePaths Configured base paths for movie images.
     * @return List of MovieInfo objects.
     * @throws TMDbException If JSON data cannot be properly parsed.
     */
    public static List<MovieInfo> getMoviesFromJson(String moviesJsonStr,
                                                    String[] posterBasePaths,
                                                    String[] backdropBasePaths) throws TMDbException {
        try {
            List<MovieInfo> result = new ArrayList<MovieInfo>();
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            checkNoErrorCode(moviesJson);

            JSONArray resultsNode = moviesJson.getJSONArray(RESULTS_NODE);
            for (int i = 0; i < resultsNode.length(); i++) {
                JSONObject movieNode = resultsNode.getJSONObject(i);
                int id = movieNode.getInt(ID_NODE);
                String synopsis = movieNode.getString(SYNOPSIS_NODE);
                String title = movieNode.getString(TITLE_NODE);
                String releaseDate = movieNode.getString(RELEASE_NODE);
                double rating = movieNode.getDouble(VOTE_AVG_NODE);

                String posterRes = movieNode.getString(POSTER_NODE);
                String[] posterPaths = new String[posterBasePaths.length];
                for (int j = 0; j < posterBasePaths.length; j++) {
                    posterPaths[j] = posterBasePaths[j] + posterRes;
                }

                String backdropRes = movieNode.getString(BACKDROP_AVG_NODE);
                String[] backdropPaths = new String[posterBasePaths.length];
                for (int j = 0; j < backdropBasePaths.length; j++) {
                    backdropPaths[j] = backdropBasePaths[j] + backdropRes;
                }

                MovieInfo movie = new MovieInfo(id,
                                                title,
                                                releaseDate,
                                                rating,
                                                synopsis,
                                                posterPaths,
                                                backdropPaths);
                result.add(movie);
            }
            return result;
        } catch(JSONException e) {
            throw new TMDbException(ERROR_JSON, e);
        }
    }

    private static void checkNoErrorCode(JSONObject root) throws TMDbException, JSONException {
        if (root.has(ERROR_CODE_NODE)) {
            int errorCode = root.getInt(ERROR_CODE_NODE);
            if (errorCode == ERROR_CODE_API) throw new TMDbException(ERROR_API);
            if (errorCode == ERROR_CODE_RES) throw new TMDbException(ERROR_RESOURCE);
        }
    }

    /**
     * This method parse JSON from a configuration request response and returns the base path
     * for all movie poster images.
     *
     * @param configJsonStr JSON response from server for configuration request.
     * @return Base paths URLs for all movie poster images.
     * @throws TMDbException If JSON data cannot be properly parsed.
     */
    public static String[] getPosterBasePathsFromJson(String configJsonStr) throws TMDbException {
        return getImagesBasePathsFromJson(configJsonStr, POSTER_SIZES_NODE);
    }

    public static String[] getBackdropBasePathsFromJson(String configJsonStr) throws TMDbException {
        return getImagesBasePathsFromJson(configJsonStr, BACKDROP_SIZES_NODE);
    }

    private static String[] getImagesBasePathsFromJson(String configJsonStr, String imageNode) throws TMDbException {
        try {
            JSONObject configJson = new JSONObject(configJsonStr);
            checkNoErrorCode(configJson);
            JSONObject imagesConfJson = configJson.getJSONObject(IMAGES_NODE);
            String baseUrl = imagesConfJson.getString(URL_NODE);
            JSONArray sizesArray = imagesConfJson.getJSONArray(imageNode);

            int length = sizesArray.length();
            if (length <= 0) {
                throw new TMDbException(ERROR_NO_DATA);
            }
            String[] sizes = new String[length];
            for (int i = 0; i < length; i++) {
                sizes[i] = baseUrl + sizesArray.getString(i);
            }
            return sizes;
        } catch(JSONException e) {
            throw new TMDbException(ERROR_JSON, e);
        }
    }


    /**
     * This method gets and builds youtube video paths contained in given JSON.
     * It ignores any other video website because it does not know how to compose the path.
     * @param videosJsonStr
     * @return A map with video name as key and video path as value.
     * @throws TMDbException
     */
    public static Map<String, String> getVideoPathsFromJson(String videosJsonStr) throws TMDbException {
        try {
            JSONObject videosJson = new JSONObject(videosJsonStr);
            checkNoErrorCode(videosJson);
            JSONArray resultsNode = videosJson.getJSONArray(VIDEO_RESULTS_NODE);
            Map<String, String> videoPaths = new HashMap<>();
            for (int i = 0; i < resultsNode.length(); i++) {
                JSONObject videoNode = resultsNode.getJSONObject(i);
                String videoSite = videoNode.getString(VIDEO_SITE_NODE);
                // check that the site is youtube
                if (videoSite.toLowerCase().equals(YOUTUBE_SITE.toLowerCase())) {
                    String videoName = videoNode.getString(VIDEO_NAME_NODE);
                    String videoKey = videoNode.getString(VIDEO_KEY_NODE);
                    String videoPath = YOUTUBE_BASE_PATH + videoKey;
                    videoPaths.put(videoName, videoPath);
                }
            }
            return videoPaths;
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
