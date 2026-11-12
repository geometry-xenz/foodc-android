package com.example

import com.example.data.FoodoscopeSeedCatalog
import com.example.data.local.*
import com.example.domain.model.*
import com.example.domain.usecase.ComputationalGastronomyEngine
import com.example.domain.usecase.IngredientAliasNormalizer
import com.example.domain.usecase.NutritionEngine
import com.example.vision.TemporalIngredientTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleRobolectricTest {

    private class FakePantryDao : PantryDao {
        private val state = MutableStateFlow<List<PantryItemEntity>>(emptyList())
        override fun observeAll(): Flow<List<PantryItemEntity>> = state
        override suspend fun getAllOnce(): List<PantryItemEntity> = state.value
        override suspend fun upsert(item: PantryItemEntity) {
            state.value = state.value.filterNot { it.id == item.id } + item
        }
        override suspend fun upsertAll(items: List<PantryItemEntity>) {
            val map = state.value.associateBy { it.id }.toMutableMap()
            items.forEach { map[it.id] = it }
            state.value = map.values.toList()
        }
        override suspend fun deleteById(id: String) {
            state.value = state.value.filterNot { it.id == id }
        }
        override suspend fun clearAll() {
            state.value = emptyList()
        }
    }

    private class FakeSyncOutboxDao : SyncOutboxDao {
        private val state = MutableStateFlow<List<SyncOutboxEntity>>(emptyList())
        override fun observeAll(): Flow<List<SyncOutboxEntity>> = state
        override suspend fun enqueue(op: SyncOutboxEntity) {
            state.value = listOf(op) + state.value
        }
        override suspend fun markAllSynced() {
            state.value = state.value.map { it.copy(synced = true) }
        }
    }

    @Test
    fun `verify end-to-end pantry persistence, alias deduplication, and idempotent sync outbox`() = runBlocking {
        val pantryDao = FakePantryDao()
        val syncOutboxDao = FakeSyncOutboxDao()

        // 1. Seed initial pantry items
        val seedEntities = FoodoscopeSeedCatalog.defaultPantryItems.map {
            PantryItemEntity(
                id = it.id,
                canonicalName = it.canonicalName,
                displayName = it.displayName,
                aliasesCsv = it.aliases.joinToString(","),
                categoryName = it.category.name,
                quantity = it.quantity,
                unit = it.unit,
                source = it.source,
                confidence = it.confidence,
                confirmed = it.confirmed,
                isAvailable = it.isAvailable,
                expiryDateIso = it.expiryDateIso,
                lowStockThreshold = it.lowStockThreshold,
                createdAtMs = it.createdAtMs,
                updatedAtMs = it.updatedAtMs
            )
        }
        pantryDao.upsertAll(seedEntities)
        assertEquals(FoodoscopeSeedCatalog.defaultPantryItems.size, pantryDao.getAllOnce().size)

        // 2. Confirm scanned ingredient ("Paneer") and enqueue outbox operation
        val meta = IngredientAliasNormalizer.resolve("Paneer")
        val paneerEntity = PantryItemEntity(
            id = "pantry_paneer",
            canonicalName = meta.canonicalName,
            displayName = meta.displayName,
            aliasesCsv = meta.aliases.joinToString(","),
            categoryName = meta.category.name,
            quantity = 250.0,
            unit = "g",
            source = "android_scanner",
            confidence = 0.96f,
            confirmed = true,
            isAvailable = true,
            expiryDateIso = null,
            lowStockThreshold = 50.0,
            createdAtMs = 1000L,
            updatedAtMs = 1000L
        )
        pantryDao.upsert(paneerEntity)
        syncOutboxDao.enqueue(
            SyncOutboxEntity(
                idempotencyKey = "idem_PANTRY_pantry_paneer_1000",
                entityType = "PANTRY",
                entityId = "pantry_paneer",
                action = "UPSERT",
                timestampMs = 1000L,
                synced = false
            )
        )

        val currentPantry = pantryDao.observeAll().first()
        assertTrue(currentPantry.any { it.canonicalName == "paneer" && it.quantity == 250.0 })

        val outboxBeforeSync = syncOutboxDao.observeAll().first()
        assertFalse(outboxBeforeSync.first().synced)

        // 3. Mark synchronized and verify exactly-once checkpoint
        syncOutboxDao.markAllSynced()
        val outboxAfterSync = syncOutboxDao.observeAll().first()
        assertTrue(outboxAfterSync.all { it.synced })
    }

    @Test
    fun `verify rotation coordinate mapping for portrait and landscape camera frames`() {
        val rect = NormalizedRect(0.2f, 0.3f, 0.6f, 0.7f)
        val rot0 = TemporalIngredientTracker.mapCoordinatesForRotation(rect, 0)
        assertEquals(rect, rot0)

        val rot90 = TemporalIngredientTracker.mapCoordinatesForRotation(rect, 90)
        assertEquals(0.3f, rot90.left, 0.001f)
        assertEquals(0.2f, rot90.top, 0.001f)

        val rot180 = TemporalIngredientTracker.mapCoordinatesForRotation(rect, 180)
        assertEquals(0.4f, rot180.left, 0.001f)
        assertEquals(0.3f, rot180.top, 0.001f)
    }

    @Test
    fun `verify Jain-friendly restriction strictly filters allium recipes`() {
        val jainProfile = UserProfile(
            dietaryLifestyle = DietaryLifestyle.VEGETARIAN,
            dietaryRestrictions = setOf(DietaryRestriction.JAIN_FRIENDLY)
        )
        val targets = NutritionEngine.calculateTargets(jainProfile)
        val ranked = ComputationalGastronomyEngine.rankRecommendations(
            allRecipes = FoodoscopeSeedCatalog.recipes,
            pantryItems = FoodoscopeSeedCatalog.defaultPantryItems,
            profile = jainProfile,
            targets = targets,
            flavourProfiles = FoodoscopeSeedCatalog.flavourProfiles
        )
        assertEquals(1, ranked.size)
        assertEquals("rcp_jain_jeera_aloo_dal", ranked.first().recipe.id)
    }
}
