/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.nfdz.popularmovies.types.MovieInfo;

public class DetailActivity extends AppCompatActivity {

    /** Expected key to store MovieInfo data as extra Parcelable date inside intent */
    public final static String INTENT_KEY = "MOVIE";

    private static final String TAG = DetailActivity.class.getSimpleName();

    private static final String MOVIE_SHARE_FORMAT = "Let's watch this movie! \"%s\" #PopularMoviesApp";

    /** MovieInfo object that describes this activity */
    private MovieInfo mMovie;

    @BindView(R.id.tv_movie_detail_title) TextView mTitle;
    @BindView(R.id.tv_movie_detail_rating) TextView mRating;
    @BindView(R.id.tv_movie_detail_release) TextView mReleaseDate;
    @BindView(R.id.tv_movie_detail_synopsis) TextView mSynopsis;
    @BindView(R.id.iv_movie_detail_poster) ImageView mPoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        // Check if the intent contains expected movie object
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(INTENT_KEY)) {
            mMovie = intent.getParcelableExtra(INTENT_KEY);
            // Set this activity title the movie title
            setTitle(mMovie.getTitle());
            // Set movie data in ui views
            mTitle.setText(mMovie.getTitle());
            mRating.setText(Double.toString(mMovie.getRating())+"/10");
            mReleaseDate.setText(mMovie.getReleaseDate());
            mSynopsis.setText(mMovie.getSynopsis());
            Picasso.with(this).load(mMovie.getPosterPath()).into(mPoster);
        } else {
            // If intent has no movie information, finish activity (this situation will never happen)
            Log.e(TAG, "Created detail activity without movie data stored in intent as expected.");
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareItem.setIntent(createShareMovieIntent());
        return true;
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
}
