package com.example.propaintersplastererspayment.feature.selection.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.propaintersplastererspayment.ui.theme.IndustrialGold

@Composable
fun MainSelectionScreen(
    onNavigateToInvoice: () -> Unit,
    onNavigateToJobs: () -> Unit,
    onNavigateToPaint: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF050505)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = IndustrialGold
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = Color(0xFF252527)
                ) {
                    DropdownMenuItem(
                        text = { Text("Settings", color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = IndustrialGold)
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateToSettings()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Backup & Restore", color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = IndustrialGold)
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateToBackupRestore()
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1.2f))

                // Title Area
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PRO PAINTERS",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            shadow = Shadow(
                                color = IndustrialGold.copy(alpha = 0.4f),
                                blurRadius = 25f
                            )
                        ),
                        color = IndustrialGold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "& PLASTERERS",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            shadow = Shadow(
                                color = IndustrialGold.copy(alpha = 0.4f),
                                blurRadius = 25f
                            )
                        ),
                        color = IndustrialGold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Decorative glowing line
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(1.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        IndustrialGold.copy(alpha = 0.8f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.weight(1.2f))

                // Buttons Area
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    SelectionMenuButton(
                        text = "INVOICE",
                        icon = Icons.Default.Description,
                        onClick = onNavigateToInvoice
                    )

                    SelectionMenuButton(
                        text = "JOB",
                        icon = Icons.Default.Work,
                        onClick = onNavigateToJobs
                    )

                    SelectionMenuButton(
                        text = "PAINT",
                        icon = Icons.Default.FormatPaint,
                        onClick = onNavigateToPaint
                    )

                    SelectionMenuButton(
                        text = "PAYMENT",
                        icon = Icons.Default.Payments,
                        onClick = onNavigateToPayment
                    )
                }

                Spacer(modifier = Modifier.weight(1.5f))

                // Footer
                Text(
                    text = "TRUSTED QUALITY FINISH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 5.sp,
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = IndustrialGold,
                            blurRadius = 10f
                        )
                    ),
                    color = IndustrialGold,
                    modifier = Modifier.padding(bottom = 40.dp)
                )
            }
        }
    }
}

@Composable
private fun SelectionMenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF252527).copy(alpha = 0.4f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IndustrialGold,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // The small gold dot on the right
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(IndustrialGold, CircleShape)
            )
        }
    }
}
