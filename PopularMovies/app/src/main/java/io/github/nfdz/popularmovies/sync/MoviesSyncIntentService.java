/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

public class MoviesSyncIntentService extends IntentService {

    public static final String SERVICE_NAME = "MoviesSyncIntentService";

    public MoviesSyncIntentService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
