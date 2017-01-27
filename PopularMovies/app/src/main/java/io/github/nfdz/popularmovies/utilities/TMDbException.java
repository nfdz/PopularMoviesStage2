/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.utilities;

/**
 * This custom exception is used by TMDb util methods to wrap any possible error and offer a good
 * level of abstraction.
 */
public class TMDbException extends Exception {

    private static final long serialVersionUID = 1L;

    public TMDbException(String message) {
        super(message);
    }

    public TMDbException(String message, Throwable cause) {
        super(message, cause);
    }
}
