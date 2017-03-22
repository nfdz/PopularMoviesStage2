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

    public static final String SORT_BY_KEY = "sort_by";
    public static final String SORT_BY_POPULAR = "most_popular";
    public static final String SORT_BY_HIGHEST_RATED = "highest_rated";
    public static final String SORT_BY_FAVORITES = "favorites";
    public static final String SORT_BY_DEFAULT = SORT_BY_POPULAR;

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
}
