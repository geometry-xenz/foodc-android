package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.FoodoscopeSeedCatalog
import com.example.domain.model.*
import com.example.domain.usecase.NutritionEngine
import com.example.ui.DrishyamUiState
import com.example.ui.SubScreen
import com.example.ui.theme.DrishyamColors
import kotlin.math.roundToInt

@Composable
fun MealPlannerScreen(
    state: DrishyamUiState,
    onBack: () -> Unit,
    onSelectDay: (Int) -> Unit,
    onAddRecipeToDay: (Recipe, Int, MealSlot) -> Unit,
    onRemovePlannedMeal: (String) -> Unit,
    onGenerateShoppingList: () -> Unit
) {
    BackHandler { onBack() }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dayMeals = state.plannedMeals.filter { it.dayIndex == state.selectedPlannerDayIndex }
    val plannedCalories = dayMeals.sumOf { it.calories }
    val plannedProtein = dayMeals.sumOf { it.proteinG }.roundToInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DrishyamColors.Background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DrishyamColors.PrimaryText)
                    }
                    Column {
                        Text("7-DAY CULINARY SCHEDULER", style = MaterialTheme.typography.labelSmall, color = DrishyamColors.PrimaryOrange)
                        Text("Weekly Meal Planner", style = MaterialTheme.typography.headlineLarge)
                    }
                }
                OutlinedButton(
                    onClick = onGenerateShoppingList,
                    border = BorderStroke(1.dp, DrishyamColors.SecondaryGold)
                ) {
                    Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, tint = DrishyamColors.SecondaryGold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sync Shopping", color = DrishyamColors.SecondaryGold)
                }
            }
        }

        // Day Selector Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = state.selectedPlannerDayIndex == idx,
                        onClick = { onSelectDay(idx) },
                        label = { Text(label) }
                    )
                }
            }
        }

        // Planned Day Summary vs Target
        item {
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Planned: $plannedCalories / ${state.nutritionTargets.targetCalories} kcal",
                        style = MaterialTheme.typography.labelLarge,
                        color = DrishyamColors.PrimaryOrange
                    )
                    Text(
                        text = "Protein: ${plannedProtein}g / ${state.nutritionTargets.proteinG}g",
                        style = MaterialTheme.typography.labelLarge,
                        color = DrishyamColors.SecondaryGold
                    )
                }
            }
        }

        items(dayMeals, key = { it.id }) { meal ->
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
                        Text(meal.mealSlot.label.uppercase(), style = MaterialTheme.typography.labelSmall, color = DrishyamColors.PrimaryOrange)
                        Text(meal.title, style = MaterialTheme.typography.titleMedium)
                        Text("${meal.calories} kcal • ${meal.proteinG}g Protein • ${meal.carbsG}g Carbs", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onRemovePlannedMeal(meal.id) }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = DrishyamColors.Danger)
                    }
                }
            }
        }

        // Quick Add Goal-Compatible Recipes to Selected Day
        item {
            Spacer(Modifier.height(6.dp))
            Text("Add Goal-Compatible Recipe to ${days[state.selectedPlannerDayIndex]}:", style = MaterialTheme.typography.titleMedium)
        }

        items(state.scoredRecipes.take(4), key = { "add_${it.recipe.id}" }) { scored ->
            Surface(
                color = DrishyamColors.ElevatedSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(scored.recipe.title, style = MaterialTheme.typography.titleSmall)
                        Text("${scored.recipe.nutritionPerServing.calories} kcal • ${scored.recipe.nutritionPerServing.proteinG.roundToInt()}g P", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { onAddRecipeToDay(scored.recipe, state.selectedPlannerDayIndex, MealSlot.LUNCH) }) {
                            Text("+ Lunch", color = DrishyamColors.PrimaryOrange)
                        }
                        TextButton(onClick = { onAddRecipeToDay(scored.recipe, state.selectedPlannerDayIndex, MealSlot.DINNER) }) {
                            Text("+ Dinner", color = DrishyamColors.SecondaryGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListScreen(
    state: DrishyamUiState,
    onBack: () -> Unit,
    onGenerateSmartSuggestions: () -> Unit,
    onAddManualItem: (String, Double, String) -> Unit,
    onTogglePurchased: (ShoppingListItem) -> Unit,
    onClearPurchased: () -> Unit
) {
    BackHandler { onBack() }

    var newItemName by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("100") }
    var newItemUnit by remember { mutableStateOf("g") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DrishyamColors.Background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DrishyamColors.PrimaryText)
                    }
                    Column {
                        Text("PANTRY-AWARE SHOPPING ENGINE", style = MaterialTheme.typography.labelSmall, color = DrishyamColors.Success)
                        Text("Shopping Intelligence", style = MaterialTheme.typography.headlineLarge)
                    }
                }
                TextButton(onClick = onClearPurchased) {
                    Text("Clear Bought", color = DrishyamColors.SecondaryText)
                }
            }
        }

        item {
            Button(
                onClick = onGenerateSmartSuggestions,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DrishyamColors.PrimaryOrange,
                    contentColor = DrishyamColors.Background
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_smart_shopping_button")
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Generate From Missing Recipe Items & Low-Stock Pantry")
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    placeholder = { Text("Add item...") },
                    singleLine = true,
                    modifier = Modifier.weight(1.4f)
                )
                OutlinedTextField(
                    value = newItemQty,
                    onValueChange = { newItemQty = it },
                    placeholder = { Text("Qty") },
                    singleLine = true,
                    modifier = Modifier.weight(0.7f)
                )
                Button(
                    onClick = {
                        onAddManualItem(newItemName, newItemQty.toDoubleOrNull() ?: 100.0, newItemUnit)
                        newItemName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DrishyamColors.SecondaryGold, contentColor = DrishyamColors.Background)
                ) {
                    Text("Add")
                }
            }
        }

        items(state.shoppingItems, key = { it.id }) { item ->
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTogglePurchased(item) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Checkbox(
                            checked = item.isPurchased,
                            onCheckedChange = { onTogglePurchased(item) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${item.displayName} (${item.quantity.roundToInt()} ${item.unit})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(text = item.reason, style = MaterialTheme.typography.bodySmall, color = DrishyamColors.SecondaryGold)
                            item.alternativeSuggestion?.let { alt ->
                                Text(text = "FlavorDB Alternative: $alt", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileAndGoalsScreen(
    state: DrishyamUiState,
    onBack: () -> Unit,
    onUpdateProfile: (UserProfile) -> Unit,
    onOpenAuthOrSync: () -> Unit
) {
    BackHandler { onBack() }

    val profile = state.userProfile
    var weightInput by remember(profile.weightKg) { mutableStateOf(profile.weightKg.roundToInt().toString()) }
    var heightInput by remember(profile.heightCm) { mutableStateOf(profile.heightCm.roundToInt().toString()) }
    var ageInput by remember(profile.age) { mutableStateOf(profile.age.toString()) }
    var customCalInput by remember(profile.customCalorieOverride) {
        mutableStateOf(profile.customCalorieOverride?.toString() ?: "")
    }
    var customProtInput by remember(profile.customProteinOverrideG) {
        mutableStateOf(profile.customProteinOverrideG?.toString() ?: "")
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DrishyamColors.PrimaryText)
                    }
                    Column {
                        Text("8-GOAL PHYSIOLOGICAL ENGINE", style = MaterialTheme.typography.labelSmall, color = DrishyamColors.PrimaryOrange)
                        Text("Profile, Goals & Allergies", style = MaterialTheme.typography.headlineLarge)
                    }
                }
                TextButton(onClick = onOpenAuthOrSync) {
                    Text("Account & Sync", color = DrishyamColors.SecondaryGold)
                }
            }
        }

        // Biometrics & Unit System (Part 7.1)
        item {
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Biometrics & Units", style = MaterialTheme.typography.titleMedium)
                        FilterChip(
                            selected = profile.unitSystem == UnitSystem.IMPERIAL,
                            onClick = {
                                val nextUnit = if (profile.unitSystem == UnitSystem.METRIC) UnitSystem.IMPERIAL else UnitSystem.METRIC
                                onUpdateProfile(profile.copy(unitSystem = nextUnit))
                            },
                            label = {
                                val displayWeight = if (profile.unitSystem == UnitSystem.METRIC) {
                                    "${profile.weightKg.roundToInt()} kg"
                                } else {
                                    "${NutritionEngine.kgToLb(profile.weightKg)} lb"
                                }
                                Text("${profile.unitSystem.name} ($displayWeight)")
                            }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = {
                                weightInput = it
                                it.toDoubleOrNull()?.let { w -> onUpdateProfile(profile.copy(weightKg = w)) }
                            },
                            label = { Text("Weight (kg)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = {
                                heightInput = it
                                it.toDoubleOrNull()?.let { h -> onUpdateProfile(profile.copy(heightCm = h)) }
                            },
                            label = { Text("Height (cm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ageInput,
                            onValueChange = {
                                ageInput = it
                                it.toIntOrNull()?.let { a -> onUpdateProfile(profile.copy(age = a)) }
                            },
                            label = { Text("Age") },
                            singleLine = true,
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                }
            }
        }

        // Complete 8 Fitness Goals Selector (Part 8.1 - 8.9)
        item {
            Text(
                text = "PRIMARY FITNESS GOAL (INDEPENDENT OF DIETARY LIFESTYLE)",
                style = MaterialTheme.typography.labelMedium,
                color = DrishyamColors.PrimaryOrange
            )
        }

        items(FitnessGoalType.entries, key = { it.id }) { goal ->
            val selected = profile.primaryGoal == goal
            Surface(
                color = if (selected) DrishyamColors.PrimaryOrangeDim else DrishyamColors.Surface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    if (selected) DrishyamColors.PrimaryOrange else DrishyamColors.BorderSubtle
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpdateProfile(profile.copy(primaryGoal = goal)) }
                    .testTag("goal_option_${goal.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(goal.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${(goal.calorieDeltaFactor * 100).roundToInt()}% TDEE • ${goal.proteinGramsPerKg}g/kg P",
                            style = MaterialTheme.typography.labelSmall,
                            color = DrishyamColors.SecondaryGold
                        )
                    }
                    Text(goal.subtitle, style = MaterialTheme.typography.bodySmall)
                    goal.cautionNote?.let { note ->
                        Spacer(Modifier.height(4.dp))
                        Text("Note: $note", style = MaterialTheme.typography.bodySmall, color = DrishyamColors.Warning)
                    }
                }
            }
        }

        // Activity Level (Part 7.2)
        item {
            Text("ACTIVITY LEVEL", style = MaterialTheme.typography.labelMedium, color = DrishyamColors.SecondaryGold)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActivityLevel.entries.forEach { act ->
                    FilterChip(
                        selected = profile.activityLevel == act,
                        onClick = { onUpdateProfile(profile.copy(activityLevel = act)) },
                        label = { Text("${act.title} (×${act.multiplier})") }
                    )
                }
            }
        }

        // Dietary Lifestyle & Restrictions (Part 9.1 & 9.2)
        item {
            Text("DIETARY LIFESTYLE", style = MaterialTheme.typography.labelMedium, color = DrishyamColors.PrimaryOrange)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DietaryLifestyle.entries.forEach { diet ->
                    FilterChip(
                        selected = profile.dietaryLifestyle == diet,
                        onClick = { onUpdateProfile(profile.copy(dietaryLifestyle = diet)) },
                        label = { Text(diet.label) },
                        modifier = Modifier.testTag("diet_chip_${diet.name}")
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("ADDITIONAL RESTRICTIONS", style = MaterialTheme.typography.labelMedium, color = DrishyamColors.SecondaryGold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DietaryRestriction.entries.forEach { rest ->
                    val active = rest in profile.dietaryRestrictions
                    FilterChip(
                        selected = active,
                        onClick = {
                            val updated = if (active) profile.dietaryRestrictions - rest else profile.dietaryRestrictions + rest
                            onUpdateProfile(profile.copy(dietaryRestrictions = updated))
                        },
                        label = { Text(rest.label) }
                    )
                }
            }
        }

        // Hard Allergy Constraints (Part 9.3)
        item {
            Text(
                text = "HARD ALLERGY CONSTRAINTS (ZERO-TOLERANCE EXCLUSION)",
                style = MaterialTheme.typography.labelMedium,
                color = DrishyamColors.Danger
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AllergenType.entries.forEach { allergen ->
                    val active = allergen in profile.allergies
                    FilterChip(
                        selected = active,
                        onClick = {
                            val updated = if (active) profile.allergies - allergen else profile.allergies + allergen
                            onUpdateProfile(profile.copy(allergies = updated))
                        },
                        label = { Text(allergen.label) },
                        modifier = Modifier.testTag("allergen_chip_${allergen.name}")
                    )
                }
            }
        }

        // Custom Calorie & Protein Target Overrides (Part 8.9 & Part 10.1)
        item {
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Custom Calorie & Macro Overrides (Optional)", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customCalInput,
                            onValueChange = {
                                customCalInput = it
                                onUpdateProfile(profile.copy(customCalorieOverride = it.toIntOrNull()))
                            },
                            label = { Text("Custom kcal (blank = auto)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customProtInput,
                            onValueChange = {
                                customProtInput = it
                                onUpdateProfile(profile.copy(customProteinOverrideG = it.toIntOrNull()))
                            },
                            label = { Text("Custom Protein g") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsAndSyncScreen(
    state: DrishyamUiState,
    onBack: () -> Unit,
    onUpdateBackendUrlAndSync: (String) -> Unit,
    onLoginOrRegister: (String, String, String?) -> Unit,
    onLogout: () -> Unit
) {
    BackHandler { onBack() }

    var urlInput by remember(state.backendBaseUrl) { mutableStateOf(state.backendBaseUrl) }
    var emailInput by remember(state.userProfile.email) { mutableStateOf(state.userProfile.email) }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember(state.userProfile.displayName) { mutableStateOf(state.userProfile.displayName) }

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
                Column {
                    Text("CROSS-PLATFORM ACCOUNT & CLOUD SYNC", style = MaterialTheme.typography.labelSmall, color = DrishyamColors.PrimaryOrange)
                    Text("Authentication & Sync Outbox", style = MaterialTheme.typography.headlineLarge)
                }
            }
        }

        // Cross-Platform Authentication Card (Part 6)
        item {
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (state.isAuthenticated) {
                            "ACTIVE SESSION: ${state.userProfile.email} (Android Keystore AES-GCM Protected)"
                        } else {
                            "SIGNED OUT — Log in with your DRISHYAM Web or Mobile account"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.isAuthenticated) DrishyamColors.Success else DrishyamColors.Warning
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Shared DRISHYAM Account Email") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onLoginOrRegister(emailInput, passwordInput.ifBlank { "drishyam2026" }, nameInput) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DrishyamColors.PrimaryOrange,
                                contentColor = DrishyamColors.Background
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("auth_submit_button")
                        ) {
                            Text(if (state.isAuthenticated) "Switch / Refresh Session" else "Sign In / Register")
                        }
                        if (state.isAuthenticated) {
                            OutlinedButton(
                                onClick = onLogout,
                                border = BorderStroke(1.dp, DrishyamColors.Danger),
                                modifier = Modifier.testTag("auth_logout_button")
                            ) {
                                Text("Log Out", color = DrishyamColors.Danger)
                            }
                        }
                    }
                }
            }
        }

        // Backend Endpoint & Incremental Sync Outbox (Part 21 & 24)
        item {
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("DRISHYAM Shared Backend Endpoint", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("REST API Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onUpdateBackendUrlAndSync(urlInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DrishyamColors.SecondaryGold,
                            contentColor = DrishyamColors.Background
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trigger_sync_button")
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Push Outbox & Synchronize Now")
                    }
                }
            }
        }

        item {
            Text(
                text = "IDEMPOTENT SYNC OUTBOX LOG (${state.syncOperations.size})",
                style = MaterialTheme.typography.labelMedium,
                color = DrishyamColors.SecondaryGold
            )
        }

        items(state.syncOperations.take(10), key = { it.idempotencyKey }) { op ->
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${op.entityType} • ${op.action}", style = MaterialTheme.typography.labelSmall, color = DrishyamColors.PrimaryText)
                    Text(
                        text = if (op.synced) "SYNCED" else "QUEUED (OFFLINE SAFE)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (op.synced) DrishyamColors.Success else DrishyamColors.Warning
                    )
                }
            }
        }
    }
}
