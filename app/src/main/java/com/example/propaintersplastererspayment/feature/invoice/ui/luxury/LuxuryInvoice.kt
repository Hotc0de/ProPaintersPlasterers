package com.example.propaintersplastererspayment.feature.invoice.ui.luxury

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import java.util.Locale

@Composable
fun LuxuryInvoice(
    invoiceData: InvoiceData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        InvoiceColors.OffWhite,
                        InvoiceColors.LightGray1
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.horizontalGradient(
                        0f to InvoiceColors.Bronze,
                        0.2f to InvoiceColors.DarkSlate,
                        0.8f to InvoiceColors.DarkSlate,
                        1f to InvoiceColors.Bronze
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            InvoiceHeader(invoiceData)
            Spacer(modifier = Modifier.height(16.dp))
            InvoiceDetailsCards(invoiceData)
            Spacer(modifier = Modifier.height(28.dp))
            ServicesTable(invoiceData.lineItems)
            Spacer(modifier = Modifier.height(16.dp))
            TotalsSection(
                subtotal = invoiceData.subtotal,
                gst = invoiceData.gstAmount,
                total = invoiceData.total,
                invoiceNumber = invoiceData.invoiceNumber,
                dueDate = invoiceData.dueDate,
                gstRate = invoiceData.gstRate,
                includeGst = invoiceData.includeGst
            )
            Spacer(modifier = Modifier.height(12.dp))
            InvoiceFooter(invoiceData.businessName)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.horizontalGradient(
                        0f to InvoiceColors.Bronze,
                        0.2f to InvoiceColors.DarkSlate,
                        0.8f to InvoiceColors.DarkSlate,
                        1f to InvoiceColors.Bronze
                    )
                )
        )
    }
}

@Composable
fun InvoiceHeader(invoiceData: InvoiceData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        InvoiceColors.Bronze.copy(alpha = 0.03f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(2.dp)
            )
            .border(
                width = 1.dp,
                color = InvoiceColors.Bronze.copy(alpha = 0.2f),
                shape = RoundedCornerShape(2.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        InvoiceColors.DarkSlate,
                                        InvoiceColors.DarkSlate2
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .shadow(4.dp, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PPP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = InvoiceColors.Bronze
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = invoiceData.businessName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = InvoiceColors.DarkSlate,
                            letterSpacing = (-1).sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = invoiceData.businessSubtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = InvoiceColors.Bronze,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .width(80.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                0f to InvoiceColors.Bronze,
                                0.7f to Color.Transparent
                            )
                        )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ADDRESS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = InvoiceColors.MediumGray,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = invoiceData.businessAddress,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = InvoiceColors.DarkGray,
                            lineHeight = 16.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CONTACT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = InvoiceColors.MediumGray,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${invoiceData.businessPhone}\n${invoiceData.businessEmail}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = InvoiceColors.DarkGray,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(InvoiceColors.MediumGray.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "ACCOUNT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = InvoiceColors.MediumGray,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = invoiceData.accountNumber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = InvoiceColors.DarkGray
                )
            }

            Column(
                modifier = Modifier.widthIn(min = 120.dp, max = 150.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(1.dp)
                            .background(InvoiceColors.Bronze)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .rotate(45f)
                            .background(InvoiceColors.Bronze)
                    )
                }

                Text(
                    text = "INVOICE",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light,
                    color = InvoiceColors.DarkSlate,
                    letterSpacing = (-1.5).sp,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun InvoiceDetailsCards(invoiceData: InvoiceData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(2.dp),
            colors = CardDefaults.cardColors(containerColor = InvoiceColors.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(
                            text = "INVOICE NUMBER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = InvoiceColors.MediumGray,
                            letterSpacing = 1.4.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = invoiceData.invoiceNumber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InvoiceColors.DarkSlate
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ISSUE DATE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = InvoiceColors.MediumGray,
                                letterSpacing = 1.4.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = invoiceData.issueDate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InvoiceColors.DarkGray
                            )
                        }

                        if (invoiceData.dueDate != null) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DUE DATE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InvoiceColors.MediumGray,
                                    letterSpacing = 1.4.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = invoiceData.dueDate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InvoiceColors.Bronze
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(2.dp),
            colors = CardDefaults.cardColors(containerColor = InvoiceColors.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(
                            text = "BILL TO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = InvoiceColors.MediumGray,
                            letterSpacing = 1.4.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = invoiceData.billTo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InvoiceColors.DarkSlate
                        )
                    }

                    Column {
                        Text(
                            text = "JOB ADDRESS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = InvoiceColors.MediumGray,
                            letterSpacing = 1.4.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = invoiceData.jobAddress,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InvoiceColors.DarkGray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServicesTable(lineItems: List<InvoiceLineItem>) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(InvoiceColors.Bronze)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SERVICES & MATERIALS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = InvoiceColors.DarkSlate,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(InvoiceColors.LightGray3)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, InvoiceColors.LightGray3, RoundedCornerShape(2.dp))
                .clip(RoundedCornerShape(2.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                InvoiceColors.DarkSlate,
                                InvoiceColors.DarkSlate2
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DESCRIPTION",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = InvoiceColors.BorderGray,
                    letterSpacing = 1.8.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "QTY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = InvoiceColors.BorderGray,
                    letterSpacing = 1.8.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(52.dp)
                )
                Text(
                    text = "UNIT PRICE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = InvoiceColors.BorderGray,
                    letterSpacing = 1.8.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(82.dp)
                )
                Text(
                    text = "LINE TOTAL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = InvoiceColors.BorderGray,
                    letterSpacing = 1.8.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(96.dp)
                )
            }

            lineItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0) InvoiceColors.White
                            else InvoiceColors.LightBg
                        )
                        .border(
                            width = 1.dp,
                            color = InvoiceColors.LightGray2,
                            shape = RectangleShape
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InvoiceColors.DarkSlate,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.quantity.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = InvoiceColors.TextGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(52.dp)
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%.2f", item.rate)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = InvoiceColors.TextGray,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(82.dp)
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%.2f", item.amount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = InvoiceColors.DarkSlate,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(96.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TotalsSection(
    subtotal: Double,
    gst: Double,
    total: Double,
    invoiceNumber: String,
    dueDate: String?,
    gstRate: Double,
    includeGst: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.82f)) {
            Card(
                shape = RoundedCornerShape(2.dp),
                colors = CardDefaults.cardColors(containerColor = InvoiceColors.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TotalRow("SUBTOTAL", subtotal, false)
                    if (includeGst) {
                        TotalRow(
                            "GST",
                            gst,
                            true,
                            "Goods & Services Tax (${(gstRate * 100).roundToInt()}%)"
                        )
                    } else {
                        TotalRow(
                            "GST (EXEMPT)",
                            0.0,
                            true,
                            "No GST applied to this invoice"
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                InvoiceColors.DarkSlate,
                                Color(0xFF0F172A)
                            )
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
                    .border(2.dp, InvoiceColors.Bronze, RoundedCornerShape(2.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AMOUNT DUE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = InvoiceColors.Bronze,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (dueDate != null) {
                            Text(
                                text = "Due: $dueDate",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = InvoiceColors.BorderGray
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", total)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InvoiceColors.White,
                            letterSpacing = (-2).sp
                        )
                    }
                }
            }

            if (dueDate != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    InvoiceColors.Bronze.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                        .border(1.dp, InvoiceColors.Bronze.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = InvoiceColors.TextGray)) {
                                append("Please make the payment in 10 working days. Reference invoice number ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = InvoiceColors.DarkSlate,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(invoiceNumber)
                            }
                        },
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TotalRow(
    label: String,
    amount: Double,
    isLast: Boolean,
    subtitle: String? = null
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = InvoiceColors.MediumGray,
                    letterSpacing = 1.2.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = InvoiceColors.MediumGray
                    )
                }
            }

            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", amount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = InvoiceColors.DarkSlate
            )
        }

        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(1.dp)
                    .background(InvoiceColors.LightGray2)
            )
        }
    }
}

@Composable
fun InvoiceFooter(businessName: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(InvoiceColors.MediumGray.copy(alpha = 0.3f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Thank you for choosing $businessName",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = InvoiceColors.DarkSlate,
            letterSpacing = 0.6.sp
        )
    }
}

@Preview(showBackground = true, widthDp = 900)
@Composable
fun LuxuryInvoicePreview() {
    val sampleData = InvoiceData(
        invoiceNumber = "INV-2026-0842",
        issueDate = "10-04-2026",
        dueDate = "24-04-2026",
        billTo = "Harbour View Properties",
        jobAddress = "45 Harbour Street, Mosman NSW 2088",
        lineItems = listOf(
            InvoiceLineItem("Interior wall preparation and plastering", 1, 3200.0, 3200.0),
            InvoiceLineItem("Premium paint application (3 coats)", 1, 4850.0, 4850.0),
            InvoiceLineItem("Ceiling restoration and finishing", 1, 2100.0, 2100.0),
            InvoiceLineItem("Premium materials and supplies", 1, 1450.0, 1450.0)
        ),
        subtotal = 11600.0,
        gstAmount = 1160.0,
        total = 12760.0
    )

    LuxuryInvoice(invoiceData = sampleData)
}



