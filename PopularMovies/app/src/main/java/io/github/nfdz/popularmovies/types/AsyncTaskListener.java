/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.types;

/**
 * This is a useful callback mechanism so we can abstract our AsyncTasks.
 *
 * @param <T>
 */
public interface AsyncTaskListener<T> {
    /**
     * Invoked before that the AsyncTask starts its execution.
     */
    void onPreTaskExecution();

    /**
     * Invoked when the AsyncTask has completed its execution.
     * @param result The resulting object from the AsyncTask.
     */
    void onTaskComplete(T result);
}