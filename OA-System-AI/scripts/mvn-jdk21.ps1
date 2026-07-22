param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $MavenArgs
)

$jdkHome = $env:OA_JDK21_HOME
if ([string]::IsNullOrWhiteSpace($jdkHome)) {
    $jdkHome = "C:\Program Files\Java\jdk-21"
}

if (-not (Test-Path -LiteralPath $jdkHome)) {
    throw "JDK 21 not found: $jdkHome. Set OA_JDK21_HOME to the JDK 21 path."
}

$env:JAVA_HOME = $jdkHome
$env:Path = "$jdkHome\bin;$env:Path"

$localRepo = Join-Path $PSScriptRoot "..\.m2\repository"
New-Item -ItemType Directory -Force -Path $localRepo | Out-Null

if ($MavenArgs.Count -eq 0) {
    mvn "-Dmaven.repo.local=$localRepo" -version
} else {
    mvn "-Dmaven.repo.local=$localRepo" @MavenArgs
}

exit $LASTEXITCODE
