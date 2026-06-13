package com.worldcup.calendar2026.data.mapper

import com.worldcup.calendar2026.data.remote.dto.FixtureResponseDto
import com.worldcup.calendar2026.data.remote.dto.FixtureStatisticsResponseDto
import com.worldcup.calendar2026.data.remote.dto.LineupResponseDto
import com.worldcup.calendar2026.data.remote.dto.MatchEventDto
import com.worldcup.calendar2026.data.remote.dto.StandingEntryDto
import com.worldcup.calendar2026.data.remote.dto.TeamDto
import com.worldcup.calendar2026.domain.model.EventType
import com.worldcup.calendar2026.domain.model.GroupStanding
import com.worldcup.calendar2026.domain.model.Lineup
import com.worldcup.calendar2026.domain.model.LineupPlayer
import com.worldcup.calendar2026.domain.model.Match
import com.worldcup.calendar2026.domain.model.MatchEvent
import com.worldcup.calendar2026.domain.model.MatchState
import com.worldcup.calendar2026.domain.model.MatchStatistic
import com.worldcup.calendar2026.domain.model.StandingRow
import com.worldcup.calendar2026.domain.model.Team
import java.time.ZoneId
import java.time.ZonedDateTime

private val liveStatuses = setOf("1H", "2H", "HT", "ET", "BT", "P", "LIVE", "INT")
private val finishedStatuses = setOf("FT", "AET", "PEN")
private val scheduledStatuses = setOf("NS", "TBD")
private val postponedStatuses = setOf("PST", "CANC", "ABD", "SUSP", "AWD", "WO")

private fun String?.toMatchState(): MatchState = when (this) {
    in liveStatuses -> MatchState.LIVE
    in finishedStatuses -> MatchState.FINISHED
    in scheduledStatuses -> MatchState.SCHEDULED
    in postponedStatuses -> MatchState.POSTPONED
    else -> MatchState.UNKNOWN
}

private fun TeamDto.toTeam() = Team(
    id = id,
    name = name ?: "TBD",
    logoUrl = logo
)

fun FixtureResponseDto.toMatch(): Match {
    // API returns ISO-8601 with offset, e.g. 2026-06-14T16:00:00+00:00.
    // Display it in the device's local zone.
    val kickoff = ZonedDateTime.parse(fixture.date)
        .withZoneSameInstant(ZoneId.systemDefault())

    return Match(
        id = fixture.id,
        kickoff = kickoff,
        round = league.round ?: league.name.orEmpty(),
        venue = fixture.venue?.name,
        city = fixture.venue?.city,
        home = teams.home.toTeam(),
        away = teams.away.toTeam(),
        homeGoals = goals.home,
        awayGoals = goals.away,
        state = fixture.status.short.toMatchState(),
        statusShort = fixture.status.short ?: "NS",
        elapsed = fixture.status.elapsed
    )
}

fun List<StandingEntryDto>.toGroupStanding(): GroupStanding {
    val groupName = firstOrNull()?.group ?: "Group"
    val rows = map { e ->
        StandingRow(
            rank = e.rank,
            team = e.team.toTeam(),
            played = e.all.played,
            win = e.all.win,
            draw = e.all.draw,
            lose = e.all.lose,
            goalsFor = e.all.goals.goalsFor,
            goalsAgainst = e.all.goals.goalsAgainst,
            goalsDiff = e.goalsDiff,
            points = e.points,
            form = e.form
        )
    }
    return GroupStanding(groupName = groupName, rows = rows)
}

// ---- Match Events -----------------------------------------------------------

private fun String?.toEventType(): EventType = when (this?.lowercase()) {
    "goal" -> EventType.GOAL
    "card" -> EventType.CARD
    "subst" -> EventType.SUBSTITUTION
    "var" -> EventType.VAR
    else -> EventType.VAR
}

fun MatchEventDto.toMatchEvent(): MatchEvent = MatchEvent(
    minute = time?.elapsed ?: 0,
    extraMinute = time?.extra,
    teamId = team?.id ?: 0,
    playerName = player?.name ?: "Unknown",
    assistName = assist?.name,
    type = type.toEventType(),
    detail = detail
)

// ---- Lineups ----------------------------------------------------------------

fun LineupResponseDto.toLineup(): Lineup = Lineup(
    teamId = team?.id ?: 0,
    teamName = team?.name ?: "TBD",
    teamLogo = team?.logo,
    coach = coach?.name,
    startingXI = startingXI.mapNotNull { wrapper ->
        wrapper.player?.let { p ->
            LineupPlayer(
                id = p.id ?: 0,
                name = p.name ?: "Unknown",
                number = p.number ?: 0,
                position = p.position
            )
        }
    },
    substitutes = substitutes.mapNotNull { wrapper ->
        wrapper.player?.let { p ->
            LineupPlayer(
                id = p.id ?: 0,
                name = p.name ?: "Unknown",
                number = p.number ?: 0,
                position = p.position
            )
        }
    }
)

// ---- Statistics -------------------------------------------------------------

fun List<FixtureStatisticsResponseDto>.toMatchStatistics(): List<MatchStatistic> {
    if (size < 2) return emptyList()
    val homeStats = this[0].statistics.associate { it.type to it.value?.toString() }
    val awayStats = this[1].statistics.associate { it.type to it.value?.toString() }
    val allTypes = (homeStats.keys + awayStats.keys).distinct()
    return allTypes.map { type ->
        MatchStatistic(
            type = type ?: "Unknown",
            home = homeStats[type],
            away = awayStats[type]
        )
    }
}
