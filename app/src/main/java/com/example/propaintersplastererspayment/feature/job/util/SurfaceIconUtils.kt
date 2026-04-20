package com.example.propaintersplastererspayment.feature.job.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.propaintersplastererspayment.data.local.entity.SurfaceType

/**
 * Maps each SurfaceType to a representative Material icon. Chosen icons are
 * conservative (already used elsewhere in the project) to avoid introducing
 * new, potentially unavailable icons.
 */
object SurfaceIconUtils {
    fun getIconForSurfaceType(surfaceType: SurfaceType): ImageVector = when (surfaceType) {
        SurfaceType.CEILING -> Icons.Default.Home
        SurfaceType.WALL -> Icons.Default.FormatPaint
        SurfaceType.SKIRTING -> Icons.Default.Inventory2
        SurfaceType.DOOR -> Icons.Default.Inventory2
        SurfaceType.DOOR_FRAME -> Icons.Default.Inventory2
        SurfaceType.WINDOW -> Icons.Default.Inventory2
        SurfaceType.WINDOW_FRAME -> Icons.Default.Inventory2
        SurfaceType.ARCHITRAVE -> Icons.Default.FormatPaint
        SurfaceType.CORNICE -> Icons.Default.FormatPaint
        SurfaceType.TRIM -> Icons.Default.FormatPaint
        SurfaceType.WARDROBE -> Icons.Default.Business
        SurfaceType.CUPBOARD -> Icons.Default.Inventory2
        SurfaceType.SHELVING -> Icons.Default.Inventory2
        SurfaceType.VANITY -> Icons.Default.Inventory2
        SurfaceType.BEAM -> Icons.Default.FormatPaint
        SurfaceType.COLUMN -> Icons.Default.FormatPaint
        SurfaceType.OTHER -> Icons.Default.Category
    }
}


