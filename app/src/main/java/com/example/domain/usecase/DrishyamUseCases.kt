package com.example.domain.usecase

import com.example.domain.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Normalizes culinary ingredient names, regional aliases, unit conversions,
 * and authoritative per-100g IFCT/USDA nutritional reference values across all
 * supported culinary classes (Part 10.4, Part 11.3, Part 12).
 */
object IngredientAliasNormalizer {

    data class CanonicalMeta(
        val canonicalName: String,
        val displayName: String,
        val category: IngredientCategory,
        val aliases: List<String>,
        val defaultUnit: String,
        val nutrientPer100g: IngredientNutrientProfile
    )

    val supportedCatalog: List<CanonicalMeta> = listOf(
        // Powders & Granules
        CanonicalMeta(
            "salt", "Salt", IngredientCategory.SPICES,
            listOf("namak", "sea salt", "table salt", "rock salt", "sendha namak"), "g",
            IngredientNutrientProfile("salt", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 38758.0, 0.3, 24.0, 0.0, 8.0, 5.0)
        ),
        CanonicalMeta(
            "turmeric_powder", "Turmeric Powder", IngredientCategory.SPICES,
            listOf("turmeric", "haldi", "curcuma", "manjal"), "g",
            IngredientNutrientProfile("turmeric_powder", 312.0, 9.7, 67.1, 3.3, 22.7, 3.2, 1.8, 27.0, 55.0, 168.0, 0.7, 2080.0, 5.0)
        ),
        CanonicalMeta(
            "red_chilli_powder", "Red Chilli Powder", IngredientCategory.SPICES,
            listOf("chilli powder", "lal mirch", "cayenne", "kashmiri mirch"), "g",
            IngredientNutrientProfile("red_chilli_powder", 282.0, 13.5, 49.7, 14.3, 34.8, 7.2, 2.5, 1640.0, 17.3, 148.0, 0.8, 1950.0, 5.0)
        ),
        CanonicalMeta(
            "coriander_powder", "Coriander Powder", IngredientCategory.SPICES,
            listOf("dhania powder", "ground coriander"), "g",
            IngredientNutrientProfile("coriander_powder", 298.0, 12.4, 55.0, 17.8, 41.9, 0.9, 1.0, 35.0, 16.3, 709.0, 21.0, 1267.0, 5.0)
        ),
        CanonicalMeta(
            "black_pepper_powder", "Black Pepper Powder", IngredientCategory.SPICES,
            listOf("kali mirch powder", "ground pepper"), "g",
            IngredientNutrientProfile("black_pepper_powder", 251.0, 10.4, 64.0, 3.3, 25.3, 0.6, 1.4, 20.0, 9.7, 443.0, 0.0, 1329.0, 5.0)
        ),
        CanonicalMeta(
            "garam_masala", "Garam Masala", IngredientCategory.SPICES,
            listOf("spice blend", "punjabi garam masala", "masala"), "g",
            IngredientNutrientProfile("garam_masala", 325.0, 11.5, 58.0, 11.0, 28.5, 2.4, 1.6, 48.0, 18.4, 380.0, 3.2, 1180.0, 5.0)
        ),
        // Whole Spices & Aromatics
        CanonicalMeta(
            "cumin_seeds", "Cumin Seeds", IngredientCategory.SPICES,
            listOf("cumin", "jeera", "zeera", "shahi jeera"), "g",
            IngredientNutrientProfile("cumin_seeds", 375.0, 17.8, 44.2, 22.3, 10.5, 2.3, 1.5, 168.0, 66.4, 931.0, 7.7, 1788.0, 5.0)
        ),
        CanonicalMeta(
            "mustard_seeds", "Mustard Seeds", IngredientCategory.SPICES,
            listOf("rai", "sarson", "black mustard"), "g",
            IngredientNutrientProfile("mustard_seeds", 508.0, 26.1, 28.1, 36.2, 12.2, 6.8, 2.0, 13.0, 9.2, 266.0, 7.1, 738.0, 5.0)
        ),
        CanonicalMeta(
            "cloves", "Cloves", IngredientCategory.SPICES,
            listOf("laung", "clove"), "g",
            IngredientNutrientProfile("cloves", 274.0, 6.0, 65.5, 13.0, 33.9, 2.4, 4.0, 277.0, 11.8, 632.0, 0.2, 1020.0, 1.0)
        ),
        CanonicalMeta(
            "cardamom", "Cardamom", IngredientCategory.SPICES,
            listOf("elaichi", "green cardamom"), "g",
            IngredientNutrientProfile("cardamom", 311.0, 10.8, 68.5, 6.7, 28.0, 0.0, 0.7, 18.0, 13.9, 383.0, 21.0, 1119.0, 1.0)
        ),
        CanonicalMeta(
            "cinnamon_sticks", "Cinnamon Sticks", IngredientCategory.SPICES,
            listOf("cinnamon", "dalchini"), "g",
            IngredientNutrientProfile("cinnamon_sticks", 247.0, 4.0, 80.6, 1.2, 53.1, 2.2, 0.3, 10.0, 8.3, 1002.0, 3.8, 431.0, 4.0)
        ),
        CanonicalMeta(
            "star_anise", "Star Anise", IngredientCategory.SPICES,
            listOf("chakri phool", "badian"), "g",
            IngredientNutrientProfile("star_anise", 337.0, 17.6, 50.0, 15.9, 14.6, 0.0, 0.6, 16.0, 36.9, 646.0, 21.0, 1441.0, 2.0)
        ),
        CanonicalMeta(
            "bay_leaf", "Bay Leaf", IngredientCategory.SPICES,
            listOf("tej patta", "indian bay leaf"), "g",
            IngredientNutrientProfile("bay_leaf", 313.0, 7.6, 75.0, 8.4, 26.3, 0.0, 2.3, 23.0, 43.0, 834.0, 46.5, 529.0, 1.0)
        ),
        CanonicalMeta(
            "black_peppercorns", "Black Peppercorns", IngredientCategory.SPICES,
            listOf("peppercorns", "whole kali mirch"), "g",
            IngredientNutrientProfile("black_peppercorns", 251.0, 10.4, 64.0, 3.3, 25.3, 0.6, 1.4, 20.0, 9.7, 443.0, 0.0, 1329.0, 5.0)
        ),
        CanonicalMeta(
            "coriander_seeds", "Coriander Seeds", IngredientCategory.SPICES,
            listOf("sabut dhania", "whole coriander"), "g",
            IngredientNutrientProfile("coriander_seeds", 298.0, 12.4, 55.0, 17.8, 41.9, 0.9, 1.0, 35.0, 16.3, 709.0, 21.0, 1267.0, 5.0)
        ),
        // Fresh Produce
        CanonicalMeta(
            "onion", "Onion", IngredientCategory.VEGETABLES,
            listOf("pyaaz", "red onion", "yellow onion", "kanda"), "g",
            IngredientNutrientProfile("onion", 40.0, 1.1, 9.3, 0.1, 1.7, 4.2, 0.04, 4.0, 0.2, 23.0, 7.4, 146.0, 110.0)
        ),
        CanonicalMeta(
            "tomato", "Tomato", IngredientCategory.VEGETABLES,
            listOf("tamatar", "roma tomato", "tomatoes"), "g",
            IngredientNutrientProfile("tomato", 18.0, 0.9, 3.9, 0.2, 1.2, 2.6, 0.03, 5.0, 0.3, 10.0, 13.7, 237.0, 120.0)
        ),
        CanonicalMeta(
            "garlic", "Garlic", IngredientCategory.VEGETABLES,
            listOf("lehsun", "garlic cloves", "lahsun"), "g",
            IngredientNutrientProfile("garlic", 149.0, 6.4, 33.1, 0.5, 2.1, 1.0, 0.09, 17.0, 1.7, 181.0, 31.2, 401.0, 5.0)
        ),
        CanonicalMeta(
            "ginger", "Ginger", IngredientCategory.VEGETABLES,
            listOf("adrak", "fresh ginger"), "g",
            IngredientNutrientProfile("ginger", 80.0, 1.8, 17.8, 0.8, 2.0, 1.7, 0.2, 13.0, 0.6, 16.0, 5.0, 415.0, 15.0)
        ),
        CanonicalMeta(
            "green_chilli", "Green Chilli", IngredientCategory.VEGETABLES,
            listOf("hari mirch", "chilli", "serrano", "thai chilli"), "g",
            IngredientNutrientProfile("green_chilli", 40.0, 2.0, 8.8, 0.2, 1.5, 5.1, 0.02, 7.0, 1.2, 18.0, 242.5, 340.0, 4.0)
        ),
        CanonicalMeta(
            "lemon", "Lemon", IngredientCategory.FRUITS,
            listOf("nimbu", "lime", "lemon juice"), "pcs",
            IngredientNutrientProfile("lemon", 29.0, 1.1, 9.3, 0.3, 2.8, 2.5, 0.04, 2.0, 0.6, 26.0, 53.0, 138.0, 58.0)
        ),
        CanonicalMeta(
            "coriander_leaves", "Coriander Leaves", IngredientCategory.VEGETABLES,
            listOf("cilantro", "hara dhania", "fresh coriander", "kothimbir"), "g",
            IngredientNutrientProfile("coriander_leaves", 23.0, 2.1, 3.7, 0.5, 2.8, 0.9, 0.01, 46.0, 1.8, 67.0, 27.0, 521.0, 20.0)
        ),
        CanonicalMeta(
            "potato", "Potato", IngredientCategory.VEGETABLES,
            listOf("aloo", "potatoes", "batata"), "g",
            IngredientNutrientProfile("potato", 77.0, 2.0, 17.5, 0.1, 2.2, 0.8, 0.03, 6.0, 0.8, 12.0, 19.7, 421.0, 150.0)
        ),
        CanonicalMeta(
            "carrot", "Carrot", IngredientCategory.VEGETABLES,
            listOf("gajar", "carrots"), "g",
            IngredientNutrientProfile("carrot", 41.0, 0.9, 9.6, 0.2, 2.8, 4.7, 0.04, 69.0, 0.3, 33.0, 5.9, 320.0, 75.0)
        ),
        CanonicalMeta(
            "capsicum", "Capsicum", IngredientCategory.VEGETABLES,
            listOf("bell pepper", "shimla mirch", "green pepper"), "g",
            IngredientNutrientProfile("capsicum", 26.0, 1.0, 6.0, 0.3, 2.0, 4.2, 0.05, 4.0, 0.4, 10.0, 127.7, 211.0, 120.0)
        ),
        CanonicalMeta(
            "spinach", "Spinach", IngredientCategory.VEGETABLES,
            listOf("palak", "baby spinach", "saag"), "g",
            IngredientNutrientProfile("spinach", 23.0, 2.9, 3.6, 0.4, 2.2, 0.4, 0.06, 79.0, 2.7, 99.0, 28.1, 558.0, 100.0)
        ),
        // Staples
        CanonicalMeta(
            "rice", "Basmati Rice", IngredientCategory.GRAINS,
            listOf("chawal", "basmati", "white rice", "steamed rice"), "g",
            IngredientNutrientProfile("rice", 130.0, 2.7, 28.2, 0.3, 0.4, 0.1, 0.08, 1.0, 0.2, 10.0, 0.0, 35.0, 150.0)
        ),
        CanonicalMeta(
            "wheat_flour", "Wheat Flour", IngredientCategory.GRAINS,
            listOf("atta", "whole wheat flour", "chapati flour", "roti"), "g",
            IngredientNutrientProfile("wheat_flour", 340.0, 13.2, 72.0, 2.5, 10.7, 0.4, 0.4, 2.0, 3.6, 34.0, 0.0, 363.0, 40.0)
        ),
        CanonicalMeta(
            "lentils", "Red & Yellow Lentils", IngredientCategory.LEGUMES,
            listOf("dal", "toor dal", "moong dal", "masoor dal", "arhar dal"), "g",
            IngredientNutrientProfile("lentils", 116.0, 9.0, 20.1, 0.4, 7.9, 1.8, 0.05, 2.0, 3.3, 19.0, 1.5, 369.0, 100.0)
        ),
        CanonicalMeta(
            "chickpeas", "Chickpeas", IngredientCategory.LEGUMES,
            listOf("chana", "kabuli chana", "garbanzo", "chole"), "g",
            IngredientNutrientProfile("chickpeas", 164.0, 8.9, 27.4, 2.6, 7.6, 4.8, 0.27, 7.0, 2.9, 49.0, 1.3, 291.0, 100.0)
        ),
        CanonicalMeta(
            "kidney_beans", "Kidney Beans", IngredientCategory.LEGUMES,
            listOf("rajma", "red kidney beans"), "g",
            IngredientNutrientProfile("kidney_beans", 127.0, 8.7, 22.8, 0.5, 7.4, 0.3, 0.07, 2.0, 2.2, 35.0, 1.2, 405.0, 100.0)
        ),
        // Dairy & Protein
        CanonicalMeta(
            "paneer", "Paneer", IngredientCategory.DAIRY,
            listOf("indian cottage cheese", "cottage cheese", "chhena"), "g",
            IngredientNutrientProfile("paneer", 265.0, 18.3, 3.4, 20.8, 0.0, 2.6, 13.1, 22.0, 0.2, 480.0, 0.0, 100.0, 100.0)
        ),
        CanonicalMeta(
            "yogurt", "Greek / Plain Yogurt", IngredientCategory.DAIRY,
            listOf("dahi", "curd", "hung curd"), "g",
            IngredientNutrientProfile("yogurt", 97.0, 9.0, 3.9, 5.0, 0.0, 3.5, 3.2, 36.0, 0.1, 115.0, 0.8, 141.0, 100.0)
        ),
        CanonicalMeta(
            "ghee", "Clarified Butter (Ghee)", IngredientCategory.OILS,
            listOf("desi ghee", "butter", "makhan"), "ml",
            IngredientNutrientProfile("ghee", 876.0, 0.3, 0.0, 99.5, 0.0, 0.0, 61.9, 2.0, 0.0, 4.0, 0.0, 5.0, 15.0)
        ),
        CanonicalMeta(
            "eggs", "Eggs", IngredientCategory.MEAT_SEAFOOD,
            listOf("anda", "whole egg", "egg"), "pcs",
            IngredientNutrientProfile("eggs", 143.0, 12.6, 0.7, 9.5, 0.0, 0.4, 3.1, 142.0, 1.8, 56.0, 0.0, 138.0, 50.0)
        ),
        CanonicalMeta(
            "chicken_breast", "Chicken Breast", IngredientCategory.MEAT_SEAFOOD,
            listOf("chicken", "murgh", "poultry"), "g",
            IngredientNutrientProfile("chicken_breast", 165.0, 31.0, 0.0, 3.6, 0.0, 0.0, 1.0, 74.0, 1.0, 15.0, 0.0, 256.0, 150.0)
        ),
        CanonicalMeta(
            "tofu", "Firm Tofu", IngredientCategory.LEGUMES,
            listOf("soy paneer", "bean curd"), "g",
            IngredientNutrientProfile("tofu", 144.0, 15.7, 2.8, 8.7, 2.3, 0.6, 1.3, 14.0, 2.7, 350.0, 0.2, 237.0, 100.0)
        )
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
        val fallbackCanonical = cleaned.replace(Regex("[^a-z0-9]+"), "_").trim('_').ifEmpty { "custom_ingredient" }
        val fallbackDisplay = rawInput.trim().replaceFirstChar { it.uppercase() }.ifEmpty { "Custom Ingredient" }
        return CanonicalMeta(
            canonicalName = fallbackCanonical,
            displayName = fallbackDisplay,
            category = IngredientCategory.OTHER,
            aliases = emptyList(),
            defaultUnit = "g",
            nutrientPer100g = IngredientNutrientProfile(
                canonicalName = fallbackCanonical,
                caloriesPer100g = 110.0,
                proteinPer100g = 4.5,
                carbsPer100g = 15.0,
                fatPer100g = 3.2,
                fibrePer100g = 2.5,
                sugarPer100g = 2.0,
                saturatedFatPer100g = 0.8,
                sodiumMgPer100g = 45.0,
                ironMgPer100g = 1.2,
                calciumMgPer100g = 35.0,
                vitaminCMgPer100g = 6.0,
                potassiumMgPer100g = 210.0,
                gramsPerPiece = 60.0,
                dataSource = "Estimated Reference"
            )
        )
    }

    /**
     * Converts any culinary unit (`g`, `kg`, `mg`, `ml`, `l`, `tsp`, `tbsp`, `cup`, `oz`, `lb`, `pcs`)
     * into normalized grams (Part 10.4).
     */
    fun normalizeQuantityToGrams(quantity: Double, unit: String, gramsPerPiece: Double): Double {
        return when (unit.trim().lowercase()) {
            "g", "gram", "grams", "ml" -> quantity
            "kg", "kilogram", "l", "liter" -> quantity * 1000.0
            "mg" -> quantity / 1000.0
            "tsp", "teaspoon" -> quantity * 5.0
            "tbsp", "tablespoon" -> quantity * 15.0
            "cup", "cups", "katori" -> quantity * 200.0
            "oz", "ounce" -> quantity * 28.3495
            "lb", "pound" -> quantity * 453.592
            "pcs", "pc", "piece", "pieces", "clove", "pod" -> quantity * gramsPerPiece
            else -> quantity
        }.coerceAtLeast(0.0)
    }

    /**
     * Dynamically aggregates per-serving nutrition from ingredient quantities (Part 10.4 & Part 16.3).
     */
    fun calculateRecipeNutritionFromIngredients(
        ingredients: List<RecipeIngredient>,
        baseServings: Int
    ): RecipeNutrition {
        val servings = baseServings.coerceAtLeast(1).toDouble()
        var totalCal = 0.0
        var totalProt = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0
        var totalFibre = 0.0
        var totalSugar = 0.0
        var totalSatFat = 0.0
        var totalSodium = 0.0
        var totalIron = 0.0
        var totalCalcium = 0.0
        var totalVitC = 0.0
        var totalPotassium = 0.0

        for (ing in ingredients) {
            val meta = resolve(ing.canonicalName)
            val n = meta.nutrientPer100g
            val grams = normalizeQuantityToGrams(ing.quantity, ing.unit, n.gramsPerPiece)
            val factor = grams / 100.0
            totalCal += n.caloriesPer100g * factor
            totalProt += n.proteinPer100g * factor
            totalCarbs += n.carbsPer100g * factor
            totalFat += n.fatPer100g * factor
            totalFibre += n.fibrePer100g * factor
            totalSugar += n.sugarPer100g * factor
            totalSatFat += n.saturatedFatPer100g * factor
            totalSodium += n.sodiumMgPer100g * factor
            totalIron += n.ironMgPer100g * factor
            totalCalcium += n.calciumMgPer100g * factor
            totalVitC += n.vitaminCMgPer100g * factor
            totalPotassium += n.potassiumMgPer100g * factor
        }

        fun round1(v: Double): Double = (v / servings * 10.0).roundToInt() / 10.0

        return RecipeNutrition(
            calories = (totalCal / servings).roundToInt().coerceAtLeast(25),
            proteinG = round1(totalProt),
            carbsG = round1(totalCarbs),
            fatG = round1(totalFat),
            fibreG = round1(totalFibre),
            sugarG = round1(totalSugar),
            saturatedFatG = round1(totalSatFat),
            sodiumMg = (totalSodium / servings).roundToInt(),
            ironMg = round1(totalIron),
            calciumMg = (totalCalcium / servings).roundToInt(),
            vitaminCMg = round1(totalVitC),
            potassiumMg = (totalPotassium / servings).roundToInt(),
            dataSourceType = "Calculated from IFCT/USDA Per-100g Ingredient Profiles"
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

        val bmr = ((10.0 * safeWeight) + (6.25 * safeHeight) - (5.0 * safeAge) + profile.biologicalSex.mifflinOffset)
            .roundToInt()
        val tdee = (bmr * profile.activityLevel.multiplier).roundToInt()

        val activeGoal = if (profile.isGoalPaused) FitnessGoalType.MAINTENANCE else profile.primaryGoal
        val calculatedCalories = (tdee * activeGoal.calorieDeltaFactor).roundToInt()
        val finalCalories = profile.customCalorieOverride ?: calculatedCalories

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
            assumptionsSummary = "Estimated from ${safeWeight}kg, ${safeHeight}cm, age $safeAge, goal: ${activeGoal.title}. Values are physiological estimates.",
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
    fun feetInchesToCm(feet: Int, inches: Int): Double = ((feet * 12 + inches) * 2.54 * 10.0).roundToInt() / 10.0
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
        if (detectAllergens(recipe, profile).isNotEmpty()) return false

        val excludedLower = profile.excludedIngredients.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        if (excludedLower.isNotEmpty()) {
            val hasExcluded = recipe.ingredients.any { ing ->
                excludedLower.any { ex ->
                    ing.canonicalName.lowercase().contains(ex) || ing.displayName.lowercase().contains(ex)
                }
            }
            if (hasExcluded) return false
        }

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

        val coreCoverage = (availableCoreCount.toDouble() / max(1, coreIngredients.size)).coerceIn(0.0, 1.0)
        val counterUtilisation = if (availablePantrySet.isEmpty()) {
            0.0
        } else {
            (availableIngredients.size.toDouble() / min(availablePantrySet.size, recipe.ingredients.size).coerceAtLeast(1))
                .coerceIn(0.0, 1.0)
        }

        val compoundCounts = mutableMapOf<String, Int>()
        recipe.ingredients.forEach { ing ->
            flavourProfiles[ing.canonicalName]?.keyCompounds?.forEach { comp ->
                compoundCounts[comp.id] = (compoundCounts[comp.id] ?: 0) + 1
            }
        }
        val sharedBridges = compoundCounts.values.count { it >= 2 }
        val flavourCoherence = (0.45 + (sharedBridges * 0.14)).coerceIn(0.0, 1.0)

        val gapFit = when (missingIngredients.size) {
            0 -> 1.00
            1 -> 0.82
            2 -> 0.62
            3 -> 0.42
            else -> max(0.15, 1.0 - (missingIngredients.size.toDouble() / recipe.ingredients.size.coerceAtLeast(1)))
        }.coerceIn(0.0, 1.0)

        val gastronomyScore = (
            0.30 * coreCoverage +
            0.20 * counterUtilisation +
            0.20 * flavourCoherence +
            0.30 * gapFit
        ).coerceIn(0.0, 1.0)

        val idealMealCalories = targets.targetCalories / 3.0
        val calRatioDiff = kotlin.math.abs(recipe.nutritionPerServing.calories - idealMealCalories) / idealMealCalories.coerceAtLeast(300.0)
        val calSuitability = (1.0 - (calRatioDiff * 0.7)).coerceIn(0.25, 1.0)

        val idealMealProtein = targets.proteinG / 3.0
        val proteinSuitability = (recipe.nutritionPerServing.proteinG / idealMealProtein.coerceAtLeast(18.0)).coerceIn(0.3, 1.0)

        val timeSuitability = if (recipe.totalMinutes <= profile.maxCookingMinutes) 1.0 else 0.65
        val utensilMatch = if (recipe.requiredUtensils.all { req ->
                profile.availableUtensils.any { it.equals(req, ignoreCase = true) }
            }
        ) 1.0 else 0.72
        val cuisineBonus = if (recipe.cuisine in profile.preferredCuisines) 1.0 else 0.82

        val goalCompatibilityScore = (
            0.35 * proteinSuitability +
            0.25 * calSuitability +
            0.15 * timeSuitability +
            0.15 * utensilMatch +
            0.10 * cuisineBonus
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

    /**
     * Part 18.4: Generates a realistic 7-day meal plan tailored to the user's
     * pantry, allergies, dietary restrictions, and calorie/macro targets.
     */
    fun generateAutoWeeklyPlan(
        eligibleRecipes: List<ScoredRecipe>,
        targets: NutritionTargets
    ): List<PlannedMealEntry> {
        if (eligibleRecipes.isEmpty()) return emptyList()
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val slots = listOf(MealSlot.BREAKFAST, MealSlot.LUNCH, MealSlot.DINNER)
        val slotCalShare = mapOf(
            MealSlot.BREAKFAST to 0.28,
            MealSlot.LUNCH to 0.36,
            MealSlot.DINNER to 0.36
        )

        val entries = mutableListOf<PlannedMealEntry>()
        for (dayIdx in 0..6) {
            slots.forEachIndexed { slotIdx, slot ->
                val picked = eligibleRecipes[(dayIdx * 3 + slotIdx) % eligibleRecipes.size].recipe
                val desiredSlotCalories = targets.targetCalories * (slotCalShare[slot] ?: 0.33)
                val rawServings = (desiredSlotCalories / picked.nutritionPerServing.calories.coerceAtLeast(100))
                val servings = ((rawServings * 2.0).roundToInt() / 2.0).coerceIn(0.5, 3.0)
                val n = picked.nutritionPerServing

                entries.add(
                    PlannedMealEntry(
                        id = "auto_plan_${dayIdx}_${slot.name}",
                        dayIndex = dayIdx,
                        dayName = dayNames[dayIdx],
                        mealSlot = slot,
                        recipeId = picked.id,
                        title = picked.title,
                        servings = servings,
                        calories = (n.calories * servings).roundToInt(),
                        proteinG = (n.proteinG * servings * 10).roundToInt() / 10.0,
                        carbsG = (n.carbsG * servings * 10).roundToInt() / 10.0,
                        fatG = (n.fatG * servings * 10).roundToInt() / 10.0
                    )
                )
            }
        }
        return entries
    }
}
