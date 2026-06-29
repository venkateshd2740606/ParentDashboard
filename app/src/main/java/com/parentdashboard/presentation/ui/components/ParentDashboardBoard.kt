package com.parentdashboard.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentdashboard.R
import com.parentdashboard.domain.model.*
import com.parentdashboard.engine.ParentDashboardEngine

private val parentPrimary = Color(0xFF37474F)
private val parentAccent = Color(0xFF546E7A)
private val cardBg = Color(0xFFECEFF1)

@Composable
fun ParentDashboardBoard(
    game: ParentDashboardGame,
    reducedMotion: Boolean,
    onNextStep: () -> Unit,
    onAddChild: (String, Int, String) -> Unit,
    onSelectChild: (String) -> Unit,
    onLogProgress: (LearningSubject, Int, Int) -> Unit,
    onViewWeeklyReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val step = game.currentStep ?: return
    val progress = (game.currentStepIndex + 1f) / game.level.stepCount
    var showAddDialog by remember { mutableStateOf(false) }
    var logSubject by remember { mutableStateOf(LearningSubject.ABC) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = parentAccent,
            trackColor = cardBg
        )
        Text(
            game.level.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = parentPrimary
        )
        Text(
            game.level.instruction,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        ChildSwitcher(game.children, game.selectedChildId, onSelectChild)

        when (step) {
            DashboardStepMode.INTRO -> IntroStep(game)
            DashboardStepMode.ACTION -> ActionStep(
                game = game,
                onAddClick = { showAddDialog = true },
                onLogClick = { logSubject = it; onLogProgress(it, 10, 1) },
                onWeeklyClick = onViewWeeklyReport
            )
            DashboardStepMode.REVIEW -> ReviewStep(game)
        }

        SubjectCards(game)
        WeeklyBarChart(ParentDashboardEngine.weeklyReport(game))

        if (ParentDashboardEngine.canNextStep(game)) {
            Button(
                onClick = onNextStep,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = parentPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (step == DashboardStepMode.REVIEW) stringResource(R.string.tap_next) else "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else if (step == DashboardStepMode.ACTION) {
            Text(
                "Complete the task above to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showAddDialog) {
        AddChildDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, age, emoji ->
                onAddChild(name, age, emoji)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ChildSwitcher(children: List<ChildProfile>, selectedId: String?, onSelect: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        children.forEach { child ->
            val selected = child.id == selectedId
            FilterChip(
                selected = selected,
                onClick = { onSelect(child.id) },
                label = { Text("${child.avatarEmoji} ${child.name}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = parentAccent.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun IntroStep(game: ParentDashboardGame) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📊 Parent Dashboard", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Track learning progress across ABC, 123, Math, English, and Rhymes.")
            Text("${game.children.size} demo child profiles loaded locally.")
        }
    }
}

@Composable
private fun ActionStep(
    game: ParentDashboardGame,
    onAddClick: () -> Unit,
    onLogClick: (LearningSubject) -> Unit,
    onWeeklyClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (game.level.taskType) {
            DashboardTaskType.ADD_CHILD, DashboardTaskType.ADD_SECOND_CHILD -> {
                OutlinedButton(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Add Child")
                }
            }
            DashboardTaskType.SELECT_CHILD, DashboardTaskType.SWITCH_CHILD -> {
                Text("Tap a child chip above to select them.", style = MaterialTheme.typography.bodyMedium)
            }
            DashboardTaskType.LOG_FIRST_ACTIVITY, DashboardTaskType.LOG_MULTI_SUBJECT,
            DashboardTaskType.LOG_ENGLISH, DashboardTaskType.LOG_RHYMES -> {
                Text("Log progress for selected child:", fontWeight = FontWeight.Medium)
                LearningSubject.entries.forEach { subject ->
                    OutlinedButton(
                        onClick = { onLogClick(subject) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = game.selectedChildId != null
                    ) {
                        Text("Log ${subject.label} +10%")
                    }
                }
            }
            DashboardTaskType.VIEW_WEEKLY_REPORT, DashboardTaskType.EXPORT_SUMMARY -> {
                Button(onClick = onWeeklyClick, modifier = Modifier.fillMaxWidth()) {
                    Text("View Weekly Report")
                }
            }
            else -> Text("Explore the dashboard cards below.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReviewStep(game: ParentDashboardGame) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = parentPrimary.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Task complete!", fontWeight = FontWeight.Bold, color = parentPrimary)
            Text("Level ${game.level.levelNumber}: ${game.level.title}")
        }
    }
}

@Composable
private fun SubjectCards(game: ParentDashboardGame) {
    val childId = game.selectedChildId ?: game.children.firstOrNull()?.id
    val progress = childId?.let { game.progressByChild[it] }.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Subject Progress", fontWeight = FontWeight.SemiBold, color = parentPrimary)
        progress.forEach { sp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(sp.subject.label, fontWeight = FontWeight.Bold)
                        Text("⭐ ${sp.stars}")
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { sp.percentComplete / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = parentAccent
                    )
                    Text(
                        "${sp.percentComplete}% · Last: ${sp.lastPlayedDate}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(report: List<WeeklyReportEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Weekly Activity", fontWeight = FontWeight.SemiBold, color = parentPrimary)
        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Canvas(Modifier.fillMaxSize().padding(16.dp)) {
                val maxStars = (report.maxOfOrNull { it.totalStars } ?: 1).coerceAtLeast(1)
                val barWidth = size.width / (report.size * 2f)
                report.forEachIndexed { index, entry ->
                    val barHeight = (entry.totalStars.toFloat() / maxStars) * size.height * 0.8f
                    val left = index * barWidth * 2 + barWidth * 0.25f
                    drawRoundRect(
                        color = parentAccent,
                        topLeft = androidx.compose.ui.geometry.Offset(left, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            report.forEach { entry ->
                Text(entry.subject.label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun AddChildDialog(onDismiss: () -> Unit, onConfirm: (String, Int, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("5") }
    var emoji by remember { mutableStateOf("🧒") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Child") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(value = age, onValueChange = { age = it.filter { c -> c.isDigit() }.take(2) }, label = { Text("Age") }, singleLine = true)
                OutlinedTextField(value = emoji, onValueChange = { emoji = it.take(2) }, label = { Text("Avatar emoji") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, age.toIntOrNull() ?: 5, emoji.ifBlank { "🧒" }) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun GameStatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Text(text = "$label: $value", style = MaterialTheme.typography.labelLarge, modifier = modifier)
}
