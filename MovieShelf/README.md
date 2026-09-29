# MovieShelf

A Java Android app for discovering movies with TMDB and keeping your watchlist, watched log, ratings, notes, favorites and rewatch count on-device. Saved films and downloaded posters remain available offline.

## Open and run

1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Obtain a TMDB API Read Access Token from your TMDB account API settings. TMDB use is subject to its terms and attribution rules; the app includes attribution.
3. In Android Studio choose **Open** and select this `MovieShelf` folder. Let Gradle sync and install any prompted SDK/build tools.
4. Supply the token as the Gradle property `TMDB_READ_ACCESS_TOKEN`, preferably in your user-level Gradle properties file (`%USERPROFILE%\.gradle\gradle.properties` on Windows, `~/.gradle/gradle.properties` on macOS/Linux):

   ```properties
   TMDB_READ_ACCESS_TOKEN=your_read_access_token
   ```

   Or add that property to the project `local.properties` file beside `settings.gradle` (already ignored by Git). Never commit or share the token. Without it, saved lists work and online search explains the configuration issue.
5. Select an Android 7.0 (API 24) or newer device/emulator and press **Run**.

The project uses Android Gradle Plugin 8.7.3, Java 17, compile SDK 35, Room 2.6.1, Retrofit 2.11.0, and AndroidX RecyclerView/Material. Android Studio downloads Gradle/dependencies at first sync. Build with `gradle :app:assembleDebug` after installing Gradle and Android SDK 35.

## How it works

- **Search** calls TMDB movie search; opening a result fetches full details.
- **Watchlist**, **Watched**, and **Favorites** are local Room filters and work offline.
- `MovieMetadata` is separate from `UserMovie`; half-star rating, notes, favorite, watch date and rewatch count belong to your entry.
- Posters download to app-private storage when saving. If a download fails, the TMDB image path is kept as a fallback and the movie is still saved.
- Removing a personal entry removes it from all lists. There is no account or cloud sync.

Source is under `app/src/main/java/com/example/movieshelf/`. Layouts are built in Java for a straightforward project structure. Android 7.0+ is required.
