package com.example.vision

import com.example.domain.model.DetectionState
import com.example.domain.model.IngredientCategory
import com.example.domain.model.NormalizedRect
import com.example.domain.model.ScannedCandidate
import com.example.domain.usecase.IngredientAliasNormalizer
import kotlin.math.max
import kotlin.math.min

data class RawDetectionBox(
    val classLabel: String,
    val confidence: Float,
    val rect: NormalizedRect
)

data class VisionModelStatus(
    val weightsFound: Boolean,
    val modelFileName: String,
    val engineName: String,
    val accelerationMode: String,
    val statusBanner: String
)

/**
 * Complete DRISHYAM Native Vision Pipeline (Part 11.2, 11.4, 11.5):
 * - Non-Maximum Suppression (IoU filtering)
 * - Coordinate rotation mapping
 * - Timestamp-based 1,000ms persistence & >=75% confidence accumulator
 * - Duplicate suppression & stable track IDs
 */
class TemporalIngredientTracker(
    private val confidenceThreshold: Float = 0.75f,
    private val requiredStabilityMs: Long = 1000L,
    private val trackExpirationMs: Long = 1800L
) {
    private val activeTracks = mutableMapOf<String, ScannedCandidate>()
    private val confirmedOrQueuedCanonicals = mutableSetOf<String>()

    companion object {
        fun computeIoU(a: NormalizedRect, b: NormalizedRect): Float {
            val interLeft = max(a.left, b.left)
            val interTop = max(a.top, b.top)
            val interRight = min(a.right, b.right)
            val interBottom = min(a.bottom, b.bottom)

            val interWidth = max(0f, interRight - interLeft)
            val interHeight = max(0f, interBottom - interTop)
            val interArea = interWidth * interHeight
            if (interArea <= 0f) return 0f

            val areaA = max(0f, a.right - a.left) * max(0f, a.bottom - a.top)
            val areaB = max(0f, b.right - b.left) * max(0f, b.bottom - b.top)
            val union = areaA + areaB - interArea
            return if (union <= 0f) 0f else (interArea / union).coerceIn(0f, 1f)
        }

        fun applyNms(boxes: List<RawDetectionBox>, iouThreshold: Float = 0.45f): List<RawDetectionBox> {
            val sorted = boxes.sortedByDescending { it.confidence }.toMutableList()
            val selected = mutableListOf<RawDetectionBox>()

            while (sorted.isNotEmpty()) {
                val current = sorted.removeAt(0)
                selected.add(current)
                val iterator = sorted.iterator()
                while (iterator.hasNext()) {
                    val candidate = iterator.next()
                    if (candidate.classLabel == current.classLabel &&
                        computeIoU(current.rect, candidate.rect) >= iouThreshold
                    ) {
                        iterator.remove()
                    }
                }
            }
            return selected
        }

        fun mapCoordinatesForRotation(rect: NormalizedRect, rotationDegrees: Int): NormalizedRect {
            return when ((rotationDegrees % 360 + 360) % 360) {
                90 -> NormalizedRect(
                    left = (1f - rect.bottom).coerceIn(0f, 1f),
                    top = rect.left.coerceIn(0f, 1f),
                    right = (1f - rect.top).coerceIn(0f, 1f),
                    bottom = rect.right.coerceIn(0f, 1f)
                )
                180 -> NormalizedRect(
                    left = (1f - rect.right).coerceIn(0f, 1f),
                    top = (1f - rect.bottom).coerceIn(0f, 1f),
                    right = (1f - rect.left).coerceIn(0f, 1f),
                    bottom = (1f - rect.top).coerceIn(0f, 1f)
                )
                270 -> NormalizedRect(
                    left = rect.top.coerceIn(0f, 1f),
                    top = (1f - rect.right).coerceIn(0f, 1f),
                    right = rect.bottom.coerceIn(0f, 1f),
                    bottom = (1f - rect.left).coerceIn(0f, 1f)
                )
                else -> rect
            }
        }
    }

    /**
     * Processes a frame of NMS-filtered detections at [nowMs] and returns:
     * 1. Active tracked overlays for the camera HUD
     * 2. Newly stabilized ingredients that just crossed the 1,000ms & >=75% threshold
     */
    fun processFrameDetections(
        rawDetections: List<RawDetectionBox>,
        nowMs: Long,
        existingPantryCanonicals: Set<String>
    ): Pair<List<ScannedCandidate>, List<ScannedCandidate>> {
        val nmsFiltered = applyNms(rawDetections)
        val newlyStabilized = mutableListOf<ScannedCandidate>()

        // Prune expired tracks that haven't reached AWAITING_CONFIRMATION
        val expiredKeys = activeTracks.filterValues { track ->
            track.state != DetectionState.AWAITING_CONFIRMATION &&
                track.state != DetectionState.CONFIRMED &&
                (nowMs - track.lastSeenMs) > trackExpirationMs
        }.keys
        expiredKeys.forEach { activeTracks.remove(it) }

        for (box in nmsFiltered) {
            val meta = IngredientAliasNormalizer.resolve(box.classLabel)
            val canonical = meta.canonicalName
            val existing = activeTracks[canonical]

            if (box.confidence < confidenceThreshold) {
                // Uncertain detection (< 75%): display as UNCERTAIN, never auto-accumulate
                val uncertainCandidate = ScannedCandidate(
                    trackId = existing?.trackId ?: "trk_${canonical}",
                    canonicalName = canonical,
                    displayName = meta.displayName,
                    category = meta.category,
                    confidence = box.confidence,
                    boundingBox = box.rect,
                    firstDetectedMs = existing?.firstDetectedMs ?: nowMs,
                    lastSeenMs = nowMs,
                    accumulatedMs = existing?.accumulatedMs ?: 0L,
                    state = DetectionState.UNCERTAIN,
                    suggestedQuantity = if (meta.defaultUnit == "pcs") 4.0 else 100.0,
                    suggestedUnit = meta.defaultUnit
                )
                activeTracks[canonical] = uncertainCandidate
                continue
            }

            if (existing == null) {
                activeTracks[canonical] = ScannedCandidate(
                    trackId = "trk_${canonical}",
                    canonicalName = canonical,
                    displayName = meta.displayName,
                    category = meta.category,
                    confidence = box.confidence,
                    boundingBox = box.rect,
                    firstDetectedMs = nowMs,
                    lastSeenMs = nowMs,
                    accumulatedMs = 0L,
                    state = DetectionState.CANDIDATE,
                    suggestedQuantity = if (meta.defaultUnit == "pcs") 4.0 else 100.0,
                    suggestedUnit = meta.defaultUnit
                )
            } else {
                // Smooth confidence and bounding box
                val smoothedConfidence = (existing.confidence * 0.4f) + (box.confidence * 0.6f)
                val smoothedRect = NormalizedRect(
                    left = existing.boundingBox.left * 0.35f + box.rect.left * 0.65f,
                    top = existing.boundingBox.top * 0.35f + box.rect.top * 0.65f,
                    right = existing.boundingBox.right * 0.35f + box.rect.right * 0.65f,
                    bottom = existing.boundingBox.bottom * 0.35f + box.rect.bottom * 0.65f
                )
                val deltaMs = (nowMs - existing.lastSeenMs).coerceIn(0L, 600L)
                val totalAccumulated = existing.accumulatedMs + deltaMs

                val nextState = when {
                    existing.state == DetectionState.AWAITING_CONFIRMATION ||
                        existing.state == DetectionState.CONFIRMED -> existing.state
                    totalAccumulated >= requiredStabilityMs && smoothedConfidence >= confidenceThreshold ->
                        DetectionState.AWAITING_CONFIRMATION
                    totalAccumulated >= 250L -> DetectionState.STABILIZING
                    else -> DetectionState.CANDIDATE
                }

                val updated = existing.copy(
                    confidence = smoothedConfidence,
                    boundingBox = smoothedRect,
                    lastSeenMs = nowMs,
                    accumulatedMs = totalAccumulated,
                    state = nextState
                )
                activeTracks[canonical] = updated

                val isDuplicate = canonical in confirmedOrQueuedCanonicals || canonical in existingPantryCanonicals
                if (nextState == DetectionState.AWAITING_CONFIRMATION && !isDuplicate) {
                    confirmedOrQueuedCanonicals.add(canonical)
                    newlyStabilized.add(updated)
                }
            }
        }

        return activeTracks.values.toList() to newlyStabilized
    }

    fun removeTrack(canonicalName: String) {
        activeTracks.remove(canonicalName)
        confirmedOrQueuedCanonicals.remove(canonicalName)
    }

    fun reset() {
        activeTracks.clear()
        confirmedOrQueuedCanonicals.clear()
    }
}
