package com.parentdashboard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.parentdashboard.data.local.database.entity.AchievementEntity
import com.parentdashboard.data.local.database.entity.ChallengeEntity
import com.parentdashboard.data.local.database.entity.EconomyEntity
import com.parentdashboard.data.local.database.entity.GameEntity
import com.parentdashboard.data.local.database.entity.ProfileEntity
import com.parentdashboard.data.local.database.entity.StatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: GameEntity): Long

    @Update
    suspend fun update(game: GameEntity)

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: Long): GameEntity?

    @Query("SELECT * FROM games WHERE status = 'IN_PROGRESS' ORDER BY lastPlayedAt DESC LIMIT 1")
    suspend fun getInProgress(): GameEntity?

    @Query("SELECT * FROM games WHERE status = 'IN_PROGRESS' ORDER BY lastPlayedAt DESC LIMIT 1")
    fun observeInProgress(): Flow<GameEntity?>

    @Query("UPDATE games SET status = 'FAILED' WHERE id = :id")
    suspend fun abandon(id: Long)
}

@Dao
interface StatsDao {
    @Query("SELECT * FROM stats WHERE id = 1")
    fun observe(): Flow<StatsEntity?>

    @Query("SELECT * FROM stats WHERE id = 1")
    suspend fun get(): StatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: StatsEntity)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAll(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Update
    suspend fun update(achievement: AchievementEntity)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE key = :key")
    fun observeByKey(key: String): Flow<ChallengeEntity?>

    @Query("SELECT * FROM challenges WHERE key = :key")
    suspend fun getByKey(key: String): ChallengeEntity?

    @Query("SELECT * FROM challenges WHERE type = :type ORDER BY key DESC")
    fun observeByType(type: String): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: ChallengeEntity)

    @Update
    suspend fun update(challenge: ChallengeEntity)

    @Query("SELECT * FROM challenges WHERE type = :type AND isCompleted = 1 ORDER BY key DESC LIMIT 30")
    suspend fun getCompletedHistory(type: String): List<ChallengeEntity>
}

@Dao
interface EconomyDao {
    @Query("SELECT * FROM economy WHERE id = 1")
    fun observe(): Flow<EconomyEntity?>

    @Query("SELECT * FROM economy WHERE id = 1")
    suspend fun get(): EconomyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(economy: EconomyEntity)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM puzzle_profile WHERE id = 1")
    fun observe(): Flow<ProfileEntity?>

    @Query("SELECT * FROM puzzle_profile WHERE id = 1")
    suspend fun get(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProfileEntity)
}
