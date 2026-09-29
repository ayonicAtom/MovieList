package com.example.movieshelf.data;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class PosterCache {
    private PosterCache() {}
    public static String download(Context context, long movieId, String path) {
        if (path == null || path.isEmpty()) return null;
        File destination = new File(context.getFilesDir(), "posters/" + movieId + ".jpg");
        if (destination.exists() && destination.length() > 0) return destination.getAbsolutePath();
        HttpURLConnection connection = null;
        try {
            File parent = destination.getParentFile(); if (parent != null) parent.mkdirs();
            connection = (HttpURLConnection) new URL("https://image.tmdb.org/t/p/w500" + path).openConnection();
            connection.setConnectTimeout(10000); connection.setReadTimeout(15000);
            try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(destination)) {
                byte[] buffer = new byte[8192]; int count;
                while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            }
            return destination.getAbsolutePath();
        } catch (Exception ignored) { destination.delete(); return null; }
        finally { if (connection != null) connection.disconnect(); }
    }
}
