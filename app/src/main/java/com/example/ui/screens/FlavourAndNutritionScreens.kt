package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.FoodoscopeSeedCatalog
import com.example.domain.model.*
import com.example.domain.usecase.IngredientAliasNormalizer
import com.example.ui.DrishyamUiState
import com.example.ui.theme.DrishyamColors
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FlavourScienceScreen(
    state: DrishyamUiState,
    onBack: () -> Unit,
    onSelectIngredient: (String) -> Unit,
    onSelectCompound: (String) -> Unit,
    onSubstitutionQueryChange: (String) -> Unit
) {
    BackHandler { onBack() }

    val allProfiles = FoodoscopeSeedCatalog.flavourProfiles.values.toList()
    val currentProfile = FoodoscopeSeedCatalog.flavourProfiles[state.selectedFlavourIngredient]
        ?: allProfiles.first()
    val selectedCompound = currentProfile.keyCompounds.find { it.id == state.selectedCompoundId }
        ?: currentProfile.keyCompounds.firstOrNull()
    val currentMeta = IngredientAliasNormalizer.resolve(currentProfile.canonicalName)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DrishyamColors.Background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DrishyamColors.PrimaryText)
                }
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = "FOODOSCOPE FLAVORDB GRAPH",
                        style = MaterialTheme.typography.labelSmall,
                        color = DrishyamColors.SecondaryGold
                    )
                    Text(
                        text = "Molecular Flavour Science",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
        }

        // Ingredient Selector Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allProfiles.forEach { prof ->
                    FilterChip(
                        selected = prof.canonicalName == currentProfile.canonicalName,
                        onClick = { onSelectIngredient(prof.canonicalName) },
                        label = { Text(prof.displayName) }
                    )
                }
            }
        }

        // Interactive Compose Canvas Molecular Network (Part 15.2)
        item {
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${currentProfile.displayName} (${currentProfile.botanicalFamily})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "IFCT: ${currentMeta.nutrientPer100g.caloriesPer100g} kcal/100g • P: ${currentMeta.nutrientPer100g.proteinPer100g}g • C: ${currentMeta.nutrientPer100g.carbsPer100g}g",
                                style = MaterialTheme.typography.bodySmall,
                                color = DrishyamColors.SecondaryGold
                            )
                        }
                        Surface(
                            color = DrishyamColors.ElevatedSurface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${currentProfile.keyCompounds.size} Compounds",
                                style = MaterialTheme.typography.labelSmall,
                                color = DrishyamColors.PrimaryOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(DrishyamColors.Background, RoundedCornerShape(14.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val innerRadius = size.minDimension * 0.28f
                            val outerRadius = size.minDimension * 0.42f

                            val compounds = currentProfile.keyCompounds
                            val complements = currentProfile.complementaryIngredients

                            // Draw outer complementary ingredient nodes and edges
                            complements.forEachIndexed { idx, _ ->
                                val angle = (2.0 * Math.PI * idx / complements.size.coerceAtLeast(1)) + 0.4
                                val target = Offset(
                                    x = center.x + (outerRadius * cos(angle)).toFloat(),
                                    y = center.y + (outerRadius * sin(angle)).toFloat()
                                )
                                drawLine(
                                    color = DrishyamColors.BorderSubtle,
                                    start = center,
                                    end = target,
                                    strokeWidth = 1.5.dp.toPx()
                                )
                                drawCircle(
                                    color = DrishyamColors.Success,
                                    radius = 8.dp.toPx(),
                                    center = target
                                )
                            }

                            // Draw aroma compound nodes and chemical bridge lines
                            compounds.forEachIndexed { idx, comp ->
                                val angle = 2.0 * Math.PI * idx / compounds.size.coerceAtLeast(1)
                                val nodePos = Offset(
                                    x = center.x + (innerRadius * cos(angle)).toFloat(),
                                    y = center.y + (innerRadius * sin(angle)).toFloat()
                                )
                                val isSelected = comp.id == selectedCompound?.id
                                drawLine(
                                    color = if (isSelected) DrishyamColors.PrimaryOrange else DrishyamColors.SecondaryGold.copy(alpha = 0.65f),
                                    start = center,
                                    end = nodePos,
                                    strokeWidth = if (isSelected) 3.dp.toPx() else 2.dp.toPx()
                                )
                                drawCircle(
                                    color = if (isSelected) DrishyamColors.PrimaryOrange else DrishyamColors.SecondaryGold,
                                    radius = if (isSelected) 13.dp.toPx() else 10.dp.toPx(),
                                    center = nodePos
                                )
                            }

                            // Central Ingredient Hub Node
                            drawCircle(
                                color = DrishyamColors.PrimaryOrange,
                                radius = 22.dp.toPx(),
                                center = center
                            )
                        }

                        // Legend overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("● Center: ${currentProfile.displayName}", style = MaterialTheme.typography.labelSmall, color = DrishyamColors.PrimaryOrange)
                            Text("● Inner: Aroma Molecules", style = MaterialTheme.typography.labelSmall, color = DrishyamColors.SecondaryGold)
                            Text("● Outer: Pairings", style = MaterialTheme.typography.labelSmall, color = DrishyamColors.Success)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = currentProfile.culinaryRole,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(10.dp))
                    Text("Tap an Aroma Compound to Inspect:", style = MaterialTheme.typography.labelSmall, color = DrishyamColors.SecondaryText)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentProfile.keyCompounds.forEach { comp ->
                            FilterChip(
                                selected = comp.id == selectedCompound?.id,
                                onClick = { onSelectCompound(comp.id) },
                                label = { Text("${comp.name} (${comp.chemicalFormula})") }
                            )
                        }
                    }

                    selectedCompound?.let { comp ->
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = DrishyamColors.ElevatedSurface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "${comp.name} • Formula: ${comp.chemicalFormula} • ${comp.flavorDbId}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DrishyamColors.SecondaryGold
                                )
                                Text(
                                    text = "Functional Group: ${comp.functionalGroup} • Aroma Notes: ${comp.aromaDescriptors.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Molecular Ingredient Substitutions (Part 15.3)
        item {
            Text(
                text = "MOLECULAR INGREDIENT SUBSTITUTION ENGINE",
                style = MaterialTheme.typography.labelMedium,
                color = DrishyamColors.PrimaryOrange
            )
            Text(
                text = "Find Chemically Compatible Alternatives",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.substitutionQuery,
                onValueChange = onSubstitutionQueryChange,
                label = { Text("Missing Ingredient (e.g., paneer, cumin_seeds, cardamom)") },
                leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("substitution_search_input")
            )
        }

        items(state.substitutionResults, key = { it.substituteCanonicalName }) { sub ->
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    if (sub.inPantry) DrishyamColors.Success.copy(alpha = 0.6f) else DrishyamColors.BorderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sub.substituteDisplayName, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (sub.inPantry) {
                                Surface(color = DrishyamColors.SuccessDim, shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        "IN PANTRY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DrishyamColors.Success,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Surface(color = DrishyamColors.PrimaryOrangeDim, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    "${(sub.compoundOverlapRatio * 100).roundToInt()}% Overlap",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DrishyamColors.PrimaryOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(sub.flavourSimilarityDescription, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Expected Taste Shift: ${sub.expectedTasteDifference} (${sub.ratioAdjustment})",
                        style = MaterialTheme.typography.bodySmall,
                        color = DrishyamColors.SecondaryGold
                    )
                }
            }
        }
    }
}

@Composable
fun NutritionDashboardScreen(
    state: DrishyamUiState,
    onOpenGoals: () -> Unit,
    onOpenMealPlanner: () -> Unit,
    onLogCustomMeal: (String, MealSlot, Double, Int, Double, Double, Double, Double) -> Unit,
    onDeleteMealLog: (String) -> Unit
) {
    var showLogModal by remember { mutableStateOf(false) }

    val logs = state.mealLogs
    val targets = state.nutritionTargets
    val totalCal = logs.sumOf { it.calories }
    val totalProt = logs.sumOf { it.proteinG }
    val totalCarbs = logs.sumOf { it.carbsG }
    val totalFat = logs.sumOf { it.fatG }
    val totalFibre = logs.sumOf { it.fibreG }
    val totalSugar = logs.sumOf { it.sugarG }
    val totalSatFat = logs.sumOf { it.saturatedFatG }
    val totalSodium = logs.sumOf { it.sodiumMg }
    val totalIron = logs.sumOf { it.ironMg }
    val totalCalcium = logs.sumOf { it.calciumMg }
    val totalVitC = logs.sumOf { it.vitaminCMg }
    val totalPotassium = logs.sumOf { it.potassiumMg }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DrishyamColors.Background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PERSONALIZED NUTRITION ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = DrishyamColors.PrimaryOrange
                    )
                    Text(
                        text = "Macro & Micronutrient Telemetry",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                Button(
                    onClick = { showLogModal = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DrishyamColors.PrimaryOrange,
                        contentColor = DrishyamColors.Background
                    ),
                    modifier = Modifier.testTag("log_meal_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Log Meal")
                }
            }
        }

        // Goal & Mifflin-St Jeor Equation Transparency Card (Part 10.1)
        item {
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE GOAL: ${state.userProfile.primaryGoal.title.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = DrishyamColors.SecondaryGold
                        )
                        TextButton(onClick = onOpenGoals) {
                            Text("Customize Goal / Targets", color = DrishyamColors.PrimaryOrange)
                        }
                    }
                    Text(
                        text = "BMR: ${targets.bmrKcal} kcal • TDEE Maintenance: ${targets.tdeeKcal} kcal • Goal Target: ${targets.targetCalories} kcal",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = targets.equationUsed,
                        style = MaterialTheme.typography.labelSmall,
                        color = DrishyamColors.SecondaryText
                    )
                    Text(
                        text = targets.assumptionsSummary,
                        style = MaterialTheme.typography.bodySmall
                    )

                    targets.safetyWarnings.forEach { warn ->
                        Surface(
                            color = DrishyamColors.WarningDim,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠ $warn",
                                style = MaterialTheme.typography.bodySmall,
                                color = DrishyamColors.Warning,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Primary Macro Progress Rings & Bars (Part 10.3 & Part 16.1)
        item {
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("DAILY ENERGY & MACRONUTRIENT BUDGET", style = MaterialTheme.typography.labelMedium, color = DrishyamColors.PrimaryOrange)
                    NutrientProgressRow("Calories", totalCal, targets.targetCalories, "kcal", DrishyamColors.PrimaryOrange)
                    NutrientProgressRow("Protein", totalProt.roundToInt(), targets.proteinG, "g", DrishyamColors.SecondaryGold)
                    NutrientProgressRow("Carbohydrates", totalCarbs.roundToInt(), targets.carbsG, "g", DrishyamColors.Success)
                    NutrientProgressRow("Dietary Fat", totalFat.roundToInt(), targets.fatG, "g", DrishyamColors.Warning)
                    NutrientProgressRow("Dietary Fibre", totalFibre.roundToInt(), targets.fibreG, "g", DrishyamColors.Success)
                }
            }
        }

        // Secondary Nutrients & Micronutrients Card (Part 10.3)
        item {
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("MICRONUTRIENTS & LIPID/SODIUM TELEMETRY", style = MaterialTheme.typography.labelMedium, color = DrishyamColors.SecondaryGold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sugar: ${totalSugar.roundToInt()}g", style = MaterialTheme.typography.bodyMedium)
                        Text("Sat Fat: ${totalSatFat.roundToInt()}g", style = MaterialTheme.typography.bodyMedium)
                        Text("Sodium: ${totalSodium}mg", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Iron: ${(totalIron * 10).roundToInt() / 10.0}mg", style = MaterialTheme.typography.bodySmall, color = DrishyamColors.Success)
                        Text("Calcium: ${totalCalcium}mg", style = MaterialTheme.typography.bodySmall, color = DrishyamColors.Success)
                        Text("Vit C: ${(totalVitC * 10).roundToInt() / 10.0}mg", style = MaterialTheme.typography.bodySmall, color = DrishyamColors.Success)
                        Text("Potassium: ${totalPotassium}mg", style = MaterialTheme.typography.bodySmall, color = DrishyamColors.Success)
                    }
                    Text(
                        text = "Values dynamically aggregated from IFCT 2017 / USDA per-100g ingredient compositions vs. user estimates.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Meal History & Logged Entries (Part 19)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Logged Meals (${logs.size})", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onOpenMealPlanner) {
                    Text("Open 7-Day Planner", color = DrishyamColors.SecondaryGold)
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Surface(
                    color = DrishyamColors.Surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No Meals Logged Yet Today", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Complete any recipe in Interactive Cooking Mode or tap 'Log Meal' above to record your real intake.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DrishyamColors.SecondaryText
                        )
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                Surface(
                    color = DrishyamColors.Surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${log.mealSlot.label.uppercase()} • ${log.dateIso} • ${log.sourceLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = DrishyamColors.SecondaryGold
                            )
                            Text(log.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${log.servings} srv • ${log.calories} kcal • P: ${log.proteinG}g • C: ${log.carbsG}g • F: ${log.fatG}g • Fibre: ${log.fibreG}g",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { onDeleteMealLog(log.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete Meal Log", tint = DrishyamColors.Danger)
                        }
                    }
                }
            }
        }
    }

    if (showLogModal) {
        var titleInput by remember { mutableStateOf("") }
        var calInput by remember { mutableStateOf("420") }
        var protInput by remember { mutableStateOf("28") }
        var carbsInput by remember { mutableStateOf("42") }
        var fatInput by remember { mutableStateOf("14") }
        var fibreInput by remember { mutableStateOf("6") }
        var selectedSlot by remember { mutableStateOf(MealSlot.DINNER) }

        AlertDialog(
            onDismissRequest = { showLogModal = false },
            containerColor = DrishyamColors.ElevatedSurface,
            title = { Text("Log Consumed Meal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Meal Name") },
                        singleLine = true,
                        modifier = Modifier.testTag("custom_meal_title_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MealSlot.entries.take(3).forEach { slot ->
                            FilterChip(
                                selected = selectedSlot == slot,
                                onClick = { selectedSlot = slot },
                                label = { Text(slot.label) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = calInput,
                            onValueChange = { calInput = it },
                            label = { Text("Calories") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = protInput,
                            onValueChange = { protInput = it },
                            label = { Text("Protein (g)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = carbsInput,
                            onValueChange = { carbsInput = it },
                            label = { Text("Carbs (g)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fatInput,
                            onValueChange = { fatInput = it },
                            label = { Text("Fat (g)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onLogCustomMeal(
                            titleInput.ifBlank { "Custom Kitchen Meal" },
                            selectedSlot,
                            1.0,
                            calInput.toIntOrNull() ?: 400,
                            protInput.toDoubleOrNull() ?: 25.0,
                            carbsInput.toDoubleOrNull() ?: 40.0,
                            fatInput.toDoubleOrNull() ?: 12.0,
                            fibreInput.toDoubleOrNull() ?: 5.0
                        )
                        showLogModal = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DrishyamColors.PrimaryOrange,
                        contentColor = DrishyamColors.Background
                    ),
                    modifier = Modifier.testTag("save_custom_meal_button")
                ) {
                    Text("Save Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NutrientProgressRow(
    label: String,
    consumed: Int,
    target: Int,
    unit: String,
    color: Color
) {
    val remaining = (target - consumed).coerceAtLeast(0)
    val progress = (consumed.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "Consumed: $consumed$unit • Target: $target$unit • Rem: $remaining$unit",
                style = MaterialTheme.typography.labelSmall,
                color = DrishyamColors.SecondaryText
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = DrishyamColors.ElevatedSurface,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}
