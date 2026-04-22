package com.example.propaintersplastererspayment.feature.invoice.ui.luxury

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun CleanInvoice(
    invoiceData: InvoiceData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(InvoiceColors.LightGray2)
            .padding(32.dp)
    ) {
        // Main Invoice Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(InvoiceColors.White)
        ) {
            // Top Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(InvoiceColors.DarkSlate)
            )

            // Header Section
            InvoiceHeader(invoiceData)

            // Bill To Section
            BillToSection(invoiceData)

            // Invoice Items
            InvoiceItemsSection(invoiceData)

            // Payment Info
            PaymentInfoSection(invoiceData)

            // Footer
            InvoiceFooter()
        }
    }
}

@Composable
fun InvoiceHeader(invoiceData: InvoiceData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column - Company Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Logo and Company Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = InvoiceColors.DarkSlate2,
                            shape = RoundedCornerShape(8.dp)
                        ),
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = InvoiceColors.DarkSlate
                    )
                    Text(
                        text = invoiceData.businessSubtitle,
                        fontSize = 14.sp,
                        color = InvoiceColors.DarkSlate
                    )
                }
            }

            // Contact Info
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InfoLabel("Address:")
                Text(
                    text = invoiceData.businessAddress,
                    fontSize = 12.sp,
                    color = InvoiceColors.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                InfoLabel("Contact:")
                Text(
                    text = invoiceData.businessPhone,
                    fontSize = 12.sp,
                    color = InvoiceColors.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                InfoLabel("Email:")
                Text(
                    text = invoiceData.businessEmail,
                    fontSize = 12.sp,
                    color = InvoiceColors.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                InfoLabel("Account:")
                Text(
                    text = invoiceData.accountNumber,
                    fontSize = 12.sp,
                    color = InvoiceColors.DarkGray
                )
            }
        }

        // Center Column - INVOICE Title
        Column(
            modifier = Modifier.weight(1.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top line with diamond
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(InvoiceColors.BorderGray)
                )
                DiamondShape(color = InvoiceColors.DarkSlate, size = 8.dp)
            }

            // INVOICE text
            Text(
                text = "INVOICE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 0.3.em,
                color = InvoiceColors.DarkSlate
            )

            // Bottom line with diamond
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiamondShape(color = InvoiceColors.DarkSlate, size = 8.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(InvoiceColors.BorderGray)
                )
            }
        }

        // Right Column - Invoice Info
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoField("Invoice Number", invoiceData.invoiceNumber)
            InfoField("Invoice Date", invoiceData.issueDate)
            if (invoiceData.dueDate != null) {
                InfoField("Due Date", invoiceData.dueDate)
            }
        }
    }
}

@Composable
fun DiamondShape(color: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .rotate(45f)
            .background(color)
    )
}

@Composable
fun InfoLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        color = InvoiceColors.MediumGray,
        letterSpacing = 0.05.em
    )
}

@Composable
fun InfoField(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            color = InvoiceColors.MediumGray,
            letterSpacing = 0.05.em
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = InvoiceColors.DarkSlate
        )
    }
}

@Composable
fun BillToSection(invoiceData: InvoiceData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(InvoiceColors.LightGray1)
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SectionHeader("BILL TO")
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = invoiceData.billTo,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = InvoiceColors.DarkSlate
            )
            Text(
                text = invoiceData.billToAddress,
                fontSize = 12.sp,
                color = InvoiceColors.DarkGray,
                lineHeight = 16.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            SectionHeader("PROJECT")
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (invoiceData.projectName.isNotEmpty()) invoiceData.projectName else "House Painting & Plastering",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = InvoiceColors.DarkSlate
            )
            Text(
                text = if (invoiceData.projectDescription.isNotEmpty()) invoiceData.projectDescription else "Residential Project",
                fontSize = 12.sp,
                color = InvoiceColors.DarkGray
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(InvoiceColors.Bronze, CircleShape)
        )
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = InvoiceColors.DarkSlate2,
            letterSpacing = 0.05.em
        )
    }
}

@Composable
fun InvoiceItemsSection(invoiceData: InvoiceData) {
    Column(modifier = Modifier.padding(32.dp)) {
        SectionHeader("INVOICE ITEMS")
        Spacer(modifier = Modifier.height(12.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InvoiceColors.DarkSlate2)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TableHeaderText("DESCRIPTION", Modifier.weight(2f))
            TableHeaderText("QTY", Modifier.width(60.dp), TextAlign.End)
            TableHeaderText("RATE", Modifier.width(80.dp), TextAlign.End)
            TableHeaderText("AMOUNT", Modifier.width(100.dp), TextAlign.End)
        }

        // Table Rows
        invoiceData.lineItems.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = InvoiceColors.BorderGray,
                        shape = RectangleShape
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    color = InvoiceColors.DarkSlate,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = if (item.isLabour) "${item.quantity} hrs" else item.quantity.toString(),
                    fontSize = 13.sp,
                    color = InvoiceColors.DarkSlate,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(60.dp)
                )
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", item.rate)}",
                    fontSize = 13.sp,
                    color = InvoiceColors.DarkSlate,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(80.dp)
                )
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", item.amount)}",
                    fontSize = 13.sp,
                    color = InvoiceColors.DarkSlate,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(100.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Totals Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Column(modifier = Modifier.width(320.dp)) {
                // Subtotal
                TotalRow("Subtotal:", invoiceData.subtotal, false)

                // GST
                if (invoiceData.includeGst) {
                    TotalRow(
                        "GST (${(invoiceData.gstRate * 100).toInt()}%):",
                        invoiceData.gstAmount,
                        false
                    )
                }

                // Total (highlighted in bronze)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InvoiceColors.LightGray1)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = InvoiceColors.Bronze
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%.2f", invoiceData.total)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = InvoiceColors.Bronze
                    )
                }
            }
        }
    }
}

@Composable
fun TotalRow(label: String, amount: Double, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = InvoiceColors.DarkGray
        )
        Text(
            text = "$${String.format(Locale.getDefault(), "%.2f", amount)}",
            fontSize = 13.sp,
            color = InvoiceColors.DarkSlate
        )
    }

    if (!isLast) {
        Divider(
            color = InvoiceColors.BorderGray,
            thickness = 1.dp
        )
    }
}

@Composable
fun TableHeaderText(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = InvoiceColors.White,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun PaymentInfoSection(invoiceData: InvoiceData) {
    Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 0.dp)) {
        SectionHeader("PAYMENT INFORMATION")
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(InvoiceColors.LightGray1)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentInfoRow("Bank:", invoiceData.bankName)
                PaymentInfoRow("Account Name:", invoiceData.businessName + " " + invoiceData.businessSubtitle)
                PaymentInfoRow("Account Number:", invoiceData.accountNumber)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PaymentInfoRow(label: String, value: String) {
    Row {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = InvoiceColors.DarkSlate
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            color = InvoiceColors.DarkGray
        )
    }
}

@Composable
fun InvoiceFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = InvoiceColors.BorderGray,
                shape = RectangleShape
            )
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Generated by Pro Painters – Thank you for your business! HEHE",
            fontSize = 10.sp,
            color = InvoiceColors.MediumGray
        )
    }
}
