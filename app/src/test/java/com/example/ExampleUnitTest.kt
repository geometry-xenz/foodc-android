package com.example

import com.example.data.FoodoscopeSeedCatalog
import com.example.domain.model.*
import com.example.domain.usecase.ComputationalGastronomyEngine
import com.example.domain.usecase.IngredientAliasNormalizer
import com.example.domain.usecase.NutritionEngine
import com.example.vision.RawDetectionBox
import com.example.vision.TemporalIngredientTracker
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test alias normalization resolves regional and English names to canonical class`() {
        assertEquals("turmeric_powder", IngredientAliasNormalizer.resolve("Haldi").canonicalName)
        assertEquals("cumin_seeds", IngredientAliasNormalizer.resolve("jeera").canonicalName)
        assertEquals("chickpeas", IngredientAliasNormalizer.resolve("Kabuli Chana").canonicalName)
        assertEquals("paneer", IngredientAliasNormalizer.resolve("Indian Cottage Cheese").canonicalName)
    }

    @Test
    fun `test all 8 fitness goals compute accurate Mifflin-St Jeor BMR TDEE and macro targets`() {
        val baseProfile = UserProfile(
            age = 28,
            heightCm = 176.0,
            weightKg = 74.0,
            biologicalSex = BiologicalSex.MALE,
            activityLevel = ActivityLevel.MODERATELY_ACTIVE
        )

        val fatLossTargets = NutritionEngine.calculateTargets(baseProfile.copy(primaryGoal = FitnessGoalType.FAT_LOSS))
        val leanBulkTargets = NutritionEngine.calculateTargets(baseProfile.copy(primaryGoal = FitnessGoalType.LEAN_BULK))
        val tradBulkTargets = NutritionEngine.calculateTargets(baseProfile.copy(primaryGoal = FitnessGoalType.TRADITIONAL_BULK))
        val dirtyBulkTargets = NutritionEngine.calculateTargets(baseProfile.copy(primaryGoal = FitnessGoalType.DIRTY_BULK))
        val maintenanceTargets = NutritionEngine.calculateTargets(baseProfile.copy(primaryGoal = FitnessGoalType.MAINTENANCE))
        val recompTargets = NutritionEngine.calculateTargets(baseProfile.copy(primaryGoal = FitnessGoalType.BODY_RECOMPOSITION))
        val athleticTargets = NutritionEngine.calculateTargets(baseProfile.copy(primaryGoal = FitnessGoalType.ATHLETIC_PERFORMANCE))
        val healthTargets = NutritionEngine.calculateTargets(baseProfile.copy(primaryGoal = FitnessGoalType.GENERAL_HEALTH))

        // Verify BMR = 10*74 + 6.25*176 - 5*28 + 5 = 740 + 1100 - 140 + 5 = 1705 kcal
        assertEquals(1705, maintenanceTargets.bmrKcal)
        // TDEE = 1705 * 1.55 = 2643 kcal
        assertEquals(2643, maintenanceTargets.tdeeKcal)

        assertTrue(fatLossTargets.targetCalories < maintenanceTargets.targetCalories)
        assertTrue(recompTargets.targetCalories < maintenanceTargets.targetCalories)
        assertTrue(leanBulkTargets.targetCalories > maintenanceTargets.targetCalories)
        assertTrue(tradBulkTargets.targetCalories > leanBulkTargets.targetCalories)
        assertTrue(dirtyBulkTargets.targetCalories > tradBulkTargets.targetCalories)
        assertTrue(athleticTargets.carbsG > fatLossTargets.carbsG)
        assertTrue(healthTargets.fibreG >= 35)
        assertTrue(dirtyBulkTargets.safetyWarnings.isNotEmpty())
    }

    @Test
    fun `test hard allergy constraint strictly excludes recipes containing declared allergens`() {
        val milkAllergicProfile = UserProfile(
            dietaryLifestyle = DietaryLifestyle.VEGETARIAN,
            allergies = setOf(AllergenType.MILK)
        )
        val targets = NutritionEngine.calculateTargets(milkAllergicProfile)
        val ranked = ComputationalGastronomyEngine.rankRecommendations(
            allRecipes = FoodoscopeSeedCatalog.recipes,
            pantryItems = FoodoscopeSeedCatalog.defaultPantryItems,
            profile = milkAllergicProfile,
            targets = targets,
            flavourProfiles = FoodoscopeSeedCatalog.flavourProfiles
        )

        // Paneer Tikka and Saffron Biryani (contains yogurt) must be hard-filtered out
        assertFalse(ranked.any { it.recipe.id == "rcp_paneer_tikka" })
        assertFalse(ranked.any { it.recipe.id == "rcp_saffron_biryani" })
        // Vegan Moong Palak Dal must remain eligible
        assertTrue(ranked.any { it.recipe.id == "rcp_moong_palak_dal" })
    }

    @Test
    fun `test computational gastronomy equation and single ingredient unlock engine`() {
        val profile = UserProfile(
            dietaryLifestyle = DietaryLifestyle.VEGETARIAN,
            primaryGoal = FitnessGoalType.LEAN_BULK
        )
        val targets = NutritionEngine.calculateTargets(profile)
        val ranked = ComputationalGastronomyEngine.rankRecommendations(
            allRecipes = FoodoscopeSeedCatalog.recipes,
            pantryItems = FoodoscopeSeedCatalog.defaultPantryItems,
            profile = profile,
            targets = targets,
            flavourProfiles = FoodoscopeSeedCatalog.flavourProfiles
        )
        assertTrue(ranked.isNotEmpty())
        val top = ranked.first()
        val expectedFormula = (
            0.30 * top.coreCoverage +
            0.20 * top.counterUtilisation +
            0.20 * top.flavourCoherence +
            0.30 * top.gapFit
        )
        assertEquals(expectedFormula, top.gastronomyScore, 0.0001)

        val unlock = ComputationalGastronomyEngine.computeUnlockSuggestion(
            eligibleRecipes = ranked,
            flavourProfiles = FoodoscopeSeedCatalog.flavourProfiles
        )
        assertNotNull(unlock)
        assertTrue(unlock!!.unlockedRecipes.isNotEmpty())
    }

    @Test
    fun `test scanner NMS IoU and 1000ms temporal stability persistence`() {
        val boxA = RawDetectionBox("turmeric", 0.92f, NormalizedRect(0.1f, 0.1f, 0.5f, 0.5f))
        val boxBDuplicate = RawDetectionBox("turmeric", 0.81f, NormalizedRect(0.12f, 0.12f, 0.48f, 0.48f))
        val lowConfBox = RawDetectionBox("star_anise", 0.62f, NormalizedRect(0.6f, 0.6f, 0.8f, 0.8f))

        val nmsResult = TemporalIngredientTracker.applyNms(listOf(boxA, boxBDuplicate, lowConfBox), iouThreshold = 0.45f)
        assertEquals(2, nmsResult.size)

        val tracker = TemporalIngredientTracker(confidenceThreshold = 0.75f, requiredStabilityMs = 1000L)

        // Frame 1 at t=0ms -> CANDIDATE, not yet stabilized
        val (tracks0, stable0) = tracker.processFrameDetections(listOf(boxA, lowConfBox), 1000L, emptySet())
        assertTrue(stable0.isEmpty())
        assertEquals(DetectionState.UNCERTAIN, tracks0.find { it.canonicalName == "star_anise" }?.state)

        // Frame 2 at t=500ms -> STABILIZING
        val (tracks500, stable500) = tracker.processFrameDetections(listOf(boxA), 1500L, emptySet())
        assertTrue(stable500.isEmpty())
        assertEquals(DetectionState.STABILIZING, tracks500.first { it.canonicalName == "turmeric_powder" }.state)

        // Frame 3 at t=1100ms -> crosses 1,000ms threshold -> AWAITING_CONFIRMATION
        val (_, stable1100) = tracker.processFrameDetections(listOf(boxA), 2100L, emptySet())
        assertEquals(1, stable1100.size)
        assertEquals("turmeric_powder", stable1100.first().canonicalName)

        // Frame 4 at t=1400ms -> duplicate suppression prevents re-emitting
        val (_, stable1400) = tracker.processFrameDetections(listOf(boxA), 2400L, emptySet())
        assertTrue(stable1400.isEmpty())
    }

    @Test
    fun `test FlavorDB molecular substitution engine ranks shared aroma compounds`() {
        val subs = FoodoscopeSeedCatalog.computeSubstitutions("cumin_seeds", setOf("coriander_powder"))
        assertTrue(subs.isNotEmpty())
        assertTrue(subs.first().inPantry)
        assertTrue(subs.first().sharedCompounds.isNotEmpty())
    }
}
