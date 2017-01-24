/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.types;

import java.io.Serializable;

public class MovieInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String mTitle;
    private final String mReleaseDate;
    private final double mRating;
    private final String mPosterPath;
    private final String mSynopsis;

    public MovieInfo(String title,
                     String releaseDate,
                     double rating,
                     String posterPath,
                     String synopsis) {
        mTitle = title;
        mRating = rating;
        mReleaseDate = releaseDate;
        mPosterPath = posterPath;
        mSynopsis = synopsis;
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

    public String getPosterPath() {
        return mPosterPath;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

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
        if (getPosterPath() != null ? !getPosterPath().equals(movieInfo.getPosterPath()) : movieInfo.getPosterPath() != null)
            return false;
        return getSynopsis() != null ? getSynopsis().equals(movieInfo.getSynopsis()) : movieInfo.getSynopsis() == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getTitle() != null ? getTitle().hashCode() : 0;
        result = 31 * result + (getReleaseDate() != null ? getReleaseDate().hashCode() : 0);
        temp = Double.doubleToLongBits(getRating());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (getPosterPath() != null ? getPosterPath().hashCode() : 0);
        result = 31 * result + (getSynopsis() != null ? getSynopsis().hashCode() : 0);
        return result;
    }
}