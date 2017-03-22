/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.sync;


import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * This firebase service implementation checks if it is needed sync movies data and performs it
 * in background.
 */
public class MoviesSyncFirebaseJobService extends JobService {

    private AsyncTask<Void, Void, Void> mSyncTask;

    @Override
    public boolean onStartJob(final JobParameters job) {
        mSyncTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                if (!MoviesSyncUtils.isMoviesDataValid(MoviesSyncFirebaseJobService.this)) {
                    MoviesTasks.syncMovies(MoviesSyncFirebaseJobService.this);
                }
                jobFinished(job, false);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(job, false);
            }
        };
        mSyncTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (mSyncTask != null) mSyncTask.cancel(true);
        return true;
    }
}
