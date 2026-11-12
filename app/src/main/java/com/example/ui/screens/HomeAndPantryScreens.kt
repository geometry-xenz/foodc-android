package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.*
import com.example.domain.usecase.IngredientAliasNormalizer
import com.example.ui.DrishyamUiState
import com.example.ui.PrimaryTab
import com.example.ui.SubScreen
import com.example.ui.theme.DrishyamColors
import kotlin.math.roundToInt

@Composable
fun HomeDashboardScreen(
    state: DrishyamUiState,
    onSelectTab: (PrimaryTab) -> Unit,
    onOpenSubScreen: (SubScreen) -> Unit,
    onOpenRecipe: (String) -> Unit,
    onAddUnlockToPantry: (String) -> Unit,
    onTriggerSync: () -> Unit
) {
    val todayLogs = state.mealLogs
    val consumedCal = todayLogs.sumOf { it.calories }
    val consumedProt = todayLogs.sumOf { it.proteinG }.roundToInt()
    val consumedCarbs = todayLogs.sumOf { it.carbsG }.roundToInt()
    val consumedFat = todayLogs.sumOf { it.fatG }.roundToInt()
    val pendingSyncCount = state.syncOperations.count { !it.synced }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DrishyamColors.Background)
            .testTag("home_dashboard_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Editorial Hero Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_gastronomy),
                    contentDescription = "DRISHYAM Computational Gastronomy Hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    DrishyamColors.Background.copy(alpha = 0.35f),
                                    DrishyamColors.Background.copy(alpha = 0.85f),
                                    DrishyamColors.Background
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DRISHYAM",
                                style = MaterialTheme.typography.labelLarge,
                                color = DrishyamColors.PrimaryOrange
                            )
                            Text(
                                text = "Computational Gastronomy",
                                style = MaterialTheme.typography.bodySmall,
                                color = DrishyamColors.SecondaryText
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = DrishyamColors.ElevatedSurface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                                modifier = Modifier
                                    .clickable { onTriggerSync() }
                                    .testTag("sync_status_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (pendingSyncCount == 0) Icons.Default.CloudDone else Icons.Default.Sync,
                                        contentDescription = "Sync Status",
                                        tint = if (pendingSyncCount == 0) DrishyamColors.Success else DrishyamColors.SecondaryGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = if (pendingSyncCount == 0) "Synced" else "$pendingSyncCount Queued",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DrishyamColors.PrimaryText
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onOpenSubScreen(SubScreen.PROFILE_AND_GOALS) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DrishyamColors.ElevatedSurface, CircleShape)
                                    .testTag("open_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile and Fitness Goals",
                                    tint = DrishyamColors.PrimaryOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Column {
                        Surface(
                            color = DrishyamColors.PrimaryOrangeDim,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, DrishyamColors.PrimaryOrange.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${state.userProfile.primaryGoal.title.uppercase()} • ${state.userProfile.dietaryLifestyle.label.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = DrishyamColors.PrimaryOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Welcome back, ${state.userProfile.displayName.substringBefore(" ")}",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            text = "${state.pantryItems.count { it.isAvailable }} active kitchen ingredients • ${state.scoredRecipes.size} allergen-safe matches",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DrishyamColors.SecondaryText
                        )
                    }
                }
            }
        }

        // Quick Culinary & Science Modules Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickModulePill(
                    icon = Icons.Default.Science,
                    title = "FlavorDB Graph",
                    subtitle = "Molecular Bridges",
                    accent = DrishyamColors.SecondaryGold,
                    tag = "quick_flavour_science",
                    onClick = { onOpenSubScreen(SubScreen.FLAVOUR_SCIENCE) }
                )
                QuickModulePill(
                    icon = Icons.Default.CalendarMonth,
                    title = "Meal Planner",
                    subtitle = "7-Day Schedule",
                    accent = DrishyamColors.PrimaryOrange,
                    tag = "quick_meal_planner",
                    onClick = { onOpenSubScreen(SubScreen.MEAL_PLANNER) }
                )
                QuickModulePill(
                    icon = Icons.Default.ShoppingCart,
                    title = "Smart Shopping",
                    subtitle = "${state.shoppingItems.count { !it.isPurchased }} Needed",
                    accent = DrishyamColors.Success,
                    tag = "quick_shopping_list",
                    onClick = { onOpenSubScreen(SubScreen.SHOPPING_LIST) }
                )
                QuickModulePill(
                    icon = Icons.Default.Tune,
                    title = "8-Step Wizard",
                    subtitle = "Calibrate Profile",
                    accent = DrishyamColors.PrimaryOrange,
                    tag = "quick_onboarding_wizard",
                    onClick = { onOpenSubScreen(SubScreen.ONBOARDING_WIZARD) }
                )
                QuickModulePill(
                    icon = Icons.Default.FitnessCenter,
                    title = "Goals & Diet",
                    subtitle = state.userProfile.primaryGoal.title,
                    accent = DrishyamColors.PrimaryOrange,
                    tag = "quick_fitness_goals",
                    onClick = { onOpenSubScreen(SubScreen.PROFILE_AND_GOALS) }
                )
                QuickModulePill(
                    icon = Icons.Default.SyncLock,
                    title = "Cloud & Auth",
                    subtitle = if (state.isAuthenticated) "Session Active" else "Sign In",
                    accent = DrishyamColors.SecondaryGold,
                    tag = "quick_cloud_sync",
                    onClick = { onOpenSubScreen(SubScreen.SETTINGS_AND_SYNC) }
                )
            }
        }

        // Telemetry Summary Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .clickable { onSelectTab(PrimaryTab.NUTRITION) },
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(88.dp)
                    ) {
                        val progress = (consumedCal.toFloat() / state.nutritionTargets.targetCalories.coerceAtLeast(1))
                            .coerceIn(0f, 1f)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = DrishyamColors.ElevatedSurface,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = DrishyamColors.PrimaryOrange,
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$consumedCal",
                                style = MaterialTheme.typography.titleMedium,
                                color = DrishyamColors.PrimaryText
                            )
                            Text(
                                text = "/ ${state.nutritionTargets.targetCalories} kcal",
                                style = MaterialTheme.typography.labelSmall,
                                color = DrishyamColors.SecondaryText
                            )
                        }
                    }

                    Spacer(Modifier.width(18.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "DAILY TELEMETRY • ${(state.nutritionTargets.targetCalories - consumedCal).coerceAtLeast(0)} KCAL REMAINING",
                            style = MaterialTheme.typography.labelSmall,
                            color = DrishyamColors.SecondaryGold
                        )
                        MacroMiniBar("Protein", consumedProt, state.nutritionTargets.proteinG, "g", DrishyamColors.PrimaryOrange)
                        MacroMiniBar("Carbs", consumedCarbs, state.nutritionTargets.carbsG, "g", DrishyamColors.SecondaryGold)
                        MacroMiniBar("Fat", consumedFat, state.nutritionTargets.fatG, "g", DrishyamColors.Success)
                    }
                }
            }
        }

        // Part 17.2: Single-Ingredient Unlock Suggestion
        state.unlockSuggestion?.let { unlock ->
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    color = DrishyamColors.ElevatedSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DrishyamColors.SecondaryGold.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Unlock Suggestion",
                                    tint = DrishyamColors.SecondaryGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "SINGLE-INGREDIENT UNLOCK ENGINE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DrishyamColors.SecondaryGold
                                )
                            }
                            Text(
                                text = "+${(unlock.averageScoreBoost * 100).roundToInt()}% FEASIBILITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = DrishyamColors.Success
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Add ${unlock.displayName} to unlock ${unlock.unlockedRecipes.size} high-synergy dishes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = unlock.whyItMatters,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onAddUnlockToPantry(unlock.missingCanonicalName) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DrishyamColors.PrimaryOrange,
                                    contentColor = DrishyamColors.Background
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("unlock_add_pantry_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("I Have ${unlock.displayName}", style = MaterialTheme.typography.labelLarge)
                            }
                            OutlinedButton(
                                onClick = { onSelectTab(PrimaryTab.SCAN) },
                                border = BorderStroke(1.dp, DrishyamColors.SecondaryGold),
                                modifier = Modifier.testTag("home_scan_now_button")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = DrishyamColors.SecondaryGold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Scan Kitchen", color = DrishyamColors.SecondaryGold)
                            }
                        }
                    }
                }
            }
        }

        // Top Ranked Computational Gastronomy Recommendations
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "GASTRONOMY RANKED FOR YOU",
                        style = MaterialTheme.typography.labelMedium,
                        color = DrishyamColors.PrimaryOrange
                    )
                    Text(
                        text = "RecipeDB × FlavorDB Matches",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                TextButton(onClick = { onSelectTab(PrimaryTab.DISCOVER) }) {
                    Text("View All", color = DrishyamColors.PrimaryOrange)
                }
            }
        }

        items(state.scoredRecipes.take(4), key = { it.recipe.id }) { scored ->
            GastronomyRecipeCard(
                scored = scored,
                isSaved = scored.recipe.id in state.savedRecipeIds,
                onClick = { onOpenRecipe(scored.recipe.id) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun QuickModulePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        color = DrishyamColors.Surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
        modifier = Modifier
            .width(152.dp)
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(accent.copy(alpha = 0.16f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun MacroMiniBar(label: String, current: Int, target: Int, unit: String, color: Color) {
    val fraction = (current.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = DrishyamColors.PrimaryText)
            Text("$current / $target$unit", style = MaterialTheme.typography.labelSmall, color = DrishyamColors.SecondaryText)
        }
        Spacer(Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { fraction },
            color = color,
            trackColor = DrishyamColors.ElevatedSurface,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
fun GastronomyRecipeCard(
    scored: ScoredRecipe,
    isSaved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = scored.recipe
    val imageRes = when (recipe.drawableResName) {
        "img_recipe_paneer_tikka" -> R.drawable.img_recipe_paneer_tikka
        "img_recipe_saffron_biryani" -> R.drawable.img_recipe_saffron_biryani
        else -> R.drawable.img_hero_gastronomy
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("recipe_card_${recipe.id}"),
        color = DrishyamColors.Surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, DrishyamColors.BorderSubtle)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DrishyamColors.Surface.copy(alpha = 0.92f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = DrishyamColors.Background.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, DrishyamColors.PrimaryOrange)
                    ) {
                        Text(
                            text = "${(scored.gastronomyScore * 100).roundToInt()}% GASTRONOMY MATCH",
                            style = MaterialTheme.typography.labelSmall,
                            color = DrishyamColors.PrimaryOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = DrishyamColors.Background.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${recipe.totalMinutes}m • ${recipe.dietaryClassification.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DrishyamColors.PrimaryText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricBadge("${recipe.nutritionPerServing.calories} kcal", DrishyamColors.PrimaryOrange)
                    MetricBadge("${recipe.nutritionPerServing.proteinG.roundToInt()}g Protein", DrishyamColors.SecondaryGold)
                    MetricBadge("${scored.availableIngredients.size}/${recipe.ingredients.size} in Pantry", DrishyamColors.Success)
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = scored.whyRecommended,
                    style = MaterialTheme.typography.bodySmall,
                    color = DrishyamColors.SecondaryText
                )

                if (scored.missingIngredients.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Missing: ${scored.missingIngredients.joinToString(", ") { it.displayName }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DrishyamColors.Warning
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PantryScreen(
    state: DrishyamUiState,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (IngredientCategory?) -> Unit,
    onAddOrUpdateItem: (String, Double, String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onOpenScanner: () -> Unit,
    onInspectFlavourScience: (String) -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<PantryItem?>(null) }
    var inspectedItem by remember { mutableStateOf<PantryItem?>(null) }

    val filteredItems = state.pantryItems.filter { item ->
        val matchesCategory = state.pantryCategoryFilter == null || item.category == state.pantryCategoryFilter
        val matchesQuery = state.pantrySearchQuery.isBlank() ||
            item.displayName.contains(state.pantrySearchQuery, ignoreCase = true) ||
            item.aliases.any { it.contains(state.pantrySearchQuery, ignoreCase = true) }
        matchesCategory && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DrishyamColors.Background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SYNCHRONIZED KITCHEN INVENTORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = DrishyamColors.PrimaryOrange
                )
                Text(
                    text = "Intelligent Pantry (${state.pantryItems.size})",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onOpenScanner,
                    border = BorderStroke(1.dp, DrishyamColors.SecondaryGold)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = DrishyamColors.SecondaryGold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Scan", color = DrishyamColors.SecondaryGold)
                }
                Button(
                    onClick = {
                        editingItem = null
                        showAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DrishyamColors.PrimaryOrange,
                        contentColor = DrishyamColors.Background
                    ),
                    modifier = Modifier.testTag("add_pantry_manual_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Ingredient", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.pantrySearchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by ingredient or Hindi/regional alias (e.g., haldi, jeera)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pantry_search_input")
        )

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.pantryCategoryFilter == null,
                onClick = { onCategorySelect(null) },
                label = { Text("All (${state.pantryItems.size})") }
            )
            IngredientCategory.entries.forEach { cat ->
                FilterChip(
                    selected = state.pantryCategoryFilter == cat,
                    onClick = { onCategorySelect(if (state.pantryCategoryFilter == cat) null else cat) },
                    label = { Text(cat.label) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                val meta = IngredientAliasNormalizer.resolve(item.canonicalName)
                val grams = IngredientAliasNormalizer.normalizeQuantityToGrams(
                    item.quantity,
                    item.unit,
                    meta.nutrientPer100g.gramsPerPiece
                )
                val itemCalories = (meta.nutrientPer100g.caloriesPer100g * grams / 100.0).roundToInt()

                Surface(
                    color = DrishyamColors.Surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (item.isLowStock) DrishyamColors.Warning.copy(alpha = 0.6f) else DrishyamColors.BorderSubtle
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { inspectedItem = item }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(item.displayName, style = MaterialTheme.typography.titleMedium)
                                Surface(
                                    color = DrishyamColors.ElevatedSurface,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = item.category.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DrishyamColors.SecondaryGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (item.isLowStock) {
                                    Surface(
                                        color = DrishyamColors.WarningDim,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "LOW STOCK",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DrishyamColors.Warning,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "IFCT: ${meta.nutrientPer100g.caloriesPer100g} kcal/100g (~$itemCalories kcal total) • Aliases: ${item.aliases.joinToString(", ").ifEmpty { "Canonical" }}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${item.quantity.roundToInt()} ${item.unit}",
                                style = MaterialTheme.typography.labelLarge,
                                color = DrishyamColors.PrimaryOrange
                            )
                            IconButton(
                                onClick = {
                                    editingItem = item
                                    showAddDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit ${item.displayName}",
                                    tint = DrishyamColors.SecondaryGold
                                )
                            }
                            IconButton(onClick = { onDeleteItem(item.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete ${item.displayName}",
                                    tint = DrishyamColors.Danger
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Ingredient Details Sheet / Dialog (IFCT/USDA Per-100g Telemetry + Aliases + FlavorDB Jump)
    inspectedItem?.let { item ->
        val meta = IngredientAliasNormalizer.resolve(item.canonicalName)
        val n100 = meta.nutrientPer100g
        val grams = IngredientAliasNormalizer.normalizeQuantityToGrams(item.quantity, item.unit, n100.gramsPerPiece)
        AlertDialog(
            onDismissRequest = { inspectedItem = null },
            containerColor = DrishyamColors.ElevatedSurface,
            title = {
                Column {
                    Text("${item.displayName} (${item.category.label})", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Canonical ID: ${item.canonicalName} • Stock: ${item.quantity.roundToInt()} ${item.unit} (~${grams.roundToInt()}g)",
                        style = MaterialTheme.typography.labelSmall,
                        color = DrishyamColors.PrimaryOrange
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "IFCT / USDA Per-100g Composition:",
                        style = MaterialTheme.typography.labelMedium,
                        color = DrishyamColors.SecondaryGold
                    )
                    Text(
                        "• Energy: ${n100.caloriesPer100g} kcal | Protein: ${n100.proteinPer100g}g | Carbs: ${n100.carbsPer100g}g | Fat: ${n100.fatPer100g}g",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Fibre: ${n100.fibrePer100g}g | Sugar: ${n100.sugarPer100g}g | Sat Fat: ${n100.saturatedFatPer100g}g",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Micronutrients: Iron ${n100.ironMgPer100g}mg • Calcium ${n100.calciumMgPer100g}mg • Vit C ${n100.vitaminCMgPer100g}mg • Potassium ${n100.potassiumMgPer100g}mg",
                        style = MaterialTheme.typography.bodySmall,
                        color = DrishyamColors.Success
                    )
                    HorizontalDivider(color = DrishyamColors.BorderSubtle)
                    Text(
                        "Recognized Regional Aliases: ${meta.aliases.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Detection Source: ${item.source} (${(item.confidence * 100).roundToInt()}% confidence)",
                        style = MaterialTheme.typography.bodySmall,
                        color = DrishyamColors.SecondaryText
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val canonical = item.canonicalName
                        inspectedItem = null
                        onInspectFlavourScience(canonical)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DrishyamColors.SecondaryGold,
                        contentColor = DrishyamColors.Background
                    )
                ) {
                    Text("Inspect FlavorDB Molecules")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editingItem = item
                        inspectedItem = null
                        showAddDialog = true
                    }
                ) {
                    Text("Edit Quantity")
                }
            }
        )
    }

    if (showAddDialog) {
        var nameInput by remember { mutableStateOf(editingItem?.displayName ?: "") }
        var qtyInput by remember { mutableStateOf(editingItem?.quantity?.roundToInt()?.toString() ?: "100") }
        var unitInput by remember { mutableStateOf(editingItem?.unit ?: "g") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DrishyamColors.ElevatedSurface,
            title = {
                Text(if (editingItem == null) "Add Kitchen Ingredient" else "Edit ${editingItem?.displayName}")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Ingredient or Alias (e.g., Paneer, Haldi, Jeera)") },
                        singleLine = true,
                        modifier = Modifier.testTag("dialog_pantry_name_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qtyInput,
                            onValueChange = { qtyInput = it },
                            label = { Text("Quantity") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitInput,
                            onValueChange = { unitInput = it },
                            label = { Text("Unit (g, ml, pcs)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = qtyInput.toDoubleOrNull() ?: 100.0
                        onAddOrUpdateItem(nameInput, qty, unitInput)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DrishyamColors.PrimaryOrange,
                        contentColor = DrishyamColors.Background
                    ),
                    modifier = Modifier.testTag("dialog_pantry_save_button")
                ) {
                    Text("Save to Pantry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
