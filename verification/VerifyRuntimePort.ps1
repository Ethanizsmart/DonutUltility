param([string]$JdkHome = 'D:\Android Studio\jbr')
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
$main = Join-Path $project 'build\classes\java\main'
if (!(Test-Path -LiteralPath $main)) { throw 'Build the project first.' }
$cache = if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME } else { Join-Path $env:USERPROFILE '.gradle' }
$jars = foreach ($module in @('asm','asm-tree')) {
    $folder = Join-Path $cache "caches\modules-2\files-2.1\org.ow2.asm\$module\9.9"
    $jar = Get-ChildItem -LiteralPath $folder -Recurse -File -Filter "$module-9.9.jar" | Select-Object -First 1
    if (!$jar) { throw "Missing ASM 9.9 $module dependency; build with Loom first." }
    $jar.FullName
}
$asmClasspath = $jars -join ';'
$work = Join-Path $project 'build\port-fixtures'
$toolBin = Join-Path $work 'tool-bin'
New-Item -ItemType Directory -Path $toolBin -Force | Out-Null
& (Join-Path $JdkHome 'bin\javac.exe') -cp $asmClasspath -d $toolBin (Join-Path $PSScriptRoot 'RuntimePortChecks.java')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& (Join-Path $JdkHome 'bin\java.exe') -cp "$toolBin;$asmClasspath" RuntimePortChecks $main $work
exit $LASTEXITCODE
