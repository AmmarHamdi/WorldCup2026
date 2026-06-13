# Future Features – World Cup 2026 Calendar App

A prioritised backlog of proposed features, each with a ready-to-use **Copilot prompt** you can paste into GitHub Copilot to implement it.

---

## Priority 1 — Core Experience

### 1. Match Detail Screen

**What:** Tapping a `MatchCard` opens a full-screen detail view showing lineups, match events timeline (goals ⚽, yellow/red cards 🟨🟥, substitutions 🔄), and team statistics (possession, shots, fouls, corners). The screen should include a header with both team crests, the live/final score, and the venue.

**Why:** Users expect to drill into any match for richer data; this is the single most requested feature in any football app.

**Copilot prompt:**

> Add a Match Detail screen to the World Cup 2026 app. When a user taps a `MatchCard` (in `Components.kt`), navigate to a new `MatchDetailScreen` composable, passing the fixture `id` as a nav argument.
>
> **API layer:**
> - Add three new endpoints to `ApiFootballService`:
>   - `GET /fixtures?id={id}` → returns a single `FixtureResponseDto` (reuse existing DTO).
>   - `GET /fixtures/events?fixture={id}` → create `MatchEventDto` (time, team id, player name, assist name, event type, detail).
>   - `GET /fixtures/lineups?fixture={id}` → create `LineupDto` (team id/name/logo, coach name, starting XI list with player id/name/number/position, substitutes list).
> - Add corresponding DTOs in `Dtos.kt` with Moshi `@JsonClass(generateAdapter = true)`.
> - Add mapper functions in `Mappers.kt` to convert DTOs to new domain models `MatchEvent`, `Lineup`, and `LineupPlayer` in `Models.kt`.
> - Add `matchDetail(id)`, `matchEvents(id)`, and `matchLineups(id)` methods to `WorldCupRepository`.
>
> **Domain models** (in `Models.kt`):
> - `MatchEvent(minute: Int, extraMinute: Int?, teamId: Int, playerName: String, assistName: String?, type: EventType, detail: String?)` where `EventType` is an enum: `GOAL, CARD, SUBSTITUTION, VAR`.
> - `Lineup(teamId: Int, teamName: String, teamLogo: String?, coach: String?, startingXI: List<LineupPlayer>, substitutes: List<LineupPlayer>)`.
> - `LineupPlayer(id: Int, name: String, number: Int, position: String?)`.
>
> **UI layer:**
> - Create `MatchDetailViewModel` (Hilt-injected) that loads the fixture, events, and lineups in parallel using `async`/`awaitAll`. Expose a combined `UiState<MatchDetailData>` where `MatchDetailData` holds the `Match`, list of `MatchEvent`, and two `Lineup` objects.
> - Create `MatchDetailScreen` composable with:
>   - A score header showing both team logos, names, and the score (or kickoff time if scheduled).
>   - A tab row with "Events", "Lineups", and "Stats" tabs.
>   - Events tab: a vertical timeline with minute labels, event icons, and player names.
>   - Lineups tab: two side-by-side columns listing starting XI with shirt numbers.
>   - Stats tab: horizontal bar comparisons for possession, shots, fouls, corners (use data from the fixture response's `statistics` field if available).
> - Register the route `"match/{matchId}"` in `MainScreen.kt`'s `NavHost` and make `MatchCard` clickable with `navController.navigate("match/${match.id}")`.
>
> Follow the existing architecture: MVVM, Hilt injection, `StateContainer` for loading/error states, Material 3 theming, and Coil for images.

---

### 2. Offline Mode with Room Caching

**What:** Cache fixtures, standings, and match results in a local Room database. The app loads cached data instantly on launch, then silently refreshes from the API. A banner shows "Offline — showing cached data" when there is no network.

**Why:** Users at stadiums or travelling between host cities (US, Canada, Mexico) often have poor connectivity. The calendar and standings should always be available.

**Copilot prompt:**

> Add offline caching to the World Cup 2026 app using Room.
>
> **Database layer** (new package `data/local/`):
> - Create `WorldCupDatabase` extending `RoomDatabase` with tables for `MatchEntity`, `GroupStandingEntity`, and `StandingRowEntity`.
> - `MatchEntity` should mirror all fields in the `Match` domain model. Use `TypeConverter`s for `ZonedDateTime` (store as ISO-8601 string) and `MatchState` (store as string).
> - `GroupStandingEntity` has `groupName` as primary key. `StandingRowEntity` has a composite key of `groupName` + `rank`, with a foreign key to `GroupStandingEntity`.
> - Create `MatchDao` with `@Insert(onConflict = REPLACE) suspend fun insertAll(matches: List<MatchEntity>)`, `@Query("SELECT * FROM matches ORDER BY kickoff") suspend fun getAll(): List<MatchEntity>`, and `@Query("SELECT * FROM matches WHERE localDate = :date") suspend fun getByDate(date: String): List<MatchEntity>`.
> - Create `StandingsDao` with similar insert-all and get-all methods.
> - Add mapper extension functions between entities and domain models.
>
> **Repository changes:**
> - Refactor `WorldCupRepository` to implement an offline-first strategy:
>   1. Return cached data immediately from Room.
>   2. Fetch from API in the background.
>   3. On success, update Room and emit the fresh data.
>   4. On failure (no network), keep showing cached data and surface a non-blocking warning.
> - Use `kotlinx.coroutines.flow.Flow` instead of plain suspend functions so the UI reacts to cache updates.
>
> **DI changes:**
> - Add a `DatabaseModule` in `di/` that provides the `WorldCupDatabase` singleton and its DAOs via Hilt.
>
> **UI changes:**
> - Update all ViewModels to collect `Flow` instead of calling suspend functions.
> - Show a subtle top banner (`Snackbar` or small `Surface`) when data is served from cache due to a network error.
>
> Add the Room dependencies (`room-runtime`, `room-ktx`, `room-compiler` via KSP) to `app/build.gradle.kts`. Follow the existing code style and architecture patterns.

---

### 3. Auto-Refresh for Live Matches

**What:** The Live screen automatically polls for score updates every 30 seconds while visible, with a visible countdown indicator. Polling stops when the user navigates away.

**Why:** A live score screen that doesn't auto-update defeats its purpose. The README already mentions "auto-refreshing every 30s" but the current `LiveViewModel` only fetches once.

**Copilot prompt:**

> Implement auto-refresh polling on the Live screen.
>
> **In `LiveViewModel`:**
> - Add a `private val _isPolling = MutableStateFlow(true)` flag.
> - In `init`, launch a coroutine that loops while `_isPolling.value` is true:
>   1. Call `repo.liveMatches()`.
>   2. Update `_state` and call `notifications.notifyLiveMatches(it)` on success.
>   3. `delay(30_000)` (30 seconds) before the next iteration.
> - Add `fun startPolling()` and `fun stopPolling()` methods that set `_isPolling.value`.
> - Override `onCleared()` to set `_isPolling.value = false`.
>
> **In `LiveScreen` (in `MatchScreens.kt`):**
> - Use `DisposableEffect` or `LifecycleEventEffect` to call `viewModel.startPolling()` on `ON_RESUME` and `viewModel.stopPolling()` on `ON_PAUSE`, so polling only runs while the screen is visible.
> - Add a small "Last updated: HH:mm:ss" text below the top bar or at the bottom of the list, updated each time a refresh completes.
> - Show a subtle `LinearProgressIndicator` at the top of the list during each refresh (without replacing the current data with a loading spinner — keep showing stale data while refreshing).
>
> Keep the manual pull-to-refresh / retry button as a fallback. Follow existing patterns.

---

### 4. Knockout Bracket View

**What:** A new "Bracket" tab (or a section within Standings) that visualises the knockout stage as a tournament tree: Round of 32 → Round of 16 → Quarter-finals → Semi-finals → Final. Each node shows the two teams, score, and match status.

**Why:** The 2026 World Cup expands to 48 teams with a knockout round of 32. Without a bracket, users lose context of the tournament progression after the group stage.

**Copilot prompt:**

> Add a Knockout Bracket screen to the World Cup 2026 app.
>
> **Data layer:**
> - The existing `/fixtures` endpoint already returns knockout-stage matches with round names like "Round of 32", "Round of 16", "Quarter-finals", "Semi-finals", "Final". No new API calls are needed.
> - In `WorldCupRepository`, add `suspend fun knockoutFixtures(): Map<String, List<Match>>` that calls `fixtures()` and filters/groups matches where the round does NOT start with "Group". Return them grouped by round name in tournament order.
>
> **Domain:**
> - Add a `KnockoutRound` enum: `ROUND_OF_32, ROUND_OF_16, QUARTER_FINALS, SEMI_FINALS, THIRD_PLACE, FINAL` with a `displayName` property and a `fromRoundString(round: String)` parser.
>
> **UI layer:**
> - Create `BracketViewModel` with `UiState<Map<KnockoutRound, List<Match>>>`.
> - Create `BracketScreen` composable that renders a horizontally scrollable bracket:
>   - Each round is a vertical column of match cards.
>   - Columns are laid out left-to-right in tournament progression order.
>   - Draw connecting lines (using `Canvas` or `drawBehind`) between matches to show which winners feed into the next round.
>   - Each match node shows team logos, names, and score (or "TBD" if teams aren't determined yet).
>   - Highlight completed matches and show the winner in bold.
> - Add a new `Tab.Bracket` entry in `MainScreen.kt` with a bracket icon (`Icons.Filled.AccountTree`) and register it in the `NavHost`.
>
> Use `horizontalScroll` with `rememberScrollState` for the bracket container. Follow existing Material 3 theming and architecture.

---

## Priority 2 — Engagement & Retention

### 5. Favorite Teams

**What:** Users can mark teams as favorites. Matches involving favorite teams are highlighted across all screens with a distinct accent colour or a star badge. A dedicated "My Teams" filter appears in the Calendar tab.

**Why:** With 48 teams and 104 matches, personalisation is essential to keep users engaged daily.

**Copilot prompt:**

> Add a "Favorite Teams" feature to the World Cup 2026 app.
>
> **Persistence:**
> - Create a `FavoriteTeamsStore` class (similar to `ApiKeyStore`) that stores a `Set<Int>` of team IDs in `SharedPreferences` (key: `"favorite_team_ids"`, stored as a comma-separated string).
> - Provide it as a Hilt singleton in `NetworkModule` or a new `AppModule`.
> - Expose `fun getIds(): Set<Int>`, `fun add(teamId: Int)`, `fun remove(teamId: Int)`, and `fun isFavorite(teamId: Int): Boolean`.
>
> **UI — MatchCard changes (in `Components.kt`):**
> - Add an optional `isFavorite: Boolean` parameter to `MatchCard`.
> - When true, show a small ⭐ icon next to the favourite team's name and apply a subtle primary-colour left border to the card.
>
> **UI — Team Selection screen:**
> - Create a `FavoriteTeamsScreen` composable accessible from Settings.
> - Fetch all fixtures via the repository, extract a distinct sorted list of all teams.
> - Display a searchable grid/list of teams (logo + name) with a toggleable star/check icon.
> - Persist selections to `FavoriteTeamsStore`.
>
> **UI — Calendar filter:**
> - Add a `FilterChip` row at the top of `CalendarScreen` with "All Matches" and "My Teams" options.
> - When "My Teams" is selected, filter the match list to only show matches where `home.id` or `away.id` is in the favorite set.
>
> Wire the `FavoriteTeamsStore` through ViewModels via Hilt injection. Follow existing patterns.

---

### 6. Match Reminders (Scheduled Alarms)

**What:** Each match card has a bell icon that lets users schedule a local reminder 15, 30, or 60 minutes before kickoff. The reminder fires as a notification even if the app is closed.

**Why:** Users don't want to miss their team's match. Currently, notifications only fire when the app is open.

**Copilot prompt:**

> Add match reminder functionality to the World Cup 2026 app.
>
> **Notification scheduling:**
> - Create `MatchReminderScheduler` in the `notifications/` package.
> - Use `AlarmManager.setExactAndAllowWhileIdle()` to schedule alarms.
> - Create `ReminderBroadcastReceiver extends BroadcastReceiver` that shows a notification when the alarm fires. Include match details (teams, kickoff time, venue) in the notification.
> - Store scheduled reminders in SharedPreferences as a JSON map of `matchId → reminderTimeMillis` so they can be cancelled or restored after reboot.
> - Register a `BootCompleteReceiver` that re-schedules all active reminders after device reboot.
>
> **UI changes:**
> - Add a bell icon (`Icons.Outlined.NotificationsActive`) to `MatchCard` for matches with `state == SCHEDULED`.
> - Tapping the bell opens a bottom sheet (`ModalBottomSheet`) with options: "15 min before", "30 min before", "1 hour before", and "Cancel reminder".
> - If a reminder is already set, show the bell as filled (`Icons.Filled.NotificationsActive`) and display the scheduled time.
>
> **Manifest:**
> - Add `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>` and `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>`.
> - Register `ReminderBroadcastReceiver` and `BootCompleteReceiver` in `AndroidManifest.xml`.
>
> Handle the `SCHEDULE_EXACT_ALARM` permission request for Android 12+. Follow existing code style.

---

### 7. Goal & Event Push Alerts (Background)

**What:** A `WorkManager` periodic task polls for live match updates every 2 minutes in the background. When a goal, red card, or full-time result occurs for a favourite team, the user receives a push notification — even when the app is closed.

**Why:** This transforms the app from a "check when I remember" tool to a real-time companion that keeps users informed throughout every match day.

**Copilot prompt:**

> Implement background goal alerts using WorkManager.
>
> **Worker:**
> - Create `LiveMatchWorker extends CoroutineWorker` in a new `workers/` package.
> - In `doWork()`, call `WorldCupRepository.liveMatches()` and compare results against the last-known state stored in SharedPreferences.
> - Detect new goals (homeGoals or awayGoals changed), red cards, and match completions.
> - For each detected event involving a favourite team (read from `FavoriteTeamsStore`), fire a notification via `MatchNotificationManager`.
> - Use a unique notification ID per match (`match.id + event hash`) to avoid duplicates.
> - Return `Result.success()`.
>
> **Scheduling:**
> - In `WorldCupApp.onCreate()`, enqueue a `PeriodicWorkRequestBuilder<LiveMatchWorker>(15, TimeUnit.MINUTES)` with `ExistingPeriodicWorkPolicy.KEEP`.
> - Add a `OneTimeWorkRequest` variant that runs every 2 minutes during active match windows (schedule it when live matches are detected, cancel when no live matches remain).
>
> **Hilt integration:**
> - Add `@HiltWorker` annotation and inject `WorldCupRepository`, `FavoriteTeamsStore`, and `MatchNotificationManager` via `@AssistedInject`.
> - Add the `HiltWorkerFactory` configuration in `WorldCupApp`.
>
> **Settings toggle:**
> - Add a "Background goal alerts" switch in `SettingsScreen` that enables/disables the periodic worker.
>
> Add `work-runtime-ktx` dependency to `app/build.gradle.kts`. Follow existing architecture.

---

### 8. Search & Filter

**What:** A search bar at the top of the Calendar screen allows users to filter matches by team name, venue, or city. Additional filter chips let users show only group-stage, knockout-stage, or specific-date matches.

**Why:** With 104 matches, scrolling through the full calendar is cumbersome. Quick search dramatically improves usability.

**Copilot prompt:**

> Add search and filter functionality to the Calendar screen.
>
> **ViewModel changes (`CalendarViewModel`):**
> - Add `private val _searchQuery = MutableStateFlow("")` and `val searchQuery = _searchQuery.asStateFlow()`.
> - Add `private val _stageFilter = MutableStateFlow(StageFilter.ALL)` where `StageFilter` is an enum: `ALL, GROUP_STAGE, KNOCKOUT`.
> - Create a combined flow using `combine(_state, _searchQuery, _stageFilter)` that filters the match list:
>   - Text search: match if the query appears in `home.name`, `away.name`, `venue`, `city`, or `round` (case-insensitive).
>   - Stage filter: Group stage = rounds starting with "Group", Knockout = everything else.
> - Expose `fun updateSearch(query: String)` and `fun updateStageFilter(filter: StageFilter)`.
>
> **UI changes (`CalendarScreen` in `MatchScreens.kt`):**
> - Add a `SearchBar` or `OutlinedTextField` with a search icon at the top of the screen (above the `LazyColumn`).
> - Below the search bar, add a `LazyRow` of `FilterChip`s: "All", "Group Stage", "Knockout".
> - The match list should react to both search and filter in real time (no submit button).
> - Show a "No matches found" empty state when filters yield no results.
> - Add a clear button (X icon) in the search field to reset the query.
>
> Debounce the search input by 300ms using `debounce()` from kotlinx.coroutines. Follow existing patterns.

---

## Priority 3 — Polish & Delight

### 9. Share Match Results

**What:** A share button on each `MatchCard` (for finished matches) lets users share a formatted text snippet to WhatsApp, Twitter, or any share target. Example: "⚽ Argentina 🇦🇷 2 – 1 🇫🇷 France | World Cup 2026 — Semi-final | MetLife Stadium, New York".

**Why:** Social sharing drives organic growth and is trivial to implement.

**Copilot prompt:**

> Add a share button to `MatchCard` for finished and live matches.
>
> **In `Components.kt`:**
> - Add an `IconButton` with `Icons.Outlined.Share` to the `MatchCard` composable, positioned in the top-right corner next to the `StatusBadge`.
> - Only show the share button when `match.state` is `FINISHED` or `LIVE`.
> - On click, build a share text string:
>   ```
>   ⚽ {home.name} {homeGoals} – {awayGoals} {away.name}
>   🏆 FIFA World Cup 2026 — {round}
>   🏟️ {venue}, {city}
>   📱 #WorldCup2026
>   ```
> - Launch an `ACTION_SEND` intent with type `"text/plain"` using `context.startActivity(Intent.createChooser(...))`.
> - Get the context inside the composable using `LocalContext.current`.
>
> Keep the card layout clean — use a small icon that doesn't crowd the existing round label and status badge row. Follow Material 3 icon sizing.

---

### 10. Dark / Light Theme Toggle

**What:** Add an explicit theme selector in Settings with three options: "System default", "Light", and "Dark". The preference persists across app restarts.

**Why:** Many users watch matches at night and want forced dark mode regardless of system settings.

**Copilot prompt:**

> Add a theme toggle to the Settings screen.
>
> **Persistence:**
> - Create a `ThemePreferences` class that stores the user's theme choice in SharedPreferences. Use an enum `ThemeMode { SYSTEM, LIGHT, DARK }` with a default of `SYSTEM`.
> - Provide it as a Hilt singleton.
>
> **Theme application (`Theme.kt`):**
> - Modify the existing `WorldCup2026Theme` composable to accept a `ThemeMode` parameter.
> - Use `isSystemInDarkTheme()` only when mode is `SYSTEM`. Force `darkColorScheme()` for `DARK` and `lightColorScheme()` for `LIGHT`.
>
> **UI (`SettingsScreen`):**
> - Add a "Theme" section with three `RadioButton` options: System, Light, Dark.
> - Read/write via `SettingsViewModel` which injects `ThemePreferences`.
>
> **Wiring (`MainActivity`):**
> - Read the stored `ThemeMode` as a `StateFlow` and pass it to `WorldCup2026Theme` in `setContent`. The theme change should take effect immediately without restarting the activity.
>
> Follow the existing settings layout style.

---

### 11. Multi-Language Support (i18n)

**What:** Localise all user-facing strings into Arabic, Spanish, French, and Portuguese using Android string resources. The app follows the device locale automatically.

**Why:** The World Cup is a global event hosted across three countries. Multi-language support significantly expands the audience.

**Copilot prompt:**

> Add multi-language support to the World Cup 2026 app.
>
> **String extraction:**
> - Audit every composable in `ui/screens/` and `ui/components/` for hardcoded strings.
> - Move all user-facing text to `res/values/strings.xml` with descriptive keys (e.g., `tab_calendar`, `tab_live`, `empty_no_fixtures`, `settings_api_key_label`, `settings_test_connection`, `match_status_live`, `match_status_ft`).
> - Replace all hardcoded strings with `stringResource(R.string.key)` calls.
>
> **Translations:**
> - Create `res/values-ar/strings.xml` (Arabic).
> - Create `res/values-es/strings.xml` (Spanish).
> - Create `res/values-fr/strings.xml` (French).
> - Create `res/values-pt/strings.xml` (Portuguese).
> - Provide accurate translations for all strings.
>
> **RTL support for Arabic:**
> - Ensure `android:supportsRtl="true"` is set in `AndroidManifest.xml`.
> - Audit layouts for hardcoded `start`/`end` vs `left`/`right` padding/margin and fix any RTL issues.
>
> **Date formatting:**
> - Verify that `DateTimeFormatter` patterns respect `Locale.getDefault()` (they already do for day/month names).
>
> Don't change any app logic — only extract and translate strings.

---

### 12. Add to Device Calendar Export

**What:** Users can export a match to their phone's Google/Samsung calendar as an event with the correct kickoff time, venue as the location, and team names in the title.

**Why:** Even with in-app reminders, many users rely on their system calendar. This is a one-tap integration.

**Copilot prompt:**

> Add a "Add to Calendar" action for scheduled matches.
>
> **In `Components.kt`:**
> - Add a calendar icon button (`Icons.Outlined.CalendarMonth`) to `MatchCard` for matches where `state == SCHEDULED`.
> - On click, create an `Intent(Intent.ACTION_INSERT)` with:
>   - `data = CalendarContract.Events.CONTENT_URI`
>   - `EXTRA_EVENT_BEGIN_TIME` = `match.kickoff.toInstant().toEpochMilli()`
>   - `EXTRA_EVENT_END_TIME` = kickoff + 2 hours (approximate match duration).
>   - `TITLE` = "${match.home.name} vs ${match.away.name} — FIFA World Cup 2026"
>   - `DESCRIPTION` = "${match.round}\n${match.venue}, ${match.city}"
>   - `EVENT_LOCATION` = "${match.venue}, ${match.city}"
> - Launch the intent with `context.startActivity(intent)`.
>
> This requires no permissions — the system calendar app handles the insert. Use `LocalContext.current` to get the context. Follow existing icon placement patterns in `MatchCard`.

---

### 13. Stadium Info & Map

**What:** Tapping a venue name on a match card opens a bottom sheet with the stadium's photo, capacity, city, and a "View on Map" button that opens Google Maps.

**Why:** Fans attending matches in person need quick access to stadium info and directions. Even remote viewers enjoy the context.

**Copilot prompt:**

> Add a Stadium Info bottom sheet to the World Cup 2026 app.
>
> **Data:**
> - Create a `Stadium` data class in `Models.kt`: `Stadium(name: String, city: String, country: String, capacity: Int, imageUrl: String?, latitude: Double, longitude: Double)`.
> - Create a `StadiumRepository` or a constant map `STADIUMS: Map<String, Stadium>` in a new `data/StadiumData.kt` file with all 16 World Cup 2026 venues pre-populated (MetLife Stadium, Rose Bowl, AT&T Stadium, SoFi Stadium, Estadio Azteca, BMO Field, etc.) keyed by venue name as returned by the API.
>
> **UI:**
> - Make the venue text in `MatchCard` clickable (use `Modifier.clickable`).
> - On click, show a `ModalBottomSheet` containing:
>   - Stadium image loaded via Coil `AsyncImage` (use a placeholder if no image URL).
>   - Stadium name, city, country, and capacity.
>   - A "View on Map" button that opens Google Maps via intent: `Intent(Intent.ACTION_VIEW, Uri.parse("geo:{lat},{lng}?q={venue name}"))`.
> - If the venue is not found in the map, show a simpler sheet with just the venue name and a Google Maps search link.
>
> Follow Material 3 bottom sheet patterns and existing theming.

---

## Priority 4 — Future / Post-Launch

### 14. Top Scorers Leaderboard

**What:** A new screen showing the Golden Boot race — top scorers sorted by goals, with player photo, name, team, and goal count.

**Copilot prompt:**

> Add a Top Scorers screen to the World Cup 2026 app.
>
> **API layer:**
> - Add a new endpoint to `ApiFootballService`: `GET /players/topscorers?league={league}&season={season}` → returns a list of player entries.
> - Create DTOs: `TopScorerResponseDto` containing `PlayerInfoDto(id, name, firstname, lastname, photo)` and `PlayerStatsDto` with nested statistics including `goals.total`, `games.appearences`, and team info.
> - Add mapper to convert to a domain model `TopScorer(rank, playerName, photoUrl, teamName, teamLogo, goals, assists, appearances)` in `Models.kt`.
> - Add `suspend fun topScorers(): List<TopScorer>` to `WorldCupRepository`.
>
> **UI:**
> - Create `TopScorersViewModel` and `TopScorersScreen`.
> - Display a numbered list with player photo (circular, via Coil), name, team logo, goals, and assists.
> - Highlight the top 3 with gold/silver/bronze accent.
> - Access this screen from a menu item or a sub-tab within the Standings tab.
>
> Follow existing architecture and code style.

---

### 15. Head-to-Head History

**What:** On the Match Detail screen, show the historical record between the two teams — previous World Cup meetings and last 5 encounters with dates and scores.

**Copilot prompt:**

> Add Head-to-Head history to the Match Detail screen.
>
> **API layer:**
> - Add a new endpoint to `ApiFootballService`: `GET /fixtures/headtohead?h2h={teamId1}-{teamId2}&last=5` → returns a list of `FixtureResponseDto` (reuse existing DTO).
> - Add `suspend fun headToHead(team1Id: Int, team2Id: Int): List<Match>` to `WorldCupRepository`.
>
> **UI (in `MatchDetailScreen`):**
> - Add a "Head-to-Head" section or tab in the match detail view.
> - Show a summary row: "{team1} {wins} – {draws} – {wins} {team2}".
> - Below, list the last 5 encounters as compact cards showing date, score, and competition name.
> - If no history is available, show "No previous meetings found".
>
> Load H2H data in `MatchDetailViewModel` alongside existing match data. Follow existing patterns.

---

### 16. Home Screen Widget (Jetpack Glance)

**What:** A home-screen widget showing the next upcoming match (or current live score) for the user's favourite team. Tapping opens the app to the match detail.

**Copilot prompt:**

> Add a home screen widget using Jetpack Glance.
>
> **Dependencies:**
> - Add `androidx.glance:glance-appwidget` and `androidx.glance:glance-material3` to `app/build.gradle.kts`.
>
> **Widget implementation:**
> - Create `NextMatchWidget extends GlanceAppWidget` in a new `widget/` package.
> - Create `NextMatchWidgetReceiver extends GlanceAppWidgetReceiver`.
> - The widget should display:
>   - If a live match exists for a favourite team: both team logos, the score, and elapsed time with a red "LIVE" badge.
>   - Otherwise: the next scheduled match for a favourite team with logos, team names, and kickoff time.
>   - If no favourites are set, show the next match globally.
> - Use `GlanceModifier`, `Column`, `Row`, `Image` (for logos), and `Text` composables from Glance.
> - Use a `WorkManager` periodic task (every 15 min) to update the widget data via `NextMatchWidget.update(context, glanceId)`.
>
> **Widget metadata:**
> - Create `res/xml/next_match_widget_info.xml` with `minWidth="250dp"`, `minHeight="80dp"`, `resizeMode="horizontal|vertical"`, `updatePeriodMillis="1800000"`.
> - Register the receiver in `AndroidManifest.xml` with `<intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE"/></intent-filter>` and `<meta-data>` pointing to the widget info XML.
>
> **Deep link:**
> - On widget click, open the app and navigate to the match detail screen using a deep link intent.
>
> Follow Material 3 Glance theming.

---

### 17. Analytics & Crash Reporting

**What:** Integrate Firebase Crashlytics for crash reporting and Firebase Analytics to track screen views and key user actions (favourite a team, set a reminder, share a match).

**Copilot prompt:**

> Integrate Firebase Crashlytics and Analytics into the World Cup 2026 app.
>
> **Setup:**
> - Add the Firebase BOM (`firebase-bom`), `firebase-crashlytics-ktx`, and `firebase-analytics-ktx` to `app/build.gradle.kts`.
> - Add the `com.google.gms.google-services` and `com.google.firebase.crashlytics` Gradle plugins.
> - Add a placeholder `google-services.json` note in `README.md` (the actual file is gitignored).
>
> **Crashlytics:**
> - Initialise Crashlytics in `WorldCupApp.onCreate()`.
> - In `WorldCupRepository`, catch and log non-fatal exceptions to Crashlytics using `Firebase.crashlytics.recordException(e)` alongside the existing error handling.
>
> **Analytics:**
> - Create an `AnalyticsHelper` singleton (Hilt-provided) wrapping `FirebaseAnalytics`.
> - Log screen views: call `logEvent(FirebaseAnalytics.Event.SCREEN_VIEW)` in each screen's `LaunchedEffect(Unit)`.
> - Log custom events: `"favorite_team_added"`, `"reminder_set"`, `"match_shared"`, `"theme_changed"`.
>
> **Privacy:**
> - Add a "Send anonymous usage data" toggle in Settings (default: true) that calls `FirebaseAnalytics.setAnalyticsCollectionEnabled(enabled)` and `FirebaseCrashlytics.setCrashlyticsCollectionEnabled(enabled)`.
>
> Follow the existing Hilt DI patterns. Don't commit `google-services.json` — add it to `.gitignore`.

---

## Summary Table

| # | Feature | Priority | Complexity |
|---|---------|----------|------------|
| 1 | Match Detail Screen | 🔴 P1 | High |
| 2 | Offline Mode (Room) | 🔴 P1 | High |
| 3 | Auto-Refresh Live | 🔴 P1 | Low |
| 4 | Knockout Bracket | 🔴 P1 | Medium |
| 5 | Favorite Teams | 🟡 P2 | Medium |
| 6 | Match Reminders | 🟡 P2 | Medium |
| 7 | Background Goal Alerts | 🟡 P2 | High |
| 8 | Search & Filter | 🟡 P2 | Low |
| 9 | Share Match Results | 🟢 P3 | Low |
| 10 | Theme Toggle | 🟢 P3 | Low |
| 11 | Multi-Language (i18n) | 🟢 P3 | Medium |
| 12 | Calendar Export | 🟢 P3 | Low |
| 13 | Stadium Info & Map | 🟢 P3 | Medium |
| 14 | Top Scorers | 🔵 P4 | Medium |
| 15 | Head-to-Head | 🔵 P4 | Low |
| 16 | Home Screen Widget | 🔵 P4 | High |
| 17 | Analytics & Crash Reporting | 🔵 P4 | Medium |
