package com.example.data

import com.example.domain.model.*
import com.example.domain.usecase.IngredientAliasNormalizer
import kotlin.math.roundToInt

/**
 * Curated Foodoscope RecipeDB & FlavorDB molecular gastronomy dataset
 * providing verified aroma compounds, botanical families, chemical bridges,
 * and complete Indian & global recipes with USDA/IFCT nutritional profiles.
 */
object FoodoscopeSeedCatalog {

    val aromaCompounds: Map<String, AromaCompound> = mapOf(
        "curcumene" to AromaCompound(
            id = "curcumene",
            name = "ar-Curcumene",
            chemicalFormula = "C15H22",
            functionalGroup = "Sesquiterpene",
            aromaDescriptors = listOf("Earthy", "Woody", "Warm Spice", "Root-like"),
            flavorDbId = "FDB001204"
        ),
        "cuminaldehyde" to AromaCompound(
            id = "cuminaldehyde",
            name = "Cuminaldehyde",
            chemicalFormula = "C10H12O",
            functionalGroup = "Aromatic Aldehyde",
            aromaDescriptors = listOf("Toasted Cumin", "Pungent", "Herbal", "Warm"),
            flavorDbId = "FDB000891"
        ),
        "linalool" to AromaCompound(
            id = "linalool",
            name = "Linalool",
            chemicalFormula = "C10H18O",
            functionalGroup = "Monoterpene Alcohol",
            aromaDescriptors = listOf("Citrus-Floral", "Coriander Seed", "Fresh", "Sweet"),
            flavorDbId = "FDB002115"
        ),
        "cineole" to AromaCompound(
            id = "cineole",
            name = "1,8-Cineole (Eucalyptol)",
            chemicalFormula = "C10H18O",
            functionalGroup = "Cyclic Ether / Terpenoid",
            aromaDescriptors = listOf("Cooling", "Cardamom-like", "Camphoraceous", "Fresh Herb"),
            flavorDbId = "FDB001480"
        ),
        "eugenol" to AromaCompound(
            id = "eugenol",
            name = "Eugenol",
            chemicalFormula = "C10H12O2",
            functionalGroup = "Phenylpropanoid",
            aromaDescriptors = listOf("Clove", "Warm Phenolic", "Sweet-Spicy", "Woody"),
            flavorDbId = "FDB000512"
        ),
        "capsaicin" to AromaCompound(
            id = "capsaicin",
            name = "Capsaicin",
            chemicalFormula = "C18H27NO3",
            functionalGroup = "Vanilloid Alkaloid",
            aromaDescriptors = listOf("Pungent Heat", "Trigeminal Warmth", "Fruity Pepper"),
            flavorDbId = "FDB003190"
        ),
        "allicin" to AromaCompound(
            id = "allicin",
            name = "Allicin & Diallyl Disulfide",
            chemicalFormula = "C6H10OS2",
            functionalGroup = "Organosulfur",
            aromaDescriptors = listOf("Savory Allium", "Roasted Garlic", "Umami Depth"),
            flavorDbId = "FDB000944"
        ),
        "zingiberene" to AromaCompound(
            id = "zingiberene",
            name = "alpha-Zingiberene",
            chemicalFormula = "C15H24",
            functionalGroup = "Monocyclic Sesquiterpene",
            aromaDescriptors = listOf("Zesty Ginger", "Citrusy Warmth", "Aromatic Root"),
            flavorDbId = "FDB001872"
        ),
        "citral" to AromaCompound(
            id = "citral",
            name = "Citral (Geranial + Neral)",
            chemicalFormula = "C10H16O",
            functionalGroup = "Terpene Aldehyde",
            aromaDescriptors = listOf("Bright Lemon", "Zesty", "High-Note Acidic"),
            flavorDbId = "FDB000630"
        ),
        "piperine" to AromaCompound(
            id = "piperine",
            name = "Piperine",
            chemicalFormula = "C17H19NO3",
            functionalGroup = "Alkaloid",
            aromaDescriptors = listOf("Sharp Pepper", "Woody Heat", "Bioavailability Enhancer"),
            flavorDbId = "FDB002741"
        ),
        "furaneol" to AromaCompound(
            id = "furaneol",
            name = "Furaneol & Lycopene Volatiles",
            chemicalFormula = "C6H8O3",
            functionalGroup = "Furanone",
            aromaDescriptors = listOf("Caramelized Tomato", "Savory-Sweet", "Roasted Umami"),
            flavorDbId = "FDB001109"
        ),
        "lactones" to AromaCompound(
            id = "lactones",
            name = "delta-Decalactone & Diacetyl",
            chemicalFormula = "C10H18O2",
            functionalGroup = "Lactone / Diketone",
            aromaDescriptors = listOf("Creamy Dairy", "Cultured Butter", "Rich Mouthfeel"),
            flavorDbId = "FDB000399"
        )
    )

    private fun c(vararg ids: String): List<AromaCompound> = ids.mapNotNull { aromaCompounds[it] }

    val flavourProfiles: Map<String, IngredientFlavourProfile> = mapOf(
        "turmeric_powder" to IngredientFlavourProfile(
            canonicalName = "turmeric_powder",
            displayName = "Turmeric Powder",
            category = IngredientCategory.SPICES,
            botanicalFamily = "Zingiberaceae",
            aliases = listOf("haldi", "turmeric", "curcuma longa"),
            keyCompounds = c("curcumene", "cineole", "zingiberene"),
            complementaryIngredients = listOf("ginger", "cumin_seeds", "black_pepper_powder", "mustard_seeds"),
            culinaryRole = "Warm earthy base note; fat-soluble curcuminoids pair synergistically with piperine in black pepper."
        ),
        "cumin_seeds" to IngredientFlavourProfile(
            canonicalName = "cumin_seeds",
            displayName = "Cumin Seeds",
            category = IngredientCategory.SPICES,
            botanicalFamily = "Apiaceae",
            aliases = listOf("jeera", "cumin", "zeera"),
            keyCompounds = c("cuminaldehyde", "cineole", "linalool"),
            complementaryIngredients = listOf("coriander_powder", "coriander_seeds", "turmeric_powder", "garlic", "tomato"),
            culinaryRole = "High-impact tempering aromatic that releases toasted pyrazines and cuminaldehyde in hot oil/ghee."
        ),
        "coriander_powder" to IngredientFlavourProfile(
            canonicalName = "coriander_powder",
            displayName = "Coriander Powder",
            category = IngredientCategory.SPICES,
            botanicalFamily = "Apiaceae",
            aliases = listOf("dhania powder", "ground coriander"),
            keyCompounds = c("linalool", "cineole", "citral"),
            complementaryIngredients = listOf("cumin_seeds", "lemon", "cardamom", "ginger"),
            culinaryRole = "Floral-citrus terpene bridge that mellows pungent spices and thickens masala gravies."
        ),
        "red_chilli_powder" to IngredientFlavourProfile(
            canonicalName = "red_chilli_powder",
            displayName = "Red Chilli Powder",
            category = IngredientCategory.SPICES,
            botanicalFamily = "Solanaceae",
            aliases = listOf("lal mirch", "kashmiri mirch", "cayenne"),
            keyCompounds = c("capsaicin", "furaneol"),
            complementaryIngredients = listOf("tomato", "lemon", "garlic", "yogurt"),
            culinaryRole = "Provides trigeminal capsaicin warmth and vibrant carotenoid color; tempered by dairy lactones."
        ),
        "garam_masala" to IngredientFlavourProfile(
            canonicalName = "garam_masala",
            displayName = "Garam Masala",
            category = IngredientCategory.SPICES,
            botanicalFamily = "Multi-Botanical Blend",
            aliases = listOf("spice blend", "warming masala"),
            keyCompounds = c("eugenol", "cineole", "piperine", "cuminaldehyde"),
            complementaryIngredients = listOf("cardamom", "cloves", "cinnamon_sticks", "black_pepper_powder"),
            culinaryRole = "Finishing aromatic blend rich in volatile phenylpropanoids and terpenes."
        ),
        "cardamom" to IngredientFlavourProfile(
            canonicalName = "cardamom",
            displayName = "Cardamom",
            category = IngredientCategory.SPICES,
            botanicalFamily = "Zingiberaceae",
            aliases = listOf("elaichi", "green cardamom"),
            keyCompounds = c("cineole", "linalool", "zingiberene"),
            complementaryIngredients = listOf("cinnamon_sticks", "cloves", "rice", "paneer"),
            culinaryRole = "High-note cooling terpene aromatic essential to biryanis, pulaos, and rich curries."
        ),
        "cloves" to IngredientFlavourProfile(
            canonicalName = "cloves",
            displayName = "Cloves",
            category = IngredientCategory.SPICES,
            botanicalFamily = "Myrtaceae",
            aliases = listOf("laung", "syzygium aromaticum"),
            keyCompounds = c("eugenol", "piperine"),
            complementaryIngredients = listOf("cinnamon_sticks", "cardamom", "bay_leaf", "onion"),
            culinaryRole = "Dominant eugenol donor creating deep phenolic warmth in slow-cooked masalas."
        ),
        "ginger" to IngredientFlavourProfile(
            canonicalName = "ginger",
            displayName = "Ginger",
            category = IngredientCategory.VEGETABLES,
            botanicalFamily = "Zingiberaceae",
            aliases = listOf("adrak", "fresh ginger"),
            keyCompounds = c("zingiberene", "curcumene", "citral", "cineole"),
            complementaryIngredients = listOf("garlic", "turmeric_powder", "lemon", "green_chilli"),
            culinaryRole = "Shares sesquiterpene backbone with turmeric and citral lift with lemon."
        ),
        "garlic" to IngredientFlavourProfile(
            canonicalName = "garlic",
            displayName = "Garlic",
            category = IngredientCategory.VEGETABLES,
            botanicalFamily = "Amaryllidaceae",
            aliases = listOf("lehsun", "garlic cloves"),
            keyCompounds = c("allicin", "furaneol"),
            complementaryIngredients = listOf("onion", "ginger", "tomato", "cumin_seeds"),
            culinaryRole = "Organosulfur powerhouse that undergoes Maillard browning to form savory backbone."
        ),
        "onion" to IngredientFlavourProfile(
            canonicalName = "onion",
            displayName = "Onion",
            category = IngredientCategory.VEGETABLES,
            botanicalFamily = "Amaryllidaceae",
            aliases = listOf("pyaaz", "red onion"),
            keyCompounds = c("allicin", "furaneol"),
            complementaryIngredients = listOf("tomato", "garlic", "ginger", "cumin_seeds"),
            culinaryRole = "Caramelized fructans and sulfur volatiles build structural sweetness and body."
        ),
        "tomato" to IngredientFlavourProfile(
            canonicalName = "tomato",
            displayName = "Tomato",
            category = IngredientCategory.VEGETABLES,
            botanicalFamily = "Solanaceae",
            aliases = listOf("tamatar", "roma tomato"),
            keyCompounds = c("furaneol", "citral", "linalool"),
            complementaryIngredients = listOf("onion", "garlic", "coriander_leaves", "cumin_seeds"),
            culinaryRole = "Natural glutamate (umami) and organic acids balance rich fats and pungent spices."
        ),
        "lemon" to IngredientFlavourProfile(
            canonicalName = "lemon",
            displayName = "Lemon",
            category = IngredientCategory.FRUITS,
            botanicalFamily = "Rutaceae",
            aliases = listOf("nimbu", "lime"),
            keyCompounds = c("citral", "linalool"),
            complementaryIngredients = listOf("coriander_leaves", "ginger", "coriander_powder", "black_pepper_powder"),
            culinaryRole = "Citric acid and citral brighten heavy proteins and lift volatile top notes."
        ),
        "paneer" to IngredientFlavourProfile(
            canonicalName = "paneer",
            displayName = "Paneer",
            category = IngredientCategory.DAIRY,
            botanicalFamily = "Cultured / Acid-Set Dairy",
            aliases = listOf("cottage cheese", "indian paneer"),
            keyCompounds = c("lactones", "furaneol"),
            complementaryIngredients = listOf("yogurt", "capsicum", "garam_masala", "turmeric_powder"),
            culinaryRole = "High-casein protein matrix that absorbs fat-soluble spice aromatics and browns cleanly."
        ),
        "yogurt" to IngredientFlavourProfile(
            canonicalName = "yogurt",
            displayName = "Greek / Plain Yogurt",
            category = IngredientCategory.DAIRY,
            botanicalFamily = "Fermented Dairy",
            aliases = listOf("dahi", "curd", "hung curd"),
            keyCompounds = c("lactones", "citral"),
            complementaryIngredients = listOf("paneer", "cumin_seeds", "garlic", "red_chilli_powder"),
            culinaryRole = "Lactic acid tenderizes proteins while dairy lactones buffer capsaicin heat."
        ),
        "chickpeas" to IngredientFlavourProfile(
            canonicalName = "chickpeas",
            displayName = "Chickpeas",
            category = IngredientCategory.LEGUMES,
            botanicalFamily = "Fabaceae",
            aliases = listOf("chana", "kabuli chana", "garbanzo"),
            keyCompounds = c("furaneol", "linalool"),
            complementaryIngredients = listOf("cumin_seeds", "coriander_powder", "tomato", "onion", "ginger"),
            culinaryRole = "High-fibre complex carbohydrate and plant protein base with nutty earthiness."
        ),
        "lentils" to IngredientFlavourProfile(
            canonicalName = "lentils",
            displayName = "Red & Yellow Lentils",
            category = IngredientCategory.LEGUMES,
            botanicalFamily = "Fabaceae",
            aliases = listOf("dal", "moong dal", "toor dal", "masoor dal"),
            keyCompounds = c("furaneol", "curcumene"),
            complementaryIngredients = listOf("turmeric_powder", "cumin_seeds", "garlic", "ghee", "tomato"),
            culinaryRole = "Foundational Indian legume rich in lysine, iron, and soluble dietary fibre."
        )
    )

    fun computeSubstitutions(
        missingCanonicalOrRaw: String,
        pantryCanonicals: Set<String>
    ): List<SubstitutionCandidate> {
        val targetMeta = IngredientAliasNormalizer.resolve(missingCanonicalOrRaw)
        val targetProfile = flavourProfiles[targetMeta.canonicalName]
        val targetCompounds = targetProfile?.keyCompounds?.map { it.id }?.toSet() ?: setOf("cineole", "linalool")

        return flavourProfiles.values
            .filter { it.canonicalName != targetMeta.canonicalName }
            .map { candidate ->
                val shared = candidate.keyCompounds.filter { it.id in targetCompounds }
                val overlap = if (targetCompounds.isEmpty()) 0.35 else {
                    (shared.size.toDouble() / targetCompounds.size).coerceIn(0.15, 0.95)
                }
                val sameCategoryBonus = if (candidate.category == targetMeta.category) 0.20 else 0.0
                val score = (overlap * 0.8 + sameCategoryBonus).coerceIn(0.15, 0.96)

                SubstitutionCandidate(
                    targetIngredient = targetMeta.displayName,
                    substituteCanonicalName = candidate.canonicalName,
                    substituteDisplayName = candidate.displayName,
                    sharedCompounds = shared.ifEmpty { candidate.keyCompounds.take(1) },
                    compoundOverlapRatio = (score * 100).roundToInt() / 100.0,
                    flavourSimilarityDescription = "Shares ${shared.joinToString(", ") { it.name }.ifEmpty { "compatible aromatic functional groups" }} (${candidate.botanicalFamily}).",
                    expectedTasteDifference = when (candidate.category) {
                        IngredientCategory.SPICES -> "Slightly more ${candidate.keyCompounds.firstOrNull()?.aromaDescriptors?.firstOrNull()?.lowercase() ?: "aromatic"} profile; adjust tempering time by 10–15 seconds."
                        IngredientCategory.DAIRY -> "Maintains creamy lactone mouthfeel with slightly different moisture content."
                        IngredientCategory.LEGUMES -> "Comparable protein-to-fibre matrix; adjust simmer time by 5–8 minutes."
                        else -> "Preserves structural role while shifting subtle top-note aromatics."
                    },
                    bestForCuisines = listOf("North Indian", "South Indian", "Mediterranean"),
                    ratioAdjustment = "Use 1 : 1 by weight/volume",
                    inPantry = candidate.canonicalName in pantryCanonicals
                )
            }
            .sortedWith(compareByDescending<SubstitutionCandidate> { it.inPantry }.thenByDescending { it.compoundOverlapRatio })
            .take(5)
    }

    val recipes: List<Recipe> = listOf(
        Recipe(
            id = "rcp_paneer_tikka",
            title = "Smoky Tandoori Paneer Tikka",
            subtitle = "High-protein yogurt-marinated cottage cheese charred with bell peppers, onion, and toasted cumin-ajwain aromatics",
            cuisine = "North Indian",
            mealTypes = listOf("Lunch", "Dinner", "Post-Workout"),
            dietaryClassification = DietaryLifestyle.VEGETARIAN,
            isDairyFree = false,
            isGlutenFree = true,
            isJainFriendly = false,
            prepMinutes = 15,
            cookMinutes = 15,
            difficulty = "Easy",
            baseServings = 2,
            requiredUtensils = listOf("Pan", "Gas stove"),
            tasteTags = listOf("Smoky", "Spicy", "Savoury"),
            drawableResName = "img_recipe_paneer_tikka",
            ingredients = listOf(
                RecipeIngredient("paneer", "Paneer", 250.0, "g", isCore = true),
                RecipeIngredient("yogurt", "Greek / Plain Yogurt", 100.0, "g", isCore = true),
                RecipeIngredient("capsicum", "Capsicum", 120.0, "g", isCore = true),
                RecipeIngredient("onion", "Onion", 100.0, "g", isCore = false),
                RecipeIngredient("ginger", "Ginger", 15.0, "g", isCore = false),
                RecipeIngredient("garlic", "Garlic", 15.0, "g", isCore = false),
                RecipeIngredient("turmeric_powder", "Turmeric Powder", 5.0, "g", isCore = false),
                RecipeIngredient("red_chilli_powder", "Red Chilli Powder", 8.0, "g", isCore = false),
                RecipeIngredient("garam_masala", "Garam Masala", 6.0, "g", isCore = false),
                RecipeIngredient("lemon", "Lemon", 1.0, "pcs", isCore = false)
            ),
            steps = listOf(
                RecipeStep(
                    1,
                    "Whisk thick yogurt with turmeric powder, red chilli powder, garam masala, minced ginger-garlic, and lemon juice until velvety.",
                    durationSeconds = 180,
                    highlightedIngredients = listOf("Yogurt", "Turmeric Powder", "Red Chilli Powder", "Garam Masala"),
                    scientificTip = "Lactic acid in yogurt denatures surface proteins gently while dairy lactones dissolve fat-soluble curcuminoids and capsaicin."
                ),
                RecipeStep(
                    2,
                    "Fold cubed paneer, diced capsicum, and onion petals into the marinade. Rest for 10 minutes to allow surface adsorption.",
                    durationSeconds = 600,
                    highlightedIngredients = listOf("Paneer", "Capsicum", "Onion"),
                    scientificTip = "Resting allows volatile terpenes from garam masala to penetrate the porous casein structure."
                ),
                RecipeStep(
                    3,
                    "Sear in a smoking-hot cast-iron pan for 3–4 minutes per side until edges blister with golden Maillard char.",
                    durationSeconds = 420,
                    highlightedIngredients = listOf("Paneer"),
                    scientificTip = "High surface heat (>180°C) triggers Maillard reactions between lactose and amino acids without drying the core."
                ),
                RecipeStep(
                    4,
                    "Finish with a squeeze of fresh lemon juice and serve immediately.",
                    durationSeconds = 60,
                    highlightedIngredients = listOf("Lemon"),
                    scientificTip = "Citral and citric acid cut through rich dairy fats to heighten perceived savouriness."
                )
            ),
            nutritionPerServing = RecipeNutrition(
                calories = 420,
                proteinG = 28.5,
                carbsG = 14.0,
                fatG = 27.0,
                fibreG = 3.8,
                sugarG = 5.2,
                saturatedFatG = 14.1,
                sodiumMg = 410,
                ironMg = 2.4,
                calciumMg = 540,
                vitaminCMg = 68.0,
                potassiumMg = 420
            ),
            dominantCompounds = listOf("delta-Decalactone", "ar-Curcumene", "Eugenol", "Capsaicin"),
            flavourPairingNote = "Dairy lactones bridge with phenolic eugenol and sesquiterpenes while capsicum pyrazines add roasted depth."
        ),
        Recipe(
            id = "rcp_saffron_biryani",
            title = "Saffron Chickpea & Vegetable Dum Biryani",
            subtitle = "Aromatic layered basmati rice & protein-rich chickpeas slow-steamed with whole spices, caramelized onion, and fresh herbs",
            cuisine = "North Indian",
            mealTypes = listOf("Lunch", "Dinner"),
            dietaryClassification = DietaryLifestyle.VEGETARIAN,
            isDairyFree = false,
            isGlutenFree = true,
            isJainFriendly = false,
            prepMinutes = 15,
            cookMinutes = 25,
            difficulty = "Intermediate",
            baseServings = 3,
            requiredUtensils = listOf("Pot", "Gas stove"),
            tasteTags = listOf("Savoury", "Rich", "Spicy"),
            drawableResName = "img_recipe_saffron_biryani",
            ingredients = listOf(
                RecipeIngredient("rice", "Basmati Rice", 220.0, "g", isCore = true),
                RecipeIngredient("chickpeas", "Chickpeas", 200.0, "g", isCore = true),
                RecipeIngredient("onion", "Onion", 150.0, "g", isCore = true),
                RecipeIngredient("tomato", "Tomato", 120.0, "g", isCore = false),
                RecipeIngredient("yogurt", "Greek / Plain Yogurt", 80.0, "g", isCore = false),
                RecipeIngredient("cumin_seeds", "Cumin Seeds", 6.0, "g", isCore = true),
                RecipeIngredient("cardamom", "Cardamom", 4.0, "g", isCore = false),
                RecipeIngredient("cloves", "Cloves", 3.0, "g", isCore = false),
                RecipeIngredient("turmeric_powder", "Turmeric Powder", 5.0, "g", isCore = false),
                RecipeIngredient("coriander_leaves", "Coriander Leaves", 25.0, "g", isCore = false)
            ),
            steps = listOf(
                RecipeStep(
                    1,
                    "Parboil soaked basmati rice with cardamom and cloves for 6 minutes until 75% tender, then drain.",
                    durationSeconds = 360,
                    highlightedIngredients = listOf("Basmati Rice", "Cardamom", "Cloves"),
                    scientificTip = "1,8-Cineole from cardamom and eugenol from cloves infuse into gelatinizing amylose starch granules."
                ),
                RecipeStep(
                    2,
                    "Caramelize sliced onions until deep golden, then temper cumin seeds, turmeric powder, diced tomatoes, yogurt, and cooked chickpeas.",
                    durationSeconds = 600,
                    highlightedIngredients = listOf("Onion", "Cumin Seeds", "Chickpeas", "Tomato"),
                    scientificTip = "Slow onion caramelization generates furaneol and sulfur volatiles that anchor the chickpea masala."
                ),
                RecipeStep(
                    3,
                    "Layer the aromatic basmati rice over the spiced chickpea masala, top with fresh coriander leaves, seal tightly, and steam on low (Dum) for 10 minutes.",
                    durationSeconds = 600,
                    highlightedIngredients = listOf("Basmati Rice", "Coriander Leaves"),
                    scientificTip = "Closed-vessel Dum steaming traps volatile monoterpenes (linalool, cuminaldehyde) inside the rice canopy."
                )
            ),
            nutritionPerServing = RecipeNutrition(
                calories = 485,
                proteinG = 17.8,
                carbsG = 76.0,
                fatG = 11.5,
                fibreG = 9.4,
                sugarG = 6.8,
                saturatedFatG = 3.4,
                sodiumMg = 380,
                ironMg = 4.2,
                calciumMg = 165,
                vitaminCMg = 24.0,
                potassiumMg = 590
            ),
            dominantCompounds = listOf("1,8-Cineole", "Cuminaldehyde", "Linalool", "Eugenol"),
            flavourPairingNote = "High-note terpene ethers from cardamom and coriander interlock with toasted cuminaldehyde and caramelized onion furanones."
        ),
        Recipe(
            id = "rcp_moong_palak_dal",
            title = "High-Protein Moong Dal & Spinach Tadka",
            subtitle = "Velvety split yellow lentils simmered with iron-rich spinach and tempered with roasted garlic, cumin, and turmeric",
            cuisine = "North Indian",
            mealTypes = listOf("Lunch", "Dinner", "Post-Workout"),
            dietaryClassification = DietaryLifestyle.VEGAN,
            isDairyFree = true,
            isGlutenFree = true,
            isJainFriendly = false,
            prepMinutes = 10,
            cookMinutes = 20,
            difficulty = "Easy",
            baseServings = 2,
            requiredUtensils = listOf("Pressure cooker", "Pan", "Gas stove"),
            tasteTags = listOf("Savoury", "Mild", "Low-oil"),
            drawableResName = "img_hero_gastronomy",
            ingredients = listOf(
                RecipeIngredient("lentils", "Red & Yellow Lentils", 180.0, "g", isCore = true),
                RecipeIngredient("spinach", "Spinach", 150.0, "g", isCore = false),
                RecipeIngredient("tomato", "Tomato", 100.0, "g", isCore = true),
                RecipeIngredient("onion", "Onion", 80.0, "g", isCore = false),
                RecipeIngredient("garlic", "Garlic", 12.0, "g", isCore = true),
                RecipeIngredient("ginger", "Ginger", 10.0, "g", isCore = false),
                RecipeIngredient("cumin_seeds", "Cumin Seeds", 6.0, "g", isCore = true),
                RecipeIngredient("turmeric_powder", "Turmeric Powder", 5.0, "g", isCore = true),
                RecipeIngredient("lemon", "Lemon", 1.0, "pcs", isCore = false)
            ),
            steps = listOf(
                RecipeStep(
                    1,
                    "Pressure-cook rinsed lentils with turmeric powder, diced tomato, and ginger for 8 minutes until creamy.",
                    durationSeconds = 480,
                    highlightedIngredients = listOf("Red & Yellow Lentils", "Turmeric Powder", "Tomato"),
                    scientificTip = "Pressure gelatinizes lentil starches rapidly while preserving heat-stable curcuminoids."
                ),
                RecipeStep(
                    2,
                    "Fold wilted spinach leaves into the hot lentils and simmer for 3 minutes.",
                    durationSeconds = 180,
                    highlightedIngredients = listOf("Spinach"),
                    scientificTip = "Brief simmering softens oxalates while retaining folates and lutein."
                ),
                RecipeStep(
                    3,
                    "Bloom cumin seeds and sliced garlic in a hot pan for 60 seconds until fragrant, pour over the dal, and finish with fresh lemon juice.",
                    durationSeconds = 90,
                    highlightedIngredients = listOf("Cumin Seeds", "Garlic", "Lemon"),
                    scientificTip = "Vitamin C in lemon juice chelates non-heme iron from lentils and spinach, increasing intestinal absorption up to 3x."
                )
            ),
            nutritionPerServing = RecipeNutrition(
                calories = 340,
                proteinG = 23.4,
                carbsG = 51.0,
                fatG = 4.8,
                fibreG = 12.6,
                sugarG = 4.1,
                saturatedFatG = 0.7,
                sodiumMg = 290,
                ironMg = 6.1,
                calciumMg = 140,
                vitaminCMg = 42.0,
                potassiumMg = 780
            ),
            dominantCompounds = listOf("Cuminaldehyde", "Allicin", "ar-Curcumene", "Citral"),
            flavourPairingNote = "Organosulfur allicin derivatives and toasted cuminaldehyde cut grassy spinach notes while citral lifts the finish."
        ),
        Recipe(
            id = "rcp_jain_jeera_aloo_dal",
            title = "Jain-Friendly Royal Jeera Aloo & Golden Dal",
            subtitle = "Zero-allium cumin-crusted baby carrots, capsicum & golden lentils tempered with ginger, green chilli, and fresh coriander",
            cuisine = "Rajasthani",
            mealTypes = listOf("Lunch", "Dinner"),
            dietaryClassification = DietaryLifestyle.VEGAN,
            isDairyFree = true,
            isGlutenFree = true,
            isJainFriendly = true,
            prepMinutes = 10,
            cookMinutes = 18,
            difficulty = "Easy",
            baseServings = 2,
            requiredUtensils = listOf("Pan", "Pot", "Gas stove"),
            tasteTags = listOf("Mild", "Savoury", "Tangy"),
            drawableResName = "img_hero_gastronomy",
            ingredients = listOf(
                RecipeIngredient("lentils", "Red & Yellow Lentils", 160.0, "g", isCore = true),
                RecipeIngredient("tomato", "Tomato", 120.0, "g", isCore = true),
                RecipeIngredient("capsicum", "Capsicum", 100.0, "g", isCore = true),
                RecipeIngredient("cumin_seeds", "Cumin Seeds", 8.0, "g", isCore = true),
                RecipeIngredient("coriander_powder", "Coriander Powder", 8.0, "g", isCore = true),
                RecipeIngredient("turmeric_powder", "Turmeric Powder", 5.0, "g", isCore = true),
                RecipeIngredient("green_chilli", "Green Chilli", 6.0, "g", isCore = false),
                RecipeIngredient("coriander_leaves", "Coriander Leaves", 20.0, "g", isCore = false),
                RecipeIngredient("lemon", "Lemon", 1.0, "pcs", isCore = false)
            ),
            steps = listOf(
                RecipeStep(
                    1,
                    "Simmer yellow lentils with turmeric powder and diced tomatoes for 12 minutes until tender and golden.",
                    durationSeconds = 720,
                    highlightedIngredients = listOf("Red & Yellow Lentils", "Turmeric Powder", "Tomato"),
                    scientificTip = "Tomato glutamates build rich umami depth without requiring onion or garlic."
                ),
                RecipeStep(
                    2,
                    "Crackle a generous measure of cumin seeds with coriander powder, green chilli, and diced capsicum for 5 minutes.",
                    durationSeconds = 300,
                    highlightedIngredients = listOf("Cumin Seeds", "Coriander Powder", "Capsicum"),
                    scientificTip = "Combining cuminaldehyde and linalool creates a full aromatic mid-palate that compensates for absent alliums."
                ),
                RecipeStep(
                    3,
                    "Garnish generously with chopped coriander leaves and fresh lemon juice.",
                    durationSeconds = 60,
                    highlightedIngredients = listOf("Coriander Leaves", "Lemon")
                )
            ),
            nutritionPerServing = RecipeNutrition(
                calories = 310,
                proteinG = 20.2,
                carbsG = 48.0,
                fatG = 4.2,
                fibreG = 11.0,
                sugarG = 4.5,
                saturatedFatG = 0.6,
                sodiumMg = 270,
                ironMg = 4.8,
                calciumMg = 95,
                vitaminCMg = 58.0,
                potassiumMg = 640
            ),
            dominantCompounds = listOf("Cuminaldehyde", "Linalool", "ar-Curcumene", "Citral"),
            flavourPairingNote = "Pure terpene-aldehyde harmony designed for Jain and allium-free kitchens."
        ),
        Recipe(
            id = "rcp_masala_egg_bhurji",
            title = "Spiced Masala Egg Bhurji Skillet",
            subtitle = "Quick high-bioavailability scrambled eggs folded with caramelized onion, tomato, green chilli, turmeric, and coriander",
            cuisine = "Maharashtrian",
            mealTypes = listOf("Breakfast", "Post-Workout", "Dinner"),
            dietaryClassification = DietaryLifestyle.EGGETARIAN,
            isDairyFree = true,
            isGlutenFree = true,
            isJainFriendly = false,
            prepMinutes = 5,
            cookMinutes = 10,
            difficulty = "Easy",
            baseServings = 2,
            requiredUtensils = listOf("Pan", "Gas stove"),
            tasteTags = listOf("Spicy", "Savoury"),
            drawableResName = "img_recipe_paneer_tikka",
            ingredients = listOf(
                RecipeIngredient("eggs", "Eggs", 4.0, "pcs", isCore = true),
                RecipeIngredient("onion", "Onion", 100.0, "g", isCore = true),
                RecipeIngredient("tomato", "Tomato", 100.0, "g", isCore = true),
                RecipeIngredient("green_chilli", "Green Chilli", 8.0, "g", isCore = false),
                RecipeIngredient("turmeric_powder", "Turmeric Powder", 4.0, "g", isCore = true),
                RecipeIngredient("cumin_seeds", "Cumin Seeds", 5.0, "g", isCore = false),
                RecipeIngredient("coriander_leaves", "Coriander Leaves", 15.0, "g", isCore = false)
            ),
            steps = listOf(
                RecipeStep(
                    1,
                    "Sauté cumin seeds, finely diced onion, and green chilli in a hot pan for 3 minutes until translucent.",
                    durationSeconds = 180,
                    highlightedIngredients = listOf("Cumin Seeds", "Onion", "Green Chilli")
                ),
                RecipeStep(
                    2,
                    "Add chopped tomatoes and turmeric powder; cook for 3 minutes until pulpy.",
                    durationSeconds = 180,
                    highlightedIngredients = listOf("Tomato", "Turmeric Powder")
                ),
                RecipeStep(
                    3,
                    "Pour in whisked eggs and fold gently over medium-low heat for 2–3 minutes until soft curds form. Top with coriander leaves.",
                    durationSeconds = 150,
                    highlightedIngredients = listOf("Eggs", "Coriander Leaves"),
                    scientificTip = "Gentle coagulation at ~70°C prevents disulfide cross-linking from squeezing moisture out of egg proteins."
                )
            ),
            nutritionPerServing = RecipeNutrition(
                calories = 295,
                proteinG = 24.6,
                carbsG = 9.2,
                fatG = 18.0,
                fibreG = 2.4,
                sugarG = 3.8,
                saturatedFatG = 4.9,
                sodiumMg = 340,
                ironMg = 3.2,
                calciumMg = 110,
                vitaminCMg = 28.0,
                potassiumMg = 390
            ),
            dominantCompounds = listOf("Allicin", "Furaneol", "ar-Curcumene", "Capsaicin"),
            flavourPairingNote = "Sulfur compounds in eggs and onions harmonize with tomato furanones and warm turmeric sesquiterpenes."
        ),
        Recipe(
            id = "rcp_rajma_masala_bowl",
            title = "Athletic Recovery Rajma & Basmati Power Bowl",
            subtitle = "Slow-simmered red kidney beans in a ginger-garlic tomato masala paired with steamed basmati for optimal glycogen & protein replenishment",
            cuisine = "Punjabi",
            mealTypes = listOf("Lunch", "Dinner", "Post-Workout"),
            dietaryClassification = DietaryLifestyle.VEGAN,
            isDairyFree = true,
            isGlutenFree = true,
            isJainFriendly = false,
            prepMinutes = 10,
            cookMinutes = 25,
            difficulty = "Easy",
            baseServings = 2,
            requiredUtensils = listOf("Pressure cooker", "Pot", "Gas stove"),
            tasteTags = listOf("Spicy", "Rich", "Savoury"),
            drawableResName = "img_recipe_saffron_biryani",
            ingredients = listOf(
                RecipeIngredient("kidney_beans", "Kidney Beans", 200.0, "g", isCore = true),
                RecipeIngredient("rice", "Basmati Rice", 150.0, "g", isCore = true),
                RecipeIngredient("onion", "Onion", 120.0, "g", isCore = true),
                RecipeIngredient("tomato", "Tomato", 150.0, "g", isCore = true),
                RecipeIngredient("ginger", "Ginger", 15.0, "g", isCore = true),
                RecipeIngredient("garlic", "Garlic", 15.0, "g", isCore = true),
                RecipeIngredient("cumin_seeds", "Cumin Seeds", 6.0, "g", isCore = true),
                RecipeIngredient("coriander_powder", "Coriander Powder", 8.0, "g", isCore = false),
                RecipeIngredient("garam_masala", "Garam Masala", 6.0, "g", isCore = false)
            ),
            steps = listOf(
                RecipeStep(
                    1,
                    "Temper cumin seeds, onion, ginger, and garlic until deeply browned, then add tomato puree and coriander powder.",
                    durationSeconds = 420,
                    highlightedIngredients = listOf("Cumin Seeds", "Onion", "Ginger", "Garlic", "Tomato")
                ),
                RecipeStep(
                    2,
                    "Add cooked kidney beans and simmer for 12 minutes, mashing a few beans to naturally thicken the gravy. Finish with garam masala and serve over steamed rice.",
                    durationSeconds = 720,
                    highlightedIngredients = listOf("Kidney Beans", "Garam Masala", "Basmati Rice"),
                    scientificTip = "Combining legume lysine with rice methionine yields a complete essential amino acid profile."
                )
            ),
            nutritionPerServing = RecipeNutrition(
                calories = 520,
                proteinG = 22.8,
                carbsG = 86.0,
                fatG = 7.4,
                fibreG = 14.8,
                sugarG = 5.6,
                saturatedFatG = 1.1,
                sodiumMg = 390,
                ironMg = 5.4,
                calciumMg = 135,
                vitaminCMg = 31.0,
                potassiumMg = 890
            ),
            dominantCompounds = listOf("alpha-Zingiberene", "Cuminaldehyde", "Eugenol", "Allicin"),
            flavourPairingNote = "Ginger sesquiterpenes and clove eugenol cut the earthy starchiness of red kidney beans."
        ),
        Recipe(
            id = "rcp_lemon_pepper_chicken",
            title = "Herb-Crusted Lemon Pepper Chicken Tikka",
            subtitle = "Lean high-protein chicken breast marinated in yogurt, crushed black peppercorns, lemon zest, garlic, and coriander",
            cuisine = "Mediterranean",
            mealTypes = listOf("Lunch", "Dinner", "Post-Workout"),
            dietaryClassification = DietaryLifestyle.NON_VEGETARIAN,
            isDairyFree = false,
            isGlutenFree = true,
            isJainFriendly = false,
            prepMinutes = 12,
            cookMinutes = 14,
            difficulty = "Easy",
            baseServings = 2,
            requiredUtensils = listOf("Pan", "Gas stove"),
            tasteTags = listOf("Savoury", "Tangy", "Low-oil"),
            drawableResName = "img_recipe_paneer_tikka",
            ingredients = listOf(
                RecipeIngredient("chicken_breast", "Chicken Breast", 300.0, "g", isCore = true),
                RecipeIngredient("yogurt", "Greek / Plain Yogurt", 80.0, "g", isCore = true),
                RecipeIngredient("lemon", "Lemon", 1.0, "pcs", isCore = true),
                RecipeIngredient("garlic", "Garlic", 15.0, "g", isCore = true),
                RecipeIngredient("black_pepper_powder", "Black Pepper Powder", 8.0, "g", isCore = true),
                RecipeIngredient("coriander_leaves", "Coriander Leaves", 20.0, "g", isCore = false)
            ),
            steps = listOf(
                RecipeStep(
                    1,
                    "Coat cubed chicken breast in yogurt, lemon juice, minced garlic, and freshly cracked black pepper for 10 minutes.",
                    durationSeconds = 600,
                    highlightedIngredients = listOf("Chicken Breast", "Yogurt", "Lemon", "Black Pepper Powder")
                ),
                RecipeStep(
                    2,
                    "Pan-sear over medium-high heat for 6–7 minutes per side until golden and internal temperature reaches 74°C.",
                    durationSeconds = 420,
                    highlightedIngredients = listOf("Chicken Breast")
                )
            ),
            nutritionPerServing = RecipeNutrition(
                calories = 365,
                proteinG = 46.0,
                carbsG = 7.5,
                fatG = 14.2,
                fibreG = 1.4,
                sugarG = 3.2,
                saturatedFatG = 4.1,
                sodiumMg = 360,
                ironMg = 1.9,
                calciumMg = 120,
                vitaminCMg = 34.0,
                potassiumMg = 610
            ),
            dominantCompounds = listOf("Piperine", "Citral", "Allicin", "delta-Decalactone"),
            flavourPairingNote = "Piperine pungency pairs with bright lemon citral and creamy yogurt lactones."
        )
    )

    val defaultPantryItems: List<PantryItem> = listOf(
        PantryItem(
            id = "pantry_turmeric",
            canonicalName = "turmeric_powder",
            displayName = "Turmeric Powder",
            aliases = listOf("haldi", "turmeric"),
            category = IngredientCategory.SPICES,
            quantity = 120.0,
            unit = "g",
            source = "android_scanner",
            confidence = 0.97f,
            confirmed = true,
            lowStockThreshold = 30.0
        ),
        PantryItem(
            id = "pantry_cumin",
            canonicalName = "cumin_seeds",
            displayName = "Cumin Seeds",
            aliases = listOf("jeera", "cumin"),
            category = IngredientCategory.SPICES,
            quantity = 95.0,
            unit = "g",
            source = "web_sync",
            confidence = 1.0f,
            confirmed = true,
            lowStockThreshold = 25.0
        ),
        PantryItem(
            id = "pantry_onion",
            canonicalName = "onion",
            displayName = "Onion",
            aliases = listOf("pyaaz", "red onion"),
            category = IngredientCategory.VEGETABLES,
            quantity = 450.0,
            unit = "g",
            source = "android_scanner",
            confidence = 0.94f,
            confirmed = true,
            lowStockThreshold = 150.0
        ),
        PantryItem(
            id = "pantry_tomato",
            canonicalName = "tomato",
            displayName = "Tomato",
            aliases = listOf("tamatar"),
            category = IngredientCategory.VEGETABLES,
            quantity = 380.0,
            unit = "g",
            source = "android_scanner",
            confidence = 0.92f,
            confirmed = true,
            lowStockThreshold = 150.0
        ),
        PantryItem(
            id = "pantry_garlic",
            canonicalName = "garlic",
            displayName = "Garlic",
            aliases = listOf("lehsun"),
            category = IngredientCategory.VEGETABLES,
            quantity = 85.0,
            unit = "g",
            source = "manual",
            confidence = 1.0f,
            confirmed = true,
            lowStockThreshold = 30.0
        ),
        PantryItem(
            id = "pantry_ginger",
            canonicalName = "ginger",
            displayName = "Ginger",
            aliases = listOf("adrak"),
            category = IngredientCategory.VEGETABLES,
            quantity = 60.0,
            unit = "g",
            source = "manual",
            confidence = 1.0f,
            confirmed = true,
            lowStockThreshold = 25.0
        ),
        PantryItem(
            id = "pantry_lentils",
            canonicalName = "lentils",
            displayName = "Red & Yellow Lentils",
            aliases = listOf("dal", "moong dal"),
            category = IngredientCategory.LEGUMES,
            quantity = 600.0,
            unit = "g",
            source = "web_sync",
            confidence = 1.0f,
            confirmed = true,
            lowStockThreshold = 150.0
        ),
        PantryItem(
            id = "pantry_yogurt",
            canonicalName = "yogurt",
            displayName = "Greek / Plain Yogurt",
            aliases = listOf("dahi", "curd"),
            category = IngredientCategory.DAIRY,
            quantity = 250.0,
            unit = "g",
            source = "android_scanner",
            confidence = 0.89f,
            confirmed = true,
            lowStockThreshold = 100.0
        ),
        PantryItem(
            id = "pantry_lemon",
            canonicalName = "lemon",
            displayName = "Lemon",
            aliases = listOf("nimbu"),
            category = IngredientCategory.FRUITS,
            quantity = 3.0,
            unit = "pcs",
            source = "android_scanner",
            confidence = 0.91f,
            confirmed = true,
            lowStockThreshold = 2.0
        ),
        PantryItem(
            id = "pantry_chilli",
            canonicalName = "red_chilli_powder",
            displayName = "Red Chilli Powder",
            aliases = listOf("lal mirch"),
            category = IngredientCategory.SPICES,
            quantity = 18.0, // Low stock example!
            unit = "g",
            source = "web_sync",
            confidence = 1.0f,
            confirmed = true,
            lowStockThreshold = 25.0
        )
    )
}
