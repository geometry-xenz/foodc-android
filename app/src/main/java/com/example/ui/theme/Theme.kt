package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DrishyamDarkColorScheme = darkColorScheme(
    primary = DrishyamColors.PrimaryOrange,
    onPrimary = DrishyamColors.Background,
    primaryContainer = DrishyamColors.PrimaryOrangeDim,
    onPrimaryContainer = DrishyamColors.PrimaryText,
    secondary = DrishyamColors.SecondaryGold,
    onSecondary = DrishyamColors.Background,
    secondaryContainer = DrishyamColors.SecondaryGoldDim,
    onSecondaryContainer = DrishyamColors.SecondaryGold,
    tertiary = DrishyamColors.Success,
    onTertiary = DrishyamColors.Background,
    background = DrishyamColors.Background,
    onBackground = DrishyamColors.PrimaryText,
    surface = DrishyamColors.Surface,
    onSurface = DrishyamColors.PrimaryText,
    surfaceVariant = DrishyamColors.ElevatedSurface,
    onSurfaceVariant = DrishyamColors.SecondaryText,
    error = DrishyamColors.Danger,
    onError = DrishyamColors.Background,
    outline = DrishyamColors.BorderSubtle
)

@Composable
fun DrishyamTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DrishyamDarkColorScheme,
        typography = DrishyamTypography,
        content = content
    )
}
