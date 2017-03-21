/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.popularmovies.types.MovieInfo;
import io.github.nfdz.popularmovies.utilities.FavoritesUtils;
import io.github.nfdz.popularmovies.utilities.MovieInfoUtils;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        BottomNavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = DetailActivity.class.getSimpleName();

    private static final String MOVIE_SHARE_FORMAT = "Let's watch this movie! \"%s\" #PopularMoviesApp";

    private static final int ID_DETAIL_LOADER = 866;

    /** MovieInfo object that describes this activity */
    private Uri mMovieUri;
    private MovieInfo mMovie;

    @BindView(R.id.pb_movie_detail_loading) ProgressBar mLoading;
    @BindView(R.id.nv_movie_detail_nav) BottomNavigationView mNavigation;
    @BindView(R.id.cl_movie_detail_layout) ConstraintLayout mLayout;
    @BindView(R.id.tv_movie_detail_title) TextView mTitle;
    @BindView(R.id.tv_movie_detail_release_date) TextView mReleaseDate;
    @BindView(R.id.tv_movie_detail_rating) TextView mRating;
    @BindView(R.id.iv_movie_detail_poster) ImageView mPoster;
    @BindView(R.id.iv_movie_detail_backdrop) ImageView mBackdrop;
    @BindView(R.id.ib_movie_fav) ImageButton mFavButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        // Check if the intent contains expected movie object
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            mMovieUri = intent.getData();
            getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
        } else {
            // If intent has no movie information, finish activity (this situation will never happen)
            Log.e(TAG, "Created detail activity without movie data URI stored in intent as expected.");
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        // FIXME
        //shareItem.setIntent(createShareMovieIntent());
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mNavigation.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNavigation.setOnNavigationItemSelectedListener(null);
    }

    /**
     * Uses the ShareCompat Intent builder to create intent for sharing. It sets the type
     * of content that is sharing (regular text), the text itself, and returns the created Intent.
     *
     * @return The Intent to use to start our share.
     */
    private Intent createShareMovieIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(String.format(MOVIE_SHARE_FORMAT, mMovie.getTitle()))
                .getIntent();
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_DETAIL_LOADER:
                showLoading();
                return new CursorLoader(this,
                        mMovieUri,
                        MovieInfoUtils.MOVIES_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Map<Integer, MovieInfo> movies = MovieInfoUtils.getMoviesFromCursor(data);
        if (movies.size() < 1) {
            Log.e(TAG, "Given movie data URI has no information.");
            finish();
        }
        Map.Entry<Integer, MovieInfo> entry = movies.entrySet().iterator().next();
        mMovie = entry.getValue();

        // Set this activity title the movie title
        setTitle(mMovie.getTitle());
        // Set movie data in ui views
        mTitle.setText(mMovie.getTitle());
        mReleaseDate.setText(mMovie.getReleaseDate());
        mRating.setText(Double.toString(mMovie.getRating())+"/10");
        Picasso.with(this)
                .load(mMovie.getPosterPaths()[0])
                .placeholder(ContextCompat.getDrawable(this, R.drawable.art_no_poster))
                .into(mPoster);
        Picasso.with(this)
                .load(mMovie.getBackdropPaths()[0])
                .placeholder(ContextCompat.getDrawable(this, R.drawable.art_no_backdrop))
                .into(mBackdrop);

        FavoritesUtils.resolveFavorite(this, mMovie.getMovieId(), new FavoritesUtils.ResolveFavoriteCallback() {
            @Override
            public void notifyResult(boolean isFavorite) {
                setFavorite(isFavorite);
            }
        });

        showDetails();

        // Use handler to ensure that there is no state loss
        new Handler(){
        }.post(new Runnable() {
            @Override
            public void run() {
                changeFragment(MovieDetailsFragment.newInstance(mMovie));
            }
        });
    }

    private void showDetails() {
        mLayout.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.GONE);
    }

    private void showLoading() {
        mLayout.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.ib_movie_fav)
    public void toggleFav() {
        if (mMovie == null) return;
        FavoritesUtils.toggleFavorite(this, mMovie.getMovieId(),  new FavoritesUtils.ResolveFavoriteCallback() {
            @Override
            public void notifyResult(boolean isFavorite) {
                setFavorite(isFavorite);
            }
        });
    }

    private void setFavorite(boolean isFavorite) {
        mFavButton.setVisibility(View.VISIBLE);
        if (isFavorite) {
            mFavButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.art_favorite_on));
        } else {
            mFavButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.art_favorite_off));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to do
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail_navigation_info:
                changeFragment(MovieDetailsFragment.newInstance(mMovie));
                return true;
            case R.id.detail_navigation_videos:
                changeFragment(MovieVideosFragment.newInstance(mMovie));
                return true;
            case R.id.detail_navigation_reviews:
                //changeFragment(new ItemFragment());
                return true;
        }
        return false;
    }

    private void changeFragment(Fragment fm){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.movie_detail_nav_content, fm);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

}
