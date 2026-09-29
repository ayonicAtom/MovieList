package com.example.movieshelf.data;

import androidx.room.Embedded;
import androidx.room.Relation;

public class MovieWithUserData {
    @Embedded public MovieMetadata movie;
    @Relation(parentColumn = "movieId", entityColumn = "movieId") public UserMovie personal;
}
