$ErrorActionPreference = 'Stop'

$failed = $false
$scriptFiles = @(Get-ChildItem -LiteralPath $PSScriptRoot -Filter '*.ps1' -File | Sort-Object FullName)
if ($scriptFiles.Count -lt 1) {
    Write-Host '[FAIL] No PowerShell scripts were found for parser validation.'
    exit 1
}

foreach ($scriptFile in $scriptFiles) {
    $tokens = $null
    $parseErrors = $null
    [void][System.Management.Automation.Language.Parser]::ParseFile(
        $scriptFile.FullName,
        [ref]$tokens,
        [ref]$parseErrors
    )
    if ($parseErrors.Count -gt 0) {
        $failed = $true
        foreach ($parseError in $parseErrors) {
            $line = $parseError.Extent.StartLineNumber
            $column = $parseError.Extent.StartColumnNumber
            Write-Host "[FAIL] PowerShell parse error: $($scriptFile.Name):${line}:${column} $($parseError.Message)"
        }
    }
}

if ($failed) {
    exit 1
}

Write-Host "[PASS] PowerShell parser validation complete: $($scriptFiles.Count) script(s)"
exit 0
