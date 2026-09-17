package app.voicecloud.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * VoiceCloud icon catalog. Current vectors are Material outlined placeholders
 * behind a stable VoiceCloud API so custom brand assets can replace them later
 * without changing feature call sites.
 */
object VoiceCloudIcons {
    val Home: ImageVector get() = Icons.Outlined.Home
    val Explore: ImageVector get() = Icons.Outlined.Explore
    val Live: ImageVector get() = Icons.Outlined.GraphicEq
    val Messages: ImageVector get() = Icons.Outlined.ChatBubbleOutline
    val Profile: ImageVector get() = Icons.Outlined.PersonOutline
    val Search: ImageVector get() = Icons.Outlined.Search
    val Chat: ImageVector get() = Icons.Outlined.ChatBubbleOutline
    val RaiseHand: ImageVector get() = Icons.Outlined.BackHand
    val React: ImageVector get() = Icons.Outlined.FavoriteBorder
    val Gift: ImageVector get() = Icons.Outlined.CardGiftcard
    val Share: ImageVector get() = Icons.Outlined.IosShare
    val Send: ImageVector get() = Icons.AutoMirrored.Outlined.Send
    val Save: ImageVector get() = Icons.Outlined.BookmarkBorder
    val Report: ImageVector get() = Icons.Outlined.Flag
    val Settings: ImageVector get() = Icons.Outlined.Settings
    val Notifications: ImageVector get() = Icons.Outlined.NotificationsNone
    val Wallet: ImageVector get() = Icons.Outlined.AccountBalanceWallet
    val Analytics: ImageVector get() = Icons.Outlined.Analytics
    val Audience: ImageVector get() = Icons.Outlined.Groups
    val CreatorTools: ImageVector get() = Icons.Outlined.Tune
}
