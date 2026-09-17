package app.voicecloud.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import app.voicecloud.core.designsystem.VoiceCloud
import app.voicecloud.core.designsystem.icon.VoiceCloudIcons

@Composable
fun VCSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    enabled: Boolean = true,
    onSearch: (() -> Unit)? = null,
) {
    val colors = VoiceCloud.colors
    val spacing = VoiceCloud.spacing
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = spacing.touch),
        enabled = enabled,
        placeholder = { Text(placeholder, style = VoiceCloud.typography.bodySecondary, color = colors.textMuted) },
        leadingIcon = {
            Icon(VoiceCloudIcons.Search, contentDescription = placeholder, tint = colors.textMuted)
        },
        singleLine = true,
        shape = RoundedCornerShape(VoiceCloud.radii.standard),
        textStyle = VoiceCloud.typography.body,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outline,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surfaceSoft,
            cursorColor = colors.primary,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
        ),
    )
}
