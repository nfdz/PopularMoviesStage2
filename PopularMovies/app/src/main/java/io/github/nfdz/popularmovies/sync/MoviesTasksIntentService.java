/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.sync;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * This service executes movie tasks in a background service. It uses intent action (task)
 * and data (Uri) arguments.
 */
public class MoviesTasksIntentService extends IntentService {

    public static final String SERVICE_NAME = "MoviesTasksIntentService";

    public MoviesTasksIntentService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        Uri data = intent.getData();
        MoviesTasks.executeTask(this, action, data);
    }
}
