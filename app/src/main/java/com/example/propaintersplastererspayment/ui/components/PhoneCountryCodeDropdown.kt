package com.example.propaintersplastererspayment.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.example.propaintersplastererspayment.core.util.BusinessPhoneCountryCodes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneCountryCodeDropdown(
    selectedIsoCode: String,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Country Code"
) {
    val selectedOption = remember(selectedIsoCode) {
        BusinessPhoneCountryCodes.findByIsoOrDefault(selectedIsoCode)
    }
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption.label,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .onGloballyPositioned { textFieldSize = it.size }
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(androidx.compose.ui.platform.LocalDensity.current) { textFieldSize.width.toDp() })
        ) {
            BusinessPhoneCountryCodes.options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onCountrySelected(option.isoCode)
                        expanded = false
                    }
                )
            }
        }
    }
}



