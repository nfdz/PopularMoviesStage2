/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.github.nfdz.popularmovies.data.MovieContract;
import io.github.nfdz.popularmovies.utilities.MovieInfoUtils;
import io.github.nfdz.popularmovies.utilities.TMDBImagesUtils;

/**
 * This class a recycler view adapter and manage the creation and binding of movie ui items.
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    public static final String[] PROJECTION = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_POSTER_PATHS
    };

    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_TITLE = 1;
    public static final int INDEX_MOVIE_RELEASE_DATE = 2;
    public static final int INDEX_MOVIE_RATING = 3;
    public static final int INDEX_MOVIE_POSTER_PATHS = 4;


    /** This cursor has got the movies to show, it could be null */
    private Cursor mCursor;

    private final Context mContext;
    private final MoviesAdapterOnClickHandler mClickHandler;
    private final int mPosterWidth;

    /**
     * The interface that receives onClick messages.
     */
    public interface MoviesAdapterOnClickHandler {
        void onClick(long id);
    }

    /**
     * Creates a MoviesAdapter.
     *
     * @param clickHandler This single handler is called when an item is clicked.
     */
    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler, Context context) {
        mContext = context;
        mClickHandler = clickHandler;
        mPosterWidth = mContext.getResources().getDimensionPixelSize(R.dimen.movie_item_poster_width);
    }

    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = R.layout.movies_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParent = false;
        View view = inflater.inflate(layoutId, parent, shouldAttachToParent);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mTitleTextView.setText(mCursor.getString(INDEX_MOVIE_TITLE) +
                " (" + getYear(mCursor.getString(INDEX_MOVIE_RELEASE_DATE)) + ")");
        holder.mRatingTextView.setText(Double.toString(mCursor.getDouble(INDEX_MOVIE_RATING))+"/10");
        String mergedPosterPaths = mCursor.getString(INDEX_MOVIE_POSTER_PATHS);
        String[] posterPaths = MovieInfoUtils.splitPaths(mergedPosterPaths);
        String posterPath = TMDBImagesUtils.resolveImagePath(posterPaths, mPosterWidth);
        // add no poster art meanwhile Picasso is loading the poster
        Picasso.with(mContext)
                .load(posterPath)
                .placeholder(ContextCompat.getDrawable(mContext, R.drawable.art_no_poster))
                .into(holder.mPosterImageView);
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    /**
     * This methods update data cursor with the given one and refresh the view.
     * It closes the old cursor if it is not the same that the given one.
     *
     * @param cursor
     */
    public void setCursor(Cursor cursor) {
        if (mCursor != cursor) {
            Cursor oldCursor = mCursor;
            mCursor = cursor;
            if (oldCursor != null) oldCursor.close();
        }
        notifyDataSetChanged();
    }

    /**
     * Release date is like YYYY-MM. It has to extract the first four chars.
     * @param realeaseDate
     * @return year
     */
    private String getYear(String realeaseDate) {
        if (realeaseDate.length() < 4) return realeaseDate;
        return realeaseDate.substring(0, 4);
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
            mCursor.moveToPosition(adapterPosition);
            long id = mCursor.getLong(INDEX_MOVIE_ID);
            mClickHandler.onClick(id);
        }
    }
}
