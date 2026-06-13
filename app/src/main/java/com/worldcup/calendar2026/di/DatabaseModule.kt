package com.worldcup.calendar2026.di

import android.content.Context
import androidx.room.Room
import com.worldcup.calendar2026.data.local.MatchDao
import com.worldcup.calendar2026.data.local.StandingsDao
import com.worldcup.calendar2026.data.local.WorldCupDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WorldCupDatabase =
        Room.databaseBuilder(context, WorldCupDatabase::class.java, "worldcup.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMatchDao(db: WorldCupDatabase): MatchDao = db.matchDao()

    @Provides
    fun provideStandingsDao(db: WorldCupDatabase): StandingsDao = db.standingsDao()
}
