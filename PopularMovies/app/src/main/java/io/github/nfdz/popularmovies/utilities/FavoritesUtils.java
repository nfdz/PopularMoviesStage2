/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import io.github.nfdz.popularmovies.data.MovieContract;
import io.github.nfdz.popularmovies.sync.MoviesTasks;
import io.github.nfdz.popularmovies.types.AsyncTaskListener;

/**
 * This class has methods to manage favorite movies in a safe way (avoid computing long tasks in
 * UI thread).
 */
public class FavoritesUtils {

    /**
     * Interface operation callback.
     */
    public interface  ResolveFavoriteCallback {
        void notifyResult(boolean isFavorite);
    }

    /**
     * This method executes in a background thread the operations needed to resolve if the given
     * movie id is marked as favorite. It notifies using given callback in UI thread.
     * @param context
     * @param movieId
     * @param callback
     */
    public static void resolveFavorite(Context context, int movieId, ResolveFavoriteCallback callback) {
        new ResolveFavoriteTask(context, movieId, callback).execute();
    }

    /**
     * Async task implementation to resolve if a movie is marked as favorite.
     */
    private static class ResolveFavoriteTask extends AsyncTask<Void, Void, Boolean> {

        private final ResolveFavoriteCallback mCallback;
        private final int mMovieId;
        private final Context mContext;

        public ResolveFavoriteTask(Context context, int movieId, ResolveFavoriteCallback callback) {
            mCallback = callback;
            mMovieId = movieId;
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... v) {
            Uri queryUri = MovieContract.FavoriteMovieEntry.CONTENT_URI;
            String[] projection = { MovieContract.FavoriteMovieEntry.TABLE_NAME + "." + MovieContract.FavoriteMovieEntry._ID };
            String selection = MovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID + "=" + mMovieId;
            Cursor cursor = mContext.getContentResolver().query(queryUri, projection, selection, null, null);
            boolean isFavorite = cursor != null && cursor.getCount() > 0;
            if (cursor != null) cursor.close();
            return isFavorite;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mCallback.notifyResult(result);
        }
    }

    /**
     * This method toggles the given movie favorite state in a background thread. If movie is
     * favorite, it will remove from favorites. If movie is not favorite, it will add to favorite.
     * It notifies final favorite result using given callback in UI thread.
     * @param context
     * @param movieId
     * @param callback
     */
    public static void toggleFavorite(final Context context, final int movieId, final ResolveFavoriteCallback callback) {
        new ResolveFavoriteTask(context, movieId, new ResolveFavoriteCallback() {
            @Override
            public void notifyResult(boolean isFavorite) {
                final AsyncTaskListener<Void> innerCallback = new AsyncTaskListener<Void>() {
                    @Override
                    public void onPreTaskExecution() { /* nothing to do */ }
                    @Override
                    public void onTaskComplete(Void result) {
                        new ResolveFavoriteTask(context, movieId, callback).execute();
                    }
                };
                if (isFavorite) {
                    new RemoveFavoriteTask(context, movieId, innerCallback).execute();
                } else {
                    new InsertFavoriteTask(context, movieId, innerCallback).execute();
                }
            }
        }).execute();
    }

    /**
     * Async task implementation to remove a movie from favorites.
     */
    private static class RemoveFavoriteTask extends AsyncTask<Void, Void, Void> {

        private final AsyncTaskListener<Void> mCallback;
        private final int mMovieId;
        private final Context mContext;

        public RemoveFavoriteTask(Context context, int movieId, AsyncTaskListener<Void> callback) {
            mMovieId = movieId;
            mContext = context;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... v) {
            MoviesTasks.removeFavorite(mContext, mMovieId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mCallback.onTaskComplete(null);
        }
    }

    /**
     * Async task implementation to add a movie from favorites.
     */
    private static class InsertFavoriteTask extends AsyncTask<Void, Void, Void> {

        private final AsyncTaskListener<Void> mCallback;
        private final int mMovieId;
        private final Context mContext;

        public InsertFavoriteTask(Context context, int movieId, AsyncTaskListener<Void> callback) {
            mMovieId = movieId;
            mContext = context;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... v) {
            MoviesTasks.insertFavorite(mContext, mMovieId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mCallback.onTaskComplete(null);
        }
    }
}
