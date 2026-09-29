package com.example.movieshelf.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_movies")
public class UserMovie {
    @PrimaryKey public long movieId;
    public boolean inWatchlist = true;
    public boolean watched;
    public boolean favorite;
    /** Half-star increments; zero means unrated. */
    public float rating;
    public String notes;
    public String watchDate;
    public int rewatchCount;
    public long dateAdded = System.currentTimeMillis();
}
