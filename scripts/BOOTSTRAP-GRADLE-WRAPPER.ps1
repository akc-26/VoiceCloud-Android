$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $root
$version = '9.5.0'
$distributionSha256 = '553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746'
$wrapperJarSha256 = '497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7'
$cacheRoot = Join-Path $env:USERPROFILE '.voicecloud\gradle-bootstrap'
$gradleHome = Join-Path $cacheRoot "gradle-$version"
$zip = Join-Path $cacheRoot "gradle-$version-bin.zip"
$gradleBat = Join-Path $gradleHome 'bin\gradle.bat'
$remoteZip = "https://services.gradle.org/distributions/gradle-$version-bin.zip"

function Download-GradleZip {
    New-Item -ItemType Directory -Force -Path $cacheRoot | Out-Null
    for ($attempt = 1; $attempt -le 3; $attempt++) {
        try {
            Write-Host "[INFO] Downloading Gradle $version (attempt $attempt/3)..."
            Invoke-WebRequest -UseBasicParsing -Uri $remoteZip -OutFile $zip
            return
        } catch {
            Remove-Item $zip -Force -ErrorAction SilentlyContinue
            if ($attempt -eq 3) { throw }
            Start-Sleep -Seconds 3
        }
    }
}

if (-not (Test-Path $zip)) { Download-GradleZip }
$zipHash = (Get-FileHash $zip -Algorithm SHA256).Hash.ToLowerInvariant()
if ($zipHash -ne $distributionSha256) {
    Write-Host '[WARN] Cached Gradle ZIP checksum is invalid. Re-downloading...'
    Remove-Item $zip -Force -ErrorAction SilentlyContinue
    Download-GradleZip
    $zipHash = (Get-FileHash $zip -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($zipHash -ne $distributionSha256) { throw "Gradle $version ZIP checksum mismatch." }
}
Write-Host '[PASS] Cached Gradle 9.5.0 distribution checksum verified.'

if (-not (Test-Path $gradleBat)) {
    Write-Host "[INFO] Expanding Gradle $version..."
    if (Test-Path $gradleHome) { Remove-Item -Recurse -Force $gradleHome }
    Expand-Archive -Path $zip -DestinationPath $cacheRoot -Force
}
if (-not (Test-Path $gradleBat)) { throw "Gradle executable missing after extraction: $gradleBat" }

$resolvedZipPath = (Resolve-Path -LiteralPath $zip).ProviderPath
$localDistributionUri = ([System.Uri]::new($resolvedZipPath)).AbsoluteUri
Write-Host "[INFO] Generating standard Gradle Wrapper $version from verified local distribution..."
& $gradleBat wrapper --gradle-version $version --distribution-type bin --gradle-distribution-url $localDistributionUri
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$propsPath = Join-Path $root 'gradle\wrapper\gradle-wrapper.properties'
# Keep the acceptance wrapper pinned to the already verified local ZIP.
# R05 generated the wrapper from this ZIP and then accidentally rewrote the
# URL back to services.gradle.org, forcing every compile gate to redownload it.
$escapedLocalDistributionUri = $localDistributionUri -replace ':', '\:'
$props = @(
    'distributionBase=GRADLE_USER_HOME',
    'distributionPath=wrapper/dists',
    "distributionSha256Sum=$distributionSha256",
    "distributionUrl=$escapedLocalDistributionUri",
    'networkTimeout=60000',
    'validateDistributionUrl=true',
    'zipStoreBase=GRADLE_USER_HOME',
    'zipStorePath=wrapper/dists'
)
Set-Content -Path $propsPath -Value $props -Encoding ASCII

$writtenProps = Get-Content -LiteralPath $propsPath -Raw
if (-not $writtenProps.Contains('distributionUrl=file\:///')) {
    throw 'Generated wrapper is not pinned to the verified local Gradle distribution.'
}
Write-Host "[PASS] Wrapper runtime pinned to verified local Gradle ZIP: $resolvedZipPath"

if (-not (Test-Path 'gradle\wrapper\gradle-wrapper.jar')) { throw 'Gradle wrapper JAR was not generated.' }
if (-not (Test-Path 'gradlew.bat')) { throw 'gradlew.bat was not generated.' }
if (-not (Test-Path 'gradlew')) { throw 'gradlew was not generated.' }
$jarHash = (Get-FileHash 'gradle\wrapper\gradle-wrapper.jar' -Algorithm SHA256).Hash.ToLowerInvariant()
if ($jarHash -ne $wrapperJarSha256) { throw "Gradle wrapper JAR checksum mismatch: $jarHash" }
Write-Host '[PASS] Standard Gradle Wrapper generated and wrapper JAR checksum verified.'
