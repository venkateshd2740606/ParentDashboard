package com.parentdashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentdashboard.domain.model.Achievement
import com.parentdashboard.domain.model.AppTheme
import com.parentdashboard.domain.model.ChallengeRecord
import com.parentdashboard.domain.model.ChallengeType
import com.parentdashboard.domain.model.ColorBlindMode
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.EconomyState
import com.parentdashboard.domain.model.PuzzleProfile
import com.parentdashboard.domain.model.UserPreferences
import com.parentdashboard.domain.model.UserStats
import com.parentdashboard.domain.repository.ChallengeRepository
import com.parentdashboard.domain.repository.PreferencesRepository
import com.parentdashboard.domain.repository.ProgressionRepository
import com.parentdashboard.util.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val progressionRepository: ProgressionRepository
) : ViewModel() {
    private val _prefs = MutableStateFlow(UserPreferences())
    val prefs: StateFlow<UserPreferences> = _prefs.asStateFlow()
    private val _economy = MutableStateFlow(EconomyState())
    val economy: StateFlow<EconomyState> = _economy.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.getUserPreferences().collect { _prefs.value = it }
        }
        viewModelScope.launch {
            progressionRepository.observeEconomy().collect { _economy.value = it }
        }
    }

    fun setTheme(theme: AppTheme) = update { it.copy(appTheme = theme) }
    fun setHaptic(enabled: Boolean) = update { it.copy(hapticFeedback = enabled) }
    fun setSound(enabled: Boolean) = update { it.copy(soundEnabled = enabled) }
    fun setReducedMotion(enabled: Boolean) = update { it.copy(reducedMotion = enabled) }
    fun setHighContrast(enabled: Boolean) = update { it.copy(highContrastMode = enabled) }
    fun setColorBlind(mode: ColorBlindMode) = update { it.copy(colorBlindMode = mode) }
    fun setFontScale(scale: Float) = update { it.copy(fontScale = scale) }
    fun setTimerVisible(visible: Boolean) = update { it.copy(timerVisible = visible) }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesRepository.updatePreferences { it.copy(language = languageCode) }
            LocaleHelper.applyAppLocale(languageCode)
        }
    }

    fun unlockTheme(themeId: String) {
        viewModelScope.launch { progressionRepository.unlockTheme(themeId) }
    }

    private fun update(transform: (UserPreferences) -> UserPreferences) {
        viewModelScope.launch { preferencesRepository.updatePreferences(transform) }
    }
}

@HiltViewModel
class ModeSelectViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _campaignLevels = MutableStateFlow<Map<Difficulty, Int>>(emptyMap())
    val campaignLevels: StateFlow<Map<Difficulty, Int>> = _campaignLevels.asStateFlow()

    init {
        viewModelScope.launch {
            val levels = Difficulty.entries
                .filter { it != Difficulty.ENDLESS }
                .associateWith { preferencesRepository.getCampaignLevel(it) }
            _campaignLevels.value = levels
        }
    }
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val progressionRepository: ProgressionRepository
) : ViewModel() {
    private val _stats = MutableStateFlow(UserStats())
    val stats: StateFlow<UserStats> = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            progressionRepository.observeStats().collect { _stats.value = it }
        }
    }
}

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val progressionRepository: ProgressionRepository
) : ViewModel() {
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    init {
        viewModelScope.launch {
            progressionRepository.observeAchievements().collect { _achievements.value = it }
        }
    }
}

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {
    private val _challenge = MutableStateFlow<ChallengeRecord?>(null)
    val challenge: StateFlow<ChallengeRecord?> = _challenge.asStateFlow()
    private val _history = MutableStateFlow<List<ChallengeRecord>>(emptyList())
    val history: StateFlow<List<ChallengeRecord>> = _history.asStateFlow()
    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    fun load(type: ChallengeType) {
        viewModelScope.launch {
            challengeRepository.resolveActiveChallenge(type)
            challengeRepository.observeActiveChallenge(type).collect { record ->
                _challenge.value = record
                if (record != null) {
                    _streak.value = challengeRepository.getCurrentStreak(type)
                }
            }
        }
        viewModelScope.launch {
            challengeRepository.observeChallengeHistory(type).collect { _history.value = it }
        }
    }
}

@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val consentManager: com.parentdashboard.consent.ConsentManager
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun acceptAll(onDone: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            consentManager.applyConsent(analyticsEnabled = true, personalizedAds = true)
            _loading.value = false
            onDone()
        }
    }

    fun acceptEssentialOnly(onDone: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            consentManager.applyConsent(analyticsEnabled = false, personalizedAds = false)
            _loading.value = false
            onDone()
        }
    }

    fun denyAll(onDone: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            consentManager.denyAll()
            _loading.value = false
            onDone()
        }
    }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.updatePreferences { it.copy(onboardingCompleted = true) }
            onDone()
        }
    }
}

@HiltViewModel
class PuzzleProfileViewModel @Inject constructor(
    private val progressionRepository: ProgressionRepository
) : ViewModel() {
    private val _profile = MutableStateFlow(PuzzleProfile())
    val profile: StateFlow<PuzzleProfile> = _profile.asStateFlow()

    init {
        viewModelScope.launch {
            progressionRepository.observePuzzleProfile().collect { _profile.value = it }
        }
    }

    fun strengthTopPercent(profile: PuzzleProfile): Int =
        com.parentdashboard.engine.PuzzleProfileEngine.percentileTopValue(profile, profile.strength)

    fun weaknessTopPercent(profile: PuzzleProfile): Int =
        com.parentdashboard.engine.PuzzleProfileEngine.percentileTopValue(profile, profile.weakness)

    fun weaknessNeedsPractice(profile: PuzzleProfile): Boolean =
        profile.weakness == com.parentdashboard.domain.model.SkillCategory.TIME_PRESSURE
}

data class SampleSeed(val seed: Long, val levelNumber: Int, val difficulty: Difficulty)

@HiltViewModel
class SeedLabViewModel @Inject constructor() : ViewModel() {
    fun generateSampleSeed(): SampleSeed {
        val seed = System.currentTimeMillis() % 9_999_999L + 1
        return SampleSeed(seed = seed, levelNumber = 1, difficulty = Difficulty.MEDIUM)
    }
}
