package com.example.movieshelf.tmdb;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmdbApi {
    @GET("search/movie") Call<TmdbModels.SearchResponse> search(@Query("query") String query, @Query("include_adult") boolean includeAdult);
    @GET("movie/{movie_id}") Call<TmdbModels.MovieDetails> details(@Path("movie_id") long id, @Query("append_to_response") String append);
}
