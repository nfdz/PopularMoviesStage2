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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.nfdz.popularmovies.types.MovieInfo;
import io.github.nfdz.popularmovies.utilities.TMDbException;
import io.github.nfdz.popularmovies.utilities.TMDbJsonUtils;
import io.github.nfdz.popularmovies.utilities.TMDbNetworkUtils;

public class MovieVideosFragment extends Fragment implements LoaderManager.LoaderCallbacks<Map<String, String>> {

    private static final String TAG = MovieVideosFragment.class.getSimpleName();
    private static final String ARG_MOVIE = "movie";
    private static final int ID_VIDEOS_LOADER = 412;

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
    public Loader<Map<String, String>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_VIDEOS_LOADER:
                return new AsyncTaskLoader<Map<String, String>>(getContext()) {

                    Map<String, String> mVideoPaths;

                    @Override
                    protected void onStartLoading() {
                        if (mVideoPaths != null) {
                            deliverResult(mVideoPaths);
                        } else {
                            forceLoad();
                        }
                    }

                    @Override
                    public Map<String, String> loadInBackground() {
                        try {
                            URL videosURL = TMDbNetworkUtils.buildMovieVideosURL(mMovieInfo.getMovieId());
                            String videosJsonStr = TMDbNetworkUtils.getResponseFromHttpUrl(videosURL);
                            Map<String, String> videosMap = TMDbJsonUtils.getVideosFromJson(videosJsonStr);
                            return videosMap;
                        } catch (TMDbException e) {
                            Log.d(TAG, "Error retrieving movie videos. ", e);
                            return null;
                        }
                    }

                    @Override
                    public void deliverResult(Map<String, String> data) {
                        mVideoPaths = data;
                        super.deliverResult(mVideoPaths);
                    }
                };
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Map<String, String>> loader, Map<String, String> data) {
        if (data == null || data.size() == 0) {
            showErrorMsg();
        } else {
            showVideos();
            mVideosAdapter.setVideoPaths(data);
        }
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
    public void onLoaderReset(Loader<Map<String, String>> loader) {
        // nothing to do
    }

    private class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideosAdapterViewHolder> {

        private Map<String, String> mVideoPathsMap;
        private List<String> mVideoNames;
        private List<String> mVideoPaths;

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
            holder.mVideoName.setText(mVideoNames.get(position));
        }

        @Override
        public int getItemCount() {
            return mVideoPaths != null ? mVideoPaths.size() : 0;
        }

        public void setVideoPaths(Map<String, String> videoPathsMap) {
            mVideoPathsMap = videoPathsMap;
            mVideoNames = new ArrayList<>();
            mVideoPaths = new ArrayList<>();
            if (mVideoPathsMap != null) {
                for (Map.Entry<String,String> entry : mVideoPathsMap.entrySet()) {
                    mVideoNames.add(entry.getKey());
                    mVideoPaths.add(entry.getValue());
                }
            }
            notifyDataSetChanged();
        }

        public class VideosAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public TextView mVideoName;

            public VideosAdapterViewHolder(View view) {
                super(view);
                mVideoName = (TextView) view.findViewById(R.id.tv_video_name);
                view.setOnClickListener(this);
            }

            /**
             * This gets called by the child views during a click.
             *
             * @param v The View that was clicked
             */
            @Override
            public void onClick(View v) {
                String videoPath = mVideoPaths.get(getAdapterPosition());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoPath));
                Intent chooserIntent = Intent.createChooser(intent , "Choose browser of your choice");
                startActivity(chooserIntent);
            }
        }
    }

}
