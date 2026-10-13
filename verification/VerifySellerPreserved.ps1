param(
    [string]$Project = 'D:\CoreV2\DonutUtility-restored',
    [string]$Baseline = 'D:\CoreV2\death-recovery-port\baseline'
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$original = [IO.Compression.ZipFile]::OpenRead((Join-Path $Baseline 'pre-port.jar'))
$rebuilt = [IO.Compression.ZipFile]::OpenRead((Join-Path $Project 'build\libs\DonutUtility-CoreV2-restored-1.1.8.jar'))
$sha = [Security.Cryptography.SHA256]::Create()
function EntryHash($entry) {
    $stream = $entry.Open()
    try { [BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-', '').ToLowerInvariant() }
    finally { $stream.Dispose() }
}
$owners = @('SlashSellRuntime','SlashSellSettings','AutoSellRuntime','CompactSlashSellSettings',
    'OminousSurplusSellPolicy','FarmCycleRuntime','ProtectedItemAutoUseRuntime')
try {
    $checked = @()
    foreach ($entry in $original.Entries) {
        $keep = $entry.FullName.StartsWith('com/seastore/') -and $entry.FullName.EndsWith('.class')
        foreach ($owner in $owners) {
            if ($entry.FullName -eq "dev/autoominous/$owner.class" -or $entry.FullName.StartsWith('dev/autoominous/' + $owner + '$')) { $keep = $true }
        }
        if (!$keep) { continue }
        $other = $rebuilt.GetEntry($entry.FullName)
        if (!$other -or (EntryHash $entry) -ne (EntryHash $other)) { throw "Seller bytecode changed: $($entry.FullName)" }
        $checked += $entry.FullName
    }
    $sources = @(Get-ChildItem -LiteralPath (Join-Path $Baseline 'src\main\java\com') -Recurse -File)
    foreach ($owner in $owners) { $sources += Get-Item -LiteralPath (Join-Path $Baseline "src\main\java\dev\autoominous\$owner.java") }
    foreach ($source in $sources) {
        $relative = $source.FullName.Substring($Baseline.Length + 1)
        if ((Get-FileHash -LiteralPath $source.FullName).Hash -ne (Get-FileHash -LiteralPath (Join-Path $Project $relative)).Hash) { throw "Seller source changed: $relative" }
    }
    [pscustomobject]@{ Result='PASS'; SourceFiles=$sources.Count; ClassFiles=$checked.Count; Classes=$checked } | ConvertTo-Json -Depth 4
} finally { $original.Dispose(); $rebuilt.Dispose(); $sha.Dispose() }
