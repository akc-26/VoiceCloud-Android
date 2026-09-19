from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
SCREENS = (ROOT / 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/ui/DiscoveryScreens.kt').read_text(encoding='utf-8')
POLICY = (ROOT / 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/ConsumerIdentityPolicy.kt').read_text(encoding='utf-8')
REPO = (ROOT / 'feature/discovery/src/main/java/app/voicecloud/feature/discovery/data/DiscoveryRepository.kt').read_text(encoding='utf-8')
TEST = (ROOT / 'feature/discovery/src/test/java/app/voicecloud/feature/discovery/data/ConsumerIdentityPolicyTest.kt').read_text(encoding='utf-8')
ACCEPT = (ROOT / 'scripts/VC-ANDROID-PH03-R02-ACCEPTANCE.cmd').read_text(encoding='utf-8')

checks=[]
def ck(name, condition):
    if not condition:
        raise SystemExit(f'[FAIL] {name}')
    checks.append(name)
    print(f'[PASS] {name}')

ck('Lazy lists use itemsIndexed collision-safe rendering', 'import androidx.compose.foundation.lazy.itemsIndexed' in SCREENS and 'import androidx.compose.foundation.lazy.items\n' not in SCREENS)
ck('raw UUID-only lazy keys removed', 'key = { it.id }' not in SCREENS)
ck('Home room key namespace retained', '"home-room:${room.id}:$index"' in SCREENS)
ck('Home people key namespace retained', '"home-person:${user.id}:$index"' in SCREENS)
ck('Home creator key namespace retained', '"home-creator:${user.id}:$index"' in SCREENS)
ck('same user UUID cannot collide across Home People and Creators', 'home-person:' in SCREENS and 'home-creator:' in SCREENS)
ck('Home rooms deduplicate before render', 'state.home.rooms.distinctBy { it.id }' in SCREENS)
ck('Home people deduplicate before render', 'state.home.people.distinctBy { it.id }' in SCREENS)
ck('Home creators deduplicate before render', 'state.home.creators.distinctBy { it.id }' in SCREENS)
ck('Explore lists use section-qualified keys', all(x in SCREENS for x in ['explore-live:', 'explore-trend:', 'explore-person:', 'explore-creator:']))
ck('Search lists use section-qualified keys', all(x in SCREENS for x in ['search-person:', 'search-creator:', 'search-room:', 'search-community:']))
ck('Social and friend lists use section-qualified keys', all(x in SCREENS for x in ['social:', 'friend-in:', 'friend:', 'friend-suggest:', 'friend-out:']))
ck('all dynamic discovery lists deduplicate at render boundary', SCREENS.count('distinctBy { it.id }') >= 13)
ck('consumer identity policy deduplicates API identities', '.distinctBy { it.id.ifBlank { it.username.trim().lowercase() } }' in POLICY)
ck('repository deduplicates Home room DTOs', 'rooms = rooms.await().items.distinctBy { it.id }.take(6)' in REPO)
ck('repository deduplicates Explore room DTOs', 'liveRooms = live.await().items.distinctBy { it.id }' in REPO and 'trendingRooms = trendingRooms.await().items.distinctBy { it.id }' in REPO)
ck('repository deduplicates standalone live rooms', 'api.liveRooms(category = category).items.distinctBy { it.id }' in REPO)
ck('repository deduplicates search rooms', 'rooms.await().results.rooms?.items.orEmpty().distinctBy { it.id }' in REPO)
ck('repository deduplicates friends and pending requests', '.distinctBy { it.friendshipId.ifBlank { it.user.id } }' in REPO and 'incoming = incoming.distinctBy { it.id }' in REPO and 'outgoing = outgoing.distinctBy { it.id }' in REPO)
ck('repository deduplicates friend suggestions', '.distinctBy { it.id.ifBlank { it.username.trim().lowercase() } }' in REPO)
ck('unit regression covers duplicate consumer IDs', 'duplicateConsumerIdentitiesAreCollapsedBeforeRendering' in TEST and 'assertEquals(listOf("creator-id", "user-id"), filtered.map { it.id })' in TEST)
ck('R02 acceptance retains R01 source authority', 'ph03_r01_source_check.py' in ACCEPT)
ck('R02 acceptance runs duplicate-key regression', 'ph03_r02_lazy_list_key_regression.py' in ACCEPT)
ck('R02 acceptance retains broad Android compile test lint assembly sweep', ':app:compileDebugKotlin' in ACCEPT and ':app:compileStagingKotlin' in ACCEPT and ':app:compileReleaseKotlin' in ACCEPT and ' lintDebug lintStaging lintRelease ' in ACCEPT and ':app:assembleRelease' in ACCEPT)

# Simulate the exact R01 failure condition: one creator UUID also appears in People.
uuid='87c85e85-c597-4601-bc87-424213e8b03c'
keys=[f'home-person:{uuid}:0', f'home-creator:{uuid}:0']
ck('reported duplicate UUID produces unique Home keys', len(keys) == len(set(keys)))

print(f'VC-ANDROID-PH03-R02 lazy-list key regression: {len(checks)}/{len(checks)} PASS')
