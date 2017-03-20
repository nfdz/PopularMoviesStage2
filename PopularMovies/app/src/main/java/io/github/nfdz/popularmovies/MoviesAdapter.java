/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import io.github.nfdz.popularmovies.types.MovieInfo;

/**
 * This class a recycler view adapter and manage the creation and binding of movie ui items.
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    /** List of movies to show, it could be null */
    private List<MovieInfo> mMoviesData;

    private final MoviesAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface MoviesAdapterOnClickHandler {
        void onClick(MovieInfo movie);
    }

    /**
     * Creates a MoviesAdapter.
     *
     * @param clickHandler This single handler is called when an item is clicked.
     */
    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutId = R.layout.movies_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParent = false;

        View view = inflater.inflate(layoutId, parent, shouldAttachToParent);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder holder, int position) {
        MovieInfo movie = mMoviesData.get(position);
        holder.mTitleTextView.setText(movie.getTitle());
        holder.mRatingTextView.setText(Double.toString(movie.getRating())+"/10");
        Context context = holder.mPosterImageView.getContext();
        Picasso.with(context).load(movie.getPosterPath()).into(holder.mPosterImageView);
    }

    @Override
    public int getItemCount() {
        return mMoviesData != null ? mMoviesData.size() : 0;
    }

    /**
     * This methods update movies data list with given one and refresh the view.
     *
     * @param movies List of movies. Null object is the same than an empty list.
     */
    public void setMoviesData(List<MovieInfo> movies) {
        mMoviesData = movies;
        notifyDataSetChanged();
    }

    /**
     * Cache of the children views for a movies list item.
     */
    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mTitleTextView;
        public final TextView mRatingTextView;
        public final ImageView mPosterImageView;

        public MoviesAdapterViewHolder(View view) {
            super(view);
            mTitleTextView = (TextView) view.findViewById(R.id.tv_movie_title);
            mPosterImageView = (ImageView) view.findViewById(R.id.iv_movie_poster);
            mRatingTextView = (TextView) view.findViewById(R.id.tv_movie_rating);
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            MovieInfo movie = mMoviesData.get(adapterPosition);
            mClickHandler.onClick(movie);
        }
    }
}
