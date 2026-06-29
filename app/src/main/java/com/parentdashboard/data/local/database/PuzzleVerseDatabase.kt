package com.parentdashboard.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.parentdashboard.data.local.database.dao.AchievementDao
import com.parentdashboard.data.local.database.dao.ChallengeDao
import com.parentdashboard.data.local.database.dao.EconomyDao
import com.parentdashboard.data.local.database.dao.GameDao
import com.parentdashboard.data.local.database.dao.ProfileDao
import com.parentdashboard.data.local.database.dao.StatsDao
import com.parentdashboard.data.local.database.entity.ProfileEntity
import com.parentdashboard.data.local.database.entity.AchievementEntity
import com.parentdashboard.data.local.database.entity.ChallengeEntity
import com.parentdashboard.data.local.database.entity.EconomyEntity
import com.parentdashboard.data.local.database.entity.GameEntity
import com.parentdashboard.data.local.database.entity.StatsEntity

@Database(
    entities = [
        GameEntity::class,
        StatsEntity::class,
        AchievementEntity::class,
        ChallengeEntity::class,
        EconomyEntity::class,
        ProfileEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class ParentDashboardDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun statsDao(): StatsDao
    abstract fun achievementDao(): AchievementDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun economyDao(): EconomyDao
    abstract fun profileDao(): ProfileDao
}
