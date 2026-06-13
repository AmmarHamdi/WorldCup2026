package com.worldcup.calendar2026.data.repository

import com.worldcup.calendar2026.WorldCupConfig
import com.worldcup.calendar2026.data.mapper.toGroupStanding
import com.worldcup.calendar2026.data.mapper.toMatch
import com.worldcup.calendar2026.data.remote.ApiFootballService
import com.worldcup.calendar2026.data.remote.dto.StatusResponseDto
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

    private fun Map<String, String>.toErrorMessage() = values.joinToString("; ")

    suspend fun checkStatus(): StatusResponseDto = withContext(Dispatchers.IO) {
        val envelope = service.getStatus()
        if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
        envelope.response ?: throw IllegalStateException("Empty status response")
    }

    suspend fun fixtures(): List<Match> = withContext(Dispatchers.IO) {
        val envelope = service.getFixtures(league, season)
        if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
        envelope.response.map { it.toMatch() }.sortedBy { it.kickoff }
    }

    suspend fun fixturesOn(date: LocalDate): List<Match> = withContext(Dispatchers.IO) {
        val envelope = service.getFixturesByDate(league, season, date.toString())
        if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
        envelope.response.map { it.toMatch() }.sortedBy { it.kickoff }
    }

    suspend fun liveMatches(): List<Match> = withContext(Dispatchers.IO) {
        val envelope = service.getLiveFixtures(league, season)
        if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
        envelope.response.map { it.toMatch() }.sortedBy { it.kickoff }
    }

    suspend fun standings(): List<GroupStanding> = withContext(Dispatchers.IO) {
        val envelope = service.getStandings(league, season)
        if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
        envelope.response
            .firstOrNull()
            ?.league
            ?.standings
            ?.map { it.toGroupStanding() }
            ?: emptyList()
    }
}
