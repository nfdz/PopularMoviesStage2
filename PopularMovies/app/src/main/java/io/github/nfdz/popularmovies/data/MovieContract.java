/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.data;


import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "io.github.nfdz.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** This is the path for the "movies" directory */
    public static final String PATH_MOVIES = "movies";

    /** This is the path for the "popular" directory */
    public static final String PATH_POPULAR_MOVIES = "popular";

    /** This is the path for the "highest_rated" directory */
    public static final String PATH_HIGHEST_RATED_MOVIES = "highest_rated";

    /** This is the path for the "favorite" directory */
    public static final String PATH_FAVORITE_MOVIES = "favorite";

    /**
     * MovieEntry is an inner class that defines the contents of the movies table.
     */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();

        public static final String TABLE_NAME = "movies";

        /** Movie title. Stored as string. */
        public static final String COLUMN_TITLE = "title";

        /** Release date of the movie. Stored as string. */
        public static final String COLUMN_RELEASE_DATE = "release_date";

        /** Movie rating. Stored as double. */
        public static final String COLUMN_RATING = "rating";

        /**
         * Movie poster paths. Stored as string.
         * This is string contains a list of concatenated paths.
         * */
        public static final String COLUMN_POSTER_PATHS = "poster_paths";

        /** Movie synopsis. Stored as string. */
        public static final String COLUMN_SYNOPSIS = "synopsis";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE "  + TABLE_NAME + " (" +
                _ID                 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE        + " TEXT NOT NULL, " +
                COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                COLUMN_RATING       + " REAL NOT NULL, " +
                COLUMN_POSTER_PATHS + " TEXT NOT NULL, " +
                COLUMN_SYNOPSIS     + " TEXT NOT NULL);";

        public static Uri buildUriWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long extractIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }

    /**
     * PopularMovieEntry is an inner class that defines the contents of the popular movies table.
     */
    public static final class PopularMovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = MovieEntry.CONTENT_URI.buildUpon()
                .appendPath(PATH_POPULAR_MOVIES)
                .build();

        public static final String TABLE_NAME = "popular_movies";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID                   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MOVIE_ID       + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "));";
    }

    /**
     * HighestRatedMovieEntry is an inner class that defines the contents of the highest rated
     * movies table.
     */
    public static final class HighestRatedMovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = MovieEntry.CONTENT_URI.buildUpon()
                .appendPath(PATH_HIGHEST_RATED_MOVIES)
                .build();

        public static final String TABLE_NAME = "highest_rated_movies";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID                   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MOVIE_ID       + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "));";
    }

    /**
     * FavoriteMovieEntry is an inner class that defines the contents of the favorite movies table.
     */
    public static final class FavoriteMovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = MovieEntry.CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITE_MOVIES)
                .build();

        public static final String TABLE_NAME = "favorite_movies";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID                   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MOVIE_ID       + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "));";
    }
}