package com.parentdashboard.presentation.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.parentdashboard.R
import com.parentdashboard.domain.model.AppTheme
import com.parentdashboard.domain.model.Difficulty
import com.parentdashboard.domain.model.PlayerRank
import com.parentdashboard.domain.model.PuzzleArchetype
import com.parentdashboard.domain.model.SkillCategory

@Composable
@ReadOnlyComposable
fun Difficulty.localizedName(): String = when (this) {
    Difficulty.BEGINNER -> stringResource(R.string.difficulty_beginner)
    Difficulty.EASY -> stringResource(R.string.difficulty_easy)
    Difficulty.MEDIUM -> stringResource(R.string.difficulty_medium)
    Difficulty.HARD -> stringResource(R.string.difficulty_hard)
    Difficulty.EXPERT -> stringResource(R.string.difficulty_expert)
    Difficulty.MASTER -> stringResource(R.string.difficulty_master)
    Difficulty.ENDLESS -> stringResource(R.string.endless_mode)
}

@Composable
@ReadOnlyComposable
fun AppTheme.localizedName(): String = when (this) {
    AppTheme.SYSTEM -> stringResource(R.string.theme_system)
    AppTheme.LIGHT -> stringResource(R.string.theme_light)
    AppTheme.DARK -> stringResource(R.string.theme_dark)
    AppTheme.AMOLED -> stringResource(R.string.theme_amoled)
    AppTheme.NEON -> stringResource(R.string.theme_neon)
    AppTheme.CYBER -> stringResource(R.string.theme_cyber)
    AppTheme.SPACE -> stringResource(R.string.theme_space)
    AppTheme.NATURE -> stringResource(R.string.theme_nature)
}

@Composable
@ReadOnlyComposable
fun PlayerRank.localizedTitle(): String = when (this) {
    PlayerRank.NOVICE -> stringResource(R.string.rank_novice)
    PlayerRank.APPRENTICE -> stringResource(R.string.rank_apprentice)
    PlayerRank.PUZZLER -> stringResource(R.string.rank_puzzler)
    PlayerRank.STRATEGIST -> stringResource(R.string.rank_strategist)
    PlayerRank.EXPERT -> stringResource(R.string.rank_expert)
    PlayerRank.MASTER -> stringResource(R.string.rank_master)
    PlayerRank.GRANDMASTER -> stringResource(R.string.rank_grandmaster)
    PlayerRank.LEGEND -> stringResource(R.string.rank_legend)
}

@Composable
@ReadOnlyComposable
fun PuzzleArchetype.localizedTitle(): String = when (this) {
    PuzzleArchetype.ARCHITECT -> stringResource(R.string.archetype_architect_title)
    PuzzleArchetype.SPRINTER -> stringResource(R.string.archetype_sprinter_title)
    PuzzleArchetype.ANALYST -> stringResource(R.string.archetype_analyst_title)
    PuzzleArchetype.EXPLORER -> stringResource(R.string.archetype_explorer_title)
    PuzzleArchetype.STRATEGIST -> stringResource(R.string.archetype_strategist_title)
}

@Composable
@ReadOnlyComposable
fun PuzzleArchetype.localizedDescription(): String = when (this) {
    PuzzleArchetype.ARCHITECT -> stringResource(R.string.archetype_architect_desc)
    PuzzleArchetype.SPRINTER -> stringResource(R.string.archetype_sprinter_desc)
    PuzzleArchetype.ANALYST -> stringResource(R.string.archetype_analyst_desc)
    PuzzleArchetype.EXPLORER -> stringResource(R.string.archetype_explorer_desc)
    PuzzleArchetype.STRATEGIST -> stringResource(R.string.archetype_strategist_desc)
}

@Composable
@ReadOnlyComposable
fun SkillCategory.localizedLabel(): String = when (this) {
    SkillCategory.PATTERN_RECOGNITION -> stringResource(R.string.skill_pattern_recognition)
    SkillCategory.PLANNING -> stringResource(R.string.skill_planning)
    SkillCategory.SPEED -> stringResource(R.string.skill_speed)
    SkillCategory.ACCURACY -> stringResource(R.string.skill_accuracy)
    SkillCategory.COMPLEX_CHAINS -> stringResource(R.string.skill_complex_chains)
    SkillCategory.TIME_PRESSURE -> stringResource(R.string.skill_time_pressure)
}

@Composable
@ReadOnlyComposable
fun languageDisplayName(languageCode: String): String = when (languageCode) {
    "system" -> stringResource(R.string.lang_system)
    "en" -> stringResource(R.string.lang_en)
    "hi" -> stringResource(R.string.lang_hi)
    "te" -> stringResource(R.string.lang_te)
    "ta" -> stringResource(R.string.lang_ta)
    "kn" -> stringResource(R.string.lang_kn)
    "ml" -> stringResource(R.string.lang_ml)
    "mr" -> stringResource(R.string.lang_mr)
    "bn" -> stringResource(R.string.lang_bn)
    "gu" -> stringResource(R.string.lang_gu)
    "pa" -> stringResource(R.string.lang_pa)
    "es" -> stringResource(R.string.lang_es)
    "fr" -> stringResource(R.string.lang_fr)
    "de" -> stringResource(R.string.lang_de)
    "pt" -> stringResource(R.string.lang_pt)
    "it" -> stringResource(R.string.lang_it)
    "ru" -> stringResource(R.string.lang_ru)
    "ar" -> stringResource(R.string.lang_ar)
    "in" -> stringResource(R.string.lang_in)
    "ja" -> stringResource(R.string.lang_ja)
    "ko" -> stringResource(R.string.lang_ko)
    "zh" -> stringResource(R.string.lang_zh)
    "zh-TW" -> stringResource(R.string.lang_zh_tw)
    else -> languageCode
}
