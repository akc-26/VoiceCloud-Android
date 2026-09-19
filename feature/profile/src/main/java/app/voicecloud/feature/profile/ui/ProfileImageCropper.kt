package app.voicecloud.feature.profile.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import app.voicecloud.core.designsystem.component.voiceCloudTitleCase
import app.voicecloud.core.designsystem.component.VoiceCloudToastEffect
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class ProfileCropKind(
    val label: String,
    val outputWidth: Int,
    val outputHeight: Int,
) {
    AVATAR("Profile Photo", 1080, 1080),
    COVER("Cover Photo", 1920, 720),
}

data class CroppedProfileMedia(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String = "image/jpeg",
)

private data class CropBitmapState(
    val bitmap: Bitmap,
    val fileName: String,
)

@Composable
fun ProfileImageCropDialog(
    context: Context,
    uri: Uri,
    kind: ProfileCropKind,
    onDismiss: () -> Unit,
    onConfirm: (CroppedProfileMedia) -> Unit,
) {
    var loaded by remember(uri) { mutableStateOf<CropBitmapState?>(null) }
    var loadError by remember(uri) { mutableStateOf<String?>(null) }
    var zoom by remember(uri) { mutableFloatStateOf(1f) }
    var horizontal by remember(uri) { mutableFloatStateOf(0f) }
    var vertical by remember(uri) { mutableFloatStateOf(0f) }
    var processing by remember(uri) { mutableStateOf(false) }

    LaunchedEffect(uri) {
        val result = runCatching { decodeSelectedBitmap(context, uri) }
        loaded = result.getOrNull()
        loadError = result.exceptionOrNull()?.let { "This Image Couldn’t Be Opened. Choose Another Photo." }
    }

    VoiceCloudToastEffect(loadError, null) { loadError = null }

    AlertDialog(
        onDismissRequest = { if (!processing) onDismiss() },
        confirmButton = {
            Button(
                enabled = loaded != null && !processing,
                onClick = {
                    val current = loaded ?: return@Button
                    processing = true
                    runCatching {
                        cropAndEncode(current.bitmap, current.fileName, kind, zoom, horizontal, vertical)
                    }.onSuccess(onConfirm)
                        .onFailure { loadError = "This Image Couldn’t Be Prepared. Try Another Photo." }
                    processing = false
                },
            ) { Text(voiceCloudTitleCase(if (processing) "Preparing…" else "Use Photo")) }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !processing) { Text(voiceCloudTitleCase("Cancel")) } },
        title = { Text(voiceCloudTitleCase("Adjust ${kind.label}")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(voiceCloudTitleCase("Preview The Crop, Then Zoom Or Reposition It. Large Originals Are Resized Automatically."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val current = loaded
                if (current == null) {
                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        if (loadError == null) CircularProgressIndicator()
                    }
                } else {
                    val previewModifier = if (kind == ProfileCropKind.AVATAR) {
                        Modifier.fillMaxWidth().aspectRatio(1f).clip(CircleShape)
                    } else {
                        Modifier.fillMaxWidth().aspectRatio(kind.outputWidth.toFloat() / kind.outputHeight).clip(RoundedCornerShape(18.dp))
                    }
                    Box(previewModifier.background(MaterialTheme.colorScheme.surfaceContainer)) {
                        androidx.compose.foundation.Image(
                            bitmap = current.bitmap.asImageBitmap(),
                            contentDescription = "${kind.label} Crop Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(scaleX = zoom, scaleY = zoom),
                            alignment = cropAlignment(horizontal, vertical),
                        )
                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(999.dp),
                        ) {
                            Text(voiceCloudTitleCase("${(zoom * 100).roundToInt()}%"), Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Text(voiceCloudTitleCase("Zoom"), style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { zoom = max(1f, zoom - 0.1f) }) { Text(voiceCloudTitleCase("−")) }
                        Slider(
                            value = zoom,
                            onValueChange = { zoom = it },
                            valueRange = 1f..4f,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { zoom = min(4f, zoom + 0.1f) }) { Text(voiceCloudTitleCase("+")) }
                    }
                    Text(voiceCloudTitleCase("Horizontal Position"), style = MaterialTheme.typography.labelLarge)
                    Slider(value = horizontal, onValueChange = { horizontal = it }, valueRange = -1f..1f)
                    Text(voiceCloudTitleCase("Vertical Position"), style = MaterialTheme.typography.labelLarge)
                    Slider(value = vertical, onValueChange = { vertical = it }, valueRange = -1f..1f)
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
    )
}

private fun cropAlignment(horizontal: Float, vertical: Float): Alignment = object : Alignment {
    override fun align(size: IntSize, space: IntSize, layoutDirection: LayoutDirection): IntOffset {
        val horizontalBias = horizontal.coerceIn(-1f, 1f)
        val verticalBias = vertical.coerceIn(-1f, 1f)
        val x = ((space.width - size.width) * (horizontalBias + 1f) * 0.5f).roundToInt()
        val y = ((space.height - size.height) * (verticalBias + 1f) * 0.5f).roundToInt()
        return IntOffset(x, y)
    }
}

private fun decodeSelectedBitmap(context: Context, uri: Uri): CropBitmapState {
    val resolver = context.contentResolver
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val width = info.size.width.coerceAtLeast(1)
            val height = info.size.height.coerceAtLeast(1)
            val longest = max(width, height)
            if (longest > 4096) {
                val ratio = 4096f / longest.toFloat()
                decoder.setTargetSize((width * ratio).roundToInt().coerceAtLeast(1), (height * ratio).roundToInt().coerceAtLeast(1))
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth / sample, bounds.outHeight / sample) > 4096) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample.coerceAtLeast(1) }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Unable to decode selected image")
    }
    val displayName = resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }.orEmpty().ifBlank { "voicecloud-profile.jpg" }
    return CropBitmapState(bitmap = bitmap, fileName = displayName.substringBeforeLast('.', displayName) + ".jpg")
}

private fun cropAndEncode(
    bitmap: Bitmap,
    fileName: String,
    kind: ProfileCropKind,
    zoom: Float,
    horizontal: Float,
    vertical: Float,
): CroppedProfileMedia {
    val srcW = bitmap.width.toFloat()
    val srcH = bitmap.height.toFloat()
    require(srcW >= 1f && srcH >= 1f)
    val targetAspect = kind.outputWidth.toFloat() / kind.outputHeight.toFloat()
    val sourceAspect = srcW / srcH
    val baseCropW: Float
    val baseCropH: Float
    if (sourceAspect > targetAspect) {
        baseCropH = srcH
        baseCropW = srcH * targetAspect
    } else {
        baseCropW = srcW
        baseCropH = srcW / targetAspect
    }
    val effectiveZoom = zoom.coerceIn(1f, 4f)
    val cropW = (baseCropW / effectiveZoom).coerceAtLeast(1f)
    val cropH = (baseCropH / effectiveZoom).coerceAtLeast(1f)
    val maxLeft = (srcW - cropW).coerceAtLeast(0f)
    val maxTop = (srcH - cropH).coerceAtLeast(0f)
    val left = ((horizontal.coerceIn(-1f, 1f) + 1f) * 0.5f * maxLeft).roundToInt().coerceIn(0, max(0, bitmap.width - cropW.roundToInt()))
    val top = ((vertical.coerceIn(-1f, 1f) + 1f) * 0.5f * maxTop).roundToInt().coerceIn(0, max(0, bitmap.height - cropH.roundToInt()))
    val width = cropW.roundToInt().coerceAtLeast(1).coerceAtMost(bitmap.width - left)
    val height = cropH.roundToInt().coerceAtLeast(1).coerceAtMost(bitmap.height - top)
    val cropped = Bitmap.createBitmap(bitmap, left, top, width, height)
    val resized = Bitmap.createScaledBitmap(cropped, kind.outputWidth, kind.outputHeight, true)
    val output = ByteArrayOutputStream()
    check(resized.compress(Bitmap.CompressFormat.JPEG, 90, output)) { "Image encoding failed" }
    if (cropped !== bitmap) cropped.recycle()
    if (resized !== cropped) resized.recycle()
    return CroppedProfileMedia(output.toByteArray(), fileName)
}
