package com.example.propaintersplastererspayment.feature.job.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirlineSeatFlat
import androidx.compose.material.icons.filled.Bathroom
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ConnectingAirports
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.propaintersplastererspayment.data.local.entity.RoomType

object RoomIconUtils {
    fun getIconForRoomType(roomType: RoomType): ImageVector = when (roomType) {
        RoomType.BEDROOM -> Icons.Default.AirlineSeatFlat
        RoomType.KITCHEN -> Icons.Default.Kitchen
        RoomType.BATHROOM -> Icons.Default.Bathroom
        RoomType.LOUNGE -> Icons.Default.BeachAccess
        RoomType.DINING -> Icons.Default.DinnerDining
        RoomType.OFFICE -> Icons.Default.Business
        RoomType.HALLWAY -> Icons.Default.Home
        RoomType.STAIRS -> Icons.Default.Stairs
        RoomType.LANDING -> Icons.Default.ConnectingAirports
        RoomType.GARAGE -> Icons.Default.Garage
        RoomType.EXTERIOR -> Icons.Default.BeachAccess
        RoomType.CUSTOM -> Icons.Default.Gavel
    }
}


