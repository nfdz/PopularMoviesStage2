/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import io.github.nfdz.popularmovies.data.PreferencesUtils;
import io.github.nfdz.popularmovies.sync.MoviesTasks;
import io.github.nfdz.popularmovies.types.AsyncTaskListener;

public class FetchSortFromPreferences extends AsyncTask<Void, Void, Integer> {

    private static final String TAG = MoviesTasks.class.getSimpleName();

    private final AsyncTaskListener<Integer> mCallback;
    private final Context mContext;

    public FetchSortFromPreferences(Context context, AsyncTaskListener<Integer> callback) {
        mCallback = callback;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        mCallback.onPreTaskExecution();
    }

    @Override
    protected Integer doInBackground(Void... v) {
        String sortPref = PreferencesUtils.getPreferredSort(mContext);
        if (sortPref.equals(PreferencesUtils.SORT_BY_HIGHEST_RATED)) {
            return MainActivity.HIGHEST_RATED_FLAG;
        } else if (sortPref.equals(PreferencesUtils.SORT_BY_FAVORITES)) {
            return MainActivity.FAVORITES_FLAG;
        } else if (sortPref.equals(PreferencesUtils.SORT_BY_POPULAR)) {
            return MainActivity.MOST_POPULAR_FLAG;
        } else {
            Log.e(TAG, "Unknown sort by preference: " + sortPref);
            return -1;
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        mCallback.onTaskComplete(result);
    }
}
