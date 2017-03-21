/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.List;

import io.github.nfdz.popularmovies.MoviesAdapter.MoviesAdapterOnClickHandler;
import io.github.nfdz.popularmovies.data.MovieContract;
import io.github.nfdz.popularmovies.sync.MoviesSyncUtils;
import io.github.nfdz.popularmovies.types.MovieInfo;
import io.github.nfdz.popularmovies.types.AsyncTaskListener;
import io.github.nfdz.popularmovies.utilities.TMDbNetworkUtils;

public class MainActivity extends AppCompatActivity
        implements MoviesAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor>{

    /** Minimum aspect ratio to set landscape mode (grid has more columns) */
    private static final float LANDSCAPE_MODE_MIN_RATIO = 0.75f;

    /** Sort criteria configuration */
    private static int sSortCriteria = TMDbNetworkUtils.MOST_POPULAR_FLAG;

    private static final int ID_MOVIES_LOADER = 82;

    // Activity views
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;

    private MoviesAdapter mMoviesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // Compute aspect ratio and decide number of columns of the grid view
        float aspectRatio = computeAspectRatio();
        int spanCount = 2;
        if (aspectRatio < LANDSCAPE_MODE_MIN_RATIO) {
            spanCount = 3;
        }

        // Configure recycler view
        int orientation = OrientationHelper.VERTICAL;
        boolean reverseLayout = false;
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount, orientation, reverseLayout);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mMoviesAdapter = new MoviesAdapter(this);
        mRecyclerView.setAdapter(mMoviesAdapter);

        // Uncomment this line to know the network(response time, cache, etc) performance of poster images
        //Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);

        getSupportLoaderManager().initLoader(ID_MOVIES_LOADER, null, this);

        MoviesSyncUtils.initialize(this);
    }

    /**
     * This method computes aspect ratio of the view using default display.
     * @return Aspect ratio of display as height/width.
     */
    private float computeAspectRatio() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        return (screenHeight+0.0f)/screenWidth;
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
            String topRated = getString(R.string.sort_by_rated);
            String options[] = new String[] {mostPopular, topRated};
            final int selected = sSortCriteria == TMDbNetworkUtils.MOST_POPULAR_FLAG ? 0 : 1;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.sort_by_dialog_title));
            builder.setSingleChoiceItems(options, selected, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int selection) {
                    dialog.cancel();
                    // if sort criteria has changed, saved it and load movies again
                    if (selection != selected) {
                        sSortCriteria = selection == 0 ? TMDbNetworkUtils.MOST_POPULAR_FLAG
                                                       : TMDbNetworkUtils.HIGHEST_RATED_FLAG;
                        getSupportLoaderManager().restartLoader(ID_MOVIES_LOADER, null, MainActivity.this);
                    }
                }
            });
            builder.show();
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
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {

            case ID_MOVIES_LOADER:
                Uri queryUri = MovieContract.PopularMovieEntry.CONTENT_URI;
                String sortOrder = MovieContract.PopularMovieEntry.TABLE_NAME + "." +
                    MovieContract.PopularMovieEntry._ID + " ASC";
                return new CursorLoader(this,
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
        // TODO save position
        // if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        // mRecyclerView.smoothScrollToPosition(mPosition);
        if (data != null && data.getCount() != 0) showMoviesView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.setCursor(null);
    }
}
