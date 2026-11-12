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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.*
import com.example.domain.usecase.NutritionEngine
import com.example.ui.DrishyamUiState
import com.example.ui.theme.DrishyamColors
import kotlin.math.roundToInt

@Composable
fun MealPlannerScreen(
    state: DrishyamUiState,
    onBack: () -> Unit,
    onSelectDay: (Int) -> Unit,
    onAddRecipeToDay: (Recipe, Int, MealSlot) -> Unit,
    onRemovePlannedMeal: (String) -> Unit,
    onGenerateShoppingList: () -> Unit,
    onGenerateAutoWeeklyPlan: () -> Unit = {}
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

        // 1-Tap Auto-Generate 7-Day Plan Button
        item {
            Button(
                onClick = onGenerateAutoWeeklyPlan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DrishyamColors.PrimaryOrange,
                    contentColor = DrishyamColors.Background
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_generate_weekly_plan_button")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Auto-Generate 7-Day Plan for ${state.userProfile.primaryGoal.title}")
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
                    val countForDay = state.plannedMeals.count { it.dayIndex == idx }
                    FilterChip(
                        selected = state.selectedPlannerDayIndex == idx,
                        onClick = { onSelectDay(idx) },
                        label = { Text(if (countForDay > 0) "$label ($countForDay)" else label) }
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

        if (dayMeals.isEmpty()) {
            item {
                Surface(
                    color = DrishyamColors.Surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("No Meals Scheduled for ${days[state.selectedPlannerDayIndex]}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tap 'Auto-Generate 7-Day Plan' above or add any goal-compatible recipe below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DrishyamColors.SecondaryText
                        )
                    }
                }
            }
        } else {
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
                            Text(
                                "${meal.mealSlot.label.uppercase()} • ${meal.servings} srv",
                                style = MaterialTheme.typography.labelSmall,
                                color = DrishyamColors.PrimaryOrange
                            )
                            Text(meal.title, style = MaterialTheme.typography.titleMedium)
                            Text("${meal.calories} kcal • ${meal.proteinG}g Protein • ${meal.carbsG}g Carbs", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onRemovePlannedMeal(meal.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = DrishyamColors.Danger)
                        }
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
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { onAddRecipeToDay(scored.recipe, state.selectedPlannerDayIndex, MealSlot.BREAKFAST) }) {
                            Text("+ Bkfst", color = DrishyamColors.Success)
                        }
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

        if (state.shoppingItems.isEmpty()) {
            item {
                Surface(
                    color = DrishyamColors.Surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Shopping List is Clean", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tap 'Generate From Missing Recipe Items & Low-Stock Pantry' above to compute your missing kitchen stock.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DrishyamColors.SecondaryText
                        )
                    }
                }
            }
        } else {
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
}

@Composable
fun OnboardingWizardScreen(
    state: DrishyamUiState,
    onSetStep: (Int) -> Unit,
    onUpdateProfile: (UserProfile) -> Unit,
    onCompleteOnboarding: () -> Unit
) {
    BackHandler {
        if (state.onboardingStep > 0) {
            onSetStep(state.onboardingStep - 1)
        } else {
            onCompleteOnboarding()
        }
    }

    val stepTitles = listOf(
        "1. Basic Profile & Units",
        "2. Activity Level",
        "3. Primary & Secondary Fitness Goals",
        "4. Dietary Lifestyle & Restrictions",
        "5. Allergies & Hard Exclusions",
        "6. Preferred Cuisines & Taste Profile",
        "7. Cooking Skill, Time & Utensils",
        "8. Mifflin-St Jeor Target Review"
    )
    val step = state.onboardingStep.coerceIn(0, 7)
    val profile = state.userProfile
    val targets = state.nutritionTargets

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DrishyamColors.Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "DRISHYAM 8-STEP CALIBRATION WIZARD • STEP ${step + 1} OF 8",
                style = MaterialTheme.typography.labelSmall,
                color = DrishyamColors.PrimaryOrange
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stepTitles[step],
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (step + 1) / 8f },
                color = DrishyamColors.PrimaryOrange,
                trackColor = DrishyamColors.ElevatedSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (step) {
                    0 -> item {
                        var nameText by remember(profile.displayName) { mutableStateOf(profile.displayName) }
                        var ageText by remember(profile.age) { mutableStateOf(profile.age.toString()) }
                        var heightText by remember(profile.heightCm) { mutableStateOf(profile.heightCm.roundToInt().toString()) }
                        var weightText by remember(profile.weightKg) { mutableStateOf(profile.weightKg.roundToInt().toString()) }
                        var targetWeightText by remember(profile.targetWeightKg) { mutableStateOf((profile.targetWeightKg ?: profile.weightKg).roundToInt().toString()) }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = nameText,
                                onValueChange = {
                                    nameText = it
                                    onUpdateProfile(profile.copy(displayName = it))
                                },
                                label = { Text("Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BiologicalSex.entries.forEach { sex ->
                                    FilterChip(
                                        selected = profile.biologicalSex == sex,
                                        onClick = { onUpdateProfile(profile.copy(biologicalSex = sex)) },
                                        label = { Text(sex.label) }
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = ageText,
                                    onValueChange = {
                                        ageText = it
                                        it.toIntOrNull()?.let { a -> onUpdateProfile(profile.copy(age = a)) }
                                    },
                                    label = { Text("Age") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = heightText,
                                    onValueChange = {
                                        heightText = it
                                        it.toDoubleOrNull()?.let { h -> onUpdateProfile(profile.copy(heightCm = h)) }
                                    },
                                    label = { Text("Height (cm)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = weightText,
                                    onValueChange = {
                                        weightText = it
                                        it.toDoubleOrNull()?.let { w -> onUpdateProfile(profile.copy(weightKg = w)) }
                                    },
                                    label = { Text("Current Weight (kg)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = targetWeightText,
                                    onValueChange = {
                                        targetWeightText = it
                                        it.toDoubleOrNull()?.let { tw -> onUpdateProfile(profile.copy(targetWeightKg = tw)) }
                                    },
                                    label = { Text("Target Weight (kg)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    1 -> items(ActivityLevel.entries) { act ->
                        val selected = profile.activityLevel == act
                        Surface(
                            color = if (selected) DrishyamColors.PrimaryOrangeDim else DrishyamColors.Surface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (selected) DrishyamColors.PrimaryOrange else DrishyamColors.BorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateProfile(profile.copy(activityLevel = act)) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("${act.title} (×${act.multiplier} TDEE)", style = MaterialTheme.typography.titleMedium)
                                Text(act.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    2 -> items(FitnessGoalType.entries) { goal ->
                        val selected = profile.primaryGoal == goal
                        Surface(
                            color = if (selected) DrishyamColors.PrimaryOrangeDim else DrishyamColors.Surface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (selected) DrishyamColors.PrimaryOrange else DrishyamColors.BorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateProfile(profile.copy(primaryGoal = goal)) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(goal.title, style = MaterialTheme.typography.titleMedium)
                                Text(goal.subtitle, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    3 -> item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Select Primary Dietary Lifestyle:", style = MaterialTheme.typography.titleSmall)
                            DietaryLifestyle.entries.forEach { diet ->
                                FilterChip(
                                    selected = profile.dietaryLifestyle == diet,
                                    onClick = { onUpdateProfile(profile.copy(dietaryLifestyle = diet)) },
                                    label = { Text(diet.label) }
                                )
                            }
                            HorizontalDivider(color = DrishyamColors.BorderSubtle)
                            Text("Additional Dietary Restrictions:", style = MaterialTheme.typography.titleSmall)
                            DietaryRestriction.entries.forEach { rest ->
                                val active = rest in profile.dietaryRestrictions
                                FilterChip(
                                    selected = active,
                                    onClick = {
                                        val next = if (active) profile.dietaryRestrictions - rest else profile.dietaryRestrictions + rest
                                        onUpdateProfile(profile.copy(dietaryRestrictions = next))
                                    },
                                    label = { Text(rest.label) }
                                )
                            }
                        }
                    }

                    4 -> item {
                        var customExclInput by remember { mutableStateOf("") }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Hard Zero-Tolerance Allergens:", style = MaterialTheme.typography.titleSmall, color = DrishyamColors.Danger)
                            AllergenType.entries.forEach { alg ->
                                val active = alg in profile.allergies
                                FilterChip(
                                    selected = active,
                                    onClick = {
                                        val next = if (active) profile.allergies - alg else profile.allergies + alg
                                        onUpdateProfile(profile.copy(allergies = next))
                                    },
                                    label = { Text(alg.label) }
                                )
                            }
                            HorizontalDivider(color = DrishyamColors.BorderSubtle)
                            Text("Custom Excluded Ingredients: ${profile.excludedIngredients.joinToString(", ").ifEmpty { "None" }}", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = customExclInput,
                                    onValueChange = { customExclInput = it },
                                    label = { Text("Exclude ingredient (e.g. mushroom)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        if (customExclInput.isNotBlank()) {
                                            onUpdateProfile(profile.copy(excludedIngredients = profile.excludedIngredients + customExclInput.trim().lowercase()))
                                            customExclInput = ""
                                        }
                                    }
                                ) {
                                    Text("Exclude")
                                }
                            }
                        }
                    }

                    5 -> item {
                        val allCuisines = listOf("North Indian", "Punjabi", "Rajasthani", "Maharashtrian", "South Indian", "Mediterranean")
                        val allTastes = listOf("Earthy", "Smoky", "Aromatic", "Spicy", "Tangy", "Herbal")
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Preferred Cuisines:", style = MaterialTheme.typography.titleSmall)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                allCuisines.forEach { c ->
                                    val active = c in profile.preferredCuisines
                                    FilterChip(
                                        selected = active,
                                        onClick = {
                                            val next = if (active) profile.preferredCuisines - c else profile.preferredCuisines + c
                                            onUpdateProfile(profile.copy(preferredCuisines = next))
                                        },
                                        label = { Text(c) }
                                    )
                                }
                            }
                            Text("Preferred Taste Notes:", style = MaterialTheme.typography.titleSmall)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                allTastes.forEach { t ->
                                    val active = t in profile.preferredTastes
                                    FilterChip(
                                        selected = active,
                                        onClick = {
                                            val next = if (active) profile.preferredTastes - t else profile.preferredTastes + t
                                            onUpdateProfile(profile.copy(preferredTastes = next))
                                        },
                                        label = { Text(t) }
                                    )
                                }
                            }
                        }
                    }

                    6 -> item {
                        val allSkills = listOf("Beginner", "Intermediate", "Advanced")
                        val allUtensils = listOf("Kadhai", "Pressure Cooker", "Tawa", "Blender", "Heavy Handi", "Cast Iron Skillet", "Oven")
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Cooking Skill Level:", style = MaterialTheme.typography.titleSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                allSkills.forEach { sk ->
                                    FilterChip(
                                        selected = profile.skillLevel == sk,
                                        onClick = { onUpdateProfile(profile.copy(skillLevel = sk)) },
                                        label = { Text(sk) }
                                    )
                                }
                            }
                            Text("Available Kitchen Utensils:", style = MaterialTheme.typography.titleSmall)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                allUtensils.forEach { u ->
                                    val active = u in profile.availableUtensils
                                    FilterChip(
                                        selected = active,
                                        onClick = {
                                            val next = if (active) profile.availableUtensils - u else profile.availableUtensils + u
                                            onUpdateProfile(profile.copy(availableUtensils = next))
                                        },
                                        label = { Text(u) }
                                    )
                                }
                            }
                        }
                    }

                    7 -> item {
                        Surface(
                            color = DrishyamColors.Surface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, DrishyamColors.PrimaryOrange),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("CALIBRATED DAILY NUTRITIONAL TARGETS", style = MaterialTheme.typography.labelMedium, color = DrishyamColors.PrimaryOrange)
                                Text("BMR: ${targets.bmrKcal} kcal • TDEE: ${targets.tdeeKcal} kcal", style = MaterialTheme.typography.titleMedium)
                                Text("Target Energy: ${targets.targetCalories} kcal / day", style = MaterialTheme.typography.headlineMedium, color = DrishyamColors.SecondaryGold)
                                Text("Protein: ${targets.proteinG}g • Carbs: ${targets.carbsG}g • Fat: ${targets.fatG}g • Fibre: ${targets.fibreG}g", style = MaterialTheme.typography.bodyLarge)
                                Text(targets.equationUsed, style = MaterialTheme.typography.labelSmall, color = DrishyamColors.SecondaryText)
                                Text(targets.assumptionsSummary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { if (step > 0) onSetStep(step - 1) else onCompleteOnboarding() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (step > 0) "Previous" else "Close")
            }
            Button(
                onClick = { if (step < 7) onSetStep(step + 1) else onCompleteOnboarding() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (step < 7) DrishyamColors.PrimaryOrange else DrishyamColors.Success,
                    contentColor = DrishyamColors.Background
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("onboarding_next_button")
            ) {
                Text(if (step < 7) "Next Step" else "Finish Calibration")
                Spacer(Modifier.width(6.dp))
                Icon(if (step < 7) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.CheckCircle, contentDescription = null)
            }
        }
    }
}

@Composable
fun ProfileAndGoalsScreen(
    state: DrishyamUiState,
    onBack: () -> Unit,
    onUpdateProfile: (UserProfile) -> Unit,
    onOpenAuthOrSync: () -> Unit,
    onOpenOnboardingWizard: () -> Unit = {}
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
                Row {
                    TextButton(onClick = onOpenOnboardingWizard) {
                        Text("8-Step Wizard", color = DrishyamColors.PrimaryOrange)
                    }
                    TextButton(onClick = onOpenAuthOrSync) {
                        Text("Account", color = DrishyamColors.SecondaryGold)
                    }
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
                                    "${profile.weightKg.roundToInt()} kg • ${profile.heightCm.roundToInt()} cm"
                                } else {
                                    val (ft, inch) = NutritionEngine.cmToFeetInches(profile.heightCm)
                                    "${NutritionEngine.kgToLb(profile.weightKg)} lb • ${ft}'${inch}\""
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
    onSetAuthStep: (AuthFlowStep) -> Unit = {},
    onUpdateBackendUrlAndSync: (String) -> Unit,
    onLoginOrRegister: (String, String, String?) -> Unit,
    onResetPassword: (String, String) -> Unit = { _, _ -> },
    onLogout: () -> Unit
) {
    BackHandler { onBack() }

    var urlInput by remember(state.backendBaseUrl) { mutableStateOf(state.backendBaseUrl) }
    var emailInput by remember(state.userProfile.email) { mutableStateOf(state.userProfile.email) }
    var passwordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
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

        // Local Standalone Explorer Banner (No Deployed Backend Required)
        item {
            Surface(
                color = DrishyamColors.ElevatedSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DrishyamColors.PrimaryOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LOCAL STANDALONE MODE ACTIVE",
                            style = MaterialTheme.typography.labelMedium,
                            color = DrishyamColors.PrimaryOrange
                        )
                        Surface(
                            color = DrishyamColors.SuccessDim,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "NO BACKEND REQUIRED",
                                style = MaterialTheme.typography.labelSmall,
                                color = DrishyamColors.Success,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = "Every feature — RecipeDB, FlavorDB Molecular Graph, Live CameraX Scanner, IFCT/USDA Nutrition Engine, 7-Day Meal Planner, and Smart Shopping — runs 100% on-device with Room SQLite. Connect your cloud backend URL whenever you deploy it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DrishyamColors.SecondaryText
                    )
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DrishyamColors.PrimaryOrange,
                            contentColor = DrishyamColors.Background
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("explore_standalone_button")
                    ) {
                        Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Explore Full App Now (Standalone Mode)")
                    }
                }
            }
        }

        // Auth Mode Selector Strip (Welcome, Login, Register, Forgot Password, Password Reset)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AuthFlowStep.entries.forEach { step ->
                    FilterChip(
                        selected = state.authFlowStep == step,
                        onClick = { onSetAuthStep(step) },
                        label = { Text(step.name.replace("_", " ")) }
                    )
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
                            "ACTIVE SESSION: ${state.userProfile.email} (Android Keystore AES-GCM + SHA-256 Verified)"
                        } else {
                            "SIGNED OUT — Mode: ${state.authFlowStep.name}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.isAuthenticated) DrishyamColors.Success else DrishyamColors.Warning
                    )

                    when (state.authFlowStep) {
                        AuthFlowStep.WELCOME -> {
                            Text(
                                "Unified DRISHYAM Account across Website & Native Android Scanner. Your pantry, fitness goals, allergies, meal plans, and shopping list synchronize seamlessly.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onSetAuthStep(AuthFlowStep.LOGIN) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DrishyamColors.PrimaryOrange, contentColor = DrishyamColors.Background),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Sign In")
                                }
                                OutlinedButton(
                                    onClick = { onSetAuthStep(AuthFlowStep.REGISTER) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Create Account")
                                }
                            }
                        }

                        AuthFlowStep.FORGOT_PASSWORD, AuthFlowStep.PASSWORD_RESET -> {
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Registered DRISHYAM Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newPasswordInput,
                                onValueChange = { newPasswordInput = it },
                                label = { Text("New Password (min 4 chars)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = { onResetPassword(emailInput, newPasswordInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = DrishyamColors.SecondaryGold, contentColor = DrishyamColors.Background),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reset Password & Update SHA-256 Credential")
                            }
                        }

                        AuthFlowStep.LOGIN, AuthFlowStep.REGISTER -> {
                            if (state.authFlowStep == AuthFlowStep.REGISTER) {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    label = { Text("Display Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
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
                                    onClick = {
                                        onLoginOrRegister(
                                            emailInput,
                                            passwordInput.ifBlank { "drishyam2026" },
                                            if (state.authFlowStep == AuthFlowStep.REGISTER) nameInput else null
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DrishyamColors.PrimaryOrange,
                                        contentColor = DrishyamColors.Background
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("auth_submit_button")
                                ) {
                                    Text(if (state.authFlowStep == AuthFlowStep.REGISTER) "Register & Launch Wizard" else "Sign In")
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
