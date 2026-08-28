pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "VoiceCloud-Android"
include(":app")
include(":core:model")
include(":core:designsystem")
include(":core:network")
include(":core:security")
include(":core:database")
include(":feature:bootstrap")

include(":core:preferences")
include(":core:logging")
include(":core:realtime")

include(":feature:auth")

include(":feature:discovery")

include(":feature:engagement")

include(":feature:live")

include(":feature:hosting")

include(":feature:economy")

include(":feature:profile")
