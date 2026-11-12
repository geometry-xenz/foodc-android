package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.MealSlot
import com.example.ui.DrishyamUiState
import com.example.ui.DrishyamViewModel
import com.example.ui.PrimaryTab
import com.example.ui.SubScreen
import com.example.ui.screens.*
import com.example.ui.theme.DrishyamColors
import com.example.ui.theme.DrishyamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrishyamTheme {
                val drishyamViewModel: DrishyamViewModel = viewModel()
                val uiState by drishyamViewModel.uiState.collectAsStateWithLifecycle()
                DrishyamAppRoot(
                    state = uiState,
                    viewModel = drishyamViewModel
                )
            }
        }
    }
}

@Composable
fun DrishyamAppRoot(
    state: DrishyamUiState,
    viewModel: DrishyamViewModel
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DrishyamColors.Background),
        containerColor = DrishyamColors.Background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (state.activeSubScreen != SubScreen.COOKING_MODE && state.activeSubScreen != SubScreen.ONBOARDING_WIZARD) {
                DrishyamBottomBar(
                    activeTab = state.activeTab,
                    onSelectTab = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.activeSubScreen) {
                SubScreen.ONBOARDING_WIZARD -> {
                    OnboardingWizardScreen(
                        state = state,
                        onSetStep = { viewModel.setOnboardingStep(it) },
                        onUpdateProfile = { viewModel.updateProfile(it) },
                        onCompleteOnboarding = { viewModel.completeOnboarding() }
                    )
                }
                SubScreen.RECIPE_DETAIL -> {
                    RecipeDetailScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onAdjustServings = { viewModel.adjustSelectedServings(it) },
                        onToggleSave = { viewModel.toggleSaveRecipe(it) },
                        onStartCooking = { viewModel.startCookingMode(it) },
                        onAddToMealPlan = { recipe ->
                            viewModel.addRecipeToMealPlan(recipe, state.selectedPlannerDayIndex, MealSlot.DINNER)
                        },
                        onInspectFlavour = { canonical ->
                            viewModel.selectFlavourIngredient(canonical)
                            viewModel.openSubScreen(SubScreen.FLAVOUR_SCIENCE)
                        }
                    )
                }
                SubScreen.COOKING_MODE -> {
                    CookingModeScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onSelectStep = { viewModel.setCookingStep(it) },
                        onToggleTimer = { viewModel.toggleCookingTimer() },
                        onCompleteAndLogMeal = { viewModel.completeCookingAndLogMeal() }
                    )
                }
                SubScreen.FLAVOUR_SCIENCE -> {
                    FlavourScienceScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onSelectIngredient = { viewModel.selectFlavourIngredient(it) },
                        onSelectCompound = { viewModel.selectAromaCompound(it) },
                        onSubstitutionQueryChange = { viewModel.recomputeSubstitutions(it) }
                    )
                }
                SubScreen.MEAL_PLANNER -> {
                    MealPlannerScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onSelectDay = { viewModel.selectPlannerDay(it) },
                        onAddRecipeToDay = { rcp, day, slot -> viewModel.addRecipeToMealPlan(rcp, day, slot) },
                        onRemovePlannedMeal = { viewModel.removePlannedMeal(it) },
                        onGenerateShoppingList = {
                            viewModel.generateShoppingFromMissingAndLowStock()
                            viewModel.openSubScreen(SubScreen.SHOPPING_LIST)
                        },
                        onGenerateAutoWeeklyPlan = { viewModel.generateAutoWeeklyMealPlan() }
                    )
                }
                SubScreen.SHOPPING_LIST -> {
                    ShoppingListScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onGenerateSmartSuggestions = { viewModel.generateShoppingFromMissingAndLowStock() },
                        onAddManualItem = { name, qty, unit -> viewModel.addManualShoppingItem(name, qty, unit) },
                        onTogglePurchased = { viewModel.toggleShoppingItemPurchased(it) },
                        onClearPurchased = { viewModel.clearPurchasedShopping() }
                    )
                }
                SubScreen.PROFILE_AND_GOALS -> {
                    ProfileAndGoalsScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onUpdateProfile = { viewModel.updateProfile(it) },
                        onOpenAuthOrSync = { viewModel.openSubScreen(SubScreen.SETTINGS_AND_SYNC) },
                        onOpenOnboardingWizard = {
                            viewModel.setOnboardingStep(0)
                            viewModel.openSubScreen(SubScreen.ONBOARDING_WIZARD)
                        }
                    )
                }
                SubScreen.SETTINGS_AND_SYNC, SubScreen.AUTH_PORTAL -> {
                    SettingsAndSyncScreen(
                        state = state,
                        onBack = { viewModel.navigateBack() },
                        onSetAuthStep = { viewModel.setAuthFlowStep(it) },
                        onUpdateBackendUrlAndSync = { viewModel.updateBackendUrlAndSync(it) },
                        onLoginOrRegister = { email, pass, name -> viewModel.loginOrRegister(email, pass, name) },
                        onResetPassword = { email, newPass -> viewModel.resetPassword(email, newPass) },
                        onLogout = { viewModel.logout() }
                    )
                }
                SubScreen.NONE -> {
                    when (state.activeTab) {
                        PrimaryTab.HOME -> HomeDashboardScreen(
                            state = state,
                            onSelectTab = { viewModel.selectTab(it) },
                            onOpenSubScreen = { viewModel.openSubScreen(it) },
                            onOpenRecipe = { viewModel.openRecipeDetail(it) },
                            onAddUnlockToPantry = { canonical ->
                                viewModel.addOrUpdatePantryManual(canonical, 200.0, "g")
                            },
                            onTriggerSync = { viewModel.triggerCloudSync() }
                        )
                        PrimaryTab.SCAN -> LiveScannerScreen(
                            state = state,
                            onCameraFrameAnalyzed = { boxes, fps, latency ->
                                viewModel.onCameraFrameAnalyzed(boxes, fps, latency)
                            },
                            onBitmapInspected = { bmp, source ->
                                viewModel.onBitmapInspected(bmp, source)
                            },
                            onCustomModelLoaded = { fileName ->
                                viewModel.onCustomModelLoaded(fileName)
                            },
                            onToggleFreeze = { viewModel.toggleScannerFreeze() },
                            onToggleRunning = { viewModel.toggleScannerRunning() },
                            onToggleLens = { viewModel.toggleCameraLens() },
                            onToggleTorch = { viewModel.toggleTorch() },
                            onToggleSound = { viewModel.toggleScannerSound() },
                            onToggleHaptics = { viewModel.toggleScannerHaptics() },
                            onCycleSimulatedTargets = { viewModel.cycleSimulatedCameraTargets() },
                            onConfirmSingle = { viewModel.confirmSingleScannedCandidate(it) },
                            onConfirmAll = { viewModel.confirmAllPendingDetections() },
                            onDismissCandidate = { viewModel.dismissPendingCandidate(it) },
                            onUpdateCandidate = { orig, name, qty, unit ->
                                viewModel.updatePendingCandidate(orig, name, qty, unit)
                            },
                            onRequestDeepInspect = { viewModel.requestDeepInspectConsent() },
                            onDismissDeepInspect = { viewModel.dismissDeepInspectConsent() },
                            onConfirmDeepInspect = { viewModel.executeDeepInspectWithConsent() },
                            onManualAdd = { name, qty, unit -> viewModel.addOrUpdatePantryManual(name, qty, unit) }
                        )
                        PrimaryTab.PANTRY -> PantryScreen(
                            state = state,
                            onSearchChange = { viewModel.setPantrySearchQuery(it) },
                            onCategorySelect = { viewModel.setPantryCategoryFilter(it) },
                            onAddOrUpdateItem = { name, qty, unit -> viewModel.addOrUpdatePantryManual(name, qty, unit) },
                            onDeleteItem = { viewModel.deletePantryItem(it) },
                            onOpenScanner = { viewModel.selectTab(PrimaryTab.SCAN) },
                            onInspectFlavourScience = { canonical ->
                                viewModel.selectFlavourIngredient(canonical)
                                viewModel.openSubScreen(SubScreen.FLAVOUR_SCIENCE)
                            }
                        )
                        PrimaryTab.DISCOVER -> DiscoverRecipesScreen(
                            state = state,
                            onSearchChange = { viewModel.setRecipeSearchQuery(it) },
                            onCuisineSelect = { viewModel.setCuisineFilter(it) },
                            onToggleShowSavedOnly = { viewModel.toggleShowSavedOnly() },
                            onCreateCustomRecipe = { title, sub, cuisine, prep, cook, srv, ings, steps, utensils ->
                                viewModel.createCustomRecipe(title, sub, cuisine, prep, cook, srv, ings, steps, utensils)
                            },
                            onOpenRecipe = { viewModel.openRecipeDetail(it) },
                            onOpenFlavourScience = { viewModel.openSubScreen(SubScreen.FLAVOUR_SCIENCE) }
                        )
                        PrimaryTab.NUTRITION -> NutritionDashboardScreen(
                            state = state,
                            onOpenGoals = { viewModel.openSubScreen(SubScreen.PROFILE_AND_GOALS) },
                            onOpenMealPlanner = { viewModel.openSubScreen(SubScreen.MEAL_PLANNER) },
                            onLogCustomMeal = { title, slot, srv, cal, p, c, f, fib ->
                                viewModel.logCustomMeal(title, slot, srv, cal, p, c, f, fib)
                            },
                            onDeleteMealLog = { viewModel.deleteMealLog(it) }
                        )
                    }
                }
            }

            // Transient Feedback Banner
            state.statusBannerMessage?.let { msg ->
                Surface(
                    color = DrishyamColors.ElevatedSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DrishyamColors.PrimaryOrange),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .clickable { viewModel.clearStatusBanner() }
                        .testTag("status_feedback_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = DrishyamColors.PrimaryOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DrishyamColors.PrimaryText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrishyamBottomBar(
    activeTab: PrimaryTab,
    onSelectTab: (PrimaryTab) -> Unit
) {
    NavigationBar(
        containerColor = DrishyamColors.Surface,
        tonalElevation = 8.dp
    ) {
        PrimaryTab.entries.forEach { tab ->
            val selected = activeTab == tab
            val icon = when (tab) {
                PrimaryTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                PrimaryTab.SCAN -> Icons.Filled.CenterFocusStrong
                PrimaryTab.PANTRY -> if (selected) Icons.Filled.Kitchen else Icons.Outlined.Kitchen
                PrimaryTab.DISCOVER -> if (selected) Icons.Filled.MenuBook else Icons.Outlined.MenuBook
                PrimaryTab.NUTRITION -> if (selected) Icons.Filled.Analytics else Icons.Outlined.Analytics
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.label
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DrishyamColors.Background,
                    selectedTextColor = DrishyamColors.PrimaryOrange,
                    indicatorColor = DrishyamColors.PrimaryOrange,
                    unselectedIconColor = DrishyamColors.SecondaryText,
                    unselectedTextColor = DrishyamColors.SecondaryText
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
