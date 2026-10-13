param(
    [string]$JdkHome,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$GradleArgs = @('build')
)

# Select Java 21 for this invocation; restore the environment afterwards.
$candidates = if ($JdkHome) { @($JdkHome) } else { @($env:JAVA_HOME, 'D:\Android Studio\jbr') }
$selected = $null
foreach ($candidate in $candidates) {
    if (-not $candidate) { continue }
    $javaExe = Join-Path $candidate 'bin\java.exe'
    $javacExe = Join-Path $candidate 'bin\javac.exe'
    if (!(Test-Path -LiteralPath $javaExe) -or !(Test-Path -LiteralPath $javacExe)) { continue }
    $versionText = (& $javaExe -version 2>&1 | Out-String)
    if ($versionText -match 'version "21\.') { $selected = $candidate; break }
}
if (!$selected) {
    Write-Error 'Java 21 JDK required. Run build.ps1 -JdkHome <path-to-jdk-21>.'
    exit 1
}
$previousJavaHome = $env:JAVA_HOME
$previousDebug = $env:DEBUG
$exitCode = 1
Push-Location $PSScriptRoot
try {
    $env:JAVA_HOME = $selected
    $env:DEBUG = $null
    Write-Host "Using JDK 21: $selected"
    & (Join-Path $PSScriptRoot 'gradlew.bat') @GradleArgs --console=plain
    $exitCode = $LASTEXITCODE
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:DEBUG = $previousDebug
    Pop-Location
}
exit $exitCode
