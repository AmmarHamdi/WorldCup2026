package com.worldcup.calendar2026.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MatchEntity::class, GroupStandingEntity::class, StandingRowEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WorldCupDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun standingsDao(): StandingsDao
}
