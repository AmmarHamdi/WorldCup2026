package com.worldcup.calendar2026.domain.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class Team(
    val id: Int,
    val name: String,
    val logoUrl: String?
)

enum class MatchState { SCHEDULED, LIVE, FINISHED, POSTPONED, UNKNOWN }

data class Match(
    val id: Int,
    val kickoff: ZonedDateTime,
    val round: String,
    val venue: String?,
    val city: String?,
    val home: Team,
    val away: Team,
    val homeGoals: Int?,
    val awayGoals: Int?,
    val state: MatchState,
    val statusShort: String,
    val elapsed: Int?
) {
    val localDate: LocalDate get() = kickoff.toLocalDate()
    val isLive: Boolean get() = state == MatchState.LIVE
    val isFinished: Boolean get() = state == MatchState.FINISHED
}

/** One group (e.g. "Group A") with its ordered rows. */
data class GroupStanding(
    val groupName: String,
    val rows: List<StandingRow>
)

data class StandingRow(
    val rank: Int,
    val team: Team,
    val played: Int,
    val win: Int,
    val draw: Int,
    val lose: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalsDiff: Int,
    val points: Int,
    val form: String?
)
