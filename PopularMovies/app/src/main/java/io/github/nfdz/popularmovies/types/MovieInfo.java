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

    /** Movie title */
    private final String mTitle;

    /** Release date */
    private final String mReleaseDate;

    /** Votes average (over 10) */
    private final double mRating;

    /** Image poster path */
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
    public MovieInfo(String title,
                     String releaseDate,
                     double rating,
                     String synopsis,
                     String... posterPaths) {
        mTitle = title;
        mRating = rating;
        mReleaseDate = releaseDate;
        mPosterPaths = posterPaths != null ? posterPaths : new String[0];
        mSynopsis = synopsis;
    }

    /**
     * Constructor for Parcelable features.
     * @param in parcel object that contains movie data.
     */
    protected MovieInfo(Parcel in) {
        mTitle = in.readString();
        mReleaseDate = in.readString();
        mRating = in.readDouble();
        mSynopsis = in.readString();
        int pathsSize = in.readInt();
        mPosterPaths = new String[pathsSize];
        for (int i = 0; i < pathsSize; i++) {
            mPosterPaths[i] = in.readString();
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

    public String getTitle() {
        return mTitle;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public double getRating() {
        return mRating;
    }

    public String[] getPosterPaths() {
        return mPosterPaths;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

    /**
     * This implementation of equals do not use rating, synopsis or poster paths to check
     * if the other object is the same because these fields could change with time (in internet service).
     * @param o other object
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieInfo)) return false;

        MovieInfo movieInfo = (MovieInfo) o;

        if (Double.compare(movieInfo.getRating(), getRating()) != 0) return false;
        if (getTitle() != null ? !getTitle().equals(movieInfo.getTitle()) : movieInfo.getTitle() != null)
            return false;
        if (getReleaseDate() != null ? !getReleaseDate().equals(movieInfo.getReleaseDate()) : movieInfo.getReleaseDate() != null)
            return false;
        if (getPosterPaths() != null ? !Arrays.equals(getPosterPaths(), movieInfo.getPosterPaths()) : movieInfo.getPosterPaths() != null)
            return false;
        return getSynopsis() != null ? getSynopsis().equals(movieInfo.getSynopsis()) : movieInfo.getSynopsis() == null;

    }

    /**
     * This implementation of hashcode do not use rating, synopsis or poster paths to compute
     * the hash because these fields could change with time (in internet service).
     * @return hashcode
     */
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getTitle() != null ? getTitle().hashCode() : 0;
        result = 31 * result + (getReleaseDate() != null ? getReleaseDate().hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeString(mReleaseDate);
        parcel.writeDouble(mRating);
        parcel.writeString(mSynopsis);
        parcel.writeInt(mPosterPaths.length);
        for (String posterPath : mPosterPaths) {
            parcel.writeString(posterPath);
        }
    }
}