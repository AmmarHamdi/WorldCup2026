package com.worldcup.calendar2026.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Generic envelope every API-Football v3 endpoint wraps its payload in. */
@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    @Json(name = "response") val response: List<T> = emptyList(),
    @Json(name = "results") val results: Int = 0
)

// ---- Fixtures ---------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class FixtureResponseDto(
    val fixture: FixtureDto,
    val league: LeagueDto,
    val teams: TeamsDto,
    val goals: GoalsDto
)

@JsonClass(generateAdapter = true)
data class FixtureDto(
    val id: Int,
    val date: String,
    val status: StatusDto,
    val venue: VenueDto?
)

@JsonClass(generateAdapter = true)
data class StatusDto(
    val long: String?,
    val short: String?,
    val elapsed: Int?
)

@JsonClass(generateAdapter = true)
data class VenueDto(
    val name: String?,
    val city: String?
)

@JsonClass(generateAdapter = true)
data class LeagueDto(
    val id: Int,
    val name: String?,
    val round: String?,
    val season: Int?
)

@JsonClass(generateAdapter = true)
data class TeamsDto(
    val home: TeamDto,
    val away: TeamDto
)

@JsonClass(generateAdapter = true)
data class TeamDto(
    val id: Int,
    val name: String?,
    val logo: String?
)

@JsonClass(generateAdapter = true)
data class GoalsDto(
    val home: Int?,
    val away: Int?
)

// ---- Standings --------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class StandingsResponseDto(
    val league: StandingsLeagueDto
)

@JsonClass(generateAdapter = true)
data class StandingsLeagueDto(
    val id: Int,
    val name: String?,
    val season: Int?,
    // Outer list = groups, inner list = rows in that group.
    val standings: List<List<StandingEntryDto>> = emptyList()
)

@JsonClass(generateAdapter = true)
data class StandingEntryDto(
    val rank: Int,
    val team: TeamDto,
    val points: Int,
    @Json(name = "goalsDiff") val goalsDiff: Int,
    val group: String?,
    val form: String?,
    val all: StatsDto
)

@JsonClass(generateAdapter = true)
data class StatsDto(
    val played: Int,
    val win: Int,
    val draw: Int,
    val lose: Int,
    val goals: StatsGoalsDto
)

@JsonClass(generateAdapter = true)
data class StatsGoalsDto(
    @Json(name = "for") val goalsFor: Int,
    @Json(name = "against") val goalsAgainst: Int
)
