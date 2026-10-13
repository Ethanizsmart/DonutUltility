param([string]$JdkHome = 'D:\Android Studio\jbr')
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
$main = Join-Path $project 'build\classes\java\main'
$cache = if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME } else { Join-Path $env:USERPROFILE '.gradle' }
$jars = foreach ($module in @('asm','asm-tree')) {
    $jar = Get-ChildItem -LiteralPath (Join-Path $cache "caches\modules-2\files-2.1\org.ow2.asm\$module\9.9") -Recurse -File -Filter "$module-9.9.jar" | Select-Object -First 1
    if (!$jar) { throw 'Build with Loom first to resolve ASM 9.9.' }
    $jar.FullName
}
$classpath = $jars -join ';'
$work = Join-Path $project 'build\master-f8-fixtures'
$toolBin = Join-Path $work 'tool-bin'
New-Item -ItemType Directory -Path $toolBin -Force | Out-Null
& (Join-Path $JdkHome 'bin\javac.exe') -cp $classpath -d $toolBin (Join-Path $PSScriptRoot 'MasterF8Checks.java')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& (Join-Path $JdkHome 'bin\java.exe') -cp "$toolBin;$classpath" MasterF8Checks $main $work
exit $LASTEXITCODE
