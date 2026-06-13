# World Cup 2026 Calendar (Android · Kotlin · Compose)

An app that shows the FIFA World Cup 2026 match calendar, live matches, group
standings (classement) with points, and the next day's fixtures.

## Features
- **Calendar** – every fixture, grouped by date with date headers.
- **Live** – in-play matches with minute/score, auto-refreshing every 30s.
- **Table** – group standings: P, W, D, L, GD, Pts; top two of each group highlighted.
- **Tomorrow** – fixtures scheduled for the next day.

## Stack
- Kotlin + Jetpack Compose (Material 3)
- MVVM: `ViewModel` → `Repository` → Retrofit service
- Hilt for dependency injection (KSP)
- Retrofit + OkHttp + Moshi (codegen)
- Coil for team logos
- Navigation Compose with a bottom navigation bar

## Architecture
```
ui/        Compose screens, ViewModels, theme, reusable components
domain/    Plain Kotlin models the UI consumes (Match, GroupStanding, ...)
data/
  remote/  Retrofit service + DTOs
  mapper/  DTO -> domain mappers (date parsing, status -> MatchState)
  repository/ single source the ViewModels call
di/        Hilt NetworkModule (Retrofit, OkHttp, Moshi, auth header)
```

## Setup
1. Open the project in Android Studio (Hedgehog or newer).
2. Get a free key from https://www.api-football.com/ (the v3 API).
3. Copy `local.properties.example` to `local.properties` and set:
   ```
   FOOTBALL_API_KEY=your_key_here
   sdk.dir=/your/Android/sdk
   ```
   The key is injected via `BuildConfig.FOOTBALL_API_KEY` — it is never hardcoded.
4. Sync Gradle and run.

## Data source notes
This targets **API-Football v3** (`https://v3.football.api-sports.io/`).
The competition is identified in `WorldCupConfig`:
```kotlin
const val LEAGUE_ID = 1   // "World Cup"
const val SEASON   = 2026
```
If a call returns empty, verify the league id with:
`GET /leagues?search=world cup`.

The free tier limits requests/day and may not expose minute-by-minute live data;
swap to a paid plan or another provider by editing only `ApiFootballService`,
the DTOs, and `Mappers.kt` — the UI and ViewModels stay unchanged.

## Things you might add next
- Per-match detail screen (lineups, events, stats)
- Local caching with Room so the calendar works offline
- WorkManager/FCM push for goal alerts on followed teams
- Knockout bracket view once the group stage ends
