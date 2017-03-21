/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.nfdz.popularmovies.types.MovieInfo;


public class MovieDetailsFragment extends Fragment {

    private static final String ARG_MOVIE = "movie";

    private MovieInfo mMovieInfo;

    @BindView(R.id.tv_movie_detail_synopsis) TextView mSynopsis;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param movie
     * @return A new instance of fragment MovieDetailsFragment.
     */
    public static MovieDetailsFragment newInstance(MovieInfo movie) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        if (movie != null) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_MOVIE, movie);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovieInfo = getArguments().getParcelable(ARG_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        ButterKnife.bind(this, view);
        if (mMovieInfo != null) mSynopsis.setText(mMovieInfo.getSynopsis());
        return view;
    }

}
