# VC-ANDROID-PH01-R09 Manual QA

1. Ensure Tailscale is connected on the Windows development machine and on the physical Android device when using the current tailnet-only `.ts.net` backend authority.
2. Confirm root `gradle.properties` points all three VoiceCloud debug authorities to `https://voicecloud.tailfca77b.ts.net`.
3. Run `scripts\VC-ANDROID-LIVE-CONNECTIVITY-CHECK.cmd`; REST, Socket.IO and Web must pass. Transient failures may retry automatically up to three times.
4. Run `scripts\VC-ANDROID-PH01-R09-ACCEPTANCE.cmd`.
5. If the Gradle wrapper must be generated, wrapper bootstrap must complete without a PowerShell `PathInfo` / `System.Uri` conversion error.
6. Required gates must proceed in order: Debug compile, Staging compile, Release compile, unit tests, lint, assemblies, then device instrumentation when connected.
7. Install/open the debug application on the physical device while Tailscale is enabled and confirm bootstrap reaches the live backend rather than showing `Couldn't connect`.
