package app.voicecloud.feature.live.model

fun LiveRoomDetail.restrictionLabels(): List<String> = buildList {
    if (isLocked) add("Locked")
    if (isInviteOnly) add("Invite only")
    if (isTicketRequired || isPremium) add("Ticket required")
    if (isSubscriberOnly) add("Subscribers only")
    if (isVerifiedOnly) add("Verified accounts")
    if (!clubId.isNullOrBlank()) add("Community room")
}.distinct()

fun LiveRoomDetail.isJoinablePresentation(): Boolean = isLive && !status.equals("ended", ignoreCase = true) && endedAt == null
