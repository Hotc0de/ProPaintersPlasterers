package com.example.propaintersplastererspayment.core.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val CompactPhoneWidthBreakpoint: Dp = 360.dp
val CompactActionRowBreakpoint: Dp = 420.dp
val MinActionButtonWidth: Dp = 88.dp

fun isCompactPhoneWidth(maxWidth: Dp): Boolean = maxWidth < CompactPhoneWidthBreakpoint
fun isCompactActionRowWidth(maxWidth: Dp): Boolean = maxWidth < CompactActionRowBreakpoint


