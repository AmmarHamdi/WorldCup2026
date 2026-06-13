package com.worldcup.calendar2026.data.local

import com.worldcup.calendar2026.domain.model.GroupStanding
import com.worldcup.calendar2026.domain.model.Match
import com.worldcup.calendar2026.domain.model.MatchState
import com.worldcup.calendar2026.domain.model.StandingRow
import com.worldcup.calendar2026.domain.model.Team
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// ---- Match ↔ MatchEntity ----------------------------------------------------

fun Match.toEntity(): MatchEntity = MatchEntity(
    id = id,
    kickoff = kickoff.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
    localDate = localDate.toString(),
    round = round,
    venue = venue,
    city = city,
    homeId = home.id,
    homeName = home.name,
    homeLogoUrl = home.logoUrl,
    awayId = away.id,
    awayName = away.name,
    awayLogoUrl = away.logoUrl,
    homeGoals = homeGoals,
    awayGoals = awayGoals,
    state = state.name,
    statusShort = statusShort,
    elapsed = elapsed
)

fun MatchEntity.toDomain(): Match = Match(
    id = id,
    kickoff = ZonedDateTime.parse(kickoff, DateTimeFormatter.ISO_ZONED_DATE_TIME),
    round = round,
    venue = venue,
    city = city,
    home = Team(homeId, homeName, homeLogoUrl),
    away = Team(awayId, awayName, awayLogoUrl),
    homeGoals = homeGoals,
    awayGoals = awayGoals,
    state = runCatching { MatchState.valueOf(state) }.getOrDefault(MatchState.UNKNOWN),
    statusShort = statusShort,
    elapsed = elapsed
)

// ---- GroupStanding ↔ Entities -----------------------------------------------

fun GroupStanding.toEntities(): Pair<GroupStandingEntity, List<StandingRowEntity>> {
    val group = GroupStandingEntity(groupName = groupName)
    val rows = rows.map { row ->
        StandingRowEntity(
            groupName = groupName,
            rank = row.rank,
            teamId = row.team.id,
            teamName = row.team.name,
            teamLogoUrl = row.team.logoUrl,
            played = row.played,
            win = row.win,
            draw = row.draw,
            lose = row.lose,
            goalsFor = row.goalsFor,
            goalsAgainst = row.goalsAgainst,
            goalsDiff = row.goalsDiff,
            points = row.points,
            form = row.form
        )
    }
    return group to rows
}

fun groupEntitiesToDomain(
    groups: List<GroupStandingEntity>,
    rows: List<StandingRowEntity>
): List<GroupStanding> {
    val rowsByGroup = rows.groupBy { it.groupName }
    return groups.map { group ->
        GroupStanding(
            groupName = group.groupName,
            rows = (rowsByGroup[group.groupName] ?: emptyList()).map { row ->
                StandingRow(
                    rank = row.rank,
                    team = Team(row.teamId, row.teamName, row.teamLogoUrl),
                    played = row.played,
                    win = row.win,
                    draw = row.draw,
                    lose = row.lose,
                    goalsFor = row.goalsFor,
                    goalsAgainst = row.goalsAgainst,
                    goalsDiff = row.goalsDiff,
                    points = row.points,
                    form = row.form
                )
            }
        )
    }
}
