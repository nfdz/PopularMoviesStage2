/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.sync;


import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class MoviesSyncFirebaseJobService extends JobService {

    private AsyncTask<Void, Void, Void> mSyncTask;

    @Override
    public boolean onStartJob(final JobParameters job) {
        mSyncTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                // TODO check last timestamp to decide if sync
                Context context = getApplicationContext();
                MoviesTasks.syncMovies(context);
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
