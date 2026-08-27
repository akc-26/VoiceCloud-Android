package app.voicecloud.android.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import app.voicecloud.android.MainActivity
import app.voicecloud.android.R
import app.voicecloud.feature.auth.data.FcmTokenRegistrationFoundation
import app.voicecloud.feature.engagement.data.EngagementRouteResolver
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VoiceCloudFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var tokenRegistration: FcmTokenRegistrationFoundation
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        serviceScope.launch { tokenRegistration.onNewToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val route = EngagementRouteResolver.fromStringData(message.data)
        val title = message.notification?.title ?: message.data["title"] ?: "VoiceCloud"
        val body = message.notification?.body ?: message.data["message"] ?: message.data["body"] ?: "You have a new VoiceCloud notification."
        showNotification(title, body, route, message.messageId)
    }

    private fun showNotification(title: String, body: String, route: String, messageId: String?) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "VoiceCloud notifications", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Messages, community updates, reminders and account notifications"
                },
            )
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_NAVIGATION_ROUTE, route)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            route.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        manager.notify(messageId?.hashCode() ?: System.currentTimeMillis().toInt(), notification)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private companion object { const val CHANNEL_ID = "voicecloud_general" }
}
