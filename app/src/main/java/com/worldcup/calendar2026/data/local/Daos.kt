package com.worldcup.calendar2026.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(matches: List<MatchEntity>)

    @Query("SELECT * FROM matches ORDER BY kickoff")
    suspend fun getAll(): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE localDate = :date ORDER BY kickoff")
    suspend fun getByDate(date: String): List<MatchEntity>
}

@Dao
interface StandingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupStandingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRows(rows: List<StandingRowEntity>)

    @Transaction
    suspend fun insertAll(groups: List<GroupStandingEntity>, rows: List<StandingRowEntity>) {
        insertGroups(groups)
        insertRows(rows)
    }

    @Query("SELECT * FROM group_standings ORDER BY groupName")
    suspend fun getAllGroups(): List<GroupStandingEntity>

    @Query("SELECT * FROM standing_rows ORDER BY groupName, rank")
    suspend fun getAllRows(): List<StandingRowEntity>
}
