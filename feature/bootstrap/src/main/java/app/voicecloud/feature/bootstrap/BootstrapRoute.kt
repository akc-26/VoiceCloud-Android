package app.voicecloud.feature.bootstrap

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.voicecloud.core.designsystem.component.VCSecondaryButton
import app.voicecloud.core.designsystem.component.VoiceCloudBrandMark
import app.voicecloud.core.designsystem.theme.ConsumerColors
import app.voicecloud.core.designsystem.theme.VoiceCloudMotion

@Composable
fun BootstrapRoute(
    viewModel: BootstrapViewModel = hiltViewModel(),
    onOpenCreatorWorkspace: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AnimatedContent(
        targetState = state,
        label = "bootstrap",
        transitionSpec = {
            fadeIn(tween(VoiceCloudMotion.StandardMs)) togetherWith
                fadeOut(tween(VoiceCloudMotion.FastMs))
        },
    ) { current ->
        when (current) {
            BootstrapState.Loading -> LoadingScreen()
            is BootstrapState.Maintenance -> MessageScreen(
                title = "We'll be right back",
                message = current.message,
                caption = "VoiceCloud is temporarily unavailable while maintenance is completed.",
            )
            is BootstrapState.ForceUpdate -> ForceUpdateScreen(current)
            is BootstrapState.Error -> MessageScreen(
                title = "Couldn't connect to VoiceCloud",
                message = current.message,
                action = if (current.canRetry) "Try again" else null,
                onAction = viewModel::refresh,
            )
            is BootstrapState.Ready -> FoundationReadyScreen(
                loginMethods = current.config.supportedLoginMethods,
                liveKitAvailable = current.config.availableRtcProviders.any {
                    it.providerType?.contains("livekit", ignoreCase = true) == true
                },
                onOpenCreatorWorkspace = onOpenCreatorWorkspace,
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    val transition = rememberInfiniteTransition(label = "brandPulse")
    val pulse by transition.animateFloat(
        initialValue = .94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ConsumerColors.Surface, ConsumerColors.Cloud, ConsumerColors.SurfaceSoft))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            VoiceCloudBrandMark(72.dp, Modifier.scale(pulse))
            Text("VoiceCloud", style = MaterialTheme.typography.headlineMedium)
            Text("Real voices. Real connections.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(Modifier.width(120.dp), strokeCap = StrokeCap.Round)
        }
    }
}

@Composable
private fun MessageScreen(
    title: String,
    message: String,
    caption: String? = null,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                VoiceCloudBrandMark()
                Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                caption?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
                action?.let {
                    Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) { Text(it) }
                }
            }
        }
    }
}

@Composable
private fun ForceUpdateScreen(state: BootstrapState.ForceUpdate) {
    val context = LocalContext.current
    MessageScreen(
        title = "Update VoiceCloud",
        message = state.message ?: "A newer VoiceCloud version is required to continue.",
        caption = listOfNotNull(
            state.minimum?.let { "Minimum supported: $it" },
            state.latest?.let { "Latest: $it" },
        ).joinToString(" • ").ifBlank { null },
        action = if (state.downloadUrl != null) "Update now" else null,
    ) {
        state.downloadUrl?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
    }
}

@Composable
private fun FoundationReadyScreen(
    loginMethods: List<String>,
    liveKitAvailable: Boolean,
    onOpenCreatorWorkspace: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ConsumerColors.Surface, ConsumerColors.Cloud, ConsumerColors.SurfaceSoft)))
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VoiceCloudBrandMark(68.dp)
            Spacer(Modifier.height(18.dp))
            Text("VoiceCloud", style = MaterialTheme.typography.displaySmall)
            Text("Android foundation is ready", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(28.dp))
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatusRow("Mobile configuration", "Connected")
                    StatusRow(
                        "Login capabilities",
                        if (loginMethods.isEmpty()) "Backend controlled" else loginMethods.joinToString(" · ") {
                            it.replaceFirstChar { char -> char.uppercaseChar() }
                        },
                    )
                    StatusRow("Live audio", if (liveKitAvailable) "Available" else "Backend controlled")
                    HorizontalDivider()
                    Text(
                        "User and Creator authentication begin in PH02. The creator workspace below is presentation chrome only.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    VCSecondaryButton(
                        text = "Creator workspace",
                        onClick = onOpenCreatorWorkspace,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
