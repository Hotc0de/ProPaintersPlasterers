package com.example.propaintersplastererspayment.feature.job.util

import com.example.propaintersplastererspayment.data.local.entity.SurfaceType

object SurfaceLabelHelper {
    /**
     * Generates a suggested label for a new surface based on its type and existing surfaces in the room.
     * 
     * Expected behavior:
     * - If no walls exist: returns "Wall 1"
     * - If "Wall 1" exists: returns "Wall 2"
     * - If "Wall 1", "Wall 2", "Wall 5" exist: returns "Wall 6"
     * - Works case-insensitively.
     */
    fun generateNextLabel(type: SurfaceType, existingLabels: List<String>): String {
        val typeName = type.name.lowercase().replaceFirstChar { it.uppercase() }
        
        // Some types might not want numbering by default (e.g., Ceiling)
        // but for consistency with your requirements, we'll apply it to all.
        
        val prefix = "$typeName "
        
        val numbers = existingLabels.mapNotNull { label ->
            when {
                label.equals(typeName, ignoreCase = true) -> 1
                label.startsWith(prefix, ignoreCase = true) -> {
                    label.substring(prefix.length).trim().toIntOrNull()
                }
                else -> null
            }
        }

        val nextNumber = if (numbers.isEmpty()) 1 else numbers.max() + 1
        
        return "$typeName $nextNumber"
    }
}
