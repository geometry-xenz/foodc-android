# DRISHYAM — Native Android Computational Gastronomy & Intelligent Nutrition Platform

DRISHYAM is a native Android culinary intelligence application written in Kotlin with Jetpack Compose, Room, CameraX, and Retrofit. It connects physical kitchen inventory with Foodoscope RecipeDB, FlavorDB molecular aroma networks, and an 8-goal physiological nutrition engine.

## Architecture & Module Package Layout

```text
app/src/main/java/com/example/
├── domain/
│   ├── model/DrishyamModels.kt          # Pure Kotlin domain entities (8 Fitness Goals, Pantry, Recipes, FlavorDB, Sync)
│   └── usecase/DrishyamUseCases.kt      # Mifflin-St Jeor NutritionEngine, ComputationalGastronomyEngine, AliasNormalizer
├── vision/
│   └── ScannerPipeline.kt               # IoU, Non-Maximum Suppression (NMS), Rotation Mapping, 1000ms Temporal Tracker
├── data/
│   ├── FoodoscopeSeedCatalog.kt         # Verified FlavorDB aroma compounds, chemical formulas & RecipeDB dataset
│   ├── local/DrishyamDatabase.kt        # Room SQLite Database, DAOs, SyncOutbox & Android Keystore AES-GCM Vault
│   ├── remote/DrishyamApiService.kt     # Retrofit REST API interfaces for the shared DRISHYAM backend
│   └── repository/DrishyamRepository.kt # Offline-first repository & idempotent outbox synchronization
└── ui/
    ├── theme/                           # Editorial dark luxury theme (#080808, #FF8A32, #E8B56A) + local Google Fonts
    ├── DrishyamViewModel.kt             # Unidirectional Data Flow state holder
    └── screens/                         # Home, LiveScanner, Pantry, Discover, RecipeDetail, CookingMode, FlavorDB, Nutrition, Planner, Shopping, Profile, Sync
```

## Key Capabilities

1. **Real-Time AI Ingredient Scanner (`ScannerScreen.kt` & `ScannerPipeline.kt`)**
   - CameraX live preview with rear/front switching, torch control, freeze-frame, and audio/haptic confirmation toggles.
   - Non-Maximum Suppression (`IoU = 0.45`), orientation coordinate mapping, and timestamp-based **1,000ms stability lock** at `>= 75%` confidence.
   - Uncertain (`< 75%`) detections are highlighted in red and never auto-accumulated.
   - Consent-gated **Deep Inspect** single-frame capture workflow.
   - **Custom YOLO Weights**: Place a trained `.tflite` or `.onnx` culinary model in `app/src/main/assets/` (e.g., `yolov8n_drishyam_culinary_fp16.tflite`). When custom weights are not bundled, the scanner explicitly displays its Demonstration & Spectral Assist banner.

2. **Computational Gastronomy & Foodoscope RecipeDB (`DrishyamUseCases.kt`)**
   - Enforces hard constraints (declared Allergies, Ingredient Exclusions, Dietary Lifestyle, Jain/Gluten/Dairy rules) before ranking.
   - Computes the exact 4-part equation:
     `score = 0.30 * core_coverage + 0.20 * counter_utilisation + 0.20 * flavour_coherence + 0.30 * gap_fit`
   - Includes the **Single-Ingredient Unlock Engine** and full-screen **Interactive Cooking Mode** with scaled quantities and step timers.

3. **FlavorDB Molecular Science (`FlavourAndNutritionScreens.kt`)**
   - Interactive Compose `Canvas` molecular network visualizing ingredient nodes, shared volatile aroma compounds (`ar-Curcumene`, `Cuminaldehyde`, `1,8-Cineole`, `Linalool`, `Eugenol`, `Capsaicin`, `Allicin`), and chemical substitution scores.

4. **8-Goal Physiological Nutrition Engine (`DrishyamUseCases.kt`)**
   - Supports *Fat Loss / Cutting*, *Lean Bulk*, *Traditional Bulk*, *Dirty Bulk Preference*, *Maintenance*, *Body Recomposition*, *Athletic Performance*, and *General Health* using the Mifflin-St Jeor equation with transparent assumptions and safety warnings.

5. **Cross-Platform Synchronization (`DrishyamRepository.kt` & `DrishyamApiService.kt`)**
   - Android Keystore AES-GCM token storage, configurable DRISHYAM backend URL in **Account & Sync**, offline-first Room persistence, and an idempotent Sync Outbox.
