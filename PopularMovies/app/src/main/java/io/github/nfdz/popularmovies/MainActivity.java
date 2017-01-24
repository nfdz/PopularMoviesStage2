/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

import io.github.nfdz.popularmovies.MoviesAdapter.MoviesAdapterOnClickHandler;
import io.github.nfdz.popularmovies.types.MovieInfo;
import io.github.nfdz.popularmovies.utilities.TMDbException;
import io.github.nfdz.popularmovies.utilities.TMDbJsonUtils;
import io.github.nfdz.popularmovies.utilities.TMDbNetworkUtils;
import io.github.nfdz.popularmovies.utilities.TMDbNetworkUtils.OrderCriteria;

public class MainActivity extends AppCompatActivity implements MoviesAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ERROR_MOVIES = "There was an error retrieving movies data. ";

    private RecyclerView mRecyclerView;
    private LinearLayout mErrorLayout;
    private ProgressBar mLoadingIndicator;

    private MoviesAdapter mMoviesAdapter;
    private int mScreenWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);

        mErrorLayout = (LinearLayout) findViewById(R.id.layout_error);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // get screen width to choose image size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;

        //
        int spanCount = 2;
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

    private void loadMovies() {
        showMoviesView();
        new FetchMoviesTask().execute(OrderCriteria.HIGHEST_RATED);
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
        Toast.makeText(this,"SELECCIONADO: "+movie.getTitle(),Toast.LENGTH_LONG).show();
    }

    public class FetchMoviesTask extends AsyncTask<OrderCriteria, Void, List<MovieInfo>> {

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
        protected List<MovieInfo> doInBackground(OrderCriteria... params) {

            /* If there is no order criteria it cannot fetch movies info */
            if (params.length == 0) {
                return null;
            }
            OrderCriteria order = params[0];

            try {
                URL configRequestUrl = TMDbNetworkUtils.buildConfigURL();
                String jsonConfigResponse = TMDbNetworkUtils
                        .getResponseFromHttpUrl(configRequestUrl);
                String posterBasePath = TMDbJsonUtils
                        .getPosterBasePathFromJson(jsonConfigResponse, mScreenWidth);
                URL moviesRequestUrl = TMDbNetworkUtils.buildMoviesURL(order);
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
