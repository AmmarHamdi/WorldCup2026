package com.worldcup.calendar2026

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WorldCupApp : Application()

/**
 * Tournament identifiers for the API-Football v3 service.
 *
 * In API-Football, "World Cup" is league id 1 and the season is the calendar year.
 * If you ever get an empty response, confirm the id with:
 *   GET https://v3.football.api-sports.io/leagues?search=world%20cup
 */
object WorldCupConfig {
    const val LEAGUE_ID = 1
    const val SEASON = 2026
}
