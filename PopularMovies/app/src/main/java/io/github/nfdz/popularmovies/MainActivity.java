/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.net.URL;
import java.util.List;

import io.github.nfdz.popularmovies.MoviesAdapter.MoviesAdapterOnClickHandler;
import io.github.nfdz.popularmovies.types.MovieInfo;
import io.github.nfdz.popularmovies.utilities.TMDbException;
import io.github.nfdz.popularmovies.utilities.TMDbJsonUtils;
import io.github.nfdz.popularmovies.utilities.TMDbNetworkUtils;
import io.github.nfdz.popularmovies.utilities.TMDbNetworkUtils.SortCriteria;

public class MainActivity extends AppCompatActivity implements MoviesAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ERROR_MOVIES = "There was an error retrieving movies data. ";
    private static final float LANDSCAPE_MODE_MIN_RATIO = 0.75f;

    private static SortCriteria sSortCriteria = SortCriteria.MOST_POPULAR;

    private RecyclerView mRecyclerView;
    private LinearLayout mErrorLayout;
    private ProgressBar mLoadingIndicator;

    private MoviesAdapter mMoviesAdapter;
    private int mMinPosterWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);

        mErrorLayout = (LinearLayout) findViewById(R.id.layout_error);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // TODO: Instead use only one min poster width it could use two (thumbnail and detail)
        mMinPosterWidth = getResources().getDimensionPixelSize(R.dimen.movie_item_poster_width);

        float aspectRatio = computeAspectRatio();
        int spanCount = 2;
        if (aspectRatio < LANDSCAPE_MODE_MIN_RATIO) {
            spanCount = 3;
        }
        //
        int orientation = OrientationHelper.VERTICAL;
        boolean reverseLayout = false;
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount, orientation, reverseLayout);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mMoviesAdapter = new MoviesAdapter(this);
        mRecyclerView.setAdapter(mMoviesAdapter);

        //Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);
        loadMovies();
    }

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
            String mostPopular = getString(R.string.sort_by_popular);
            String topRated = getString(R.string.sort_by_rated);
            String options[] = new String[] {mostPopular, topRated};
            final int selected = sSortCriteria.equals(SortCriteria.MOST_POPULAR) ? 0 : 1;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.sort_by_dialog_title));
            builder.setSingleChoiceItems(options, selected, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int selection) {
                    dialog.cancel();
                    if (selection != selected) {
                        sSortCriteria = selection == 0 ? SortCriteria.MOST_POPULAR
                                                       : SortCriteria.HIGHEST_RATED;
                        loadMovies();
                    }
                }
            });
            builder.show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void loadMovies() {
        mMoviesAdapter.setMoviesData(null);
        showMoviesView();
        new FetchMoviesTask().execute(sSortCriteria);
    }

    private void showMoviesView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mErrorLayout.setVisibility(View.INVISIBLE);
    }

    private void showErrorView() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorLayout.setVisibility(View.VISIBLE);
    }

    protected void onRetryButtonClick(View button) {
        loadMovies();
    }

    @Override
    public void onClick(MovieInfo movie) {
        Context context = this;
        Class destination = DetailActivity.class;
        Intent intentToDetailActivity = new Intent(context, destination);
        intentToDetailActivity.putExtra(DetailActivity.INTENT_KEY, movie);
        startActivity(intentToDetailActivity);
    }

    public class FetchMoviesTask extends AsyncTask<SortCriteria, Void, List<MovieInfo>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<MovieInfo> moviesData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (moviesData != null) {
                showMoviesView();
                mMoviesAdapter.setMoviesData(moviesData);
            } else {
                showErrorView();
            }
        }

        @Override
        protected List<MovieInfo> doInBackground(SortCriteria... params) {

            /* If there is no order criteria it cannot fetch movies info */
            if (params.length == 0) {
                return null;
            }
            SortCriteria criteria = params[0];

            try {
                URL configRequestUrl = TMDbNetworkUtils.buildConfigURL();
                String jsonConfigResponse = TMDbNetworkUtils
                        .getResponseFromHttpUrl(configRequestUrl);
                String posterBasePath = TMDbJsonUtils
                        .getPosterBasePathFromJson(jsonConfigResponse, mMinPosterWidth);
                URL moviesRequestUrl = TMDbNetworkUtils.buildMoviesURL(criteria);
                String moviesJsonResponse = TMDbNetworkUtils
                        .getResponseFromHttpUrl(moviesRequestUrl);
                List<MovieInfo> movies = TMDbJsonUtils
                        .getMoviesFromJson(moviesJsonResponse, posterBasePath);
                return movies;
            } catch (TMDbException e) {
                Log.d(TAG, ERROR_MOVIES, e);
                return null;
            }
        }
    }
}
