package com.worldcup.calendar2026.data.repository

import com.worldcup.calendar2026.WorldCupConfig
import com.worldcup.calendar2026.data.mapper.toGroupStanding
import com.worldcup.calendar2026.data.mapper.toMatch
import com.worldcup.calendar2026.data.remote.ApiFootballService
import com.worldcup.calendar2026.domain.model.GroupStanding
import com.worldcup.calendar2026.domain.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorldCupRepository @Inject constructor(
    private val service: ApiFootballService
) {
    private val league = WorldCupConfig.LEAGUE_ID
    private val season = WorldCupConfig.SEASON

    suspend fun fixtures(): List<Match> = withContext(Dispatchers.IO) {
        service.getFixtures(league, season).response
            .map { it.toMatch() }
            .sortedBy { it.kickoff }
    }

    suspend fun fixturesOn(date: LocalDate): List<Match> = withContext(Dispatchers.IO) {
        service.getFixturesByDate(league, season, date.toString()).response
            .map { it.toMatch() }
            .sortedBy { it.kickoff }
    }

    suspend fun liveMatches(): List<Match> = withContext(Dispatchers.IO) {
        service.getLiveFixtures(league, season).response
            .map { it.toMatch() }
            .sortedBy { it.kickoff }
    }

    suspend fun standings(): List<GroupStanding> = withContext(Dispatchers.IO) {
        service.getStandings(league, season).response
            .firstOrNull()
            ?.league
            ?.standings
            ?.map { it.toGroupStanding() }
            ?: emptyList()
    }
}
