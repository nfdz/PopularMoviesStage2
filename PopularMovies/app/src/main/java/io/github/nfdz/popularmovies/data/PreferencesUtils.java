/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This class helps to retrieve and store preferences from application default shared preferences.
 */
public class PreferencesUtils {

    // sort preference key and values
    public static final String SORT_BY_KEY = "sort_by";
    public static final String SORT_BY_POPULAR = "most_popular";
    public static final String SORT_BY_HIGHEST_RATED = "highest_rated";
    public static final String SORT_BY_FAVORITES = "favorites";
    public static final String SORT_BY_DEFAULT = SORT_BY_POPULAR;

    // last sync time key
    public static final String LAST_SYNC_TIME_KEY = "last_sync_time";

    /**
     * This method retrieves stored sort preference, if there is nothing, it will return default value.
     * @param context
     * @return
     */
    public static String getPreferredSort(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(SORT_BY_KEY, SORT_BY_DEFAULT);
    }

    /**
     * This method stores given sort preference.
     * @param context
     * @param sort
     */
    public static void setPreferredSort(Context context, String sort) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SORT_BY_KEY, sort);
        editor.apply();
    }

    /**
     * This method returns last synchronization time stored in preferences. If there
     * is no value, it will return 0 millis (it is the oldest possible time in UNIX time).
     * @param context
     * @return UNIX time millis
     */
    public static long getLastSynchronizationTimeInMillis(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(LAST_SYNC_TIME_KEY, 0);
    }

    /**
     * Returns the elapsed time in milliseconds since the last synchronization.
     * @param context
     * @return Elapsed time in milliseconds since the last synchronization.
     */
    public static long getElapsedTimeSinceLastSynchronization(Context context) {
        long lastSyncTimeMillis = getLastSynchronizationTimeInMillis(context);
        long timeSinceLastSync = System.currentTimeMillis() - lastSyncTimeMillis;
        return timeSinceLastSync;
    }

    /**
     * This method stores given last synchronization time in preferences.
     * @param context
     * @param syncTime Time of last synchronization to store in UNIX time.
     */
    public static void setLastSynchronizationTime(Context context, long syncTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(LAST_SYNC_TIME_KEY, syncTime);
        editor.apply();
    }
}
