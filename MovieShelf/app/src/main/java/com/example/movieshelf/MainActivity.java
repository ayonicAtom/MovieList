package com.example.movieshelf;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.movieshelf.data.AppDatabase;
import com.example.movieshelf.data.MovieDao;
import com.example.movieshelf.data.MovieMetadata;
import com.example.movieshelf.data.MovieWithUserData;
import com.example.movieshelf.data.PosterCache;
import com.example.movieshelf.data.UserMovie;
import com.example.movieshelf.tmdb.TmdbClient;
import com.example.movieshelf.tmdb.TmdbModels;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private final ExecutorService io = Executors.newFixedThreadPool(3);
    private MovieDao dao;
    private RecyclerView recycler;
    private MovieAdapter adapter;
    private TextView heading, status;
    private EditText searchBox;
    private ProgressBar progress;
    private String section = "Search";
    private final List<Object> rows = new ArrayList<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        dao = AppDatabase.get(this).movies();
        buildScreen();
        showSection("Search");
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18), dp(12), dp(18), 0); root.setBackgroundColor(0xFFF7F5F2);
        TextView brand = new TextView(this); brand.setText("MOVIESHELF  ·  YOUR PRIVATE FILM LOG"); brand.setTextSize(11); brand.setTextColor(0xFF58718A); brand.setLetterSpacing(.08f); root.addView(brand);
        heading = new TextView(this); heading.setTextSize(30); heading.setTextColor(0xFF202A32); heading.setPadding(0, dp(8), 0, dp(10)); root.addView(heading);
        searchBox = new EditText(this); searchBox.setSingleLine(true); searchBox.setHint("Search movies on TMDB"); searchBox.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        Button search = new Button(this); search.setText("Search");
        LinearLayout searchRow = new LinearLayout(this); searchRow.setOrientation(LinearLayout.HORIZONTAL); searchRow.addView(searchBox, new LinearLayout.LayoutParams(0, dp(54), 1)); searchRow.addView(search, new LinearLayout.LayoutParams(dp(94), dp(54)));
        search.setOnClickListener(v -> searchMovies()); searchBox.setOnEditorActionListener((v, action, event) -> { if (action == EditorInfo.IME_ACTION_SEARCH) { searchMovies(); return true; } return false; });
        root.addView(searchRow);
        LinearLayout tabs = new LinearLayout(this); tabs.setGravity(Gravity.CENTER); tabs.setPadding(0, dp(4), 0, dp(4));
        for (String name : new String[]{"Search", "Watchlist", "Watched", "Favorites"}) { Button b = new Button(this); b.setText(name); b.setTextSize(11); b.setAllCaps(false); tabs.addView(b, new LinearLayout.LayoutParams(0, dp(48), 1)); b.setOnClickListener(v -> showSection(name)); }
        root.addView(tabs);
        status = new TextView(this); status.setTextColor(0xFF62717D); status.setTextSize(14); status.setGravity(Gravity.CENTER); status.setPadding(dp(8), dp(8), dp(8), dp(8)); root.addView(status);
        progress = new ProgressBar(this); progress.setVisibility(android.view.View.GONE); root.addView(progress, new LinearLayout.LayoutParams(-1, dp(4)));
        recycler = new RecyclerView(this); recycler.setLayoutManager(new LinearLayoutManager(this)); adapter = new MovieAdapter(); recycler.setAdapter(adapter); root.addView(recycler, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView attribution = new TextView(this); attribution.setText("Movie data & images powered by TMDB. Not endorsed or certified by TMDB."); attribution.setTextColor(0xFF89939B); attribution.setTextSize(10); attribution.setGravity(Gravity.CENTER); attribution.setPadding(0, dp(8), 0, dp(12)); root.addView(attribution);
        setContentView(root);
    }

    private void showSection(String value) {
        section = value; heading.setText(value.equals("Search") ? "Find your next film" : value);
        boolean search = value.equals("Search"); ((android.view.View) searchBox.getParent()).setVisibility(search ? android.view.View.VISIBLE : android.view.View.GONE);
        if (search) { rows.clear(); adapter.notifyDataSetChanged(); status.setText("Search TMDB, then save films to build your offline library."); return; }
        status.setText("Loading your saved films…"); progress.setVisibility(android.view.View.VISIBLE);
        io.execute(() -> { List<MovieWithUserData> list = value.equals("Watchlist") ? dao.watchlist() : value.equals("Watched") ? dao.watched() : dao.favorites();
            runOnUiThread(() -> { progress.setVisibility(android.view.View.GONE); rows.clear(); rows.addAll(list); adapter.notifyDataSetChanged(); status.setText(list.isEmpty() ? emptyMessage(value) : list.size() + (list.size() == 1 ? " film" : " films") + " · saved on this device"); }); });
    }
    private String emptyMessage(String tab) { return tab.equals("Watchlist") ? "Your watchlist is empty. Search for a film to add one." : tab.equals("Watched") ? "Your watched log is empty. Mark a saved film watched to start it." : "No favorites yet. Tap a saved film to mark it as a favorite."; }

    private void searchMovies() {
        String query = searchBox.getText().toString().trim(); if (query.isEmpty()) return;
        if (!TmdbClient.configured()) { status.setText("Add TMDB_READ_ACCESS_TOKEN in local.properties or ~/.gradle/gradle.properties, then sync Gradle."); return; }
        progress.setVisibility(android.view.View.VISIBLE); status.setText("Searching TMDB…"); rows.clear(); adapter.notifyDataSetChanged();
        TmdbClient.api().search(query, false).enqueue(new retrofit2.Callback<TmdbModels.SearchResponse>() {
            @Override public void onResponse(Call<TmdbModels.SearchResponse> call, Response<TmdbModels.SearchResponse> response) {
                progress.setVisibility(android.view.View.GONE);
                if (!response.isSuccessful() || response.body() == null) { status.setText("TMDB returned an error (" + response.code() + "). Check your token and connection."); return; }
                if (response.body().results != null) rows.addAll(response.body().results); adapter.notifyDataSetChanged(); status.setText(rows.isEmpty() ? "No films found. Try another title." : rows.size() + " results · tap one for details");
            }
            @Override public void onFailure(Call<TmdbModels.SearchResponse> call, Throwable error) { progress.setVisibility(android.view.View.GONE); status.setText("Could not reach TMDB. Check your internet connection and try again."); }
        });
    }

    private void openResult(TmdbModels.MovieResult result) {
        progress.setVisibility(android.view.View.VISIBLE); status.setText("Loading movie details…");
        TmdbClient.api().details(result.id, "credits").enqueue(new retrofit2.Callback<TmdbModels.MovieDetails>() {
            @Override public void onResponse(Call<TmdbModels.MovieDetails> call, Response<TmdbModels.MovieDetails> response) {
                progress.setVisibility(android.view.View.GONE);
                if (!response.isSuccessful() || response.body() == null) { status.setText("Couldn't load details (" + response.code() + "). Check your connection."); return; }
                TmdbModels.MovieDetails details = response.body();
                io.execute(() -> { UserMovie personal = dao.personal(details.id); runOnUiThread(() -> showDetails(details, personal)); });
            }
            @Override public void onFailure(Call<TmdbModels.MovieDetails> call, Throwable error) { progress.setVisibility(android.view.View.GONE); status.setText("Couldn't load details. Check your internet connection."); }
        });
    }

    private void openSaved(MovieWithUserData saved) {
        MovieMetadata m = saved.movie; UserMovie u = saved.personal;
        TmdbModels.MovieDetails d = new TmdbModels.MovieDetails(); d.id = m.movieId; d.title = m.title; d.overview = m.overview; d.release_date = m.releaseDate; d.poster_path = m.posterPath; d.runtime = m.runtime;
        d.genres = new ArrayList<>(); if (m.genres != null) for (String genre : m.genres.split(", ")) { TmdbModels.Genre g = new TmdbModels.Genre(); g.name = genre; d.genres.add(g); }
        showDetails(d, u);
    }

    private void showDetails(TmdbModels.MovieDetails d, UserMovie existing) {
        LinearLayout form = new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(dp(20), dp(8), dp(20), dp(8));
        TextView summary = new TextView(this); String year = d.release_date == null || d.release_date.length() < 4 ? "" : " · " + d.release_date.substring(0, 4);
        summary.setText(d.title + year + (d.runtime == null ? "" : " · " + d.runtime + " min") + "\n\n" + (d.overview == null || d.overview.isEmpty() ? "No synopsis available." : d.overview) + "\n\n" + genreText(d)); summary.setTextSize(14); summary.setTextColor(0xFF34414B); form.addView(summary);
        RatingBar rating = new RatingBar(this, null, android.R.attr.ratingBarStyleSmall); rating.setNumStars(5); rating.setStepSize(.5f); rating.setRating(existing == null ? 0 : existing.rating); form.addView(label("Your rating (half stars)")); form.addView(rating);
        EditText notes = new EditText(this); notes.setHint("Personal notes"); notes.setMinLines(2); notes.setGravity(Gravity.TOP); notes.setText(existing == null || existing.notes == null ? "" : existing.notes); form.addView(notes);
        android.widget.CheckBox favorite = new android.widget.CheckBox(this); favorite.setText("Favorite"); favorite.setChecked(existing != null && existing.favorite); form.addView(favorite);
        android.widget.CheckBox watched = new android.widget.CheckBox(this); watched.setText("Watched"); watched.setChecked(existing != null && existing.watched); form.addView(watched);
        EditText watchDate = new EditText(this); watchDate.setHint("Watch date (YYYY-MM-DD)"); watchDate.setFocusable(false); watchDate.setText(existing == null ? "" : nullToEmpty(existing.watchDate)); watchDate.setOnClickListener(v -> pickDate(watchDate)); form.addView(watchDate);
        EditText rewatches = new EditText(this); rewatches.setHint("Rewatch count"); rewatches.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); rewatches.setText(existing == null ? "0" : String.valueOf(existing.rewatchCount)); form.addView(rewatches);
        boolean alreadySaved = existing != null;
        new AlertDialog.Builder(this).setTitle(d.title == null ? "Movie details" : d.title).setView(form)
                .setPositiveButton(alreadySaved ? "Save changes" : "Add to watchlist", (dialog, which) -> saveMovie(d, existing, rating.getRating(), notes.getText().toString(), favorite.isChecked(), watched.isChecked(), watchDate.getText().toString(), rewatches.getText().toString()))
                .setNeutralButton(alreadySaved ? "Remove" : "Close", (dialog, which) -> { if (alreadySaved) removeMovie(d.id); }).setNegativeButton(alreadySaved ? "Close" : "Cancel", null).show();
    }
    private TextView label(String text) { TextView v = new TextView(this); v.setText(text); v.setTextSize(12); v.setTextColor(0xFF62717D); v.setPadding(0, dp(8), 0, 0); return v; }
    private String genreText(TmdbModels.MovieDetails d) { if (d.genres == null || d.genres.isEmpty()) return ""; StringBuilder s = new StringBuilder("Genres: "); for (int i = 0; i < d.genres.size(); i++) { if (i > 0) s.append(", "); s.append(d.genres.get(i).name); } return s.toString(); }
    private String nullToEmpty(String value) { return value == null ? "" : value; }
    private void pickDate(EditText target) { Calendar c = Calendar.getInstance(); new DatePickerDialog(this, (view, y, m, day) -> target.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, day)), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show(); }

    private void saveMovie(TmdbModels.MovieDetails d, UserMovie existing, float rating, String notes, boolean favorite, boolean watched, String watchDate, String rewatches) {
        io.execute(() -> {
            MovieMetadata m = new MovieMetadata(); m.movieId = d.id; m.title = nullToEmpty(d.title); m.overview = d.overview; m.releaseDate = d.release_date; m.posterPath = d.poster_path; m.runtime = d.runtime; m.genres = genreText(d);
            UserMovie u = existing == null ? new UserMovie() : existing; u.movieId = d.id; u.inWatchlist = true; u.rating = Math.round(rating * 2f) / 2f; u.notes = notes.trim().isEmpty() ? null : notes.trim(); u.favorite = favorite; u.watched = watched; u.watchDate = watchDate.trim().isEmpty() ? null : watchDate.trim();
            try { u.rewatchCount = Math.max(0, Integer.parseInt(rewatches.trim())); } catch (NumberFormatException ignored) { u.rewatchCount = 0; }
            dao.saveMetadata(m); dao.savePersonal(u);
            String local = PosterCache.download(this, d.id, d.poster_path); if (local != null) { m.localPosterPath = local; dao.saveMetadata(m); }
            runOnUiThread(() -> { Toast.makeText(this, "Saved to your library", Toast.LENGTH_SHORT).show(); if (!section.equals("Search")) showSection(section); });
        });
    }
    private void removeMovie(long id) { io.execute(() -> { dao.remove(id); runOnUiThread(() -> { Toast.makeText(this, "Removed from your library", Toast.LENGTH_SHORT).show(); showSection(section); }); }); }

    private class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.Holder> {
        @Override public Holder onCreateViewHolder(android.view.ViewGroup parent, int type) {
            LinearLayout card = new LinearLayout(MainActivity.this); card.setOrientation(LinearLayout.HORIZONTAL); card.setPadding(dp(8), dp(8), dp(8), dp(8)); card.setBackgroundColor(0xFFFFFFFF); RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(-1, dp(138)); lp.bottomMargin = dp(10); card.setLayoutParams(lp);
            ImageView poster = new ImageView(MainActivity.this); poster.setScaleType(ImageView.ScaleType.CENTER_CROP); card.addView(poster, new LinearLayout.LayoutParams(dp(82), -1));
            LinearLayout texts = new LinearLayout(MainActivity.this); texts.setOrientation(LinearLayout.VERTICAL); texts.setPadding(dp(12), dp(4), dp(4), dp(4)); card.addView(texts, new LinearLayout.LayoutParams(0, -1, 1));
            TextView title = new TextView(MainActivity.this); title.setTextSize(17); title.setTextColor(0xFF202A32); title.setMaxLines(2); texts.addView(title);
            TextView subtitle = new TextView(MainActivity.this); subtitle.setTextSize(12); subtitle.setTextColor(0xFF62717D); subtitle.setPadding(0, dp(4), 0, 0); texts.addView(subtitle);
            TextView overview = new TextView(MainActivity.this); overview.setTextSize(12); overview.setTextColor(0xFF56636D); overview.setMaxLines(3); overview.setPadding(0, dp(5), 0, 0); texts.addView(overview);
            return new Holder(card, poster, title, subtitle, overview);
        }
        @Override public void onBindViewHolder(Holder h, int position) {
            Object row = rows.get(position);
            if (row instanceof MovieWithUserData) {
                MovieWithUserData s = (MovieWithUserData) row; MovieMetadata m = s.movie; UserMovie u = s.personal;
                h.title.setText(m.title); String year = m.releaseDate == null || m.releaseDate.length() < 4 ? "" : m.releaseDate.substring(0, 4);
                String meta = year + (u.rating > 0 ? "   ★ " + String.format(Locale.US, "%.1f", u.rating) : "") + (u.watched ? "   ·   Watched" : "   ·   Watchlist"); h.subtitle.setText(meta); h.overview.setText(u.favorite ? "♥ Favorite" + (u.notes == null ? "" : "   ·   " + u.notes) : (u.notes == null ? nullToEmpty(m.overview) : u.notes));
                Object image = m.localPosterPath != null ? new java.io.File(m.localPosterPath) : (m.posterPath == null ? null : "https://image.tmdb.org/t/p/w342" + m.posterPath); Glide.with(MainActivity.this).load(image).placeholder(android.R.drawable.ic_menu_report_image).into(h.poster);
                h.itemView.setOnClickListener(v -> openSaved(s));
            } else {
                TmdbModels.MovieResult m = (TmdbModels.MovieResult) row; h.title.setText(m.title == null ? "Untitled" : m.title); h.subtitle.setText(m.release_date == null || m.release_date.length() < 4 ? "Release date unknown" : m.release_date.substring(0, 4)); h.overview.setText(m.overview == null ? "" : m.overview);
                Glide.with(MainActivity.this).load(m.poster_path == null ? null : "https://image.tmdb.org/t/p/w342" + m.poster_path).placeholder(android.R.drawable.ic_menu_report_image).into(h.poster); h.itemView.setOnClickListener(v -> openResult(m));
            }
        }
        @Override public int getItemCount() { return rows.size(); }
        class Holder extends RecyclerView.ViewHolder { ImageView poster; TextView title, subtitle, overview; Holder(android.view.View v, ImageView p, TextView t, TextView s, TextView o) { super(v); poster=p; title=t; subtitle=s; overview=o; } }
    }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }
    @Override protected void onDestroy() { super.onDestroy(); io.shutdown(); }
}
