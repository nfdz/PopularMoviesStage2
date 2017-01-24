/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.utilities;

public class TMDbException extends Exception {

    private static final long serialVersionUID = 1L;

    public TMDbException(String message) {
        super(message);
    }

    public TMDbException(Throwable cause) {
        super(cause);
    }

    public TMDbException(String message, Throwable cause) {
        super(message, cause);
    }
}
