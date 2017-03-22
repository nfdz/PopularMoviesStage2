/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.popularmovies.types;

public class MovieReview {
    private final String mAuthor;
    private final String mContent;
    private final String mUrl;

    public MovieReview(String author, String content, String url) {
        mAuthor = author;
        mContent = content;
        mUrl = url;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }

    public String getUrl() {
        return mUrl;
    }
}
