package com.example.propaintersplastererspayment.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.propaintersplastererspayment.ui.theme.*

@Composable
fun IndustrialTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    IndustrialTextFieldLayout(label = label, modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextSubdued) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            readOnly = readOnly,
            shape = AppShapes.large,
            colors = industrialTextFieldColors(),
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            minLines = minLines,
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun IndustrialTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    IndustrialTextFieldLayout(label = label, modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextSubdued) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            readOnly = readOnly,
            shape = AppShapes.large,
            colors = industrialTextFieldColors(),
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            minLines = minLines,
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun IndustrialTextFieldLayout(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun industrialTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CharcoalMuted,
    unfocusedContainerColor = CharcoalMuted,
    focusedBorderColor = IndustrialGold,
    unfocusedBorderColor = BorderColor,
    focusedTextColor = OffWhite,
    unfocusedTextColor = OffWhite,
    cursorColor = IndustrialGold
)
