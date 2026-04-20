package com.example.propaintersplastererspayment.feature.job.util

import com.example.propaintersplastererspayment.data.local.entity.RoomType
import com.example.propaintersplastererspayment.data.local.entity.SurfaceEntity
import com.example.propaintersplastererspayment.data.local.entity.SurfaceType

object RoomTemplateHelper {

    fun getTemplates(): List<RoomType> = listOf(
        RoomType.BEDROOM,
        RoomType.LOUNGE,
        RoomType.KITCHEN,
        RoomType.BATHROOM,
        RoomType.CUSTOM
    )

    fun createSurfacesFromTemplate(roomId: Long, roomType: RoomType): List<SurfaceEntity> {
        val surfaces = mutableListOf<SurfaceEntity>()

        when (roomType) {
            RoomType.BEDROOM -> {
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.CEILING, customName = "Ceiling", displayName = "Ceiling"))
                for (i in 1..4) {
                    surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WALL, customName = "Wall $i", displayName = "Wall $i"))
                }
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WINDOW, customName = "Window 1", displayName = "Window 1"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.DOOR, customName = "Door", displayName = "Door"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.SKIRTING, customName = "Skirting", displayName = "Skirting"))
            }
            RoomType.LOUNGE -> {
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.CEILING, customName = "Ceiling", displayName = "Ceiling"))
                for (i in 1..4) {
                    surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WALL, customName = "Wall $i", displayName = "Wall $i"))
                }
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WINDOW, customName = "Window 1", displayName = "Window 1"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WINDOW, customName = "Window 2", displayName = "Window 2"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.DOOR, customName = "Door", displayName = "Door"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.SKIRTING, customName = "Skirting", displayName = "Skirting"))
            }
            RoomType.KITCHEN -> {
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.CEILING, customName = "Ceiling", displayName = "Ceiling"))
                for (i in 1..4) {
                    surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WALL, customName = "Wall $i", displayName = "Wall $i"))
                }
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WINDOW, customName = "Window 1", displayName = "Window 1"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.DOOR, customName = "Door", displayName = "Door"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.SKIRTING, customName = "Skirting", displayName = "Skirting"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.CUPBOARD, customName = "Cupboard", displayName = "Cupboard"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.TRIM, customName = "Trim", displayName = "Trim"))
            }
            RoomType.BATHROOM -> {
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.CEILING, customName = "Ceiling", displayName = "Ceiling"))
                for (i in 1..4) {
                    surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WALL, customName = "Wall $i", displayName = "Wall $i"))
                }
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WINDOW, customName = "Window 1", displayName = "Window 1"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.DOOR, customName = "Door", displayName = "Door"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.SKIRTING, customName = "Skirting", displayName = "Skirting"))
            }
            else -> { /* Custom or unsupported templates return no surfaces */ }
        }

        return surfaces
    }
}
