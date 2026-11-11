package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.FoodoscopeSeedCatalog
import com.example.domain.model.*
import com.example.ui.DrishyamUiState
import com.example.ui.SubScreen
import com.example.ui.theme.DrishyamColors
import kotlin.math.roundToInt

@Composable
fun DiscoverRecipesScreen(
    state: DrishyamUiState,
    onSearchChange: (String) -> Unit,
    onCuisineSelect: (String) -> Unit,
    onOpenRecipe: (String) -> Unit,
    onOpenFlavourScience: () -> Unit
) {
    val cuisines = listOf("All", "North Indian", "Punjabi", "Rajasthani", "Maharashtrian", "Mediterranean")
    val filtered = state.scoredRecipes.filter { scored ->
        val matchesCuisine = state.selectedCuisineFilter == "All" || scored.recipe.cuisine == state.selectedCuisineFilter
        val matchesSearch = state.recipeSearchQuery.isBlank() ||
            scored.recipe.title.contains(state.recipeSearchQuery, ignoreCase = true) ||
            scored.recipe.ingredients.any { it.displayName.contains(state.recipeSearchQuery, ignoreCase = true) }
        matchesCuisine && matchesSearch
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
                    text = "FOODOSCOPE RECIPEDB ENGINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = DrishyamColors.PrimaryOrange
                )
                Text(
                    text = "Computational Discovery",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            OutlinedButton(
                onClick = onOpenFlavourScience,
                border = BorderStroke(1.dp, DrishyamColors.SecondaryGold)
            ) {
                Icon(Icons.Default.Science, contentDescription = null, tint = DrishyamColors.SecondaryGold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("FlavorDB", color = DrishyamColors.SecondaryGold)
            }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = state.recipeSearchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search recipes, spices, or high-protein meals...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("discover_search_input")
        )

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cuisines.forEach { cuisine ->
                FilterChip(
                    selected = state.selectedCuisineFilter == cuisine,
                    onClick = { onCuisineSelect(cuisine) },
                    label = { Text(cuisine) }
                )
            }
        }

        // Hard Allergy & Lifestyle Active Filter Banner
        Surface(
            color = DrishyamColors.ElevatedSurface,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Text(
                text = "Hard Filters Enforced: ${state.userProfile.dietaryLifestyle.label} • Allergens blocked: ${
                    state.userProfile.allergies.joinToString(", ") { it.label }.ifEmpty { "None configured" }
                } • Goal: ${state.userProfile.primaryGoal.title}",
                style = MaterialTheme.typography.labelSmall,
                color = DrishyamColors.SecondaryGold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.recipe.id }) { scored ->
                GastronomyRecipeCard(
                    scored = scored,
                    isSaved = scored.recipe.id in state.savedRecipeIds,
                    onClick = { onOpenRecipe(scored.recipe.id) }
                )
            }
        }
    }
}

@Composable
fun RecipeDetailScreen(
    state: DrishyamUiState,
    onBack: () -> Unit,
    onAdjustServings: (Int) -> Unit,
    onToggleSave: (String) -> Unit,
    onStartCooking: (String) -> Unit,
    onAddToMealPlan: (Recipe) -> Unit,
    onInspectFlavour: (String) -> Unit
) {
    BackHandler { onBack() }

    val scored = state.allScoredIncludingFiltered.find { it.recipe.id == state.selectedRecipeId }
        ?: state.scoredRecipes.firstOrNull()
        ?: return
    val recipe = scored.recipe
    val isSaved = recipe.id in state.savedRecipeIds
    val servingRatio = state.selectedServings.toDouble() / recipe.baseServings.coerceAtLeast(1)

    val imageRes = when (recipe.drawableResName) {
        "img_recipe_paneer_tikka" -> R.drawable.img_recipe_paneer_tikka
        "img_recipe_saffron_biryani" -> R.drawable.img_recipe_saffron_biryani
        else -> R.drawable.img_hero_gastronomy
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DrishyamColors.Background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(DrishyamColors.Background.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DrishyamColors.PrimaryText)
                    }
                    IconButton(
                        onClick = { onToggleSave(recipe.id) },
                        modifier = Modifier.background(DrishyamColors.Background.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Recipe",
                            tint = if (isSaved) DrishyamColors.PrimaryOrange else DrishyamColors.PrimaryText
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = "${recipe.cuisine.uppercase()} • ${recipe.dietaryClassification.label.uppercase()} • ${recipe.totalMinutes} MIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = DrishyamColors.PrimaryOrange
                )
                Spacer(Modifier.height(4.dp))
                Text(recipe.title, style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(6.dp))
                Text(recipe.subtitle, style = MaterialTheme.typography.bodyLarge, color = DrishyamColors.SecondaryText)

                // Allergen Alert if any
                if (scored.allergenMatches.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        color = DrishyamColors.DangerDim,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DrishyamColors.Danger)
                    ) {
                        Text(
                            text = "HARD ALLERGEN WARNING: Contains ${scored.allergenMatches.joinToString(", ")}",
                            style = MaterialTheme.typography.labelMedium,
                            color = DrishyamColors.Danger,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Primary Actions: Start Cooking Mode + Add to Meal Plan
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onStartCooking(recipe.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DrishyamColors.PrimaryOrange,
                            contentColor = DrishyamColors.Background
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_cooking_mode_button")
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Start Cooking Mode")
                    }
                    OutlinedButton(
                        onClick = { onAddToMealPlan(recipe) },
                        border = BorderStroke(1.dp, DrishyamColors.SecondaryGold)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = DrishyamColors.SecondaryGold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Plan Meal", color = DrishyamColors.SecondaryGold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Computational Gastronomy Equation Breakdown Card (Part 17)
                Surface(
                    color = DrishyamColors.Surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "COMPUTATIONAL GASTRONOMY SCORE: ${(scored.gastronomyScore * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = DrishyamColors.SecondaryGold
                        )
                        Text(
                            text = "0.30×Core(${(scored.coreCoverage * 100).roundToInt()}%) + 0.20×Counter(${(scored.counterUtilisation * 100).roundToInt()}%) + 0.20×Flavour(${(scored.flavourCoherence * 100).roundToInt()}%) + 0.30×GapFit(${(scored.gapFit * 100).roundToInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = DrishyamColors.PrimaryText
                        )
                        Text(
                            text = scored.whyRecommended,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Serving Selector & Scaled Nutrition Impact (Part 13.4 & Part 14)
                Surface(
                    color = DrishyamColors.Surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Portion & Nutritional Impact", style = MaterialTheme.typography.titleMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onAdjustServings(state.selectedServings - 1) }) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease Servings", tint = DrishyamColors.PrimaryOrange)
                                }
                                Text(
                                    text = "${state.selectedServings} Servings",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                IconButton(onClick = { onAdjustServings(state.selectedServings + 1) }) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase Servings", tint = DrishyamColors.PrimaryOrange)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        val n = recipe.nutritionPerServing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Calories: ${(n.calories * servingRatio).roundToInt()} kcal", style = MaterialTheme.typography.labelMedium, color = DrishyamColors.PrimaryOrange)
                            Text("Protein: ${(n.proteinG * servingRatio).roundToInt()}g", style = MaterialTheme.typography.labelMedium, color = DrishyamColors.SecondaryGold)
                            Text("Carbs: ${(n.carbsG * servingRatio).roundToInt()}g", style = MaterialTheme.typography.labelMedium)
                            Text("Fat: ${(n.fatG * servingRatio).roundToInt()}g", style = MaterialTheme.typography.labelMedium)
                            Text("Fibre: ${(n.fibreG * servingRatio).roundToInt()}g", style = MaterialTheme.typography.labelMedium, color = DrishyamColors.Success)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Ingredients (Available vs Missing)", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                recipe.ingredients.forEach { ing ->
                    val isAvailable = scored.availableIngredients.any { it.canonicalName == ing.canonicalName }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { onInspectFlavour(ing.canonicalName) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAvailable) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isAvailable) DrishyamColors.Success else DrishyamColors.Warning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = ing.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isAvailable) DrishyamColors.PrimaryText else DrishyamColors.Warning
                            )
                        }
                        Text(
                            text = "${(ing.quantity * servingRatio).roundToInt()} ${ing.unit} • Tap for FlavorDB",
                            style = MaterialTheme.typography.labelSmall,
                            color = DrishyamColors.SecondaryGold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("FlavorDB Molecular Bridges", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(recipe.flavourPairingNote, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun CookingModeScreen(
    state: DrishyamUiState,
    onBack: () -> Unit,
    onSelectStep: (Int) -> Unit,
    onToggleTimer: () -> Unit,
    onCompleteAndLogMeal: () -> Unit
) {
    BackHandler { onBack() }

    val view = LocalView.current
    DisposableEffect(state.keepScreenAwakeInCooking) {
        view.keepScreenOn = state.keepScreenAwakeInCooking
        onDispose { view.keepScreenOn = false }
    }

    val recipe = FoodoscopeSeedCatalog.recipes.find { it.id == state.selectedRecipeId }
        ?: FoodoscopeSeedCatalog.recipes.first()
    val currentStep = recipe.steps.getOrElse(state.cookingStepIndex) { recipe.steps.first() }
    val servingRatio = state.selectedServings.toDouble() / recipe.baseServings.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DrishyamColors.Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Cooking Mode", tint = DrishyamColors.PrimaryText)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "INTERACTIVE COOKING MODE • SCREEN AWAKE",
                        style = MaterialTheme.typography.labelSmall,
                        color = DrishyamColors.PrimaryOrange
                    )
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "Step ${state.cookingStepIndex + 1}/${recipe.steps.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = DrishyamColors.SecondaryGold
                )
            }

            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (state.cookingStepIndex + 1).toFloat() / recipe.steps.size.coerceAtLeast(1) },
                color = DrishyamColors.PrimaryOrange,
                trackColor = DrishyamColors.ElevatedSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(Modifier.height(24.dp))
            Surface(
                color = DrishyamColors.Surface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "STEP ${currentStep.stepNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        color = DrishyamColors.PrimaryOrange
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = currentStep.instruction,
                        style = MaterialTheme.typography.headlineLarge
                    )

                    if (currentStep.highlightedIngredients.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "SCALED STEP INGREDIENTS (${state.selectedServings} SERVINGS):",
                            style = MaterialTheme.typography.labelSmall,
                            color = DrishyamColors.SecondaryGold
                        )
                        Spacer(Modifier.height(6.dp))
                        recipe.ingredients.filter { ing ->
                            currentStep.highlightedIngredients.any { it.equals(ing.displayName, ignoreCase = true) }
                        }.forEach { ing ->
                            Text(
                                text = "• ${ing.displayName}: ${(ing.quantity * servingRatio).roundToInt()} ${ing.unit}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = DrishyamColors.PrimaryText
                            )
                        }
                    }

                    currentStep.scientificTip?.let { tip ->
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            color = DrishyamColors.ElevatedSurface,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Culinary Science Note: $tip",
                                style = MaterialTheme.typography.bodySmall,
                                color = DrishyamColors.SecondaryGold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Built-in Cooking Timer (Part 14)
            Surface(
                color = DrishyamColors.ElevatedSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DrishyamColors.PrimaryOrange.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("STEP PRECISION TIMER", style = MaterialTheme.typography.labelSmall, color = DrishyamColors.SecondaryText)
                        val mins = state.cookingTimerSecondsRemaining / 60
                        val secs = state.cookingTimerSecondsRemaining % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.displayMedium,
                            color = DrishyamColors.PrimaryOrange
                        )
                    }
                    Button(
                        onClick = onToggleTimer,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.cookingTimerRunning) DrishyamColors.Warning else DrishyamColors.PrimaryOrange,
                            contentColor = DrishyamColors.Background
                        ),
                        modifier = Modifier.testTag("cooking_timer_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (state.cookingTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (state.cookingTimerRunning) "Pause Timer" else "Start Timer")
                    }
                }
            }
        }

        // Step Navigation & Completion Logging
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onSelectStep(state.cookingStepIndex - 1) },
                enabled = state.cookingStepIndex > 0,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Previous")
            }

            if (state.cookingStepIndex < recipe.steps.lastIndex) {
                Button(
                    onClick = { onSelectStep(state.cookingStepIndex + 1) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DrishyamColors.PrimaryOrange,
                        contentColor = DrishyamColors.Background
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cooking_next_step_button")
                ) {
                    Text("Next Step")
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            } else {
                Button(
                    onClick = onCompleteAndLogMeal,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DrishyamColors.Success,
                        contentColor = DrishyamColors.Background
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("complete_cooking_log_meal_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Finish & Log Meal")
                }
            }
        }
    }
}
