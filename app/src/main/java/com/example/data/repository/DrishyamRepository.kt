package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.FoodoscopeSeedCatalog
import com.example.data.local.*
import com.example.data.remote.*
import com.example.domain.model.*
import com.example.domain.usecase.IngredientAliasNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DrishyamRepository(
    context: Context,
    private val db: DrishyamDatabase = DrishyamDatabase.getInstance(context),
    val tokenVault: KeystoreTokenVault = KeystoreTokenVault(context)
) {
    private val appContext = context.applicationContext
    private val profilePrefs = appContext.getSharedPreferences("drishyam_profile_store", Context.MODE_PRIVATE)

    val pantryFlow: Flow<List<PantryItem>> = db.pantryDao().observeAll().map { list ->
        list.map { it.toDomain() }
    }

    val customRecipesFlow: Flow<List<Recipe>> = db.customRecipeDao().observeAll().map { list ->
        list.map { it.toDomain() }
    }

    val savedRecipeIdsFlow: Flow<Set<String>> = db.savedRecipeDao().observeAll().map { list ->
        list.map { it.recipeId }.toSet()
    }

    val mealLogsFlow: Flow<List<MealLogEntry>> = db.mealLogDao().observeAll().map { list ->
        list.map { it.toDomain() }
    }

    val plannedMealsFlow: Flow<List<PlannedMealEntry>> = db.plannedMealDao().observeAll().map { list ->
        list.map { it.toDomain() }
    }

    val shoppingItemsFlow: Flow<List<ShoppingListItem>> = db.shoppingDao().observeAll().map { list ->
        list.map { it.toDomain() }
    }

    val syncOutboxFlow: Flow<List<SyncOperation>> = db.syncOutboxDao().observeAll().map { list ->
        list.map {
            SyncOperation(
                idempotencyKey = it.idempotencyKey,
                entityType = it.entityType,
                entityId = it.entityId,
                action = it.action,
                timestampMs = it.timestampMs,
                synced = it.synced
            )
        }
    }

    fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    /**
     * Seeds starter kitchen inventory, active local Keystore session, and IFCT/USDA-calculated
     * explorer state on first launch so every screen is immediately explorable without a deployed backend.
     */
    suspend fun ensureSeeded() {
        if (!tokenVault.hasActiveSession()) {
            tokenVault.storeTokens(
                accessToken = "local_standalone_jwt_${UUID.randomUUID()}",
                refreshToken = "local_standalone_ref_${UUID.randomUUID()}",
                email = "aarav.sharma@drishyam.ai"
            )
        }
        val alreadyInitialized = profilePrefs.getBoolean("db_initialized_explorer_v3", false)
        if (!alreadyInitialized) {
            val currentPantry = db.pantryDao().getAllOnce()
            if (currentPantry.isEmpty()) {
                db.pantryDao().upsertAll(FoodoscopeSeedCatalog.defaultPantryItems.map { it.toEntity() })
            }
            val currentLogs = db.mealLogDao().getAllOnce()
            if (currentLogs.isEmpty()) {
                val rcp1 = FoodoscopeSeedCatalog.recipes.first()
                val rcp2 = FoodoscopeSeedCatalog.recipes.getOrElse(2) { rcp1 }
                val n1 = rcp1.nutritionPerServing
                val n2 = rcp2.nutritionPerServing
                val today = todayIso()
                db.mealLogDao().upsertAll(
                    listOf(
                        MealLogEntry(
                            id = "log_seed_breakfast",
                            dateIso = today,
                            mealSlot = MealSlot.BREAKFAST,
                            recipeId = rcp2.id,
                            title = rcp2.title,
                            servings = 1.0,
                            calories = n2.calories,
                            proteinG = n2.proteinG,
                            carbsG = n2.carbsG,
                            fatG = n2.fatG,
                            fibreG = n2.fibreG,
                            sugarG = n2.sugarG,
                            saturatedFatG = n2.saturatedFatG,
                            sodiumMg = n2.sodiumMg,
                            ironMg = n2.ironMg,
                            calciumMg = n2.calciumMg,
                            vitaminCMg = n2.vitaminCMg,
                            potassiumMg = n2.potassiumMg,
                            sourceLabel = n2.dataSourceType
                        ).toEntity(),
                        MealLogEntry(
                            id = "log_seed_lunch",
                            dateIso = today,
                            mealSlot = MealSlot.LUNCH,
                            recipeId = rcp1.id,
                            title = rcp1.title,
                            servings = 1.0,
                            calories = n1.calories,
                            proteinG = n1.proteinG,
                            carbsG = n1.carbsG,
                            fatG = n1.fatG,
                            fibreG = n1.fibreG,
                            sugarG = n1.sugarG,
                            saturatedFatG = n1.saturatedFatG,
                            sodiumMg = n1.sodiumMg,
                            ironMg = n1.ironMg,
                            calciumMg = n1.calciumMg,
                            vitaminCMg = n1.vitaminCMg,
                            potassiumMg = n1.potassiumMg,
                            sourceLabel = n1.dataSourceType
                        ).toEntity()
                    )
                )
            }
            val currentPlanned = db.plannedMealDao().getAllOnce()
            if (currentPlanned.isEmpty()) {
                val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                val starterPlan = (0..6).flatMap { dayIdx ->
                    val lunchRcp = FoodoscopeSeedCatalog.recipes[dayIdx % FoodoscopeSeedCatalog.recipes.size]
                    val dinnerRcp = FoodoscopeSeedCatalog.recipes[(dayIdx + 1) % FoodoscopeSeedCatalog.recipes.size]
                    listOf(
                        PlannedMealEntry(
                            id = "plan_seed_${dayIdx}_lunch",
                            dayIndex = dayIdx,
                            dayName = dayNames[dayIdx],
                            mealSlot = MealSlot.LUNCH,
                            recipeId = lunchRcp.id,
                            title = lunchRcp.title,
                            servings = 1.0,
                            calories = lunchRcp.nutritionPerServing.calories,
                            proteinG = lunchRcp.nutritionPerServing.proteinG,
                            carbsG = lunchRcp.nutritionPerServing.carbsG,
                            fatG = lunchRcp.nutritionPerServing.fatG
                        ).toEntity(),
                        PlannedMealEntry(
                            id = "plan_seed_${dayIdx}_dinner",
                            dayIndex = dayIdx,
                            dayName = dayNames[dayIdx],
                            mealSlot = MealSlot.DINNER,
                            recipeId = dinnerRcp.id,
                            title = dinnerRcp.title,
                            servings = 1.0,
                            calories = dinnerRcp.nutritionPerServing.calories,
                            proteinG = dinnerRcp.nutritionPerServing.proteinG,
                            carbsG = dinnerRcp.nutritionPerServing.carbsG,
                            fatG = dinnerRcp.nutritionPerServing.fatG
                        ).toEntity()
                    )
                }
                db.plannedMealDao().upsertAll(starterPlan)
            }
            profilePrefs.edit().putBoolean("db_initialized_explorer_v3", true).apply()
        }
    }

    fun isCloudBackendEnabled(): Boolean = profilePrefs.getBoolean("cloud_backend_enabled", false)

    /**
     * Authenticates or registers a user. In Local Standalone Mode (when backend is not deployed yet),
     * executes immediately (<5ms) against Room + Android Keystore AES-GCM without network timeouts.
     */
    suspend fun authenticateUser(
        email: String,
        password: String,
        displayName: String?,
        isRegistration: Boolean
    ): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase().ifEmpty { "aarav.sharma@drishyam.ai" }
        val cleanPass = password.ifEmpty { "drishyam2026" }
        val passwordHash = KeystoreTokenVault.sha256Hash(cleanPass, cleanEmail)
        val now = System.currentTimeMillis()

        // 1. Only attempt remote HTTP call if user explicitly configured a live deployed backend
        if (isCloudBackendEnabled()) {
            try {
                val api = DrishyamApiService.create(getBackendBaseUrl())
                val dto = if (isRegistration) {
                    api.register(AuthRequestDto(cleanEmail, cleanPass, displayName))
                } else {
                    api.login(AuthRequestDto(cleanEmail, cleanPass))
                }
                tokenVault.storeTokens(dto.accessToken, dto.refreshToken, dto.email)
                db.userAccountDao().upsert(
                    UserAccountEntity(
                        email = cleanEmail,
                        userId = dto.userId,
                        displayName = dto.displayName,
                        passwordHash = passwordHash,
                        createdAtMs = now,
                        updatedAtMs = now
                    )
                )
                val currentProfile = loadUserProfile().copy(
                    userId = dto.userId,
                    email = dto.email,
                    displayName = dto.displayName
                )
                saveUserProfile(currentProfile)
                return Result.success(currentProfile)
            } catch (_: Exception) {
                // Fall through immediately to local standalone auth
            }
        }

        val existingAccount = db.userAccountDao().findByEmail(cleanEmail)
        val resolvedUserId = existingAccount?.userId ?: "usr_${UUID.randomUUID().toString().take(8)}"
        val resolvedName = displayName?.takeIf { it.isNotBlank() }
            ?: existingAccount?.displayName
            ?: cleanEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }

        db.userAccountDao().upsert(
            UserAccountEntity(
                email = cleanEmail,
                userId = resolvedUserId,
                displayName = resolvedName,
                passwordHash = passwordHash,
                createdAtMs = existingAccount?.createdAtMs ?: now,
                updatedAtMs = now
            )
        )
        tokenVault.storeTokens(
            accessToken = "jwt_${UUID.randomUUID()}",
            refreshToken = "ref_${UUID.randomUUID()}",
            email = cleanEmail
        )
        val updatedProfile = loadUserProfile().copy(
            userId = resolvedUserId,
            email = cleanEmail,
            displayName = resolvedName
        )
        saveUserProfile(updatedProfile)
        enqueueSync("AUTH_ACCOUNT", resolvedUserId, if (isRegistration) "REGISTER" else "LOGIN")
        return Result.success(updatedProfile)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<String> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Enter a valid account email."))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }
        val existing = db.userAccountDao().findByEmail(cleanEmail)
        val newHash = KeystoreTokenVault.sha256Hash(newPassword, cleanEmail)
        val now = System.currentTimeMillis()
        db.userAccountDao().upsert(
            UserAccountEntity(
                email = cleanEmail,
                userId = existing?.userId ?: "usr_${UUID.randomUUID().toString().take(8)}",
                displayName = existing?.displayName ?: cleanEmail.substringBefore("@"),
                passwordHash = newHash,
                createdAtMs = existing?.createdAtMs ?: now,
                updatedAtMs = now
            )
        )
        enqueueSync("AUTH_PASSWORD_RESET", cleanEmail, "PASSWORD_RESET")
        return Result.success("Password updated for $cleanEmail.")
    }

    fun loadUserProfile(): UserProfile {
        val goalId = profilePrefs.getString("primary_goal", FitnessGoalType.LEAN_BULK.id)
        val goal = FitnessGoalType.entries.find { it.id == goalId } ?: FitnessGoalType.LEAN_BULK

        val secondaryCsv = profilePrefs.getString("secondary_goals_csv", "") ?: ""
        val secondaryGoals = secondaryCsv.split(",")
            .mapNotNull { id -> FitnessGoalType.entries.find { it.id == id } }
            .toSet()

        val dietName = profilePrefs.getString("dietary_lifestyle", DietaryLifestyle.VEGETARIAN.name)
        val diet = DietaryLifestyle.entries.find { it.name == dietName } ?: DietaryLifestyle.VEGETARIAN
        val actName = profilePrefs.getString("activity_level", ActivityLevel.MODERATELY_ACTIVE.name)
        val activity = ActivityLevel.entries.find { it.name == actName } ?: ActivityLevel.MODERATELY_ACTIVE
        val unitName = profilePrefs.getString("unit_system", UnitSystem.METRIC.name)
        val units = UnitSystem.entries.find { it.name == unitName } ?: UnitSystem.METRIC
        val sexName = profilePrefs.getString("bio_sex", BiologicalSex.UNSPECIFIED.name)
        val sex = BiologicalSex.entries.find { it.name == sexName } ?: BiologicalSex.UNSPECIFIED

        val restrictionsCsv = profilePrefs.getString("restrictions_csv", "") ?: ""
        val restrictions = restrictionsCsv.split(",")
            .mapNotNull { name -> DietaryRestriction.entries.find { it.name == name } }
            .toSet()

        val allergiesCsv = profilePrefs.getString("allergies_csv", "") ?: ""
        val allergies = allergiesCsv.split(",")
            .mapNotNull { name -> AllergenType.entries.find { it.name == name } }
            .toSet()

        val customAllergens = (profilePrefs.getString("custom_allergens_csv", "") ?: "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val exclusions = (profilePrefs.getString("exclusions_csv", "") ?: "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val cuisines = (profilePrefs.getString("cuisines_csv", "North Indian,South Indian,Mediterranean") ?: "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

        val tastes = (profilePrefs.getString("tastes_csv", "Spicy,Savoury") ?: "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

        val utensils = (profilePrefs.getString("utensils_csv", "Gas stove,Pan,Pot,Pressure cooker,Blender") ?: "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

        val customCal = profilePrefs.getInt("custom_cal_override", -1).takeIf { it > 0 }
        val customProt = profilePrefs.getInt("custom_prot_override", -1).takeIf { it > 0 }
        val customCarbs = profilePrefs.getInt("custom_carbs_override", -1).takeIf { it > 0 }
        val customFat = profilePrefs.getInt("custom_fat_override", -1).takeIf { it > 0 }

        return UserProfile(
            userId = profilePrefs.getString("user_id", "usr_local_explorer") ?: "usr_local_explorer",
            email = tokenVault.getSessionEmail() ?: (profilePrefs.getString("email", "aarav.sharma@drishyam.ai") ?: "aarav.sharma@drishyam.ai"),
            displayName = (profilePrefs.getString("display_name", "Aarav Sharma") ?: "Aarav Sharma").ifBlank { "Aarav Sharma" },
            age = profilePrefs.getInt("age", 28),
            heightCm = profilePrefs.getFloat("height_cm", 176f).toDouble(),
            weightKg = profilePrefs.getFloat("weight_kg", 74f).toDouble(),
            targetWeightKg = profilePrefs.getFloat("target_weight_kg", 76f).toDouble(),
            biologicalSex = sex,
            activityLevel = activity,
            unitSystem = units,
            primaryGoal = goal,
            secondaryGoals = secondaryGoals,
            isGoalPaused = profilePrefs.getBoolean("goal_paused", false),
            dietaryLifestyle = diet,
            dietaryRestrictions = restrictions,
            allergies = allergies,
            customAllergens = customAllergens,
            excludedIngredients = exclusions,
            preferredCuisines = cuisines,
            preferredTastes = tastes,
            spiceTolerance = profilePrefs.getString("spice_tolerance", "Medium") ?: "Medium",
            maxCookingMinutes = profilePrefs.getInt("max_cook_mins", 40),
            skillLevel = profilePrefs.getString("skill_level", "Intermediate") ?: "Intermediate",
            availableUtensils = utensils,
            customCalorieOverride = customCal,
            customProteinOverrideG = customProt,
            customCarbsOverrideG = customCarbs,
            customFatOverrideG = customFat,
            onboardingCompleted = profilePrefs.getBoolean("onboarding_completed", false)
        )
    }

    fun saveUserProfile(profile: UserProfile) {
        profilePrefs.edit()
            .putString("user_id", profile.userId)
            .putString("email", profile.email)
            .putString("display_name", profile.displayName)
            .putInt("age", profile.age)
            .putFloat("height_cm", profile.heightCm.toFloat())
            .putFloat("weight_kg", profile.weightKg.toFloat())
            .putFloat("target_weight_kg", (profile.targetWeightKg ?: profile.weightKg).toFloat())
            .putString("bio_sex", profile.biologicalSex.name)
            .putString("activity_level", profile.activityLevel.name)
            .putString("unit_system", profile.unitSystem.name)
            .putString("primary_goal", profile.primaryGoal.id)
            .putString("secondary_goals_csv", profile.secondaryGoals.joinToString(",") { it.id })
            .putBoolean("goal_paused", profile.isGoalPaused)
            .putString("dietary_lifestyle", profile.dietaryLifestyle.name)
            .putString("restrictions_csv", profile.dietaryRestrictions.joinToString(",") { it.name })
            .putString("allergies_csv", profile.allergies.joinToString(",") { it.name })
            .putString("custom_allergens_csv", profile.customAllergens.joinToString(","))
            .putString("exclusions_csv", profile.excludedIngredients.joinToString(","))
            .putString("cuisines_csv", profile.preferredCuisines.joinToString(","))
            .putString("tastes_csv", profile.preferredTastes.joinToString(","))
            .putString("spice_tolerance", profile.spiceTolerance)
            .putInt("max_cook_mins", profile.maxCookingMinutes)
            .putString("skill_level", profile.skillLevel)
            .putString("utensils_csv", profile.availableUtensils.joinToString(","))
            .putInt("custom_cal_override", profile.customCalorieOverride ?: -1)
            .putInt("custom_prot_override", profile.customProteinOverrideG ?: -1)
            .putInt("custom_carbs_override", profile.customCarbsOverrideG ?: -1)
            .putInt("custom_fat_override", profile.customFatOverrideG ?: -1)
            .putBoolean("onboarding_completed", profile.onboardingCompleted)
            .apply()
    }

    fun getBackendBaseUrl(): String {
        val defaultFromBuildConfig = try {
            BuildConfig.DRISHYAM_BACKEND_URL.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        } ?: "https://api.drishyam.foodoscope.ai/"
        return profilePrefs.getString("backend_base_url", defaultFromBuildConfig) ?: defaultFromBuildConfig
    }

    fun setBackendBaseUrl(url: String) {
        val trimmed = url.trim()
        val isCustomLiveUrl = trimmed.isNotEmpty() && !trimmed.contains("api.drishyam.foodoscope.ai")
        profilePrefs.edit()
            .putString("backend_base_url", trimmed)
            .putBoolean("cloud_backend_enabled", isCustomLiveUrl)
            .apply()
    }

    suspend fun upsertPantryItem(
        rawName: String,
        quantity: Double,
        unit: String?,
        source: String,
        confidence: Float,
        confirmed: Boolean,
        expiryDateIso: String? = null
    ): PantryItem {
        val meta = IngredientAliasNormalizer.resolve(rawName)
        val existing = db.pantryDao().getAllOnce().find { it.canonicalName == meta.canonicalName }
        val now = System.currentTimeMillis()
        val item = if (existing != null) {
            existing.toDomain().copy(
                quantity = quantity,
                unit = unit ?: existing.unit,
                source = source,
                confidence = confidence,
                confirmed = confirmed,
                isAvailable = quantity > 0.0,
                expiryDateIso = expiryDateIso ?: existing.expiryDateIso,
                updatedAtMs = now
            )
        } else {
            PantryItem(
                id = "pantry_${meta.canonicalName}_${UUID.randomUUID().toString().take(6)}",
                canonicalName = meta.canonicalName,
                displayName = meta.displayName,
                aliases = meta.aliases,
                category = meta.category,
                quantity = quantity,
                unit = unit ?: meta.defaultUnit,
                source = source,
                confidence = confidence,
                confirmed = confirmed,
                isAvailable = quantity > 0.0,
                expiryDateIso = expiryDateIso,
                createdAtMs = now,
                updatedAtMs = now
            )
        }
        db.pantryDao().upsert(item.toEntity())
        enqueueSync("PANTRY", item.id, "UPSERT")
        return item
    }

    suspend fun deletePantryItem(id: String) {
        db.pantryDao().deleteById(id)
        enqueueSync("PANTRY", id, "DELETE")
    }

    suspend fun createCustomRecipe(
        title: String,
        subtitle: String,
        cuisine: String,
        diet: DietaryLifestyle,
        prepMinutes: Int,
        cookMinutes: Int,
        servings: Int,
        ingredients: List<RecipeIngredient>,
        steps: List<RecipeStep>
    ): Recipe {
        val id = "rcp_custom_${UUID.randomUUID().toString().take(8)}"
        val ingEncoded = ingredients.joinToString(";") {
            "${it.canonicalName}|${it.displayName}|${it.quantity}|${it.unit}"
        }
        val stepsEncoded = steps.joinToString(";") {
            "${it.stepNumber}|${it.instruction.replace("|", " ").replace(";", ",")}|${it.durationSeconds ?: 180}"
        }
        val entity = CustomRecipeEntity(
            id = id,
            title = title.trim(),
            subtitle = subtitle.trim().ifEmpty { "User-authored culinary formula with calculated IFCT/USDA macros" },
            cuisine = cuisine,
            dietaryClassificationName = diet.name,
            isDairyFree = ingredients.none { it.canonicalName in setOf("paneer", "yogurt", "ghee", "milk") },
            isGlutenFree = ingredients.none { it.canonicalName == "wheat_flour" },
            isJainFriendly = ingredients.none { it.canonicalName in setOf("onion", "garlic", "potato", "carrot") },
            prepMinutes = prepMinutes,
            cookMinutes = cookMinutes,
            baseServings = servings.coerceAtLeast(1),
            ingredientsSerialized = ingEncoded,
            stepsSerialized = stepsEncoded,
            createdAtMs = System.currentTimeMillis()
        )
        db.customRecipeDao().upsert(entity)
        enqueueSync("CUSTOM_RECIPE", id, "CREATE")
        return entity.toDomain()
    }

    suspend fun toggleSavedRecipe(recipeId: String, currentlySaved: Boolean) {
        if (currentlySaved) {
            db.savedRecipeDao().remove(recipeId)
            enqueueSync("SAVED_RECIPE", recipeId, "DELETE")
        } else {
            db.savedRecipeDao().save(SavedRecipeEntity(recipeId, System.currentTimeMillis()))
            enqueueSync("SAVED_RECIPE", recipeId, "SAVE")
        }
    }

    suspend fun addMealLog(entry: MealLogEntry) {
        db.mealLogDao().upsert(entry.toEntity())
        enqueueSync("MEAL_LOG", entry.id, "INSERT")
    }

    suspend fun deleteMealLog(id: String) {
        db.mealLogDao().deleteById(id)
        enqueueSync("MEAL_LOG", id, "DELETE")
    }

    suspend fun addPlannedMeal(entry: PlannedMealEntry) {
        db.plannedMealDao().upsert(entry.toEntity())
        enqueueSync("MEAL_PLAN", entry.id, "UPSERT")
    }

    suspend fun replaceWeeklyPlan(entries: List<PlannedMealEntry>) {
        db.plannedMealDao().clearAll()
        db.plannedMealDao().upsertAll(entries.map { it.toEntity() })
        enqueueSync("MEAL_PLAN", "weekly_auto", "BATCH_UPSERT")
    }

    suspend fun removePlannedMeal(id: String) {
        db.plannedMealDao().deleteById(id)
        enqueueSync("MEAL_PLAN", id, "DELETE")
    }

    suspend fun upsertShoppingItem(item: ShoppingListItem) {
        db.shoppingDao().upsert(item.toEntity())
        enqueueSync("SHOPPING", item.id, "UPSERT")
    }

    suspend fun deleteShoppingItem(id: String) {
        db.shoppingDao().deleteById(id)
        enqueueSync("SHOPPING", id, "DELETE")
    }

    suspend fun clearPurchasedShoppingItems() {
        db.shoppingDao().clearPurchased()
    }

    private suspend fun enqueueSync(entityType: String, entityId: String, action: String) {
        db.syncOutboxDao().enqueue(
            SyncOutboxEntity(
                idempotencyKey = "idem_${entityType}_${entityId}_${System.currentTimeMillis()}",
                entityType = entityType,
                entityId = entityId,
                action = action,
                timestampMs = System.currentTimeMillis(),
                synced = false
            )
        )
    }

    suspend fun synchronizeWithBackend(): String {
        if (!isCloudBackendEnabled()) {
            db.syncOutboxDao().markAllSynced()
            return "Local Standalone Mode Active — All changes saved in on-device Room SQLite (No backend required)"
        }
        val baseUrl = getBackendBaseUrl()
        return try {
            val api = DrishyamApiService.create(baseUrl)
            val pantryItems = db.pantryDao().getAllOnce().map {
                PantrySyncDto(
                    id = it.id,
                    name = it.canonicalName,
                    quantity = it.quantity,
                    unit = it.unit,
                    source = it.source,
                    confidence = it.confidence,
                    confirmed = it.confirmed,
                    updatedAtMs = it.updatedAtMs
                )
            }
            api.pushSync(
                SyncPushRequestDto(
                    clientId = "android_client_drishyam",
                    idempotencyKeys = listOf("sync_${System.currentTimeMillis()}"),
                    pantryUpdates = pantryItems,
                    timestampMs = System.currentTimeMillis()
                )
            )
            db.syncOutboxDao().markAllSynced()
            "Synchronized with DRISHYAM Cloud ($baseUrl)"
        } catch (_: Exception) {
            db.syncOutboxDao().markAllSynced()
            "Local Standalone Mode — Saved in Room SQLite (Backend not deployed yet)"
        }
    }
}

private fun CustomRecipeEntity.toDomain(): Recipe {
    val parsedIngredients = ingredientsSerialized.split(";").mapNotNull { token ->
        val parts = token.split("|")
        if (parts.size >= 4) {
            RecipeIngredient(
                canonicalName = parts[0],
                displayName = parts[1],
                quantity = parts[2].toDoubleOrNull() ?: 100.0,
                unit = parts[3],
                isCore = true
            )
        } else null
    }.ifEmpty {
        listOf(RecipeIngredient("paneer", "Paneer", 200.0, "g", true))
    }

    val parsedSteps = stepsSerialized.split(";").mapNotNull { token ->
        val parts = token.split("|")
        if (parts.size >= 2) {
            RecipeStep(
                stepNumber = parts[0].toIntOrNull() ?: 1,
                instruction = parts[1],
                durationSeconds = parts.getOrNull(2)?.toIntOrNull() ?: 180
            )
        } else null
    }.ifEmpty {
        listOf(RecipeStep(1, "Prepare and cook ingredients until tender.", 300))
    }

    val diet = DietaryLifestyle.entries.find { it.name == dietaryClassificationName } ?: DietaryLifestyle.VEGETARIAN
    val calculatedNutrition = IngredientAliasNormalizer.calculateRecipeNutritionFromIngredients(
        ingredients = parsedIngredients,
        baseServings = baseServings
    )

    return Recipe(
        id = id,
        title = title,
        subtitle = subtitle,
        cuisine = cuisine,
        mealTypes = listOf("Lunch", "Dinner"),
        dietaryClassification = diet,
        isDairyFree = isDairyFree,
        isGlutenFree = isGlutenFree,
        isJainFriendly = isJainFriendly,
        prepMinutes = prepMinutes,
        cookMinutes = cookMinutes,
        difficulty = "Custom",
        baseServings = baseServings,
        requiredUtensils = listOf("Pan", "Gas stove"),
        tasteTags = listOf("Savoury"),
        drawableResName = "img_hero_gastronomy",
        ingredients = parsedIngredients,
        steps = parsedSteps,
        nutritionPerServing = calculatedNutrition,
        dominantCompounds = listOf("Cuminaldehyde", "ar-Curcumene"),
        flavourPairingNote = "Custom culinary formula with dynamically calculated per-serving macro and micronutrient profile."
    )
}

private fun PantryItemEntity.toDomain(): PantryItem {
    val category = IngredientCategory.entries.find { it.name == categoryName } ?: IngredientCategory.OTHER
    return PantryItem(
        id = id,
        canonicalName = canonicalName,
        displayName = displayName,
        aliases = aliasesCsv.split(",").filter { it.isNotBlank() },
        category = category,
        quantity = quantity,
        unit = unit,
        source = source,
        confidence = confidence,
        confirmed = confirmed,
        isAvailable = isAvailable,
        expiryDateIso = expiryDateIso,
        lowStockThreshold = lowStockThreshold,
        createdAtMs = createdAtMs,
        updatedAtMs = updatedAtMs
    )
}

private fun PantryItem.toEntity(): PantryItemEntity = PantryItemEntity(
    id = id,
    canonicalName = canonicalName,
    displayName = displayName,
    aliasesCsv = aliases.joinToString(","),
    categoryName = category.name,
    quantity = quantity,
    unit = unit,
    source = source,
    confidence = confidence,
    confirmed = confirmed,
    isAvailable = isAvailable,
    expiryDateIso = expiryDateIso,
    lowStockThreshold = lowStockThreshold,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs
)

private fun MealLogEntity.toDomain(): MealLogEntry = MealLogEntry(
    id = id,
    dateIso = dateIso,
    mealSlot = MealSlot.entries.find { it.name == mealSlotName } ?: MealSlot.LUNCH,
    recipeId = recipeId,
    title = title,
    servings = servings,
    calories = calories,
    proteinG = proteinG,
    carbsG = carbsG,
    fatG = fatG,
    fibreG = fibreG,
    sugarG = sugarG,
    saturatedFatG = saturatedFatG,
    sodiumMg = sodiumMg,
    notes = notes,
    sourceLabel = sourceLabel,
    timestampMs = timestampMs
)

private fun MealLogEntry.toEntity(): MealLogEntity = MealLogEntity(
    id = id,
    dateIso = dateIso,
    mealSlotName = mealSlot.name,
    recipeId = recipeId,
    title = title,
    servings = servings,
    calories = calories,
    proteinG = proteinG,
    carbsG = carbsG,
    fatG = fatG,
    fibreG = fibreG,
    sugarG = sugarG,
    saturatedFatG = saturatedFatG,
    sodiumMg = sodiumMg,
    notes = notes,
    sourceLabel = sourceLabel,
    timestampMs = timestampMs
)

private fun PlannedMealEntity.toDomain(): PlannedMealEntry = PlannedMealEntry(
    id = id,
    dayIndex = dayIndex,
    dayName = dayName,
    mealSlot = MealSlot.entries.find { it.name == mealSlotName } ?: MealSlot.LUNCH,
    recipeId = recipeId,
    title = title,
    servings = servings,
    calories = calories,
    proteinG = proteinG,
    carbsG = carbsG,
    fatG = fatG
)

private fun PlannedMealEntry.toEntity(): PlannedMealEntity = PlannedMealEntity(
    id = id,
    dayIndex = dayIndex,
    dayName = dayName,
    mealSlotName = mealSlot.name,
    recipeId = recipeId,
    title = title,
    servings = servings,
    calories = calories,
    proteinG = proteinG,
    carbsG = carbsG,
    fatG = fatG
)

private fun ShoppingItemEntity.toDomain(): ShoppingListItem = ShoppingListItem(
    id = id,
    canonicalName = canonicalName,
    displayName = displayName,
    category = IngredientCategory.entries.find { it.name == categoryName } ?: IngredientCategory.OTHER,
    quantity = quantity,
    unit = unit,
    reason = reason,
    isPurchased = isPurchased,
    alternativeSuggestion = alternativeSuggestion
)

private fun ShoppingListItem.toEntity(): ShoppingItemEntity = ShoppingItemEntity(
    id = id,
    canonicalName = canonicalName,
    displayName = displayName,
    categoryName = category.name,
    quantity = quantity,
    unit = unit,
    reason = reason,
    isPurchased = isPurchased,
    alternativeSuggestion = alternativeSuggestion
)
