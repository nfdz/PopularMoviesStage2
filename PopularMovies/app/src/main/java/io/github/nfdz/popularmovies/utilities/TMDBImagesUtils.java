/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.utilities;


import android.net.Uri;
import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class TMDBImagesUtils {

    private static final String TAG = TMDBImagesUtils.class.getSimpleName();

    private static final String ERROR_PROCESSING_IMAGE_PATH = "Image path URI does not match with expected pattern: ";

    /**
     * This method computes given array of image paths studying each width size and chooses
     * the first one that is equals or bigger than the given minimum width size.
     * If it is impossible, it will return the bigger one (usually the original one).
     * If given array is empty, it will return null.
     * It does not use any network connection and do not download any image.
     * TMBD images has following scheme:
     * https://image.tmdb.org/t/p/{size}/{image_res}
     * Size could be any number after 'w' char, for example 'w640', or 'original'.
     * @param imagePaths
     * @param minSize
     * @return image path
     */
    public static String resolveImagePath(String[] imagePaths, int minSize) {

        if (imagePaths == null || imagePaths.length == 0) return null;

        // build a sorted map width-path because array could not be sorted
        SortedMap<Integer, String> map = new TreeMap<>();

        for (String imagePath : imagePaths) {
            Uri imageUri = Uri.parse(imagePath);
            List<String> pathSegments = imageUri.getPathSegments();
            if (pathSegments.size() < 2) {
                Log.e(TAG, ERROR_PROCESSING_IMAGE_PATH + imagePath);
                return null;
            }
            String sizeSegment = pathSegments.get(pathSegments.size() - 2);
            if (sizeSegment.startsWith("w")) {
                String widthStr = sizeSegment.substring(1, sizeSegment.length());
                int width = Integer.parseInt(widthStr);
                map.put(width, imagePath);
            } else if (sizeSegment.equals("original")) {
                // save as max integer
                map.put(Integer.MAX_VALUE, imagePath);
            } else {
                Log.e(TAG, ERROR_PROCESSING_IMAGE_PATH + imagePath);
                return null;
            }
        }

        // returns the first image path that meets with minSize
        String imagePath = null;
        for (Map.Entry<Integer,String> entry : map.entrySet()) {
            if (entry.getKey() > minSize) {
                imagePath = entry.getValue();
                break;
            }
        }

        if (imagePath == null) {
            Log.e(TAG, "Can not resolve image path for this min size = " + minSize +
                    ", and this array of images = " + imagePaths);
        }
        return imagePath;
    }
}
