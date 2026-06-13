package com.worldcup.calendar2026.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    val kickoff: String,        // ISO-8601 ZonedDateTime
    val localDate: String,      // yyyy-MM-dd for date queries
    val round: String,
    val venue: String?,
    val city: String?,
    val homeId: Int,
    val homeName: String,
    val homeLogoUrl: String?,
    val awayId: Int,
    val awayName: String,
    val awayLogoUrl: String?,
    val homeGoals: Int?,
    val awayGoals: Int?,
    val state: String,          // MatchState name
    val statusShort: String,
    val elapsed: Int?
)

@Entity(tableName = "group_standings")
data class GroupStandingEntity(
    @PrimaryKey val groupName: String
)

@Entity(
    tableName = "standing_rows",
    primaryKeys = ["groupName", "rank"],
    foreignKeys = [ForeignKey(
        entity = GroupStandingEntity::class,
        parentColumns = ["groupName"],
        childColumns = ["groupName"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("groupName")]
)
data class StandingRowEntity(
    val groupName: String,
    val rank: Int,
    val teamId: Int,
    val teamName: String,
    val teamLogoUrl: String?,
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
