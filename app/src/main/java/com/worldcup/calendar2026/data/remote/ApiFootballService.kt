package com.worldcup.calendar2026.data.remote

import com.worldcup.calendar2026.data.remote.dto.ApiEnvelope
import com.worldcup.calendar2026.data.remote.dto.FixtureResponseDto
import com.worldcup.calendar2026.data.remote.dto.StandingsResponseDto
import com.worldcup.calendar2026.data.remote.dto.StatusEnvelope
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiFootballService {

    /** Account status — used to verify API key validity and quota. */
    @GET("status")
    suspend fun getStatus(): StatusEnvelope

    /** Every fixture in the competition for the given season. */
    @GET("fixtures")
    suspend fun getFixtures(
        @Query("league") league: Int,
        @Query("season") season: Int
    ): ApiEnvelope<FixtureResponseDto>

    /** Fixtures on a single date (yyyy-MM-dd) — used for "next day". */
    @GET("fixtures")
    suspend fun getFixturesByDate(
        @Query("league") league: Int,
        @Query("season") season: Int,
        @Query("date") date: String
    ): ApiEnvelope<FixtureResponseDto>

    /** Currently in-play fixtures for the competition. */
    @GET("fixtures")
    suspend fun getLiveFixtures(
        @Query("league") league: Int,
        @Query("season") season: Int,
        @Query("live") live: String = "all"
    ): ApiEnvelope<FixtureResponseDto>

    /** Group standings (returned during the group stage). */
    @GET("standings")
    suspend fun getStandings(
        @Query("league") league: Int,
        @Query("season") season: Int
    ): ApiEnvelope<StandingsResponseDto>
}
