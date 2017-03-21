/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.nfdz.popularmovies.types.MovieInfo;

public class MovieVideosFragment  extends Fragment implements LoaderManager.LoaderCallbacks<String[]> {

    private static final String ARG_MOVIE = "movie";

    private static final int ID_VIDEOS_LOADER = 82;

    private MovieInfo mMovieInfo;
    private VideosAdapter mVideosAdapter;

    @BindView(R.id.tv_movie_detail_no_videos) TextView mNoVideosMsg;
    @BindView(R.id.pb_movie_detail_videos_loading) ProgressBar mLoadingVideos;
    @BindView(R.id.rv_movie_details_videos) RecyclerView mRecyclerView;

    public MovieVideosFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param movie
     * @return A new instance of fragment MovieDetailsFragment.
     */
    public static MovieVideosFragment newInstance(MovieInfo movie) {
        MovieVideosFragment fragment = new MovieVideosFragment();
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
        View view = inflater.inflate(R.layout.fragment_movie_videos, container, false);
        ButterKnife.bind(this, view);

        if (mMovieInfo != null) {
            int orientation = OrientationHelper.VERTICAL;
            boolean reverseLayout = false;
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), orientation, reverseLayout);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setHasFixedSize(true);
            mVideosAdapter = new VideosAdapter();
            mRecyclerView.setAdapter(mVideosAdapter);

            getActivity().getSupportLoaderManager().initLoader(ID_VIDEOS_LOADER, null, this);
        }

        return view;
    }

    @Override
    public Loader<String[]> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_VIDEOS_LOADER:
                return new AsyncTaskLoader<String[]>(getContext()) {

                    String[] mVideoPaths;

                    @Override
                    protected void onStartLoading() {
                        if (mVideoPaths != null) {
                            deliverResult(mVideoPaths);
                        } else {
                            forceLoad();
                        }
                    }

                    @Override
                    public String[] loadInBackground() {
                        // TODO
                        return new String[20];
                    }

                    @Override
                    public void deliverResult(String[] data) {
                        mVideoPaths = data;
                        super.deliverResult(mVideoPaths);
                    }
                };
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {
        showVideos();
        mVideosAdapter.setVideoPaths(data);
    }

    private void showVideos() {
        mLoadingVideos.setVisibility(View.INVISIBLE);
        mNoVideosMsg.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMsg() {
        mLoadingVideos.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mNoVideosMsg.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {
        // nothing to do
    }

    private class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideosAdapterViewHolder> {

        private String[] mVideoPaths;

        @Override
        public VideosAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            int layoutId = R.layout.video_list_item;
            LayoutInflater inflater = LayoutInflater.from(context);
            boolean shouldAttachToParent = false;
            View view = inflater.inflate(layoutId, parent, shouldAttachToParent);
            return new VideosAdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(VideosAdapterViewHolder holder, int position) {
            // nothing to do
        }

        @Override
        public int getItemCount() {
            return mVideoPaths != null ? mVideoPaths.length : 0;
        }

        public void setVideoPaths(String[] videoPaths) {
            mVideoPaths = videoPaths;
            notifyDataSetChanged();
        }
        public class VideosAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public VideosAdapterViewHolder(View view) {
                super(view);
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
                System.out.println("\n\n"+mVideoPaths[adapterPosition]);
                // intent here
            }
        }
    }

}
