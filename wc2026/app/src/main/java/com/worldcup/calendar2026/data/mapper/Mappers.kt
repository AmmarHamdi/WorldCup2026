package com.worldcup.calendar2026.data.mapper

import com.worldcup.calendar2026.data.remote.dto.FixtureResponseDto
import com.worldcup.calendar2026.data.remote.dto.StandingEntryDto
import com.worldcup.calendar2026.data.remote.dto.TeamDto
import com.worldcup.calendar2026.domain.model.GroupStanding
import com.worldcup.calendar2026.domain.model.Match
import com.worldcup.calendar2026.domain.model.MatchState
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
