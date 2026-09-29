package com.example.movieshelf.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import java.util.List;

@Dao
public abstract class MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) public abstract void saveMetadata(MovieMetadata movie);
    @Insert(onConflict = OnConflictStrategy.REPLACE) public abstract void savePersonal(UserMovie movie);
    @Update public abstract void updatePersonal(UserMovie movie);
    @Query("SELECT * FROM user_movies WHERE movieId = :id") public abstract UserMovie personal(long id);
    @Query("SELECT * FROM movie_metadata WHERE movieId = :id") public abstract MovieMetadata metadata(long id);
    @Query("DELETE FROM user_movies WHERE movieId = :id") public abstract void remove(long id);
    @Transaction @Query("SELECT * FROM movie_metadata WHERE movieId IN (SELECT movieId FROM user_movies WHERE inWatchlist = 1) ORDER BY (SELECT dateAdded FROM user_movies WHERE user_movies.movieId = movie_metadata.movieId) DESC") public abstract List<MovieWithUserData> watchlist();
    @Transaction @Query("SELECT * FROM movie_metadata WHERE movieId IN (SELECT movieId FROM user_movies WHERE watched = 1) ORDER BY (SELECT watchDate FROM user_movies WHERE user_movies.movieId = movie_metadata.movieId) DESC, (SELECT dateAdded FROM user_movies WHERE user_movies.movieId = movie_metadata.movieId) DESC") public abstract List<MovieWithUserData> watched();
    @Transaction @Query("SELECT * FROM movie_metadata WHERE movieId IN (SELECT movieId FROM user_movies WHERE favorite = 1) ORDER BY (SELECT dateAdded FROM user_movies WHERE user_movies.movieId = movie_metadata.movieId) DESC") public abstract List<MovieWithUserData> favorites();
}
