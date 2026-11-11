package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class AuthRequestDto(
    val email: String,
    val password: String,
    val displayName: String? = null
)

data class AuthResponseDto(
    val userId: String,
    val email: String,
    val displayName: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long
)

data class PantrySyncDto(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val source: String,
    val confidence: Float,
    val confirmed: Boolean,
    val updatedAtMs: Long
)

data class RecommendRequestDto(
    val ingredients: List<String>,
    val minutes: Int,
    val diet: String,
    val utensils: List<String>,
    val goal: String
)

data class DeepInspectRequestDto(
    val imageBase64: String,
    val timestampMs: Long,
    val userConsentConfirmed: Boolean = true
)

data class DeepInspectResponseDto(
    val provider: String,
    val candidates: List<PantrySyncDto>
)

data class SyncPushRequestDto(
    val clientId: String,
    val idempotencyKeys: List<String>,
    val pantryUpdates: List<PantrySyncDto>,
    val timestampMs: Long
)

data class SyncStatusResponseDto(
    val status: String,
    val serverVersion: Long,
    val syncedRecords: Int
)

/**
 * Centralized DRISHYAM Backend API contract (Part 24).
 */
interface DrishyamApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequestDto): AuthResponseDto

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequestDto): AuthResponseDto

    @POST("api/auth/logout")
    suspend fun logout(): Map<String, String>

    @POST("api/auth/refresh")
    suspend fun refresh(@Body tokenMap: Map<String, String>): AuthResponseDto

    @GET("api/auth/me")
    suspend fun getMe(): Map<String, Any>

    @GET("api/pantry")
    suspend fun getPantry(): List<PantrySyncDto>

    @POST("api/pantry")
    suspend fun upsertPantryItem(@Body item: PantrySyncDto): PantrySyncDto

    @POST("api/scan/deep-inspect")
    suspend fun deepInspect(@Body request: DeepInspectRequestDto): DeepInspectResponseDto

    @POST("api/recommend")
    suspend fun recommendRecipes(@Body request: RecommendRequestDto): List<Map<String, Any>>

    @GET("api/flavour/{ingredient}")
    suspend fun getFlavourProfile(@Path("ingredient") ingredient: String): Map<String, Any>

    @GET("api/substitute")
    suspend fun getSubstitutions(@Query("ingredient") ingredient: String): List<Map<String, Any>>

    @POST("api/sync/push")
    suspend fun pushSync(@Body request: SyncPushRequestDto): SyncStatusResponseDto

    @GET("api/sync/status")
    suspend fun getSyncStatus(): SyncStatusResponseDto

    companion object {
        fun create(baseUrl: String): DrishyamApiService {
            val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val okHttp = OkHttpClient.Builder()
                .connectTimeout(6, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(sanitizedUrl)
                .client(okHttp)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(DrishyamApiService::class.java)
        }
    }
}
