package com.parentdashboard.di

import com.parentdashboard.data.repository.ChallengeRepositoryImpl
import com.parentdashboard.data.repository.GameRepositoryImpl
import com.parentdashboard.data.repository.PreferencesRepositoryImpl
import com.parentdashboard.data.repository.ProgressionRepositoryImpl
import com.parentdashboard.domain.repository.ChallengeRepository
import com.parentdashboard.domain.repository.GameRepository
import com.parentdashboard.domain.repository.PreferencesRepository
import com.parentdashboard.domain.repository.ProgressionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository
    @Binds @Singleton abstract fun bindChallengeRepository(impl: ChallengeRepositoryImpl): ChallengeRepository
    @Binds @Singleton abstract fun bindProgressionRepository(impl: ProgressionRepositoryImpl): ProgressionRepository
    @Binds @Singleton abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
