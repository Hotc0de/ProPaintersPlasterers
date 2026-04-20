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
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.CEILING, surfaceLabel = "Ceiling"))
                for (i in 1..4) {
                    surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WALL, surfaceLabel = "Wall $i"))
                }
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WINDOW, surfaceLabel = "Window 1"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.DOOR, surfaceLabel = "Door"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.SKIRTING, surfaceLabel = "Skirting"))
            }
            RoomType.LOUNGE -> {
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.CEILING, surfaceLabel = "Ceiling"))
                for (i in 1..4) {
                    surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WALL, surfaceLabel = "Wall $i"))
                }
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WINDOW, surfaceLabel = "Window 1"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WINDOW, surfaceLabel = "Window 2"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.DOOR, surfaceLabel = "Door"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.SKIRTING, surfaceLabel = "Skirting"))
            }
            RoomType.KITCHEN -> {
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.CEILING, surfaceLabel = "Ceiling"))
                for (i in 1..4) {
                    surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WALL, surfaceLabel = "Wall $i"))
                }
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WINDOW, surfaceLabel = "Window 1"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.DOOR, surfaceLabel = "Door"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.SKIRTING, surfaceLabel = "Skirting"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.CUSTOM, surfaceLabel = "Cupboard"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.TRIM, surfaceLabel = "Trim"))
            }
            RoomType.BATHROOM -> {
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.CEILING, surfaceLabel = "Ceiling"))
                for (i in 1..4) {
                    surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WALL, surfaceLabel = "Wall $i"))
                }
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.WINDOW, surfaceLabel = "Window 1"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.DOOR, surfaceLabel = "Door"))
                surfaces.add(SurfaceEntity(roomId = roomId, surfaceType = SurfaceType.SKIRTING, surfaceLabel = "Skirting"))
            }
            else -> { /* Custom or unsupported templates return no surfaces */ }
        }

        return surfaces
    }
}
