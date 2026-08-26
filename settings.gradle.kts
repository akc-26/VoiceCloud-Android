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
