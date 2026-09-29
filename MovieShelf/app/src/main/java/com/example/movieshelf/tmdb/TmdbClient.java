package com.example.movieshelf.tmdb;

import com.example.movieshelf.BuildConfig;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class TmdbClient {
    private static final TmdbApi API = new Retrofit.Builder().baseUrl("https://api.themoviedb.org/3/")
            .client(new OkHttpClient.Builder().addInterceptor((Interceptor.Chain chain) -> {
                Request request = chain.request().newBuilder().addHeader("Authorization", "Bearer " + BuildConfig.TMDB_READ_ACCESS_TOKEN)
                        .addHeader("Accept", "application/json").build();
                return chain.proceed(request);
            }).build()).addConverterFactory(GsonConverterFactory.create()).build().create(TmdbApi.class);
    private TmdbClient() {}
    public static TmdbApi api() { return API; }
    public static boolean configured() { return BuildConfig.TMDB_READ_ACCESS_TOKEN != null && !BuildConfig.TMDB_READ_ACCESS_TOKEN.trim().isEmpty(); }
}
