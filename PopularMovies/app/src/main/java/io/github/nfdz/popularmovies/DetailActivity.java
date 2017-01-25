/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.github.nfdz.popularmovies.types.MovieInfo;

public class DetailActivity extends AppCompatActivity {

    public final static String INTENT_KEY = "MOVIE";

    private static final String MOVIE_SHARE_FORMAT = "Let's watch this movie! \"%s\" #PopularMoviesApp";

    private MovieInfo mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(INTENT_KEY)) {
            mMovie = intent.getParcelableExtra(INTENT_KEY);

            setTitle(mMovie.getTitle());

            TextView title = (TextView) findViewById(R.id.tv_movie_detail_title);
            title.setText(mMovie.getTitle());
            TextView rating = (TextView) findViewById(R.id.tv_movie_detail_rating);
            rating.setText(Double.toString(mMovie.getRating())+"/10");
            TextView releaseDate = (TextView) findViewById(R.id.tv_movie_detail_release);
            releaseDate.setText(mMovie.getReleaseDate());
            TextView synopsis = (TextView) findViewById(R.id.tv_movie_detail_synopsis);
            synopsis.setText(mMovie.getSynopsis());
            ImageView poster = (ImageView) findViewById(R.id.iv_movie_detail_poster);
            Picasso.with(this).load(mMovie.getPosterPath()).into(poster);
        } else {
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

    private Intent createShareMovieIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(String.format(MOVIE_SHARE_FORMAT, mMovie.getTitle()))
                .getIntent();
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Back menu button has to finish this activity and avoid to
                // create main activity again (avoiding to repeat network request
                // and to lose sort criteria configuration)
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
