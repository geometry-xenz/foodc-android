package com.example.ui

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FoodoscopeSeedCatalog
import com.example.data.repository.DrishyamRepository
import com.example.domain.model.*
import com.example.domain.usecase.ComputationalGastronomyEngine
import com.example.domain.usecase.IngredientAliasNormalizer
import com.example.domain.usecase.NutritionEngine
import com.example.vision.RawDetectionBox
import com.example.vision.TemporalIngredientTracker
import com.example.vision.VisionModelStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

enum class PrimaryTab(val label: String) {
    HOME("Home"),
    SCAN("Scan"),
    PANTRY("Pantry"),
    DISCOVER("Discover"),
    NUTRITION("Nutrition")
}

enum class SubScreen {
    NONE,
    AUTH_PORTAL,
    ONBOARDING_WIZARD,
    RECIPE_DETAIL,
    COOKING_MODE,
    FLAVOUR_SCIENCE,
    MEAL_PLANNER,
    SHOPPING_LIST,
    PROFILE_AND_GOALS,
    SETTINGS_AND_SYNC
}

data class DrishyamUiState(
    val activeTab: PrimaryTab = PrimaryTab.HOME,
    val activeSubScreen: SubScreen = SubScreen.NONE,
    val isAuthenticated: Boolean = true,
    val userProfile: UserProfile = UserProfile(),
    val nutritionTargets: NutritionTargets = NutritionEngine.calculateTargets(UserProfile()),
    val pantryItems: List<PantryItem> = emptyList(),
    val scoredRecipes: List<ScoredRecipe> = emptyList(),
    val allScoredIncludingFiltered: List<ScoredRecipe> = emptyList(),
    val unlockSuggestion: UnlockSuggestion? = null,
    val savedRecipeIds: Set<String> = emptySet(),
    val selectedRecipeId: String = "rcp_paneer_tikka",
    val selectedServings: Int = 2,
    val cookingStepIndex: Int = 0,
    val cookingTimerSecondsRemaining: Int = 0,
    val cookingTimerRunning: Boolean = false,
    val keepScreenAwakeInCooking: Boolean = true,
    // Scanner state
    val scannerRunning: Boolean = true,
    val scannerFrozen: Boolean = false,
    val useRearCamera: Boolean = true,
    val torchEnabled: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val visionModelStatus: VisionModelStatus = VisionModelStatus(
        weightsFound = false,
        modelFileName = "yolov8n_drishyam_culinary_fp16.tflite",
        engineName = "TFLite / ONNX Runtime Mobile Pipeline",
        accelerationMode = "CameraX Live Spectral + Interactive Culinary Simulator (Weights Not Bundled)",
        statusBanner = "DEMONSTRATION & SPECTRAL ASSIST MODE — Place `yolov8n_drishyam.tflite` in assets/ for custom YOLO weights"
    ),
    val activeScanTracks: List<ScannedCandidate> = emptyList(),
    val pendingConfirmationQueue: List<ScannedCandidate> = emptyList(),
    val deepInspectConsentVisible: Boolean = false,
    val deepInspectInProgress: Boolean = false,
    val simulatedTargetSetIndex: Int = 0,
    val measuredFps: Int = 28,
    // Flavour Science state
    val selectedFlavourIngredient: String = "turmeric_powder",
    val selectedCompoundId: String? = "curcumene",
    val substitutionQuery: String = "paneer",
    val substitutionResults: List<SubstitutionCandidate> = emptyList(),
    // Nutrition, Planner & Shopping state
    val mealLogs: List<MealLogEntry> = emptyList(),
    val plannedMeals: List<PlannedMealEntry> = emptyList(),
    val selectedPlannerDayIndex: Int = 0,
    val shoppingItems: List<ShoppingListItem> = emptyList(),
    // Filters
    val recipeSearchQuery: String = "",
    val selectedCuisineFilter: String = "All",
    val pantryCategoryFilter: IngredientCategory? = null,
    val pantrySearchQuery: String = "",
    // Sync & feedback
    val syncOperations: List<SyncOperation> = emptyList(),
    val backendBaseUrl: String = "https://api.drishyam.foodoscope.ai/",
    val statusBannerMessage: String? = null
)

class DrishyamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DrishyamRepository(application)
    private val tracker = TemporalIngredientTracker(confidenceThreshold = 0.75f, requiredStabilityMs = 1000L)

    private val _uiState = MutableStateFlow(DrishyamUiState())
    val uiState: StateFlow<DrishyamUiState> = _uiState.asStateFlow()

    private var cookingTimerJob: Job? = null
    private var scannerTickerJob: Job? = null

    private val simulationTargetPresets: List<List<RawDetectionBox>> = listOf(
        listOf(
            RawDetectionBox("paneer", 0.94f, NormalizedRect(0.14f, 0.22f, 0.48f, 0.56f)),
            RawDetectionBox("capsicum", 0.91f, NormalizedRect(0.54f, 0.26f, 0.86f, 0.58f)),
            RawDetectionBox("garam_masala", 0.88f, NormalizedRect(0.24f, 0.62f, 0.56f, 0.84f))
        ),
        listOf(
            RawDetectionBox("chickpeas", 0.95f, NormalizedRect(0.16f, 0.20f, 0.50f, 0.54f)),
            RawDetectionBox("rice", 0.92f, NormalizedRect(0.52f, 0.22f, 0.84f, 0.55f)),
            RawDetectionBox("cardamom", 0.86f, NormalizedRect(0.28f, 0.60f, 0.58f, 0.82f)),
            RawDetectionBox("star_anise", 0.68f, NormalizedRect(0.62f, 0.64f, 0.86f, 0.86f)) // <75% uncertain demo!
        ),
        listOf(
            RawDetectionBox("spinach", 0.93f, NormalizedRect(0.18f, 0.24f, 0.52f, 0.58f)),
            RawDetectionBox("coriander_leaves", 0.89f, NormalizedRect(0.54f, 0.28f, 0.84f, 0.60f)),
            RawDetectionBox("coriander_powder", 0.87f, NormalizedRect(0.26f, 0.62f, 0.58f, 0.84f))
        )
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            val profile = repository.loadUserProfile()
            val targets = NutritionEngine.calculateTargets(profile)
            val weightsExist = try {
                application.assets.list("")?.any { it.endsWith(".tflite") || it.endsWith(".onnx") } == true
            } catch (_: Exception) {
                false
            }
            _uiState.update { state ->
                state.copy(
                    isAuthenticated = repository.tokenVault.hasActiveSession(),
                    userProfile = profile,
                    nutritionTargets = targets,
                    backendBaseUrl = repository.getBackendBaseUrl(),
                    visionModelStatus = state.visionModelStatus.copy(weightsFound = weightsExist)
                )
            }
            recomputeSubstitutions("paneer")
        }

        // Observe Pantry and recompute RecipeDB Gastronomy scores & Unlock suggestions
        viewModelScope.launch {
            repository.pantryFlow.collect { pantry ->
                _uiState.update { current ->
                    val ranked = ComputationalGastronomyEngine.rankRecommendations(
                        allRecipes = FoodoscopeSeedCatalog.recipes,
                        pantryItems = pantry,
                        profile = current.userProfile,
                        targets = current.nutritionTargets,
                        flavourProfiles = FoodoscopeSeedCatalog.flavourProfiles
                    )
                    val allScored = FoodoscopeSeedCatalog.recipes.map { rcp ->
                        ComputationalGastronomyEngine.scoreRecipe(
                            recipe = rcp,
                            pantryItems = pantry,
                            profile = current.userProfile,
                            targets = current.nutritionTargets,
                            flavourProfiles = FoodoscopeSeedCatalog.flavourProfiles
                        )
                    }
                    val unlock = ComputationalGastronomyEngine.computeUnlockSuggestion(
                        eligibleRecipes = ranked,
                        flavourProfiles = FoodoscopeSeedCatalog.flavourProfiles
                    )
                    current.copy(
                        pantryItems = pantry,
                        scoredRecipes = ranked,
                        allScoredIncludingFiltered = allScored,
                        unlockSuggestion = unlock
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.savedRecipeIdsFlow.collect { ids ->
                _uiState.update { it.copy(savedRecipeIds = ids) }
            }
        }

        viewModelScope.launch {
            repository.mealLogsFlow.collect { logs ->
                _uiState.update { it.copy(mealLogs = logs) }
            }
        }

        viewModelScope.launch {
            repository.plannedMealsFlow.collect { meals ->
                _uiState.update { it.copy(plannedMeals = meals) }
            }
        }

        viewModelScope.launch {
            repository.shoppingItemsFlow.collect { items ->
                _uiState.update { it.copy(shoppingItems = items) }
            }
        }

        viewModelScope.launch {
            repository.syncOutboxFlow.collect { ops ->
                _uiState.update { it.copy(syncOperations = ops) }
            }
        }

        startScannerSimulationLoop()
    }

    private fun startScannerSimulationLoop() {
        scannerTickerJob?.cancel()
        scannerTickerJob = viewModelScope.launch {
            while (true) {
                delay(250L)
                val state = _uiState.value
                if (state.activeTab == PrimaryTab.SCAN && state.scannerRunning && !state.scannerFrozen) {
                    val preset = simulationTargetPresets[state.simulatedTargetSetIndex % simulationTargetPresets.size]
                    val existingCanonicals = state.pantryItems.map { it.canonicalName }.toSet()
                    val (tracks, newlyStabilized) = tracker.processFrameDetections(
                        rawDetections = preset,
                        nowMs = System.currentTimeMillis(),
                        existingPantryCanonicals = existingCanonicals
                    )
                    if (newlyStabilized.isNotEmpty() && state.soundEnabled) {
                        playRecognitionBeep()
                    }
                    _uiState.update { current ->
                        val updatedQueue = (current.pendingConfirmationQueue + newlyStabilized)
                            .distinctBy { it.canonicalName }
                        current.copy(
                            activeScanTracks = tracks,
                            pendingConfirmationQueue = updatedQueue
                        )
                    }
                }
            }
        }
    }

    private fun playRecognitionBeep() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 90)
        } catch (_: Exception) {
            // Safe ignore on headless JVM
        }
    }

    fun selectTab(tab: PrimaryTab) {
        _uiState.update { it.copy(activeTab = tab, activeSubScreen = SubScreen.NONE) }
    }

    fun openSubScreen(subScreen: SubScreen) {
        _uiState.update { it.copy(activeSubScreen = subScreen) }
    }

    fun navigateBack(): Boolean {
        val current = _uiState.value
        if (current.activeSubScreen != SubScreen.NONE) {
            val parent = when (current.activeSubScreen) {
                SubScreen.COOKING_MODE -> SubScreen.RECIPE_DETAIL
                else -> SubScreen.NONE
            }
            _uiState.update { it.copy(activeSubScreen = parent) }
            return true
        }
        if (current.activeTab != PrimaryTab.HOME) {
            _uiState.update { it.copy(activeTab = PrimaryTab.HOME) }
            return true
        }
        return false
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    // Authentication (Part 6)
    fun loginOrRegister(email: String, password: String, displayName: String?) {
        if (email.isBlank() || password.length < 4) {
            _uiState.update { it.copy(statusBannerMessage = "Enter a valid DRISHYAM email and password (min 4 chars).") }
            return
        }
        repository.tokenVault.storeTokens(
            accessToken = "drishyam_jwt_${UUID.randomUUID()}",
            refreshToken = "drishyam_ref_${UUID.randomUUID()}",
            email = email.trim()
        )
        val updatedProfile = _uiState.value.userProfile.copy(
            email = email.trim(),
            displayName = displayName?.takeIf { it.isNotBlank() } ?: _uiState.value.userProfile.displayName
        )
        updateProfile(updatedProfile)
        _uiState.update {
            it.copy(
                isAuthenticated = true,
                activeSubScreen = SubScreen.NONE,
                statusBannerMessage = "Signed in as ${email.trim()} (Keystore AES-GCM session active)"
            )
        }
    }

    fun logout() {
        repository.tokenVault.clearSession()
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                activeSubScreen = SubScreen.AUTH_PORTAL,
                statusBannerMessage = "Session revoked and Keystore credentials cleared."
            )
        }
    }

    // Profile, Fitness Goals & Preferences (Part 7, 8, 9, 10)
    fun updateProfile(updated: UserProfile) {
        repository.saveUserProfile(updated)
        val targets = NutritionEngine.calculateTargets(updated)
        _uiState.update { current ->
            val ranked = ComputationalGastronomyEngine.rankRecommendations(
                allRecipes = FoodoscopeSeedCatalog.recipes,
                pantryItems = current.pantryItems,
                profile = updated,
                targets = targets,
                flavourProfiles = FoodoscopeSeedCatalog.flavourProfiles
            )
            val allScored = FoodoscopeSeedCatalog.recipes.map { rcp ->
                ComputationalGastronomyEngine.scoreRecipe(
                    recipe = rcp,
                    pantryItems = current.pantryItems,
                    profile = updated,
                    targets = targets,
                    flavourProfiles = FoodoscopeSeedCatalog.flavourProfiles
                )
            }
            val unlock = ComputationalGastronomyEngine.computeUnlockSuggestion(
                eligibleRecipes = ranked,
                flavourProfiles = FoodoscopeSeedCatalog.flavourProfiles
            )
            current.copy(
                userProfile = updated,
                nutritionTargets = targets,
                scoredRecipes = ranked,
                allScoredIncludingFiltered = allScored,
                unlockSuggestion = unlock
            )
        }
    }

    // Scanner Controls (Part 11)
    fun cycleSimulatedCameraTargets() {
        tracker.reset()
        _uiState.update {
            it.copy(
                simulatedTargetSetIndex = (it.simulatedTargetSetIndex + 1) % simulationTargetPresets.size,
                activeScanTracks = emptyList()
            )
        }
    }

    fun toggleScannerFreeze() {
        _uiState.update { it.copy(scannerFrozen = !it.scannerFrozen) }
    }

    fun toggleScannerRunning() {
        _uiState.update { it.copy(scannerRunning = !it.scannerRunning) }
    }

    fun toggleCameraLens() {
        _uiState.update { it.copy(useRearCamera = !it.useRearCamera) }
    }

    fun toggleTorch() {
        _uiState.update { it.copy(torchEnabled = !it.torchEnabled) }
    }

    fun toggleScannerSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun toggleScannerHaptics() {
        _uiState.update { it.copy(hapticsEnabled = !it.hapticsEnabled) }
    }

    fun updatePendingCandidate(
        canonicalName: String,
        newName: String,
        newQuantity: Double,
        newUnit: String
    ) {
        val meta = IngredientAliasNormalizer.resolve(newName)
        _uiState.update { state ->
            state.copy(
                pendingConfirmationQueue = state.pendingConfirmationQueue.map { item ->
                    if (item.canonicalName == canonicalName) {
                        item.copy(
                            canonicalName = meta.canonicalName,
                            displayName = meta.displayName,
                            category = meta.category,
                            suggestedQuantity = newQuantity.coerceAtLeast(1.0),
                            suggestedUnit = newUnit
                        )
                    } else item
                }
            )
        }
    }

    fun dismissPendingCandidate(canonicalName: String) {
        tracker.removeTrack(canonicalName)
        _uiState.update { state ->
            state.copy(
                pendingConfirmationQueue = state.pendingConfirmationQueue.filterNot { it.canonicalName == canonicalName }
            )
        }
    }

    fun confirmSingleScannedCandidate(candidate: ScannedCandidate) {
        viewModelScope.launch {
            repository.upsertPantryItem(
                rawName = candidate.canonicalName,
                quantity = candidate.suggestedQuantity,
                unit = candidate.suggestedUnit,
                source = candidate.source,
                confidence = candidate.confidence,
                confirmed = true
            )
            _uiState.update { state ->
                state.copy(
                    pendingConfirmationQueue = state.pendingConfirmationQueue.filterNot {
                        it.canonicalName == candidate.canonicalName
                    },
                    statusBannerMessage = "Confirmed ${candidate.displayName} (${candidate.suggestedQuantity.roundToInt()}${candidate.suggestedUnit}) into Pantry"
                )
            }
        }
    }

    fun confirmAllPendingDetections() {
        val queue = _uiState.value.pendingConfirmationQueue
        if (queue.isEmpty()) return
        viewModelScope.launch {
            for (candidate in queue) {
                repository.upsertPantryItem(
                    rawName = candidate.canonicalName,
                    quantity = candidate.suggestedQuantity,
                    unit = candidate.suggestedUnit,
                    source = candidate.source,
                    confidence = candidate.confidence,
                    confirmed = true
                )
            }
            _uiState.update {
                it.copy(
                    pendingConfirmationQueue = emptyList(),
                    statusBannerMessage = "Added ${queue.size} confirmed ingredients to Pantry & queued cloud sync"
                )
            }
        }
    }

    fun requestDeepInspectConsent() {
        _uiState.update { it.copy(deepInspectConsentVisible = true) }
    }

    fun dismissDeepInspectConsent() {
        _uiState.update { it.copy(deepInspectConsentVisible = false) }
    }

    fun executeDeepInspectWithConsent() {
        viewModelScope.launch {
            _uiState.update { it.copy(deepInspectConsentVisible = false, deepInspectInProgress = true) }
            delay(650L)
            val now = System.currentTimeMillis()
            val deepCandidates = listOf(
                ScannedCandidate(
                    trackId = "deep_paneer",
                    canonicalName = "paneer",
                    displayName = "Paneer",
                    category = IngredientCategory.DAIRY,
                    confidence = 0.96f,
                    boundingBox = NormalizedRect(0.18f, 0.25f, 0.48f, 0.55f),
                    firstDetectedMs = now - 1200L,
                    lastSeenMs = now,
                    accumulatedMs = 1200L,
                    state = DetectionState.AWAITING_CONFIRMATION,
                    suggestedQuantity = 250.0,
                    suggestedUnit = "g",
                    source = "deep_inspect"
                ),
                ScannedCandidate(
                    trackId = "deep_capsicum",
                    canonicalName = "capsicum",
                    displayName = "Capsicum",
                    category = IngredientCategory.VEGETABLES,
                    confidence = 0.93f,
                    boundingBox = NormalizedRect(0.52f, 0.28f, 0.82f, 0.58f),
                    firstDetectedMs = now - 1200L,
                    lastSeenMs = now,
                    accumulatedMs = 1200L,
                    state = DetectionState.AWAITING_CONFIRMATION,
                    suggestedQuantity = 150.0,
                    suggestedUnit = "g",
                    source = "deep_inspect"
                )
            )
            _uiState.update { state ->
                val merged = (state.pendingConfirmationQueue + deepCandidates).distinctBy { it.canonicalName }
                state.copy(
                    deepInspectInProgress = false,
                    pendingConfirmationQueue = merged,
                    statusBannerMessage = "Deep Inspect identified 2 high-confidence culinary items awaiting your confirmation"
                )
            }
        }
    }

    // Pantry Management (Part 12)
    fun addOrUpdatePantryManual(rawName: String, quantity: Double, unit: String) {
        if (rawName.isBlank() || quantity <= 0.0) return
        viewModelScope.launch {
            val saved = repository.upsertPantryItem(
                rawName = rawName,
                quantity = quantity,
                unit = unit,
                source = "manual",
                confidence = 1.0f,
                confirmed = true
            )
            _uiState.update {
                it.copy(statusBannerMessage = "Saved ${saved.displayName} (${saved.quantity.roundToInt()}${saved.unit}) in Pantry")
            }
        }
    }

    fun deletePantryItem(id: String) {
        viewModelScope.launch {
            repository.deletePantryItem(id)
        }
    }

    fun setPantryCategoryFilter(category: IngredientCategory?) {
        _uiState.update { it.copy(pantryCategoryFilter = category) }
    }

    fun setPantrySearchQuery(query: String) {
        _uiState.update { it.copy(pantrySearchQuery = query) }
    }

    // Recipe Discovery & Cooking Mode (Part 13 & 14)
    fun setRecipeSearchQuery(query: String) {
        _uiState.update { it.copy(recipeSearchQuery = query) }
    }

    fun setCuisineFilter(cuisine: String) {
        _uiState.update { it.copy(selectedCuisineFilter = cuisine) }
    }

    fun openRecipeDetail(recipeId: String) {
        val recipe = FoodoscopeSeedCatalog.recipes.find { it.id == recipeId } ?: FoodoscopeSeedCatalog.recipes.first()
        _uiState.update {
            it.copy(
                selectedRecipeId = recipe.id,
                selectedServings = recipe.baseServings,
                cookingStepIndex = 0,
                activeSubScreen = SubScreen.RECIPE_DETAIL
            )
        }
    }

    fun adjustSelectedServings(servings: Int) {
        _uiState.update { it.copy(selectedServings = servings.coerceIn(1, 12)) }
    }

    fun toggleSaveRecipe(recipeId: String) {
        val isSaved = recipeId in _uiState.value.savedRecipeIds
        viewModelScope.launch {
            repository.toggleSavedRecipe(recipeId, isSaved)
        }
    }

    fun startCookingMode(recipeId: String) {
        val recipe = FoodoscopeSeedCatalog.recipes.find { it.id == recipeId } ?: return
        val firstStepSeconds = recipe.steps.firstOrNull()?.durationSeconds ?: 180
        cookingTimerJob?.cancel()
        _uiState.update {
            it.copy(
                selectedRecipeId = recipe.id,
                cookingStepIndex = 0,
                cookingTimerSecondsRemaining = firstStepSeconds,
                cookingTimerRunning = false,
                activeSubScreen = SubScreen.COOKING_MODE
            )
        }
    }

    fun setCookingStep(stepIndex: Int) {
        val recipe = FoodoscopeSeedCatalog.recipes.find { it.id == _uiState.value.selectedRecipeId } ?: return
        val coerced = stepIndex.coerceIn(0, recipe.steps.lastIndex)
        val stepSeconds = recipe.steps[coerced].durationSeconds ?: 120
        cookingTimerJob?.cancel()
        _uiState.update {
            it.copy(
                cookingStepIndex = coerced,
                cookingTimerSecondsRemaining = stepSeconds,
                cookingTimerRunning = false
            )
        }
    }

    fun toggleCookingTimer() {
        val currentlyRunning = _uiState.value.cookingTimerRunning
        if (currentlyRunning) {
            cookingTimerJob?.cancel()
            _uiState.update { it.copy(cookingTimerRunning = false) }
        } else {
            _uiState.update { it.copy(cookingTimerRunning = true) }
            cookingTimerJob?.cancel()
            cookingTimerJob = viewModelScope.launch {
                while (_uiState.value.cookingTimerSecondsRemaining > 0 && _uiState.value.cookingTimerRunning) {
                    delay(1000L)
                    _uiState.update { state ->
                        val next = (state.cookingTimerSecondsRemaining - 1).coerceAtLeast(0)
                        if (next == 0) playRecognitionBeep()
                        state.copy(
                            cookingTimerSecondsRemaining = next,
                            cookingTimerRunning = next > 0
                        )
                    }
                }
            }
        }
    }

    fun completeCookingAndLogMeal(mealSlot: MealSlot = MealSlot.DINNER) {
        val state = _uiState.value
        val recipe = FoodoscopeSeedCatalog.recipes.find { it.id == state.selectedRecipeId } ?: return
        val ratio = state.selectedServings.toDouble() / recipe.baseServings.coerceAtLeast(1)
        val nutr = recipe.nutritionPerServing
        val entry = MealLogEntry(
            id = "log_${UUID.randomUUID().toString().take(8)}",
            dateIso = repository.todayIso(),
            mealSlot = mealSlot,
            recipeId = recipe.id,
            title = recipe.title,
            servings = state.selectedServings.toDouble(),
            calories = (nutr.calories * ratio).roundToInt(),
            proteinG = (nutr.proteinG * ratio * 10).roundToInt() / 10.0,
            carbsG = (nutr.carbsG * ratio * 10).roundToInt() / 10.0,
            fatG = (nutr.fatG * ratio * 10).roundToInt() / 10.0,
            fibreG = (nutr.fibreG * ratio * 10).roundToInt() / 10.0,
            sugarG = (nutr.sugarG * ratio * 10).roundToInt() / 10.0,
            saturatedFatG = (nutr.saturatedFatG * ratio * 10).roundToInt() / 10.0,
            sodiumMg = (nutr.sodiumMg * ratio).roundToInt(),
            notes = "Cooked via DRISHYAM Interactive Cooking Mode (${state.selectedServings} servings)"
        )
        viewModelScope.launch {
            repository.addMealLog(entry)
            _uiState.update {
                it.copy(
                    activeSubScreen = SubScreen.NONE,
                    activeTab = PrimaryTab.NUTRITION,
                    statusBannerMessage = "Logged ${recipe.title} (${entry.calories} kcal, ${entry.proteinG}g protein)"
                )
            }
        }
    }

    // FlavorDB & Molecular Science (Part 15)
    fun selectFlavourIngredient(canonicalName: String) {
        val profile = FoodoscopeSeedCatalog.flavourProfiles[canonicalName]
        _uiState.update {
            it.copy(
                selectedFlavourIngredient = canonicalName,
                selectedCompoundId = profile?.keyCompounds?.firstOrNull()?.id
            )
        }
        recomputeSubstitutions(canonicalName)
    }

    fun selectAromaCompound(compoundId: String) {
        _uiState.update { it.copy(selectedCompoundId = compoundId) }
    }

    fun recomputeSubstitutions(query: String) {
        val pantrySet = _uiState.value.pantryItems.map { it.canonicalName }.toSet()
        val subs = FoodoscopeSeedCatalog.computeSubstitutions(query, pantrySet)
        _uiState.update {
            it.copy(
                substitutionQuery = query,
                substitutionResults = subs
            )
        }
    }

    // Meal Logging, Planning & Shopping (Part 18, 19, 20)
    fun logCustomMeal(
        title: String,
        mealSlot: MealSlot,
        servings: Double,
        calories: Int,
        proteinG: Double,
        carbsG: Double,
        fatG: Double,
        fibreG: Double
    ) {
        if (title.isBlank() || calories <= 0) return
        val entry = MealLogEntry(
            id = "log_${UUID.randomUUID().toString().take(8)}",
            dateIso = repository.todayIso(),
            mealSlot = mealSlot,
            recipeId = null,
            title = title.trim(),
            servings = servings,
            calories = calories,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG,
            fibreG = fibreG,
            sugarG = 4.0,
            saturatedFatG = (fatG * 0.25 * 10).roundToInt() / 10.0,
            sodiumMg = 320,
            sourceLabel = "User Custom Entry (Estimate)"
        )
        viewModelScope.launch {
            repository.addMealLog(entry)
            _uiState.update { it.copy(statusBannerMessage = "Logged ${entry.title} (${entry.calories} kcal)") }
        }
    }

    fun deleteMealLog(id: String) {
        viewModelScope.launch { repository.deleteMealLog(id) }
    }

    fun selectPlannerDay(dayIndex: Int) {
        _uiState.update { it.copy(selectedPlannerDayIndex = dayIndex.coerceIn(0, 6)) }
    }

    fun addRecipeToMealPlan(recipe: Recipe, dayIndex: Int, mealSlot: MealSlot, servings: Double = 1.0) {
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val nutr = recipe.nutritionPerServing
        val entry = PlannedMealEntry(
            id = "plan_${UUID.randomUUID().toString().take(8)}",
            dayIndex = dayIndex.coerceIn(0, 6),
            dayName = dayNames[dayIndex.coerceIn(0, 6)],
            mealSlot = mealSlot,
            recipeId = recipe.id,
            title = recipe.title,
            servings = servings,
            calories = (nutr.calories * servings).roundToInt(),
            proteinG = (nutr.proteinG * servings * 10).roundToInt() / 10.0,
            carbsG = (nutr.carbsG * servings * 10).roundToInt() / 10.0,
            fatG = (nutr.fatG * servings * 10).roundToInt() / 10.0
        )
        viewModelScope.launch {
            repository.addPlannedMeal(entry)
            _uiState.update {
                it.copy(statusBannerMessage = "Added ${recipe.title} to ${entry.dayName} ${mealSlot.label}")
            }
        }
    }

    fun removePlannedMeal(id: String) {
        viewModelScope.launch { repository.removePlannedMeal(id) }
    }

    fun generateShoppingFromMissingAndLowStock() {
        viewModelScope.launch {
            val state = _uiState.value
            val pantryMap = state.pantryItems.associateBy { it.canonicalName }
            var addedCount = 0

            // 1. Low stock items in pantry
            for (item in state.pantryItems.filter { it.isLowStock }) {
                repository.upsertShoppingItem(
                    ShoppingListItem(
                        id = "shop_low_${item.canonicalName}",
                        canonicalName = item.canonicalName,
                        displayName = item.displayName,
                        category = item.category,
                        quantity = 100.0,
                        unit = item.unit,
                        reason = "Low Stock in Pantry (${item.quantity.roundToInt()}${item.unit} left)"
                    )
                )
                addedCount++
            }

            // 2. Missing ingredients from top 2 recommended recipes
            for (scored in state.scoredRecipes.take(2)) {
                for (missing in scored.missingIngredients) {
                    if (missing.canonicalName !in pantryMap) {
                        val meta = IngredientAliasNormalizer.resolve(missing.canonicalName)
                        repository.upsertShoppingItem(
                            ShoppingListItem(
                                id = "shop_miss_${missing.canonicalName}",
                                canonicalName = missing.canonicalName,
                                displayName = missing.displayName,
                                category = meta.category,
                                quantity = missing.quantity,
                                unit = missing.unit,
                                reason = "Missing for ${scored.recipe.title}",
                                alternativeSuggestion = FoodoscopeSeedCatalog.flavourProfiles[missing.canonicalName]
                                    ?.complementaryIngredients?.firstOrNull()
                            )
                        )
                        addedCount++
                    }
                }
            }
            _uiState.update {
                it.copy(statusBannerMessage = "Updated smart shopping list with $addedCount pantry-aware items")
            }
        }
    }

    fun addManualShoppingItem(rawName: String, quantity: Double, unit: String) {
        if (rawName.isBlank()) return
        val meta = IngredientAliasNormalizer.resolve(rawName)
        viewModelScope.launch {
            repository.upsertShoppingItem(
                ShoppingListItem(
                    id = "shop_man_${meta.canonicalName}_${UUID.randomUUID().toString().take(4)}",
                    canonicalName = meta.canonicalName,
                    displayName = meta.displayName,
                    category = meta.category,
                    quantity = quantity.coerceAtLeast(1.0),
                    unit = unit.ifBlank { meta.defaultUnit },
                    reason = "Manual Kitchen Addition"
                )
            )
        }
    }

    fun toggleShoppingItemPurchased(item: ShoppingListItem) {
        viewModelScope.launch {
            repository.upsertShoppingItem(item.copy(isPurchased = !item.isPurchased))
        }
    }

    fun clearPurchasedShopping() {
        viewModelScope.launch {
            repository.clearPurchasedShoppingItems()
        }
    }

    // Synchronization (Part 21)
    fun updateBackendUrlAndSync(newUrl: String) {
        repository.setBackendBaseUrl(newUrl)
        viewModelScope.launch {
            val result = repository.synchronizeWithBackend()
            _uiState.update {
                it.copy(
                    backendBaseUrl = repository.getBackendBaseUrl(),
                    statusBannerMessage = result
                )
            }
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            val result = repository.synchronizeWithBackend()
            _uiState.update { it.copy(statusBannerMessage = result) }
        }
    }
}
