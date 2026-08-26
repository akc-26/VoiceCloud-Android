$ErrorActionPreference='Stop'
$root=Resolve-Path (Join-Path $PSScriptRoot '..')
$script:count=0
function T([string]$p){ [IO.File]::ReadAllText((Join-Path $root $p)) }
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }
$screens=T 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt'
$policy=T 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/ConsumerIdentityPolicy.kt'
$repo=T 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/DiscoveryRepository.kt'
P 'itemsIndexed collision-safe list rendering retained' ($screens.Contains('import androidx.compose.foundation.lazy.itemsIndexed'))
P 'raw UUID-only LazyColumn keys removed' (-not $screens.Contains('key = { it.id }'))
P 'Home People key namespace retained' $screens.Contains('home-person:${user.id}:$index')
P 'Home Creator key namespace retained' $screens.Contains('home-creator:${user.id}:$index')
P 'Home Rooms deduplicate before rendering' $screens.Contains('state.home.rooms.distinctBy { it.id }')
P 'consumer identity dedupe retained' $policy.Contains('.distinctBy { it.id.ifBlank { it.username.trim().lowercase() } }')
P 'repository room dedupe retained' ($repo.Contains('rooms.await().items.distinctBy { it.id }') -and $repo.Contains('live.await().items.distinctBy { it.id }'))
P 'friend/request dedupe retained' ($repo.Contains('incoming.distinctBy { it.id }') -and $repo.Contains('outgoing.distinctBy { it.id }'))
Write-Host "VC-ANDROID-PH03-R02 Windows source authority: $script:count/$script:count PASS"
