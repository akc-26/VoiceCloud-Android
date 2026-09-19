package app.voicecloud.feature.economy.billing

import app.voicecloud.feature.economy.BuildConfig

enum class PaymentRail {
    HOSTED_GATEWAY,
    GOOGLE_PLAY;

    companion object {
        fun configured(): PaymentRail = runCatching {
            valueOf(BuildConfig.VOICECLOUD_PAYMENT_MODE.trim().uppercase())
        }.getOrDefault(HOSTED_GATEWAY)
    }
}
