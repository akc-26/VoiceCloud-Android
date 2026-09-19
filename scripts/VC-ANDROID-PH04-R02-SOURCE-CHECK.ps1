$ErrorActionPreference='Stop'
$root=Resolve-Path (Join-Path $PSScriptRoot '..')
$screens=[IO.File]::ReadAllText((Join-Path $root 'feature/engagement/src/main/java/app/voicecloud/feature/engagement/ui/EngagementScreens.kt'))
$count=0
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }
P 'QuickAction is RowScope-qualified' $screens.Contains('private fun RowScope.QuickAction(')
P 'unscoped QuickAction compile defect removed' (-not $screens.Contains('private fun QuickAction('))
P 'equal-width QuickAction layout retained' ($screens.Contains('Modifier.weight(1f)') -and $screens.Contains('clickable(onClick = onClick)'))
P 'PH04 quick actions remain Row-hosted' ($screens.Contains('fun Ph04QuickActions') -and $screens.Contains('Row(Modifier.fillMaxWidth()'))
Write-Host "VC-ANDROID-PH04-R02 Windows corrective authority: $script:count/$script:count PASS"
