package com.example.domain.usecase

import com.example.domain.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Normalizes culinary ingredient names and aliases across all supported classes
 * from Part 11.3 and Part 12.
 */
object IngredientAliasNormalizer {

    data class CanonicalMeta(
        val canonicalName: String,
        val displayName: String,
        val category: IngredientCategory,
        val aliases: List<String>,
        val defaultUnit: String
    )

    val supportedCatalog: List<CanonicalMeta> = listOf(
        // Powders and granules
        CanonicalMeta("salt", "Salt", IngredientCategory.SPICES, listOf("namak", "sea salt", "table salt", "rock salt"), "g"),
        CanonicalMeta("turmeric_powder", "Turmeric Powder", IngredientCategory.SPICES, listOf("turmeric", "haldi", "curcuma"), "g"),
        CanonicalMeta("red_chilli_powder", "Red Chilli Powder", IngredientCategory.SPICES, listOf("chilli powder", "lal mirch", "cayenne", "kashmiri mirch"), "g"),
        CanonicalMeta("coriander_powder", "Coriander Powder", IngredientCategory.SPICES, listOf("dhania powder", "ground coriander"), "g"),
        CanonicalMeta("black_pepper_powder", "Black Pepper Powder", IngredientCategory.SPICES, listOf("kali mirch powder", "ground pepper"), "g"),
        CanonicalMeta("garam_masala", "Garam Masala", IngredientCategory.SPICES, listOf("spice blend", "punjabi garam masala"), "g"),
        // Whole spices and aromatics
        CanonicalMeta("cumin_seeds", "Cumin Seeds", IngredientCategory.SPICES, listOf("cumin", "jeera", "zeera"), "g"),
        CanonicalMeta("mustard_seeds", "Mustard Seeds", IngredientCategory.SPICES, listOf("rai", "sarson", "black mustard"), "g"),
        CanonicalMeta("cloves", "Cloves", IngredientCategory.SPICES, listOf("laung", "clove"), "g"),
        CanonicalMeta("cardamom", "Cardamom", IngredientCategory.SPICES, listOf("elaichi", "green cardamom"), "g"),
        CanonicalMeta("cinnamon_sticks", "Cinnamon Sticks", IngredientCategory.SPICES, listOf("cinnamon", "dalchini"), "g"),
        CanonicalMeta("star_anise", "Star Anise", IngredientCategory.SPICES, listOf("chakri phool", "badian"), "g"),
        CanonicalMeta("bay_leaf", "Bay Leaf", IngredientCategory.SPICES, listOf("tej patta", "indian bay leaf"), "g"),
        CanonicalMeta("black_peppercorns", "Black Peppercorns", IngredientCategory.SPICES, listOf("peppercorns", "whole kali mirch"), "g"),
        CanonicalMeta("coriander_seeds", "Coriander Seeds", IngredientCategory.SPICES, listOf("sabut dhania", "whole coriander"), "g"),
        // Fresh produce
        CanonicalMeta("onion", "Onion", IngredientCategory.VEGETABLES, listOf("pyaaz", "red onion", "yellow onion"), "g"),
        CanonicalMeta("tomato", "Tomato", IngredientCategory.VEGETABLES, listOf("tamatar", "roma tomato"), "g"),
        CanonicalMeta("garlic", "Garlic", IngredientCategory.VEGETABLES, listOf("lehsun", "garlic cloves"), "g"),
        CanonicalMeta("ginger", "Ginger", IngredientCategory.VEGETABLES, listOf("adrak", "fresh ginger"), "g"),
        CanonicalMeta("green_chilli", "Green Chilli", IngredientCategory.VEGETABLES, listOf("hari mirch", "chilli", "serrano"), "g"),
        CanonicalMeta("lemon", "Lemon", IngredientCategory.FRUITS, listOf("nimbu", "lime", "lemon juice"), "pcs"),
        CanonicalMeta("coriander_leaves", "Coriander Leaves", IngredientCategory.VEGETABLES, listOf("cilantro", "hara dhania", "fresh coriander"), "g"),
        CanonicalMeta("potato", "Potato", IngredientCategory.VEGETABLES, listOf("aloo", "potatoes"), "g"),
        CanonicalMeta("carrot", "Carrot", IngredientCategory.VEGETABLES, listOf("gajar", "carrots"), "g"),
        CanonicalMeta("capsicum", "Capsicum", IngredientCategory.VEGETABLES, listOf("bell pepper", "shimla mirch", "green pepper"), "g"),
        CanonicalMeta("spinach", "Spinach", IngredientCategory.VEGETABLES, listOf("palak", "baby spinach"), "g"),
        // Staples
        CanonicalMeta("rice", "Basmati Rice", IngredientCategory.GRAINS, listOf("chawal", "basmati", "white rice"), "g"),
        CanonicalMeta("wheat_flour", "Wheat Flour", IngredientCategory.GRAINS, listOf("atta", "whole wheat flour", "chapati flour"), "g"),
        CanonicalMeta("lentils", "Red & Yellow Lentils", IngredientCategory.LEGUMES, listOf("dal", "toor dal", "moong dal", "masoor dal"), "g"),
        CanonicalMeta("chickpeas", "Chickpeas", IngredientCategory.LEGUMES, listOf("chana", "kabuli chana", "garbanzo"), "g"),
        CanonicalMeta("kidney_beans", "Kidney Beans", IngredientCategory.LEGUMES, listOf("rajma", "red kidney beans"), "g"),
        // Dairy and protein
        CanonicalMeta("paneer", "Paneer", IngredientCategory.DAIRY, listOf("indian cottage cheese", "cottage cheese"), "g"),
        CanonicalMeta("yogurt", "Greek / Plain Yogurt", IngredientCategory.DAIRY, listOf("dahi", "curd", "hung curd"), "g"),
        CanonicalMeta("ghee", "Clarified Butter (Ghee)", IngredientCategory.OILS, listOf("desi ghee", "butter"), "ml"),
        CanonicalMeta("eggs", "Eggs", IngredientCategory.MEAT_SEAFOOD, listOf("anda", "whole egg"), "pcs"),
        CanonicalMeta("chicken_breast", "Chicken Breast", IngredientCategory.MEAT_SEAFOOD, listOf("chicken", "murgh"), "g")
    )

    fun resolve(rawInput: String): CanonicalMeta {
        val cleaned = rawInput.trim().lowercase().replace("_", " ")
        for (meta in supportedCatalog) {
            if (meta.canonicalName.replace("_", " ") == cleaned ||
                meta.displayName.lowercase() == cleaned ||
                meta.aliases.any { it.lowercase() == cleaned || cleaned.contains(it.lowercase()) }
            ) {
                return meta
            }
        }
        val fallbackCanonical = cleaned.replace(Regex("[^a-z0-9]+"), "_").trim('_')
        val fallbackDisplay = rawInput.trim().replaceFirstChar { it.uppercase() }
        return CanonicalMeta(
            canonicalName = fallbackCanonical.ifEmpty { "custom_ingredient" },
            displayName = fallbackDisplay.ifEmpty { "Custom Ingredient" },
            category = IngredientCategory.OTHER,
            aliases = emptyList(),
            defaultUnit = "g"
        )
    }
}

/**
 * Authoritative Mifflin-St Jeor energy & macronutrient calculation engine (Part 8 & Part 10).
 */
object NutritionEngine {

    fun calculateTargets(profile: UserProfile): NutritionTargets {
        val safeWeight = profile.weightKg.coerceIn(30.0, 250.0)
        val safeHeight = profile.heightCm.coerceIn(120.0, 230.0)
        val safeAge = profile.age.coerceIn(14, 100)

        // Mifflin-St Jeor Equation:
        // BMR = (10 * weight_kg) + (6.25 * height_cm) - (5 * age_y) + sex_offset
        val bmr = ((10.0 * safeWeight) + (6.25 * safeHeight) - (5.0 * safeAge) + profile.biologicalSex.mifflinOffset)
            .roundToInt()
        val tdee = (bmr * profile.activityLevel.multiplier).roundToInt()

        val activeGoal = if (profile.isGoalPaused) FitnessGoalType.MAINTENANCE else profile.primaryGoal
        val calculatedCalories = (tdee * activeGoal.calorieDeltaFactor).roundToInt()

        val finalCalories = profile.customCalorieOverride ?: calculatedCalories

        // Calculate evidence-informed macros
        val defaultProteinG = (safeWeight * activeGoal.proteinGramsPerKg).roundToInt()
        val finalProteinG = profile.customProteinOverrideG ?: defaultProteinG

        val proteinCalories = finalProteinG * 4
        val remainingCalories = max(400, finalCalories - proteinCalories)

        val relativeFatWeight = activeGoal.fatShare / (activeGoal.carbShare + activeGoal.fatShare)
        val defaultFatG = ((remainingCalories * relativeFatWeight) / 9.0).roundToInt()
        val finalFatG = profile.customFatOverrideG ?: defaultFatG

        val defaultCarbsG = (max(200, finalCalories - (finalProteinG * 4) - (finalFatG * 9)) / 4.0).roundToInt()
        val finalCarbsG = profile.customCarbsOverrideG ?: defaultCarbsG

        val warnings = mutableListOf<String>()
        if (finalCalories < bmr * 0.82 || finalCalories < 1200) {
            warnings.add("Configured calorie target ($finalCalories kcal) is below safe metabolic baseline ($bmr kcal BMR). Extreme restriction is not recommended.")
        }
        if (finalCalories > tdee * 1.40) {
            warnings.add("Configured calorie target ($finalCalories kcal) exceeds TDEE by over 40%, increasing rapid fat accumulation risk.")
        }
        if (finalProteinG < (safeWeight * 0.8).roundToInt()) {
            warnings.add("Protein target (${finalProteinG}g) is below the 0.8g/kg baseline required for lean tissue preservation.")
        }
        activeGoal.cautionNote?.let { warnings.add(it) }

        return NutritionTargets(
            bmrKcal = bmr,
            tdeeKcal = tdee,
            maintenanceKcal = tdee,
            targetCalories = finalCalories,
            proteinG = finalProteinG,
            carbsG = finalCarbsG,
            fatG = finalFatG,
            fibreG = activeGoal.fibreTargetG,
            equationUsed = "Mifflin-St Jeor (10×W + 6.25×H − 5×A + ${profile.biologicalSex.mifflinOffset}) × ${profile.activityLevel.multiplier} (${profile.activityLevel.title})",
            assumptionsSummary = "Estimated from ${safeWeight}kg, ${safeHeight}cm, age $safeAge, goal: ${activeGoal.title}. All values are physiological estimates.",
            isManualOverride = profile.customCalorieOverride != null ||
                profile.customProteinOverrideG != null ||
                profile.customCarbsOverrideG != null ||
                profile.customFatOverrideG != null,
            safetyWarnings = warnings
        )
    }

    fun kgToLb(kg: Double): Double = (kg * 2.20462 * 10.0).roundToInt() / 10.0
    fun lbToKg(lb: Double): Double = (lb / 2.20462 * 10.0).roundToInt() / 10.0
    fun cmToFeetInches(cm: Double): Pair<Int, Int> {
        val totalInches = (cm / 2.54).roundToInt()
        return (totalInches / 12) to (totalInches % 12)
    }
}

/**
 * DRISHYAM Computational Gastronomy Recommendation & Ranking Engine (Part 13 & Part 17).
 *
 * Preserves the exact formula:
 * score = 0.30 * core_coverage + 0.20 * counter_utilisation + 0.20 * flavour_coherence + 0.30 * gap_fit
 */
object ComputationalGastronomyEngine {

    fun detectAllergens(recipe: Recipe, profile: UserProfile): List<String> {
        val matched = mutableListOf<String>()
        val ingredientNames = recipe.ingredients.map {
            "${it.canonicalName} ${it.displayName}".lowercase()
        }
        for (allergen in profile.allergies) {
            val hit = ingredientNames.any { ing ->
                allergen.triggerKeywords.any { kw -> ing.contains(kw) }
            }
            if (hit) matched.add(allergen.label)
        }
        for (custom in profile.customAllergens) {
            val clean = custom.trim().lowercase()
            if (clean.isNotEmpty() && ingredientNames.any { it.contains(clean) }) {
                matched.add("Custom Allergen: $custom")
            }
        }
        return matched.distinct()
    }

    fun passesMandatoryDietaryConstraints(recipe: Recipe, profile: UserProfile): Boolean {
        // 1. Hard Allergy Constraint — NEVER rank recipes with declared allergens in recommendations
        if (detectAllergens(recipe, profile).isNotEmpty()) return false

        // 2. Custom Ingredient Exclusions
        val excludedLower = profile.excludedIngredients.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        if (excludedLower.isNotEmpty()) {
            val hasExcluded = recipe.ingredients.any { ing ->
                excludedLower.any { ex ->
                    ing.canonicalName.lowercase().contains(ex) || ing.displayName.lowercase().contains(ex)
                }
            }
            if (hasExcluded) return false
        }

        // 3. Dietary Lifestyle
        val dietAllowed = when (profile.dietaryLifestyle) {
            DietaryLifestyle.VEGAN -> recipe.dietaryClassification == DietaryLifestyle.VEGAN
            DietaryLifestyle.VEGETARIAN -> recipe.dietaryClassification in setOf(
                DietaryLifestyle.VEGAN,
                DietaryLifestyle.VEGETARIAN
            )
            DietaryLifestyle.EGGETARIAN -> recipe.dietaryClassification in setOf(
                DietaryLifestyle.VEGAN,
                DietaryLifestyle.VEGETARIAN,
                DietaryLifestyle.EGGETARIAN
            )
            DietaryLifestyle.PESCATARIAN -> recipe.dietaryClassification in setOf(
                DietaryLifestyle.VEGAN,
                DietaryLifestyle.VEGETARIAN,
                DietaryLifestyle.EGGETARIAN,
                DietaryLifestyle.PESCATARIAN
            )
            DietaryLifestyle.NON_VEGETARIAN, DietaryLifestyle.NONE -> true
        }
        if (!dietAllowed) return false

        // 4. Additional Dietary Restrictions
        for (restriction in profile.dietaryRestrictions) {
            when (restriction) {
                DietaryRestriction.DAIRY_FREE, DietaryRestriction.LACTOSE_FREE -> {
                    if (!recipe.isDairyFree) return false
                }
                DietaryRestriction.GLUTEN_FREE -> {
                    if (!recipe.isGlutenFree) return false
                }
                DietaryRestriction.JAIN_FRIENDLY -> {
                    if (!recipe.isJainFriendly) return false
                }
            }
        }

        return true
    }

    fun scoreRecipe(
        recipe: Recipe,
        pantryItems: List<PantryItem>,
        profile: UserProfile,
        targets: NutritionTargets,
        flavourProfiles: Map<String, IngredientFlavourProfile>
    ): ScoredRecipe {
        val availablePantrySet = pantryItems
            .filter { it.confirmed && it.isAvailable && it.quantity > 0.0 }
            .map { it.canonicalName }
            .toSet()

        val availableIngredients = recipe.ingredients.filter { it.canonicalName in availablePantrySet }
        val missingIngredients = recipe.ingredients.filter { it.canonicalName !in availablePantrySet }

        val coreIngredients = recipe.ingredients.filter { it.isCore }.ifEmpty { recipe.ingredients }
        val availableCoreCount = coreIngredients.count { it.canonicalName in availablePantrySet }

        // 1. core_coverage (0..1): Fraction of core recipe ingredients currently in the user's pantry
        val coreCoverage = (availableCoreCount.toDouble() / max(1, coreIngredients.size)).coerceIn(0.0, 1.0)

        // 2. counter_utilisation (0..1): How effectively this recipe utilizes the user's active pantry
        val counterUtilisation = if (availablePantrySet.isEmpty()) {
            0.0
        } else {
            (availableIngredients.size.toDouble() / min(availablePantrySet.size, recipe.ingredients.size).coerceAtLeast(1))
                .coerceIn(0.0, 1.0)
        }

        // 3. flavour_coherence (0..1): Shared aroma compound bridge density from FlavorDB
        val compoundCounts = mutableMapOf<String, Int>()
        recipe.ingredients.forEach { ing ->
            flavourProfiles[ing.canonicalName]?.keyCompounds?.forEach { comp ->
                compoundCounts[comp.id] = (compoundCounts[comp.id] ?: 0) + 1
            }
        }
        val sharedBridges = compoundCounts.values.count { it >= 2 }
        val flavourCoherence = (0.45 + (sharedBridges * 0.14)).coerceIn(0.0, 1.0)

        // 4. gap_fit (0..1): Feasibility of missing ingredients (1.0 when 0 missing, decays smoothly)
        val gapFit = when (missingIngredients.size) {
            0 -> 1.00
            1 -> 0.82
            2 -> 0.62
            3 -> 0.42
            else -> max(0.15, 1.0 - (missingIngredients.size.toDouble() / recipe.ingredients.size.coerceAtLeast(1)))
        }.coerceIn(0.0, 1.0)

        // Exact DRISHYAM Computational Gastronomy Score
        val gastronomyScore = (
            0.30 * coreCoverage +
            0.20 * counterUtilisation +
            0.20 * flavourCoherence +
            0.30 * gapFit
        ).coerceIn(0.0, 1.0)

        // Goal & Nutrition Compatibility Score (0..1)
        val idealMealCalories = targets.targetCalories / 3.0
        val calRatioDiff = kotlin.math.abs(recipe.nutritionPerServing.calories - idealMealCalories) / idealMealCalories.coerceAtLeast(300.0)
        val calSuitability = (1.0 - (calRatioDiff * 0.7)).coerceIn(0.25, 1.0)

        val idealMealProtein = targets.proteinG / 3.0
        val proteinSuitability = (recipe.nutritionPerServing.proteinG / idealMealProtein.coerceAtLeast(18.0)).coerceIn(0.3, 1.0)

        val timeSuitability = if (recipe.totalMinutes <= profile.maxCookingMinutes) 1.0 else 0.65
        val cuisineBonus = if (recipe.cuisine in profile.preferredCuisines) 1.0 else 0.82

        val goalCompatibilityScore = (
            0.40 * proteinSuitability +
            0.30 * calSuitability +
            0.15 * timeSuitability +
            0.15 * cuisineBonus
        ).coerceIn(0.0, 1.0)

        val combinedRankScore = (0.65 * gastronomyScore + 0.35 * goalCompatibilityScore).coerceIn(0.0, 1.0)

        val allergens = detectAllergens(recipe, profile)
        val suggestedUnlock = missingIngredients.firstOrNull { it.isCore }?.displayName
            ?: missingIngredients.firstOrNull()?.displayName

        val whyRecommended = buildString {
            append("Uses ${availableIngredients.size}/${recipe.ingredients.size} pantry ingredients ")
            append("(${(gastronomyScore * 100).roundToInt()}% gastronomy score), ")
            append("delivers ${recipe.nutritionPerServing.proteinG.roundToInt()}g protein & ${recipe.nutritionPerServing.calories} kcal ")
            append("aligned with your ${profile.primaryGoal.title} target, ")
            append("and bridges ${recipe.dominantCompounds.take(2).joinToString(" & ")} aroma networks.")
        }

        return ScoredRecipe(
            recipe = recipe,
            gastronomyScore = gastronomyScore,
            coreCoverage = coreCoverage,
            counterUtilisation = counterUtilisation,
            flavourCoherence = flavourCoherence,
            gapFit = gapFit,
            goalCompatibilityScore = goalCompatibilityScore,
            combinedRankScore = combinedRankScore,
            availableIngredients = availableIngredients,
            missingIngredients = missingIngredients,
            suggestedUnlockIngredient = suggestedUnlock,
            whyRecommended = whyRecommended,
            allergenMatches = allergens,
            crossContaminationNote = if (recipe.ingredients.any { it.uncertainAllergenWarning != null }) {
                "Verify packaged spice blends or dairy processing lines for trace cross-contact."
            } else null
        )
    }

    fun rankRecommendations(
        allRecipes: List<Recipe>,
        pantryItems: List<PantryItem>,
        profile: UserProfile,
        targets: NutritionTargets,
        flavourProfiles: Map<String, IngredientFlavourProfile>
    ): List<ScoredRecipe> {
        return allRecipes
            .filter { passesMandatoryDietaryConstraints(it, profile) }
            .map { scoreRecipe(it, pantryItems, profile, targets, flavourProfiles) }
            .sortedByDescending { it.combinedRankScore }
    }

    /**
     * Part 17.2: Identifies the single missing ingredient that most improves
     * recipe feasibility, flavour compatibility, and nutritional suitability.
     */
    fun computeUnlockSuggestion(
        eligibleRecipes: List<ScoredRecipe>,
        flavourProfiles: Map<String, IngredientFlavourProfile>
    ): UnlockSuggestion? {
        val missingFrequency = mutableMapOf<String, MutableList<ScoredRecipe>>()
        for (scored in eligibleRecipes) {
            if (scored.missingIngredients.isNotEmpty() && scored.missingIngredients.size <= 3) {
                for (missing in scored.missingIngredients) {
                    missingFrequency.getOrPut(missing.canonicalName) { mutableListOf() }.add(scored)
                }
            }
        }
        val bestEntry = missingFrequency.entries
            .maxByOrNull { (_, list) -> list.size * 10 + list.sumOf { it.goalCompatibilityScore } }
            ?: return null

        val canonical = bestEntry.key
        val affected = bestEntry.value
        val meta = IngredientAliasNormalizer.resolve(canonical)
        val profile = flavourProfiles[canonical]

        return UnlockSuggestion(
            missingCanonicalName = canonical,
            displayName = meta.displayName,
            category = meta.category,
            unlockedRecipes = affected.map { it.recipe.title },
            averageScoreBoost = 0.24,
            whyItMatters = "Adding ${meta.displayName} immediately completes or significantly boosts ${affected.size} goal-compatible recipes (${affected.take(2).joinToString(", ") { it.recipe.title }}) and bridges key volatile flavour molecules.",
            sharedCompounds = profile?.keyCompounds?.map { it.name } ?: listOf("Terpenes", "Phenolic Aromatics"),
            possibleAlternatives = profile?.complementaryIngredients?.take(2) ?: listOf("Cumin Seeds", "Coriander Leaves")
        )
    }
}
