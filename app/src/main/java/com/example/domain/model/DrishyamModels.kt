package com.example.domain.model

enum class UnitSystem(val label: String) {
    METRIC("Metric (kg, cm, g)"),
    IMPERIAL("Imperial (lb, ft/in, oz)")
}

enum class BiologicalSex(val label: String, val mifflinOffset: Int) {
    MALE("Male", 5),
    FEMALE("Female", -161),
    UNSPECIFIED("Prefer not to say / Average", -78)
}

enum class ActivityLevel(
    val title: String,
    val description: String,
    val multiplier: Double
) {
    SEDENTARY(
        "Sedentary",
        "Desk job, minimal planned exercise or walking",
        1.20
    ),
    LIGHTLY_ACTIVE(
        "Lightly Active",
        "Light training or brisk walking 1–3 days per week",
        1.375
    ),
    MODERATELY_ACTIVE(
        "Moderately Active",
        "Consistent training or active lifestyle 3–5 days per week",
        1.55
    ),
    VERY_ACTIVE(
        "Very Active",
        "Hard athletic training or physical work 6–7 days per week",
        1.725
    ),
    EXTREMELY_ACTIVE(
        "Extremely Active",
        "Intense daily training or twice-a-day endurance/strength sessions",
        1.90
    )
}

enum class FitnessGoalType(
    val id: String,
    val title: String,
    val subtitle: String,
    val calorieDeltaFactor: Double,
    val proteinGramsPerKg: Double,
    val carbShare: Double,
    val fatShare: Double,
    val fibreTargetG: Int,
    val priorityNote: String,
    val cautionNote: String? = null
) {
    FAT_LOSS(
        id = "fat_loss",
        title = "Fat Loss / Cutting",
        subtitle = "Reduce body fat while preserving lean muscle mass with high satiety",
        calorieDeltaFactor = 0.80, // 20% sustainable deficit
        proteinGramsPerKg = 2.1,
        carbShare = 0.35,
        fatShare = 0.28,
        fibreTargetG = 34,
        priorityNote = "Prioritizes high-protein, fibre-rich, lower energy-density meals for sustainable satiety."
    ),
    LEAN_BULK(
        id = "lean_bulk",
        title = "Lean Bulk",
        subtitle = "Support hypertrophy with a controlled 10% surplus to minimize fat gain",
        calorieDeltaFactor = 1.10,
        proteinGramsPerKg = 2.0,
        carbShare = 0.48,
        fatShare = 0.25,
        fibreTargetG = 32,
        priorityNote = "Prioritizes nutrient-dense, high-protein meals and recovery carbohydrates."
    ),
    TRADITIONAL_BULK(
        id = "traditional_bulk",
        title = "Traditional Bulk",
        subtitle = "Steady weight and strength gain with a 18% energy surplus",
        calorieDeltaFactor = 1.18,
        proteinGramsPerKg = 1.8,
        carbShare = 0.50,
        fatShare = 0.28,
        fibreTargetG = 30,
        priorityNote = "Focuses on energy-dense, balanced whole-food recipes that fit higher calorie schedules."
    ),
    DIRTY_BULK(
        id = "dirty_bulk",
        title = "Dirty Bulk Preference",
        subtitle = "High-calorie gaining preference (+28% energy intake)",
        calorieDeltaFactor = 1.28,
        proteinGramsPerKg = 1.8,
        carbShare = 0.48,
        fatShare = 0.32,
        fibreTargetG = 26,
        priorityNote = "High energy density while still enforcing all allergen and mandatory dietary safety rules.",
        cautionNote = "Higher energy surplus can accelerate weight gain but carries greater risk of unwanted fat gain and reduced micronutrient density."
    ),
    MAINTENANCE(
        id = "maintenance",
        title = "Maintenance",
        subtitle = "Maintain current body weight with balanced energy and micronutrient diversity",
        calorieDeltaFactor = 1.00,
        proteinGramsPerKg = 1.6,
        carbShare = 0.45,
        fatShare = 0.30,
        fibreTargetG = 30,
        priorityNote = "Balances energy equilibrium, adequate protein, and broad phytochemical diversity."
    ),
    BODY_RECOMPOSITION(
        id = "body_recomposition",
        title = "Body Recomposition",
        subtitle = "Simultaneously improve lean mass ratio near maintenance (-5% energy, high protein)",
        calorieDeltaFactor = 0.95,
        proteinGramsPerKg = 2.2,
        carbShare = 0.40,
        fatShare = 0.26,
        fibreTargetG = 32,
        priorityNote = "Emphasizes elevated protein density and precision macro partitioning."
    ),
    ATHLETIC_PERFORMANCE(
        id = "athletic_performance",
        title = "Athletic Performance",
        subtitle = "Fuel training output, glycogen replenishment, and structured recovery (+8% energy)",
        calorieDeltaFactor = 1.08,
        proteinGramsPerKg = 1.9,
        carbShare = 0.54,
        fatShare = 0.24,
        fibreTargetG = 30,
        priorityNote = "Prioritizes carbohydrate availability, pre/post-workout timing, and recovery micronutrients. Not a medical or performance guarantee."
    ),
    GENERAL_HEALTH(
        id = "general_health",
        title = "General Health",
        subtitle = "Long-term metabolic wellness, gut fibre diversity, and whole-food culinary balance",
        calorieDeltaFactor = 1.00,
        proteinGramsPerKg = 1.4,
        carbShare = 0.46,
        fatShare = 0.30,
        fibreTargetG = 35,
        priorityNote = "Maximizes fibre, spice antioxidants, and balanced culinary variety."
    )
}

enum class DietaryLifestyle(val label: String, val description: String) {
    VEGETARIAN("Vegetarian", "Plant-based + dairy; excludes meat, poultry, fish, and eggs"),
    VEGAN("Vegan", "100% plant-based; excludes all animal and dairy products"),
    EGGETARIAN("Eggetarian", "Vegetarian diet including eggs"),
    PESCATARIAN("Pescatarian", "Plant-based, dairy, eggs, and fish/seafood; no land meat"),
    NON_VEGETARIAN("Non-Vegetarian", "Omnivorous; includes poultry, meat, seafood, dairy, and plants"),
    NONE("No Dietary Restrictions", "All culinary categories permitted")
}

enum class DietaryRestriction(val label: String) {
    DAIRY_FREE("Dairy-Free"),
    LACTOSE_FREE("Lactose-Free"),
    GLUTEN_FREE("Gluten-Free"),
    JAIN_FRIENDLY("Jain-Friendly (No Onion/Garlic/Root Vegetables)")
}

enum class AllergenType(val label: String, val triggerKeywords: List<String>) {
    PEANUTS("Peanuts", listOf("peanut", "groundnut", "arachis")),
    TREE_NUTS("Tree Nuts", listOf("cashew", "almond", "walnut", "pistachio", "hazelnut", "pecan")),
    MILK("Milk & Dairy", listOf("milk", "paneer", "ghee", "butter", "cream", "yogurt", "curd", "cheese", "whey")),
    EGGS("Eggs", listOf("egg", "albumen", "mayonnaise")),
    SOY("Soy", listOf("soy", "tofu", "edamame", "tempeh", "soya")),
    WHEAT_GLUTEN("Wheat & Gluten", listOf("wheat", "atta", "maida", "semolina", "suji", "bread", "roti", "naan", "barley", "rye")),
    SHELLFISH("Shellfish", listOf("prawn", "shrimp", "crab", "lobster", "mussel", "clam")),
    FISH("Fish", listOf("fish", "salmon", "rohu", "tuna", "cod", "anchovy", "pomfret")),
    SESAME("Sesame", listOf("sesame", "til", "tahini", "gingelly"))
}

enum class IngredientCategory(val label: String) {
    SPICES("Spices"),
    VEGETABLES("Vegetables"),
    FRUITS("Fruits"),
    GRAINS("Grains"),
    LEGUMES("Legumes"),
    DAIRY("Dairy"),
    MEAT_SEAFOOD("Meat & Seafood"),
    OILS("Oils"),
    CONDIMENTS("Condiments"),
    OTHER("Other")
}

enum class DetectionState(val label: String) {
    SEARCHING("Searching"),
    CANDIDATE("Candidate Detected"),
    STABILIZING("Stabilizing (1.0s lock)"),
    STABLE("Stable >= 75%"),
    AWAITING_CONFIRMATION("Awaiting Confirmation"),
    CONFIRMED("Confirmed in Pantry"),
    UNCERTAIN("Uncertain (< 75%)")
}

data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class ScannedCandidate(
    val trackId: String,
    val canonicalName: String,
    val displayName: String,
    val category: IngredientCategory,
    val confidence: Float,
    val boundingBox: NormalizedRect,
    val firstDetectedMs: Long,
    val lastSeenMs: Long,
    val accumulatedMs: Long,
    val state: DetectionState,
    val suggestedQuantity: Double = 100.0,
    val suggestedUnit: String = "g",
    val source: String = "android_scanner"
)

data class PantryItem(
    val id: String,
    val canonicalName: String,
    val displayName: String,
    val aliases: List<String>,
    val category: IngredientCategory,
    val quantity: Double,
    val unit: String,
    val source: String, // "android_scanner", "deep_inspect", "manual", "web_sync"
    val confidence: Float,
    val confirmed: Boolean,
    val isAvailable: Boolean = true,
    val expiryDateIso: String? = null,
    val lowStockThreshold: Double = 25.0,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = isAvailable && quantity <= lowStockThreshold
}

data class UserProfile(
    val userId: String = "usr_drishyam_01",
    val email: String = "chef@drishyam.ai",
    val displayName: String = "Aarav Sharma",
    val age: Int = 28,
    val heightCm: Double = 176.0,
    val weightKg: Double = 74.0,
    val targetWeightKg: Double? = 72.0,
    val biologicalSex: BiologicalSex = BiologicalSex.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val primaryGoal: FitnessGoalType = FitnessGoalType.LEAN_BULK,
    val secondaryGoals: Set<FitnessGoalType> = setOf(FitnessGoalType.ATHLETIC_PERFORMANCE),
    val isGoalPaused: Boolean = false,
    val dietaryLifestyle: DietaryLifestyle = DietaryLifestyle.VEGETARIAN,
    val dietaryRestrictions: Set<DietaryRestriction> = emptySet(),
    val allergies: Set<AllergenType> = emptySet(),
    val customAllergens: List<String> = emptyList(),
    val excludedIngredients: List<String> = emptyList(),
    val preferredCuisines: Set<String> = setOf("North Indian", "South Indian", "Mediterranean"),
    val preferredTastes: Set<String> = setOf("Spicy", "Savoury", "Smoky"),
    val spiceTolerance: String = "Medium-High",
    val maxCookingMinutes: Int = 40,
    val skillLevel: String = "Intermediate",
    val availableUtensils: Set<String> = setOf("Gas stove", "Pan", "Pot", "Pressure cooker", "Blender"),
    val customCalorieOverride: Int? = null,
    val customProteinOverrideG: Int? = null,
    val customCarbsOverrideG: Int? = null,
    val customFatOverrideG: Int? = null,
    val onboardingCompleted: Boolean = true
)

data class NutritionTargets(
    val bmrKcal: Int,
    val tdeeKcal: Int,
    val maintenanceKcal: Int,
    val targetCalories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val fibreG: Int,
    val equationUsed: String,
    val assumptionsSummary: String,
    val isManualOverride: Boolean,
    val safetyWarnings: List<String>
)

data class RecipeIngredient(
    val canonicalName: String,
    val displayName: String,
    val quantity: Double,
    val unit: String,
    val isCore: Boolean = true,
    val uncertainAllergenWarning: String? = null
)

data class RecipeStep(
    val stepNumber: Int,
    val instruction: String,
    val durationSeconds: Int? = null,
    val highlightedIngredients: List<String> = emptyList(),
    val scientificTip: String? = null
)

data class RecipeNutrition(
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fibreG: Double,
    val sugarG: Double,
    val saturatedFatG: Double,
    val sodiumMg: Int,
    val ironMg: Double,
    val calciumMg: Int,
    val vitaminCMg: Double,
    val potassiumMg: Int,
    val dataSourceType: String = "Foodoscope RecipeDB + USDA/IFCT Calculated"
)

data class Recipe(
    val id: String,
    val title: String,
    val subtitle: String,
    val cuisine: String,
    val mealTypes: List<String>,
    val dietaryClassification: DietaryLifestyle,
    val isDairyFree: Boolean,
    val isGlutenFree: Boolean,
    val isJainFriendly: Boolean,
    val prepMinutes: Int,
    val cookMinutes: Int,
    val difficulty: String,
    val baseServings: Int,
    val requiredUtensils: List<String>,
    val tasteTags: List<String>,
    val drawableResName: String,
    val ingredients: List<RecipeIngredient>,
    val steps: List<RecipeStep>,
    val nutritionPerServing: RecipeNutrition,
    val dominantCompounds: List<String>,
    val flavourPairingNote: String
) {
    val totalMinutes: Int get() = prepMinutes + cookMinutes
}

data class ScoredRecipe(
    val recipe: Recipe,
    val gastronomyScore: Double, // 0.0..1.0 exact equation
    val coreCoverage: Double,
    val counterUtilisation: Double,
    val flavourCoherence: Double,
    val gapFit: Double,
    val goalCompatibilityScore: Double,
    val combinedRankScore: Double,
    val availableIngredients: List<RecipeIngredient>,
    val missingIngredients: List<RecipeIngredient>,
    val suggestedUnlockIngredient: String?,
    val whyRecommended: String,
    val allergenMatches: List<String>,
    val crossContaminationNote: String?
)

data class UnlockSuggestion(
    val missingCanonicalName: String,
    val displayName: String,
    val category: IngredientCategory,
    val unlockedRecipes: List<String>,
    val averageScoreBoost: Double,
    val whyItMatters: String,
    val sharedCompounds: List<String>,
    val possibleAlternatives: List<String>
)

data class AromaCompound(
    val id: String,
    val name: String,
    val chemicalFormula: String,
    val functionalGroup: String,
    val aromaDescriptors: List<String>,
    val flavorDbId: String
)

data class IngredientFlavourProfile(
    val canonicalName: String,
    val displayName: String,
    val category: IngredientCategory,
    val botanicalFamily: String,
    val aliases: List<String>,
    val keyCompounds: List<AromaCompound>,
    val complementaryIngredients: List<String>,
    val culinaryRole: String
)

data class SubstitutionCandidate(
    val targetIngredient: String,
    val substituteCanonicalName: String,
    val substituteDisplayName: String,
    val sharedCompounds: List<AromaCompound>,
    val compoundOverlapRatio: Double,
    val flavourSimilarityDescription: String,
    val expectedTasteDifference: String,
    val bestForCuisines: List<String>,
    val ratioAdjustment: String,
    val inPantry: Boolean
)

enum class MealSlot(val label: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACKS("Snacks"),
    PRE_WORKOUT("Pre-Workout"),
    POST_WORKOUT("Post-Workout")
}

data class MealLogEntry(
    val id: String,
    val dateIso: String, // YYYY-MM-DD
    val mealSlot: MealSlot,
    val recipeId: String?,
    val title: String,
    val servings: Double,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fibreG: Double,
    val sugarG: Double,
    val saturatedFatG: Double,
    val sodiumMg: Int,
    val notes: String = "",
    val sourceLabel: String = "RecipeDB Calculated",
    val timestampMs: Long = System.currentTimeMillis()
)

data class PlannedMealEntry(
    val id: String,
    val dayIndex: Int, // 0 = Mon .. 6 = Sun
    val dayName: String,
    val mealSlot: MealSlot,
    val recipeId: String?,
    val title: String,
    val servings: Double,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)

data class ShoppingListItem(
    val id: String,
    val canonicalName: String,
    val displayName: String,
    val category: IngredientCategory,
    val quantity: Double,
    val unit: String,
    val reason: String, // "Missing for Paneer Tikka", "Low Stock in Pantry", "Weekly Meal Plan"
    val isPurchased: Boolean = false,
    val alternativeSuggestion: String? = null
)

data class SyncOperation(
    val idempotencyKey: String,
    val entityType: String,
    val entityId: String,
    val action: String,
    val timestampMs: Long,
    val synced: Boolean
)
