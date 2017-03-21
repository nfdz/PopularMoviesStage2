/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.types;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * This class contains all needed information about a movie in this application.
 */
public class MovieInfo implements Parcelable {

    /** Identifier from internet database */
    private final int mMovieId;

    /** Image backdrop paths */
    private final String[] mBackdropPaths;

    /** Movie title */
    private final String mTitle;

    /** Release date */
    private final String mReleaseDate;

    /** Votes average (over 10) */
    private final double mRating;

    /** Image poster paths */
    private final String[] mPosterPaths;

    /** Plot synopsis */
    private final String mSynopsis;

    /**
     * Constructor.
     *
     * @param title movie title.
     * @param releaseDate release date.
     * @param rating votes average (over 10).
     * @param synopsis plot synopsis.
     * @param posterPaths image poster path.
     */
    public MovieInfo(int movieId,
                     String title,
                     String releaseDate,
                     double rating,
                     String synopsis,
                     String[] posterPaths,
                     String[] backdropPaths) {
        mMovieId = movieId;
        mTitle = title;
        mReleaseDate = releaseDate;
        mRating = rating;
        mSynopsis = synopsis;
        mPosterPaths = posterPaths;
        mBackdropPaths = backdropPaths;
    }

    /**
     * Constructor for Parcelable features.
     * @param in parcel object that contains movie data.
     */
    protected MovieInfo(Parcel in) {
        mMovieId = in.readInt();
        mTitle = in.readString();
        mReleaseDate = in.readString();
        mRating = in.readDouble();
        mSynopsis = in.readString();

        int postersSize = in.readInt();
        mPosterPaths = new String[postersSize];
        for (int i = 0; i < postersSize; i++) {
            mPosterPaths[i] = in.readString();
        }

        int backdropsSize = in.readInt();
        mBackdropPaths = new String[backdropsSize];
        for (int i = 0; i < backdropsSize; i++) {
            mBackdropPaths[i] = in.readString();
        }
    }

    /**
     * Creator object for Parcelable features
     */
    public static final Creator<MovieInfo> CREATOR = new Creator<MovieInfo>() {
        @Override
        public MovieInfo createFromParcel(Parcel in) {
            return new MovieInfo(in);
        }

        @Override
        public MovieInfo[] newArray(int size) {
            return new MovieInfo[size];
        }
    };

    public int getMovieId() {
        return mMovieId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public double getRating() {
        return mRating;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

    public String[] getPosterPaths() {
        return mPosterPaths;
    }

    public String[] getBackdropPaths() {
        return mBackdropPaths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieInfo)) return false;

        MovieInfo movieInfo = (MovieInfo) o;

        return mMovieId == movieInfo.mMovieId;
    }

    @Override
    public int hashCode() {
        return mMovieId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mMovieId);
        parcel.writeString(mTitle);
        parcel.writeString(mReleaseDate);
        parcel.writeDouble(mRating);
        parcel.writeString(mSynopsis);

        parcel.writeInt(mPosterPaths.length);
        for (String posterPath : mPosterPaths) {
            parcel.writeString(posterPath);
        }

        parcel.writeInt(mBackdropPaths.length);
        for (String backdropPath : mBackdropPaths) {
            parcel.writeString(backdropPath);
        }
    }
}