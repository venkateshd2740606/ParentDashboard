package com.parentdashboard.di

import android.content.Context
import androidx.room.Room
import com.parentdashboard.data.local.database.ParentDashboardDatabase
import com.parentdashboard.data.local.database.dao.AchievementDao
import com.parentdashboard.data.local.database.dao.ChallengeDao
import com.parentdashboard.data.local.database.dao.EconomyDao
import com.parentdashboard.data.local.database.dao.GameDao
import com.parentdashboard.data.local.database.dao.ProfileDao
import com.parentdashboard.data.local.database.dao.StatsDao
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
    fun provideDatabase(@ApplicationContext context: Context): ParentDashboardDatabase =
        Room.databaseBuilder(context, ParentDashboardDatabase::class.java, "parentdashboard.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideGameDao(db: ParentDashboardDatabase): GameDao = db.gameDao()
    @Provides fun provideStatsDao(db: ParentDashboardDatabase): StatsDao = db.statsDao()
    @Provides fun provideAchievementDao(db: ParentDashboardDatabase): AchievementDao = db.achievementDao()
    @Provides fun provideChallengeDao(db: ParentDashboardDatabase): ChallengeDao = db.challengeDao()
    @Provides fun provideEconomyDao(db: ParentDashboardDatabase): EconomyDao = db.economyDao()
    @Provides fun provideProfileDao(db: ParentDashboardDatabase): ProfileDao = db.profileDao()
}
