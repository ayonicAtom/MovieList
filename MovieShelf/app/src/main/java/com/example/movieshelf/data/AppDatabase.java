package com.example.movieshelf.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MovieMetadata.class, UserMovie.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;
    public abstract MovieDao movies();
    public static AppDatabase get(Context context) {
        if (instance == null) synchronized (AppDatabase.class) {
            if (instance == null) instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "movieshelf.db").build();
        }
        return instance;
    }
}
