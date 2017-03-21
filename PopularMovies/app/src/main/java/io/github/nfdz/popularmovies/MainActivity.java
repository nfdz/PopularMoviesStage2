/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import io.github.nfdz.popularmovies.MoviesAdapter.MoviesAdapterOnClickHandler;
import io.github.nfdz.popularmovies.data.MovieContract;
import io.github.nfdz.popularmovies.data.PreferencesUtils;
import io.github.nfdz.popularmovies.sync.MoviesSyncUtils;
import io.github.nfdz.popularmovies.types.AsyncTaskListener;

public class MainActivity extends AppCompatActivity
        implements MoviesAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ID_MOVIES_LOADER = 82;

    public static final String SORT_BY_FLAG_KEY = "sort_by";
    public static final int MOST_POPULAR_FLAG = 0;
    public static final int HIGHEST_RATED_FLAG = 1;
    public static final int FAVORITES_FLAG = 2;

    // Activity views
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;

    private MoviesAdapter mMoviesAdapter;

    private static final String POSITION_KEY = "position";
    private int mPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) mPosition = savedInstanceState.getInt(POSITION_KEY);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // Configure recycler view
        int spanCount = getResources().getInteger(R.integer.grid_movies_columns);
        int orientation = OrientationHelper.VERTICAL;
        boolean reverseLayout = false;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount, orientation, reverseLayout);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mMoviesAdapter = new MoviesAdapter(this);
        mRecyclerView.setAdapter(mMoviesAdapter);

        // Uncomment this line to know the network(response time, cache, etc) performance of poster images
        //Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);

        showLoading();
        restartLoader();

        MoviesSyncUtils.initialize(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            // build a dialog to let user change sort criteria
            String mostPopular = getString(R.string.sort_by_popular);
            String highestRated = getString(R.string.sort_by_rated);
            String favorites = getString(R.string.sort_by_favorite);
            final String options[] = new String[] { mostPopular, highestRated, favorites };
            new FetchSortFromPreferences(this, new AsyncTaskListener<Integer>() {
                @Override
                public void onPreTaskExecution() { /* nothing to do */ }
                @Override
                public void onTaskComplete(Integer result) {
                    final int selected = result == FAVORITES_FLAG ? 2 : result == HIGHEST_RATED_FLAG ? 1 : 0;
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(getString(R.string.sort_by_dialog_title));
                    builder.setSingleChoiceItems(options, selected, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int selection) {
                            dialog.cancel();
                            // if sort criteria has changed, saved it and load movies again
                            if (selection != selected) {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... v) {
                                        String sortBy = selection == 2 ? PreferencesUtils.SORT_BY_FAVORITES :
                                                selection == 1 ? PreferencesUtils.SORT_BY_HIGHEST_RATED :
                                                        PreferencesUtils.SORT_BY_POPULAR;
                                        PreferencesUtils.setPreferredSort(MainActivity.this, sortBy);
                                        return null;
                                    }
                                }.execute();
                            }
                        }
                    });
                    builder.show();
                }
            }).execute();


            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Show movies ui views and hide the others.
     */
    private void showMoviesView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    /**
     * Show errors ui views and hide the others.
     */
    private void showLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(long id) {
        Context context = this;
        Class destination = DetailActivity.class;
        Intent intentToDetailActivity = new Intent(context, destination);
        intentToDetailActivity.setData(MovieContract.MovieEntry.buildUriWithId(id));
        startActivity(intentToDetailActivity);
    }

    @Override
    public void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferencesUtils.SORT_BY_KEY)) {
            restartLoader();
        }
    }

    /**
     * This method firstly retrieves sort configuration and secondly restart loader with it
     */
    private void restartLoader() {
        new FetchSortFromPreferences(this, new AsyncTaskListener<Integer>() {
            @Override
            public void onPreTaskExecution() { /* nothing to do */ }

            @Override
            public void onTaskComplete(Integer result) {
                Bundle args = new Bundle();
                args.putInt(SORT_BY_FLAG_KEY, result);
                getSupportLoaderManager().restartLoader(ID_MOVIES_LOADER, args, MainActivity.this);
            }
        }).execute();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {

            case ID_MOVIES_LOADER:
                Integer flag = args.getInt(SORT_BY_FLAG_KEY);
                Uri queryUri;
                String sortOrder;
                if (flag == MOST_POPULAR_FLAG) {
                    queryUri = MovieContract.PopularMovieEntry.CONTENT_URI;
                    sortOrder = MovieContract.PopularMovieEntry.TABLE_NAME + "." +
                            MovieContract.PopularMovieEntry._ID + " ASC";
                } else if (flag == HIGHEST_RATED_FLAG) {
                    queryUri = MovieContract.HighestRatedMovieEntry.CONTENT_URI;
                    sortOrder = MovieContract.HighestRatedMovieEntry.TABLE_NAME + "." +
                            MovieContract.HighestRatedMovieEntry._ID + " ASC";
                } else if (flag == FAVORITES_FLAG) {
                    queryUri = MovieContract.FavoriteMovieEntry.CONTENT_URI;
                    sortOrder = MovieContract.FavoriteMovieEntry.TABLE_NAME + "." +
                            MovieContract.FavoriteMovieEntry._ID + " ASC";
                } else {
                    throw new IllegalArgumentException("Unknown sort by flag: " + flag);
                }
                return new CursorLoader(MainActivity.this,
                        queryUri,
                        MoviesAdapter.PROJECTION,
                        null,
                        null,
                        sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMoviesAdapter.setCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        mPosition = RecyclerView.NO_POSITION; // use only once time
        if (data != null && data.getCount() != 0) showMoviesView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        showLoading();
        mMoviesAdapter.setCursor(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int lastFirstVisiblePosition = ((LinearLayoutManager)mRecyclerView.getLayoutManager())
                .findFirstCompletelyVisibleItemPosition();
        outState.putInt(POSITION_KEY, lastFirstVisiblePosition);
    }
}
