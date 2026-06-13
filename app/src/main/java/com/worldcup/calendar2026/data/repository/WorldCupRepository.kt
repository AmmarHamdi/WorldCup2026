package com.worldcup.calendar2026.data.repository

import com.worldcup.calendar2026.WorldCupConfig
import com.worldcup.calendar2026.data.local.MatchDao
import com.worldcup.calendar2026.data.local.StandingsDao
import com.worldcup.calendar2026.data.local.groupEntitiesToDomain
import com.worldcup.calendar2026.data.local.toDomain
import com.worldcup.calendar2026.data.local.toEntities
import com.worldcup.calendar2026.data.local.toEntity
import com.worldcup.calendar2026.data.mapper.toGroupStanding
import com.worldcup.calendar2026.data.mapper.toLineup
import com.worldcup.calendar2026.data.mapper.toMatch
import com.worldcup.calendar2026.data.mapper.toMatchEvent
import com.worldcup.calendar2026.data.mapper.toMatchStatistics
import com.worldcup.calendar2026.data.remote.ApiFootballService
import com.worldcup.calendar2026.data.remote.dto.StatusResponseDto
import com.worldcup.calendar2026.domain.model.GroupStanding
import com.worldcup.calendar2026.domain.model.Lineup
import com.worldcup.calendar2026.domain.model.Match
import com.worldcup.calendar2026.domain.model.MatchEvent
import com.worldcup.calendar2026.domain.model.MatchStatistic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Wrapper that distinguishes fresh API data from stale cache. */
data class CachedResult<T>(
    val data: T,
    val cacheWarning: String? = null
)

@Singleton
class WorldCupRepository @Inject constructor(
    private val service: ApiFootballService,
    private val matchDao: MatchDao,
    private val standingsDao: StandingsDao
) {
    private val league = WorldCupConfig.LEAGUE_ID
    private val season = WorldCupConfig.SEASON

    private fun Map<String, String>.toErrorMessage() = values.joinToString("; ")

    suspend fun checkStatus(): StatusResponseDto = withContext(Dispatchers.IO) {
        val envelope = service.getStatus()
        if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
        envelope.response ?: throw IllegalStateException("Empty status response")
    }

    // ---- Offline-first flows ------------------------------------------------

    fun fixturesFlow(): Flow<CachedResult<List<Match>>> = flow {
        // 1. Emit cached data immediately
        val cached = matchDao.getAll().map { it.toDomain() }
        if (cached.isNotEmpty()) emit(CachedResult(cached))

        // 2. Try API
        try {
            val envelope = service.getFixtures(league, season)
            if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
            val fresh = envelope.response.map { it.toMatch() }.sortedBy { it.kickoff }
            matchDao.insertAll(fresh.map { it.toEntity() })
            emit(CachedResult(fresh))
        } catch (e: Exception) {
            if (cached.isNotEmpty()) {
                emit(CachedResult(cached, cacheWarning = "Showing cached data — network unavailable"))
            } else {
                throw e
            }
        }
    }.flowOn(Dispatchers.IO)

    fun fixturesOnFlow(date: LocalDate): Flow<CachedResult<List<Match>>> = flow {
        val dateStr = date.toString()
        val cached = matchDao.getByDate(dateStr).map { it.toDomain() }
        if (cached.isNotEmpty()) emit(CachedResult(cached))

        try {
            val envelope = service.getFixturesByDate(league, season, dateStr)
            if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
            val fresh = envelope.response.map { it.toMatch() }.sortedBy { it.kickoff }
            matchDao.insertAll(fresh.map { it.toEntity() })
            emit(CachedResult(fresh))
        } catch (e: Exception) {
            if (cached.isNotEmpty()) {
                emit(CachedResult(cached, cacheWarning = "Showing cached data — network unavailable"))
            } else {
                throw e
            }
        }
    }.flowOn(Dispatchers.IO)

    fun liveMatchesFlow(): Flow<CachedResult<List<Match>>> = flow {
        // Live matches are inherently real-time, so no cache-first emit.
        try {
            val envelope = service.getLiveFixtures(league, season)
            if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
            val fresh = envelope.response.map { it.toMatch() }.sortedBy { it.kickoff }
            emit(CachedResult(fresh))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    fun standingsFlow(): Flow<CachedResult<List<GroupStanding>>> = flow {
        val cachedGroups = standingsDao.getAllGroups()
        val cachedRows = standingsDao.getAllRows()
        val cached = groupEntitiesToDomain(cachedGroups, cachedRows)
        if (cached.isNotEmpty()) emit(CachedResult(cached))

        try {
            val envelope = service.getStandings(league, season)
            if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
            val fresh = envelope.response
                .firstOrNull()
                ?.league
                ?.standings
                ?.map { it.toGroupStanding() }
                ?: emptyList()

            val (groups, rows) = fresh.map { it.toEntities() }
                .let { pairs -> pairs.map { it.first } to pairs.flatMap { it.second } }
            standingsDao.insertAll(groups, rows)
            emit(CachedResult(fresh))
        } catch (e: Exception) {
            if (cached.isNotEmpty()) {
                emit(CachedResult(cached, cacheWarning = "Showing cached data — network unavailable"))
            } else {
                throw e
            }
        }
    }.flowOn(Dispatchers.IO)

    // ---- Non-cached suspend functions (detail endpoints) --------------------

    suspend fun matchDetail(id: Int): Match = withContext(Dispatchers.IO) {
        val envelope = service.getFixtureById(id)
        if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
        envelope.response.firstOrNull()?.toMatch()
            ?: throw IllegalStateException("Fixture not found")
    }

    suspend fun matchEvents(id: Int): List<MatchEvent> = withContext(Dispatchers.IO) {
        val envelope = service.getFixtureEvents(id)
        if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
        envelope.response.map { it.toMatchEvent() }
    }

    suspend fun matchLineups(id: Int): List<Lineup> = withContext(Dispatchers.IO) {
        val envelope = service.getFixtureLineups(id)
        if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
        envelope.response.map { it.toLineup() }
    }

    suspend fun matchStatistics(id: Int): List<MatchStatistic> = withContext(Dispatchers.IO) {
        val envelope = service.getFixtureStatistics(id)
        if (envelope.errors.isNotEmpty()) throw IllegalStateException(envelope.errors.toErrorMessage())
        envelope.response.toMatchStatistics()
    }
}
