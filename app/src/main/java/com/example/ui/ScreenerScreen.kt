package com.example.ui

import android.content.Intent
import android.net.Uri
import com.example.R
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScreenerScreen(
    viewModel: ScreenerViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Observe state flows
    val selectedCountry by viewModel.selectedRegion.collectAsStateWithLifecycle()
    val category by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val days by viewModel.selectedDurationDays.collectAsStateWithLifecycle()
    val activeFlags by viewModel.activeRedFlags.collectAsStateWithLifecycle()
    val isEmergencyActive by viewModel.hasEmergencySelection.collectAsStateWithLifecycle()
    val advice by viewModel.currentOtcAdvice.collectAsStateWithLifecycle()

    val medsList by viewModel.currentMedications.collectAsStateWithLifecycle()
    val allergiesList by viewModel.knownAllergies.collectAsStateWithLifecycle()
    val historyList by viewModel.consultationHistory.collectAsStateWithLifecycle()

    val parsedAiResponse by viewModel.parsedAiResponse.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiPromptText by viewModel.customAiPrompt.collectAsStateWithLifecycle()

    // Navigation state
    var currentTab by remember { mutableStateOf(0) } // 0: Screener, 1: Medication Interaction, 2: History Log, 3: AI Assistant

    // Theme Toggle State
    var isDarkMode by remember { mutableStateOf(true) }

    // About Dialog State
    var showAboutDialog by remember { mutableStateOf(false) }

    // Dynamic Theme Colors (Midnight Emerald vs. Sleek Clinical Light Theme)
    val darkBg = if (isDarkMode) Color(0xFF0C1417) else Color(0xFFF1F5F9)
    val surfaceColor = if (isDarkMode) Color(0xFF162529) else Color(0xFFFFFFFF)
    val primaryColor = if (isDarkMode) Color(0xFF10B981) else Color(0xFF059669)
    val primaryVariant = Color(0xFF047857)
    val accentColor = if (isDarkMode) Color(0xFF34D399) else Color(0xFF10B981)
    val warningRed = Color(0xFFEF4444)
    val infoBlue = Color(0xFF3B82F6)

    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color.LightGray else Color(0xFF475569)

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_pharmacist_logo),
                        contentDescription = "sympto logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Text(
                        text = "About sympto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))
                    
                    Column {
                        Text(
                            text = "Created by",
                            style = MaterialTheme.typography.labelSmall,
                            color = subtextColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Prince Patil",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column {
                        Text(
                            text = "App Version",
                            style = MaterialTheme.typography.labelSmall,
                            color = subtextColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "v1.0.0 (Build 2026.06.15)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column {
                        Text(
                            text = "Product Capabilities",
                            style = MaterialTheme.typography.labelSmall,
                            color = subtextColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "• Dynamic symptom timeline evaluation\n" +
                                   "• Adaptive over-the-counter local recommendations\n" +
                                   "• Anti-interaction background medication watchdog\n" +
                                   "• Multi-ingredient allergen screening filters\n" +
                                   "• Direct generative AI pharmacist counseling Chat",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtextColor,
                            lineHeight = 16.sp
                        )
                    }

                    HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

                    Text(
                        text = "Disclaimer: sympto provides structured educational drug informational guidance. It does NOT substitute for professional clinical judgments, medical prescriptions, or emergency healthcare services.",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = surfaceColor,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg),
        containerColor = darkBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Professional App Brand Layout
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_pharmacist_logo),
                            contentDescription = "Pharmacist Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Text(
                                text = "sympto",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "OTC Safety Screener",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }

                    // Theme, Region, and About Selectors Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // About Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
                                .clickable { showAboutDialog = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("btn_about")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "About",
                                    tint = textColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "About",
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        // Dynamic Emojis Mode Pillar Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
                                .clickable { isDarkMode = !isDarkMode }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("theme_toggle")
                        ) {
                            Text(
                                text = if (isDarkMode) "🌙 Dark" else "☀️ Light",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        // Region Selector Button
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
                                    .clickable {
                                        val nextRegion = if (selectedCountry == Region.INDIA) Region.USA else Region.INDIA
                                        viewModel.selectRegion(nextRegion)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .testTag("country_toggle"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                              ) {
                                Text(text = selectedCountry.flag, fontSize = 16.sp)
                                Text(
                                    text = selectedCountry.label,
                                    color = textColor,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = surfaceColor,
                modifier = Modifier.navigationBarsPadding(),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Screener") },
                    label = { Text("Search") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryColor,
                        selectedTextColor = primaryColor,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = primaryColor.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_screener")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        BadgedBox(badge = {
                            if (medsList.isNotEmpty()) {
                                Badge(containerColor = primaryColor) {
                                    Text(text = medsList.size.toString(), color = Color.White)
                                }
                            }
                        }) {
                            Icon(Icons.Default.Info, contentDescription = "Meds")
                        }
                    },
                    label = { Text("My Meds") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryColor,
                        selectedTextColor = primaryColor,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = primaryColor.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_meds")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "AI Assistant") },
                    label = { Text("AI Pharmacist") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryColor,
                        selectedTextColor = primaryColor,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = primaryColor.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_ai")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "History") },
                    label = { Text("Log") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryColor,
                        selectedTextColor = primaryColor,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = primaryColor.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_history")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = darkBg
        ) {
            when (currentTab) {
                0 -> ScreenerTabContent(
                    selectedCountry = selectedCountry,
                    category = category,
                    days = days,
                    activeFlags = activeFlags,
                    isEmergencyActive = isEmergencyActive,
                    advice = advice,
                    medsCount = medsList.size,
                    onCategorySelect = { viewModel.selectCategory(it) },
                    onDurationChange = { viewModel.setDuration(it) },
                    onToggleFlag = { viewModel.toggleRedFlag(it) },
                    onSaveLog = {
                        viewModel.saveConsultationLog()
                    },
                    themeBg = surfaceColor,
                    primary = primaryColor,
                    warnColor = warningRed,
                    accent = accentColor,
                    info = infoBlue,
                    textColor = textColor,
                    subtextColor = subtextColor,
                    isDarkMode = isDarkMode
                )
                1 -> MedicationTabContent(
                    medsList = medsList,
                    onAddMed = { name, active -> viewModel.addMedication(name, active) },
                    onDeleteMed = { viewModel.deleteMedication(it) },
                    allergiesList = allergiesList,
                    onAddAllergy = { viewModel.addAllergy(it) },
                    onDeleteAllergy = { viewModel.deleteAllergy(it) },
                    themeBg = surfaceColor,
                    primary = primaryColor,
                    accent = accentColor,
                    textColor = textColor,
                    subtextColor = subtextColor,
                    isDarkMode = isDarkMode
                )
                2 -> HistoryTabContent(
                    historyList = historyList,
                    onClear = { viewModel.clearHistory() },
                    themeBg = surfaceColor,
                    primary = primaryColor,
                    textColor = textColor,
                    subtextColor = subtextColor
                )
                3 -> AiAssistantTabContent(
                    prompt = aiPromptText,
                    onPromptChange = { viewModel.customAiPrompt.value = it },
                    response = parsedAiResponse,
                    isLoading = isAiLoading,
                    onQueryExecute = { viewModel.runAiQuery() },
                    category = category,
                    medsList = medsList,
                    themeBg = surfaceColor,
                    primary = primaryColor,
                    accent = accentColor,
                    textColor = textColor,
                    subtextColor = subtextColor,
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

// --- TAB 0: SCREENER MAIN CONTENT ---
@Composable
fun ScreenerTabContent(
    selectedCountry: Region,
    category: SymptomCategory?,
    days: Int,
    activeFlags: Set<String>,
    isEmergencyActive: Boolean,
    advice: ScreenerAdvice?,
    medsCount: Int,
    onCategorySelect: (SymptomCategory?) -> Unit,
    onDurationChange: (Int) -> Unit,
    onToggleFlag: (String) -> Unit,
    onSaveLog: () -> Unit,
    themeBg: Color,
    primary: Color,
    warnColor: Color,
    accent: Color,
    info: Color,
    textColor: Color,
    subtextColor: Color,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var saveSuccessTrigger by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Step 1: Select Symptom Category
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "STEP 1: Select Symptom Focus",
                    style = MaterialTheme.typography.titleSmall,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Beautiful 2-column category selector grid showing names fully
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chunks = SymptomCategory.values().toList().chunked(2)
                    chunks.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { cat ->
                                val isSel = cat == category
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) primary.copy(alpha = 0.25f) else themeBg)
                                        .clickable {
                                            if (isSel) onCategorySelect(null)
                                            else onCategorySelect(cat)
                                        }
                                        .padding(vertical = 12.dp, horizontal = 10.dp)
                                        .testTag("cat_${cat.name.lowercase()}"),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = cat.iconUnicode, fontSize = 24.sp)
                                        Text(
                                            text = cat.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSel) textColor else subtextColor,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            if (pair.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        if (category == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(themeBg)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_pharmacist_logo),
                            contentDescription = "Pharmacist Logo",
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pharmacist Brain is Ready",
                            style = MaterialTheme.typography.titleLarge,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose a symptom above to begin defensive screening, duration checking, and interaction profiling.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = subtextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Step 2: Duration Ladder
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(themeBg)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "STEP 2: Symptom Duration",
                        style = MaterialTheme.typography.titleSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "OTC recommendations change based on progress. Day 1 focuses on soothing; Day 7+ blocks OTC and requires clinician.",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Symptom Age:",
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (days >= 7) "Day 7 or more (RED)" else "Day $days",
                            color = if (days >= 7) warnColor else primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Slider(
                        value = days.toFloat(),
                        onValueChange = { onDurationChange(it.toInt()) },
                        valueRange = 1f..7f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = if (days >= 7) warnColor else primary,
                            activeTrackColor = if (days >= 7) warnColor.copy(alpha = 0.5f) else primary,
                            inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("duration_slider")
                    )

                    // Brief advice snippet for current duration
                    advice?.ladderAdvice?.let { ladder ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (days >= 7) warnColor else primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = ladder.level,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (days >= 7) warnColor else textColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                ladder.steps.forEach { step ->
                                    Text(
                                        text = "• $step",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = subtextColor,
                                        modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Step 3: Red Flag Emergency Screening (CRITICAL BEFORE RECOMMENDATIONS)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(themeBg)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STEP 3: Emergency Check (DEFENSIVE)",
                            style = MaterialTheme.typography.titleSmall,
                            color = warnColor,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(warnColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CRITICAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = warnColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mark any dangerous signals active. If any apply, immediately bypass OTC suggestions and contact emergency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Red flags checklist
                    val flags = PharmacistBrain.getRedFlags(category)
                    flags.forEachIndexed { index, flag ->
                        val isChecked = activeFlags.contains(flag)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChecked) warnColor.copy(alpha = 0.08f) else Color.Transparent)
                                .clickable { onToggleFlag(flag) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { onToggleFlag(flag) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = warnColor,
                                    uncheckedColor = if (isDarkMode) Color.Gray else Color.LightGray
                                ),
                                modifier = Modifier.testTag("red_flag_check_$index")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = flag,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isChecked) textColor else subtextColor,
                                fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Results / Recommendations Screen
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(themeBg)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Pharmacist Clearance & Recommendation",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Divider(color = textColor.copy(alpha = 0.1f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEmergencyActive) {
                        // Emergency Alert Layout
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(warnColor.copy(alpha = 0.12f))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "🚨 CRITICAL WARNING 🚨",
                                style = MaterialTheme.typography.titleMedium,
                                color = warnColor,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Red-flag clinical symptoms were reported. Self-treating with local OTC drugs is extremely unsafe.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Please call local medical assistance right away or proceed to the nearest emergency clinic.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = subtextColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val emergencyNumber = if (selectedCountry == Region.USA) "911" else "112"

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$emergencyNumber")
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = warnColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_emergency_call")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Call")
                                    Text("Dial Emergency ($emergencyNumber)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (days >= 7) {
                        // Duration Limit Reached
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(warnColor.copy(alpha = 0.08f))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "⚠️ CLINIC REFERRAL REQUIRED ⚠️",
                                style = MaterialTheme.typography.titleMedium,
                                color = warnColor,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Symptoms have lasted $days days. Continuing to treat with standard OTC drugs can hide serious underlying issues. A doctor must evaluate this immediately.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = subtextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Safe Suggestion Layout
                        advice?.let { adv ->
                            // Interaction Warning Block
                            if (adv.interactionWarning != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(warnColor.copy(alpha = 0.15f))
                                        .padding(12.dp)
                                        .testTag("interaction_warning")
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = "Alert",
                                            tint = warnColor
                                        )
                                        Column {
                                            Text(
                                                text = "DRUG CONFLICT BLOCK TRIGGERED",
                                                color = warnColor,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = adv.interactionWarning,
                                                color = textColor,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // OTC Recommendations List
                            if (adv.recommendedBrands.isEmpty()) {
                                Text(
                                    text = "All safe OTC suggestions filtered out due to severe drug interactions. Consult physical pharmacist directly.",
                                    color = subtextColor,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "Filtered Recommended OTC Products:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                adv.recommendedBrands.forEachIndexed { i, brand ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f))
                                            .padding(12.dp)
                                            .testTag("otc_brand_$i")
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = brand.brandName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = textColor,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(primary.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = brand.standardPrice,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "Active: ${brand.activeIngredient}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = accent
                                            )

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(info.copy(alpha = 0.1f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Menu,
                                                    contentDescription = "Shelf Map",
                                                    tint = info,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "📍 Shelf Store Map: ${brand.shelfLocation}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = textColor,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            Divider(
                                                color = textColor.copy(alpha = 0.05f),
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )

                                            Text(
                                                text = "Directions: ${brand.recommendedDose}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = subtextColor
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Log History Actions
                                Button(
                                    onClick = {
                                        onSaveLog()
                                        saveSuccessTrigger = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_save_consultation")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Log Save")
                                        Text("Log Consultation Instantly", fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (saveSuccessTrigger) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Consultation successfully logged locally! Check the 'Log' tab.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = accent,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Disclaimer: This software simulates automated pharmacy rules for standard reference only. This is not medical diagnosis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

// --- TAB 1: ALLERGIES & BACKGROUND MEDS ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MedicationTabContent(
    medsList: List<UserMedication>,
    onAddMed: (String, String) -> Unit,
    onDeleteMed: (UserMedication) -> Unit,
    allergiesList: List<String>,
    onAddAllergy: (String) -> Unit,
    onDeleteAllergy: (String) -> Unit,
    themeBg: Color,
    primary: Color,
    accent: Color,
    textColor: Color,
    subtextColor: Color,
    isDarkMode: Boolean
) {
    var medName by remember { mutableStateOf("") }
    var activeIngredient by remember { mutableStateOf("") }
    var allergyName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Core Title & Header Section
        item {
            Column {
                Text(
                    text = "Medical & Allergy Profile",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Build your local profile below. The Pharmacist Screener cross-references both your allergens and background medications in real-time to protect you against hazardous inputs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = subtextColor
                )
            }
        }

        // Section A: Known Medication Allergies
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = themeBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Allergy Warning Icon",
                            tint = Color(0xFFEF4444)
                        )
                        Text(
                            text = "My Drug Allergies",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Adding allergy keywords (e.g. 'Ibuprofen', 'Paracetamol') automatically screens out and filters any corresponding recommended OTC preparations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )

                    OutlinedTextField(
                        value = allergyName,
                        onValueChange = { allergyName = it },
                        label = { Text("Medication Allergen (e.g. Ibuprofen, Sulfa)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            focusedLabelColor = primary,
                            unfocusedBorderColor = if (isDarkMode) Color.Gray.copy(alpha = 0.5f) else Color.LightGray,
                            unfocusedLabelColor = subtextColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_allergy_name")
                    )

                    Button(
                        onClick = {
                            if (allergyName.isNotBlank()) {
                                onAddAllergy(allergyName)
                                allergyName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_add_allergy")
                    ) {
                        Text("Add Allergy Shield", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Interactive chip list
                    if (allergiesList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Registered Allergies (Tap to delete):",
                            style = MaterialTheme.typography.labelSmall,
                            color = subtextColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allergiesList.forEach { allergy ->
                                InputChip(
                                    selected = true,
                                    onClick = { onDeleteAllergy(allergy) },
                                    label = { Text(allergy, color = textColor, fontWeight = FontWeight.Medium) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFFEF4444)
                                        )
                                    },
                                    modifier = Modifier.testTag("allergy_chip_$allergy")
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDarkMode) Color.Black.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.04f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No drug allergies registered on profile. All safe suggestions remain fully active.",
                                style = MaterialTheme.typography.bodySmall,
                                color = subtextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Section B: Watchlist Background Meds
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = themeBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Meds Icon",
                            tint = primary
                        )
                        Text(
                            text = "My Background Medications",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Specify key medications on your current watchlist. The safety engine checks for severe warnings and blockages.",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )

                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Brand/Medication Name (e.g. Warfarin, Aspirin)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            focusedLabelColor = primary,
                            unfocusedBorderColor = if (isDarkMode) Color.Gray.copy(alpha = 0.5f) else Color.LightGray,
                            unfocusedLabelColor = subtextColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_med_name")
                    )

                    OutlinedTextField(
                        value = activeIngredient,
                        onValueChange = { activeIngredient = it },
                        label = { Text("Active Ingredient / Class (e.g. Anticoagulant, SSRI)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            focusedLabelColor = primary,
                            unfocusedBorderColor = if (isDarkMode) Color.Gray.copy(alpha = 0.5f) else Color.LightGray,
                            unfocusedLabelColor = subtextColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_active_ingredient")
                    )

                    Button(
                        onClick = {
                            if (medName.isNotBlank()) {
                                onAddMed(medName, activeIngredient)
                                medName = ""
                                activeIngredient = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_add_med")
                    ) {
                        Text("Add background Medication", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Section C: Current Watchlist List
        item {
            Text(
                text = "Tracking Background Watchlist (${medsList.size} drugs):",
                style = MaterialTheme.typography.titleSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }

        if (medsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeBg)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No trackable watchlist medications.\nInteractions checks run against zero background drugs.",
                        color = subtextColor,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(medsList) { med ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = med.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        if (med.activeIngredient.isNotBlank()) {
                            Text(
                                text = "Class/Active: ${med.activeIngredient}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDarkMode) accent else primary
                            )
                        }
                    }

                    IconButton(
                        onClick = { onDeleteMed(med) },
                        modifier = Modifier.testTag("delete_med_${med.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete medication",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

// --- TAB 2: CONSULTATION HISTORY RECORDS ---
@Composable
fun HistoryTabContent(
    historyList: List<ConsultationHistory>,
    onClear: () -> Unit,
    themeBg: Color,
    primary: Color,
    textColor: Color,
    subtextColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Action logs",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Offline local ledger of previous evaluations",
                    style = MaterialTheme.typography.bodySmall,
                    color = subtextColor
                )
            }

            if (historyList.isNotEmpty()) {
                TextButton(
                    onClick = onClear,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("btn_clear_history")
                ) {
                    Text("Clear All")
                }
            }
        }

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No log records found.\nLog a consultation from the screener stage output.",
                    color = subtextColor,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyList) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(themeBg)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.symptomCategoryName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.selectedCountry,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = primary
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Duration: ${item.durationDays} Days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subtextColor
                                )
                                Text(
                                    text = "•",
                                    color = subtextColor.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "Brand: ${item.recommendedBrandName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "📍 Retail Store Shelf: ${item.retailShelfMap}",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )

                            if (item.triggeredInteractionWarning != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = "! Warning: " + item.triggeredInteractionWarning,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Persistent About Card at the bottom of the History page
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = themeBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About sympto",
                        tint = primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "About sympto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A smart clinical pharmacist evaluation screener developed by Prince Patil.",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = primary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "App Version: 1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = subtextColor
                    )
                    Text(
                        text = "Build: Production-Ready (2026)",
                        style = MaterialTheme.typography.labelSmall,
                        color = subtextColor
                    )
                }
            }
        }
    }
}

// --- TAB 3: AI ASSISTANT CHAT SCREEN ---
@Composable
fun AiAssistantTabContent(
    prompt: String,
    onPromptChange: (String) -> Unit,
    response: ParsedAiResponse?,
    isLoading: Boolean,
    onQueryExecute: () -> Unit,
    category: SymptomCategory?,
    medsList: List<UserMedication>,
    themeBg: Color,
    primary: Color,
    accent: Color,
    textColor: Color,
    subtextColor: Color,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "AI Companion Pharmacist",
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ask drug questions or get mechanisms of active ingredients explained. Uses safe context-aware AI.",
                style = MaterialTheme.typography.bodySmall,
                color = subtextColor
            )
        }

        // Selected Context
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Selected Category: ${category?.displayName ?: "None (Ask anything)"}",
                style = MaterialTheme.typography.bodySmall,
                color = subtextColor
            )
            Text(
                text = "|",
                color = subtextColor.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Meds Loaded: ${medsList.size}",
                style = MaterialTheme.typography.bodySmall,
                color = subtextColor
            )
        }

        // Response box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(themeBg)
                .padding(16.dp)
        ) {
            if (response == null && !isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "💊", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ask Clinical Questions",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Example: Why does Caffeine speed up pain relief in Crocin? How should antihistamines be taken safely?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtextColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Consulting AI Pharmacist database...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- PHARMACIST CLINICAL SUMMARY CARD (USER SPECIAL REQUEST) ---
                    val hasSummary = response?.suggestedMedication != null ||
                                     response?.durationLadderAdvice != null ||
                                     response?.potentialSideEffects != null

                    if (hasSummary) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkMode) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f)
                                ),
                                border = CardDefaults.outlinedCardBorder(enabled = true).copy(
                                    brush = Brush.horizontalGradient(listOf(primary, accent))
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pharmacist_summary_card")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Text(text = "🏥", fontSize = 22.sp)
                                        Text(
                                            text = "Pharmacist Summary",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    }

                                    // Suggested Medication
                                    response?.suggestedMedication?.let { med ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(primary.copy(alpha = 0.12f))
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(text = "💊", fontSize = 16.sp)
                                                Text(
                                                    text = "Suggested Medication",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDarkMode) accent else primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = med,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = textColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // Duration-Ladder Advice
                                    response?.durationLadderAdvice?.let { dur ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFEAB308).copy(alpha = 0.12f))
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(text = "⏱️", fontSize = 16.sp)
                                                Text(
                                                    text = "Duration-Ladder Advice",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFEAB308)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = dur,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = textColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // Potential Side Effects
                                    response?.potentialSideEffects?.let { side ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(text = "⚠️", fontSize = 16.sp)
                                                Text(
                                                    text = "Potential Side Effects",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFEF4444)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = side,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Main supporting discussion / context explanation
                    val explanation = response?.mainExplanation ?: ""
                    if (explanation.isNotBlank()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                if (hasSummary) {
                                    Text(
                                        text = "Additional Clinical Details",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        modifier = Modifier.padding(bottom = 6.dp, top = 8.dp)
                                    )
                                }
                                Text(
                                    text = explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input bottom bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                placeholder = { Text("Ask pharmacist question...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primary,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    unfocusedBorderColor = if (isDarkMode) Color.Gray else Color.LightGray,
                    focusedPlaceholderColor = subtextColor,
                    unfocusedPlaceholderColor = subtextColor
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input")
            )

            Button(
                onClick = onQueryExecute,
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_ai_execute")
            ) {
                Text("Ask", fontWeight = FontWeight.Bold)
            }
        }
    }
}
