package com.example.movieshelf.tmdb;

import java.util.List;

public final class TmdbModels {
    private TmdbModels() {}
    public static class SearchResponse { public List<MovieResult> results; }
    public static class MovieResult {
        public long id; public String title; public String overview; public String release_date; public String poster_path;
    }
    public static class MovieDetails extends MovieResult {
        public List<Genre> genres; public Integer runtime; public Credits credits;
    }
    public static class Genre { public String name; }
    public static class Credits { public List<Crew> crew; }
    public static class Crew { public String job; public String name; }
}
