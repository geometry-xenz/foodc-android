package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.domain.model.DetectionState
import com.example.domain.model.ScannedCandidate
import com.example.ui.DrishyamUiState
import com.example.ui.theme.DrishyamColors
import kotlin.math.roundToInt

@Composable
fun LiveScannerScreen(
    state: DrishyamUiState,
    onToggleFreeze: () -> Unit,
    onToggleRunning: () -> Unit,
    onToggleLens: () -> Unit,
    onToggleTorch: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onCycleSimulatedTargets: () -> Unit,
    onConfirmSingle: (ScannedCandidate) -> Unit,
    onConfirmAll: () -> Unit,
    onDismissCandidate: (String) -> Unit,
    onUpdateCandidate: (String, String, Double, String) -> Unit,
    onRequestDeepInspect: () -> Unit,
    onDismissDeepInspect: () -> Unit,
    onConfirmDeepInspect: () -> Unit,
    onManualAdd: (String, Double, String) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var editingCandidate by remember { mutableStateOf<ScannedCandidate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DrishyamColors.Background)
    ) {
        // Top HUD Controls (Part 11.6)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DRISHYAM NATIVE VISION",
                    style = MaterialTheme.typography.labelLarge,
                    color = DrishyamColors.PrimaryOrange
                )
                Text(
                    text = "${state.measuredFps} FPS • ≥75% Conf • 1,000ms Lock",
                    style = MaterialTheme.typography.bodySmall,
                    color = DrishyamColors.SecondaryText
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onToggleTorch,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (state.torchEnabled) DrishyamColors.PrimaryOrangeDim else DrishyamColors.ElevatedSurface,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (state.torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Torch",
                        tint = if (state.torchEnabled) DrishyamColors.PrimaryOrange else DrishyamColors.PrimaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onToggleLens,
                    modifier = Modifier
                        .size(38.dp)
                        .background(DrishyamColors.ElevatedSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Rear/Front Camera",
                        tint = DrishyamColors.PrimaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(38.dp)
                        .background(DrishyamColors.ElevatedSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = if (state.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Recognition Sound",
                        tint = if (state.soundEnabled) DrishyamColors.SecondaryGold else DrishyamColors.SecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onToggleHaptics,
                    modifier = Modifier
                        .size(38.dp)
                        .background(DrishyamColors.ElevatedSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Toggle Haptics",
                        tint = if (state.hapticsEnabled) DrishyamColors.Success else DrishyamColors.SecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Honest Model Weights & Pipeline Status Banner (Part 11.2)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = DrishyamColors.ElevatedSurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, DrishyamColors.SecondaryGold.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.visionModelStatus.statusBanner,
                        style = MaterialTheme.typography.labelSmall,
                        color = DrishyamColors.SecondaryGold
                    )
                    Text(
                        text = "Pipeline: CameraX → NMS (IoU 0.45) → Temporal Tracker (1,000ms) → Human Confirmation",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(
                    onClick = onCycleSimulatedTargets,
                    modifier = Modifier.testTag("cycle_scanner_preset_button")
                ) {
                    Text("Next Kitchen Set", color = DrishyamColors.PrimaryOrange)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Central CameraX Viewport + Real-Time Bounding Box HUD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .padding(horizontal = 16.dp)
                .background(DrishyamColors.Surface, RoundedCornerShape(18.dp))
        ) {
            if (hasCameraPermission && !state.scannerFrozen) {
                CameraXLivePreview(
                    useRearCamera = state.useRearCamera,
                    torchEnabled = state.torchEnabled
                )
            } else if (!hasCameraPermission) {
                // Dark Culinary Countertop Canvas + Optional Camera Permission Button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    DrishyamColors.ElevatedSurface,
                                    DrishyamColors.Background
                                )
                            ),
                            RoundedCornerShape(18.dp)
                        )
                ) {
                    TextButton(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = DrishyamColors.PrimaryOrange, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Enable Hardware Camera", color = DrishyamColors.PrimaryOrange)
                    }
                }
            }

            // Live Bounding Boxes, Reticle & 1,000ms Stability Bars
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Subtle central reticle
                drawCircle(
                    color = DrishyamColors.SecondaryText.copy(alpha = 0.25f),
                    radius = 36.dp.toPx(),
                    center = Offset(w / 2f, h / 2f),
                    style = Stroke(width = 1.dp.toPx())
                )

                state.activeScanTracks.forEach { track ->
                    val left = track.boundingBox.left * w
                    val top = track.boundingBox.top * h
                    val boxW = (track.boundingBox.right - track.boundingBox.left) * w
                    val boxH = (track.boundingBox.bottom - track.boundingBox.top) * h

                    val boxColor = when (track.state) {
                        DetectionState.AWAITING_CONFIRMATION, DetectionState.CONFIRMED -> DrishyamColors.Success
                        DetectionState.STABILIZING, DetectionState.STABLE -> DrishyamColors.PrimaryOrange
                        DetectionState.UNCERTAIN -> DrishyamColors.Danger
                        else -> DrishyamColors.SecondaryGold
                    }

                    drawRoundRect(
                        color = boxColor,
                        topLeft = Offset(left, top),
                        size = Size(boxW, boxH),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // 1,000ms stability progress bar along bottom of bounding box
                    val stabilityProgress = (track.accumulatedMs / 1000f).coerceIn(0f, 1f)
                    drawRect(
                        color = boxColor.copy(alpha = 0.85f),
                        topLeft = Offset(left, top + boxH - 5.dp.toPx()),
                        size = Size(boxW * stabilityProgress, 5.dp.toPx())
                    )
                }
            }

            // Text labels over bounding boxes
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val maxW = maxWidth
                val maxH = maxHeight
                state.activeScanTracks.forEach { track ->
                    val labelColor = when (track.state) {
                        DetectionState.AWAITING_CONFIRMATION -> DrishyamColors.Success
                        DetectionState.UNCERTAIN -> DrishyamColors.Danger
                        else -> DrishyamColors.PrimaryOrange
                    }
                    Surface(
                        color = DrishyamColors.Background.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, labelColor),
                        modifier = Modifier
                            .offset(
                                x = maxW * track.boundingBox.left,
                                y = (maxH * track.boundingBox.top - 24.dp).coerceAtLeast(4.dp)
                            )
                    ) {
                        Text(
                            text = "${track.displayName} ${(track.confidence * 100).roundToInt()}% • ${track.state.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Scanner Action Row: Freeze, Pause/Resume, Deep Inspect (Part 11.6 & 11.9)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onToggleFreeze,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (state.scannerFrozen) Icons.Default.PlayArrow else Icons.Default.PauseCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (state.scannerFrozen) "Unfreeze" else "Freeze Frame")
            }
            OutlinedButton(
                onClick = onRequestDeepInspect,
                border = BorderStroke(1.dp, DrishyamColors.SecondaryGold),
                modifier = Modifier
                    .weight(1f)
                    .testTag("deep_inspect_button")
            ) {
                Icon(
                    imageVector = Icons.Default.TravelExplore,
                    contentDescription = null,
                    tint = DrishyamColors.SecondaryGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Deep Inspect", color = DrishyamColors.SecondaryGold)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Bottom Confirmation Panel: Human Approval Required Before Entering Permanent Pantry (Part 11.7)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = DrishyamColors.Surface,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
            border = BorderStroke(1.dp, DrishyamColors.BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DETECTED & STABILIZED INGREDIENTS (${state.pendingConfirmationQueue.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = DrishyamColors.PrimaryOrange
                        )
                        Text(
                            text = "Uncertain (<75%) detections are rejected automatically. Confirm items below:",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (state.pendingConfirmationQueue.isNotEmpty()) {
                        Button(
                            onClick = {
                                if (state.hapticsEnabled) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                }
                                onConfirmAll()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DrishyamColors.Success,
                                contentColor = DrishyamColors.Background
                            ),
                            modifier = Modifier.testTag("confirm_all_scanned_button")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Confirm All")
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.pendingConfirmationQueue, key = { it.canonicalName }) { candidate ->
                        Surface(
                            color = DrishyamColors.ElevatedSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DrishyamColors.BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { editingCandidate = candidate }
                                ) {
                                    Text(
                                        text = "${candidate.displayName} • ${candidate.suggestedQuantity.roundToInt()}${candidate.suggestedUnit}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "Conf: ${(candidate.confidence * 100).roundToInt()}% • Locked ${(candidate.accumulatedMs / 1000.0).coerceAtLeast(1.0)}s • Tap to edit class/qty",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DrishyamColors.SecondaryGold
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            if (state.hapticsEnabled) {
                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            }
                                            onConfirmSingle(candidate)
                                        }
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Confirm", tint = DrishyamColors.Success)
                                    }
                                    IconButton(onClick = { onDismissCandidate(candidate.canonicalName) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = DrishyamColors.Danger)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Candidate Dialog (Part 11.7: Rename, correct predicted class, edit quantity)
    editingCandidate?.let { cand ->
        var nameText by remember { mutableStateOf(cand.displayName) }
        var qtyText by remember { mutableStateOf(cand.suggestedQuantity.roundToInt().toString()) }
        var unitText by remember { mutableStateOf(cand.suggestedUnit) }

        AlertDialog(
            onDismissRequest = { editingCandidate = null },
            containerColor = DrishyamColors.ElevatedSurface,
            title = { Text("Verify & Adjust Detection") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Predicted Ingredient Class") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it },
                            label = { Text("Quantity") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitText,
                            onValueChange = { unitText = it },
                            label = { Text("Unit") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateCandidate(
                            cand.canonicalName,
                            nameText,
                            qtyText.toDoubleOrNull() ?: 100.0,
                            unitText
                        )
                        editingCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DrishyamColors.PrimaryOrange,
                        contentColor = DrishyamColors.Background
                    )
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCandidate = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Explicit Deep Inspect Consent Dialog (Part 11.9 & Part 26)
    if (state.deepInspectConsentVisible) {
        AlertDialog(
            onDismissRequest = onDismissDeepInspect,
            containerColor = DrishyamColors.ElevatedSurface,
            title = { Text("Deep Inspect Single-Frame Consent") },
            text = {
                Text(
                    "Continuous scanning always runs 100% on-device. Deep Inspect captures a single still image and sends it securely to the DRISHYAM vision backend to identify complex spice blends or packaged items. Proceed?"
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDeepInspect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DrishyamColors.PrimaryOrange,
                        contentColor = DrishyamColors.Background
                    ),
                    modifier = Modifier.testTag("confirm_deep_inspect_button")
                ) {
                    Text("Capture & Inspect")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeepInspect) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CameraXLivePreview(
    useRearCamera: Boolean,
    torchEnabled: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(useRearCamera, torchEnabled) {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val selector = if (useRearCamera) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                    if (camera.cameraInfo.hasFlashUnit()) {
                        camera.cameraControl.enableTorch(torchEnabled)
                    }
                } catch (_: Exception) {
                    // Graceful fallback when running on headless emulator
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (_: Exception) {
            // Safe ignore
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}
