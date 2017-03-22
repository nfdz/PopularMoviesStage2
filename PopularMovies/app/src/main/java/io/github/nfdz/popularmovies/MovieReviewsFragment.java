/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.nfdz.popularmovies.sync.MoviesTasks;
import io.github.nfdz.popularmovies.types.MovieInfo;
import io.github.nfdz.popularmovies.types.MovieReview;
import io.github.nfdz.popularmovies.utilities.TMDbException;
import io.github.nfdz.popularmovies.utilities.TMDbJsonUtils;
import io.github.nfdz.popularmovies.utilities.TMDbNetworkUtils;


public class MovieReviewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<MovieReview[]> {

    private static final String TAG = MoviesTasks.class.getSimpleName();
    private static final String ARG_MOVIE = "movie";
    private static final int ID_REVIEWS_LOADER = 884;

    private MovieInfo mMovieInfo;
    private ReviewsAdapter mReviewsAdapter;

    @BindView(R.id.tv_movie_detail_no_reviews)
    TextView mNoReviewsMsg;
    @BindView(R.id.pb_movie_detail_reviews_loading)
    ProgressBar mLoadingReviews;
    @BindView(R.id.rv_movie_details_reviews)
    RecyclerView mRecyclerView;

    public MovieReviewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param movie
     * @return A new instance of fragment MovieDetailsFragment.
     */
    public static MovieReviewsFragment newInstance(MovieInfo movie) {
        MovieReviewsFragment fragment = new MovieReviewsFragment();
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
        View view = inflater.inflate(R.layout.fragment_movie_reviews, container, false);
        ButterKnife.bind(this, view);

        if (mMovieInfo != null) {
            int orientation = OrientationHelper.VERTICAL;
            boolean reverseLayout = false;
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), orientation, reverseLayout);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setHasFixedSize(true);
            mReviewsAdapter = new ReviewsAdapter();
            mRecyclerView.setAdapter(mReviewsAdapter);

            getActivity().getSupportLoaderManager().initLoader(ID_REVIEWS_LOADER, null, this);
        }

        return view;
    }

    @Override
    public Loader<MovieReview[]> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_REVIEWS_LOADER:
                return new AsyncTaskLoader<MovieReview[]>(getContext()) {

                    MovieReview[] mReviews;

                    @Override
                    protected void onStartLoading() {
                        if (mReviews != null) {
                            deliverResult(mReviews);
                        } else {
                            forceLoad();
                        }
                    }

                    @Override
                    public MovieReview[] loadInBackground() {
                        try {
                            URL reviewsURL = TMDbNetworkUtils.buildMovieReviewsURL(mMovieInfo.getMovieId());
                            String reviewsJsonStr = TMDbNetworkUtils.getResponseFromHttpUrl(reviewsURL);
                            List<MovieReview> reviews = TMDbJsonUtils.getReviewsFromJson(reviewsJsonStr);
                            return reviews.toArray(new MovieReview[]{});
                        } catch (TMDbException e) {
                            Log.d(TAG, "Error retrieving movie videos. ", e);
                            return null;
                        }
                    }

                    @Override
                    public void deliverResult(MovieReview[] data) {
                        mReviews = data;
                        super.deliverResult(mReviews);
                    }
                };
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<MovieReview[]> loader, MovieReview[] data) {
        if (data == null || data.length == 0) {
            showErrorMsg();
        } else {
            showReviews();
            mReviewsAdapter.setReviews(data);
        }
    }

    private void showReviews() {
        mLoadingReviews.setVisibility(View.INVISIBLE);
        mNoReviewsMsg.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMsg() {
        mLoadingReviews.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mNoReviewsMsg.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<MovieReview[]> loader) {
        // nothing to do
    }

    private class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewAdapterViewHolder> {

        private MovieReview[] mReviews;

        @Override
        public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            int layoutId = R.layout.review_list_item;
            LayoutInflater inflater = LayoutInflater.from(context);
            boolean shouldAttachToParent = false;
            View view = inflater.inflate(layoutId, parent, shouldAttachToParent);
            return new ReviewAdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ReviewAdapterViewHolder holder, int position) {
            MovieReview review = mReviews[position];
            holder.mReviewAuthor.setText(review.getAuthor());
            holder.mReviewContent.setText(review.getContent());
        }

        @Override
        public int getItemCount() {
            return mReviews != null ? mReviews.length : 0;
        }

        public void setReviews(MovieReview[] reviews) {
            mReviews = reviews;
            notifyDataSetChanged();
        }

        public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public TextView mReviewAuthor;
            public TextView mReviewContent;

            public ReviewAdapterViewHolder(View view) {
                super(view);
                mReviewAuthor = (TextView) view.findViewById(R.id.tv_review_author);
                mReviewContent = (TextView) view.findViewById(R.id.tv_review_content);
                view.setOnClickListener(this);
            }

            /**
             * This gets called by the child views during a click.
             *
             * @param v The View that was clicked
             */
            @Override
            public void onClick(View v) {
                MovieReview review = mReviews[getAdapterPosition()];
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(review.getUrl()));
                Intent chooserIntent = Intent.createChooser(intent , "Choose browser of your choice");
                startActivity(chooserIntent);
            }
        }
    }

}