package com.example.movieshelf.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "movie_metadata")
public class MovieMetadata {
    @PrimaryKey public long movieId;
    @NonNull public String title = "";
    public String overview;
    public String releaseDate;
    public String posterPath;
    public String localPosterPath;
    public String genres;
    public String director;
    public Integer runtime;
}
