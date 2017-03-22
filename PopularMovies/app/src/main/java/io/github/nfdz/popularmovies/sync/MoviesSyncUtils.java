/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.sync;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

import io.github.nfdz.popularmovies.data.MovieContract;

/**
 * This class has several methods to manage data sync in a safe way (avoid computing long tasks in
 * UI thread). It is important run initialize at least once to grants that application is synchronized.
 */
public class MoviesSyncUtils {

    /** Interval at which to sync with the movies data. */
    private static final int SYNC_INTERVAL_HOURS = 24;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

    private static boolean sInitialized = false;

    private static final String MOVIES_SYNC_TAG = "movies-sync";

    /**
     * Schedules a repeating sync of movies data using FirebaseJobDispatcher.
     * @param context Context
     */
    static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context) {

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Job syncSunshineJob = dispatcher.newJobBuilder()
                .setService(MoviesSyncFirebaseJobService.class)
                .setTag(MOVIES_SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(syncSunshineJob);
    }

    /**
     * This methods initializes movies data synchronization (checking if it is needed sync just now)
     * and scheduling automatic synchronization.
     * @param context
     */
    synchronized public static void initialize(@NonNull final Context context) {

        if (sInitialized) return;
        sInitialized = true;

        scheduleFirebaseJobDispatcherSync(context);

        Thread checkIfEmpty = new Thread(new Runnable() {
            @Override
            public void run() {
                Uri moviesQueryUri = MovieContract.MovieEntry.CONTENT_URI;
                String[] projection = { MovieContract.MovieEntry._ID };
                // TODO check time
                Cursor cursor = context.getContentResolver().query(moviesQueryUri,
                        projection,
                        null,
                        null,
                        null);

                if (cursor == null || cursor.getCount() == 0) {
                    startImmediateSync(context);
                }
                cursor.close();
            }
        });

        checkIfEmpty.start();
    }

    /**
     * This method starts task executor service to sync movies data in background.
     * @param context
     */
    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSync = new Intent(context, MoviesTasksIntentService.class);
        intentToSync.setAction(MoviesTasks.ACTION_SYNC_MOVIES);
        context.startService(intentToSync);
    }
}