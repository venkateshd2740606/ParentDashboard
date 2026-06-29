package com.parentdashboard.presentation.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parentdashboard.R
import com.parentdashboard.domain.model.AppTheme
import com.parentdashboard.domain.model.ColorBlindMode
import com.parentdashboard.domain.model.LearningLanguage
import com.parentdashboard.presentation.ui.util.languageDisplayName
import com.parentdashboard.presentation.ui.util.learningLanguageDisplayName
import com.parentdashboard.presentation.ui.util.localizedName
import com.parentdashboard.presentation.viewmodel.SettingsViewModel
import com.parentdashboard.util.SupportedLanguages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val economy by viewModel.economy.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDefaultLearningLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { showThemeDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(stringResource(R.string.app_theme))
                    Text(prefs.appTheme.localizedName(), style = MaterialTheme.typography.bodySmall)
                }
            }

            SettingsSwitch(stringResource(R.string.high_contrast), prefs.highContrastMode, viewModel::setHighContrast)
            SettingsSwitch(stringResource(R.string.reduced_motion), prefs.reducedMotion, viewModel::setReducedMotion)

            Text(stringResource(R.string.accessibility), style = MaterialTheme.typography.titleMedium)
            ColorBlindSelector(prefs.colorBlindMode, viewModel::setColorBlind)
            Text(stringResource(R.string.font_scale_label))
            Slider(
                value = prefs.fontScale,
                onValueChange = viewModel::setFontScale,
                valueRange = 0.8f..1.5f,
                steps = 6
            )

            Text(stringResource(R.string.sound_feedback), style = MaterialTheme.typography.titleMedium)
            SettingsSwitch(stringResource(R.string.haptic_feedback), prefs.hapticFeedback, viewModel::setHaptic)
            SettingsSwitch(stringResource(R.string.sound_effects), prefs.soundEnabled, viewModel::setSound)
            SettingsSwitch(stringResource(R.string.show_timer), prefs.timerVisible, viewModel::setTimerVisible)

            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { showLanguageDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(stringResource(R.string.app_language))
                    Text(
                        languageDisplayName(prefs.language),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            TextButton(onClick = { showDefaultLearningLanguageDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(stringResource(R.string.default_learning_language))
                    Text(
                        learningLanguageDisplayName(prefs.defaultLearningLanguage),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(stringResource(R.string.themes_unlock), style = MaterialTheme.typography.titleMedium)
            economy.unlockableThemes.forEach { themeId ->
                val theme = runCatching { AppTheme.valueOf(themeId) }.getOrNull() ?: return@forEach
                val unlocked = themeId in economy.unlockedThemeIds || themeId in prefs.unlockedThemes
                ListItem(
                    headlineContent = { Text(theme.localizedName()) },
                    trailingContent = {
                        if (!unlocked) {
                            TextButton(onClick = { viewModel.unlockTheme(themeId) }) {
                                Text(stringResource(R.string.unlock_for_coins, 150))
                            }
                        } else Text(stringResource(R.string.unlocked))
                    }
                )
            }

            TextButton(onClick = onNavigateToPrivacy, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.privacy_policy))
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.app_theme)) },
            text = {
                Column {
                    AppTheme.entries.forEach { theme ->
                        val unlocked = theme.name in prefs.unlockedThemes ||
                            theme in listOf(AppTheme.SYSTEM, AppTheme.LIGHT, AppTheme.DARK)
                        TextButton(
                            onClick = {
                                if (unlocked) { viewModel.setTheme(theme); showThemeDialog = false }
                            },
                            enabled = unlocked,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(theme.localizedName()) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text(stringResource(R.string.close)) } }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.app_language)) },
            text = {
                Column {
                    SupportedLanguages.codes.forEach { code ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(languageDisplayName(code))
                            RadioButton(
                                selected = prefs.language == code,
                                onClick = {
                                    if (prefs.language != code) {
                                        viewModel.setLanguage(code)
                                    }
                                    showLanguageDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (showDefaultLearningLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showDefaultLearningLanguageDialog = false },
            title = { Text(stringResource(R.string.default_learning_language)) },
            text = {
                Column {
                    LearningLanguage.entries.forEach { language ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(learningLanguageDisplayName(language))
                            RadioButton(
                                selected = prefs.defaultLearningLanguage == language,
                                onClick = {
                                    viewModel.setDefaultLearningLanguage(language)
                                    showDefaultLearningLanguageDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDefaultLearningLanguageDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

@Composable
private fun SettingsSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun ColorBlindSelector(current: ColorBlindMode, onSelect: (ColorBlindMode) -> Unit) {
    ColorBlindMode.entries.forEach { mode ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(when (mode) {
                ColorBlindMode.NONE -> stringResource(R.string.color_blind_none)
                ColorBlindMode.DEUTERANOPIA -> stringResource(R.string.color_blind_deuteranopia)
                ColorBlindMode.PROTANOPIA -> stringResource(R.string.color_blind_protanopia)
                ColorBlindMode.TRITANOPIA -> stringResource(R.string.color_blind_tritanopia)
            })
            RadioButton(selected = current == mode, onClick = { onSelect(mode) })
        }
    }
}

