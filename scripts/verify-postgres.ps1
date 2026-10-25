# Local integration only. Creates an isolated database, keeps its stopped container/volume as evidence.
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
$taskEnvNames = @('POSTGRES_PASSWORD','HUMMING_TEST_DB_URL','HUMMING_TEST_DB_USER','HUMMING_TEST_DB_PASSWORD')
$taskPrevious = @{}
foreach ($taskName in $taskEnvNames) { $taskPrevious[$taskName] = [Environment]::GetEnvironmentVariable($taskName, 'Process') }
$taskSuffix = [guid]::NewGuid().ToString('N').Substring(0,12)
$taskContainer = "humming-t21-$taskSuffix"
$taskVolume = "$taskContainer-data"
$taskCreated = $false
$taskImage = 'postgres@sha256:edb0c08dc33befd0262ab3b5cf1c4f3f8cfd8b09e88c05b45adee29d6761ddb6'
function Invoke-TaskDocker {
    param([string[]]$TaskArguments)
    $taskOutput = & docker @TaskArguments
    if ($LASTEXITCODE -ne 0) { throw 'Docker operation failed; credentials are not included in this error' }
    return $taskOutput
}
function Wait-TaskDatabase {
    for ($taskAttempt=0; $taskAttempt -lt 60; $taskAttempt++) {
        $null = & docker exec $taskContainer pg_isready -h 127.0.0.1 -U humming -d humming_test 2>$null
        if ($LASTEXITCODE -eq 0) { return }
        Start-Sleep -Milliseconds 500
    }
    throw 'PostgreSQL did not become ready within 30 seconds'
}
function Update-TaskEndpoint {
    $taskBinding = Invoke-TaskDocker @('port',$taskContainer,'5432/tcp')
    $taskPort = [regex]::Match($taskBinding, '127\.0\.0\.1:(\d+)').Groups[1].Value
    if (-not $taskPort) { throw 'Expected loopback-only database binding' }
    $env:HUMMING_TEST_DB_URL = "jdbc:postgresql://127.0.0.1:$taskPort/humming_test"
    @{container=$taskContainer;volume=$taskVolume;port=$taskPort;image=$taskImage} | ConvertTo-Json | Set-Content build/t21/environment.json
    for ($taskAttempt=0; $taskAttempt -lt 20; $taskAttempt++) {
        $taskSocket = [System.Net.Sockets.TcpClient]::new()
        try {
            $taskConnect = $taskSocket.ConnectAsync('127.0.0.1', [int]$taskPort)
            if ($taskConnect.Wait(250) -and $taskSocket.Connected) { return }
        } catch [System.AggregateException] { }
        finally { $taskSocket.Dispose() }
        Start-Sleep -Milliseconds 250
    }
    throw 'Published PostgreSQL loopback endpoint did not become ready'
}
try {
    New-Item -ItemType Directory -Force build/t21 | Out-Null
    $env:POSTGRES_PASSWORD = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
    $null = Invoke-TaskDocker @('volume','create','--label','humming.task=T21',$taskVolume)
    $null = Invoke-TaskDocker @('run','-d','--name',$taskContainer,'--label','humming.task=T21',
        '--env','POSTGRES_PASSWORD','--env','POSTGRES_USER=humming','--env','POSTGRES_DB=humming_test',
        '--publish','127.0.0.1::5432','--mount',"type=volume,source=$taskVolume,target=/var/lib/postgresql",$taskImage)
    $taskCreated = $true
    Wait-TaskDatabase
    Update-TaskEndpoint
    $env:HUMMING_TEST_DB_USER = 'humming'
    $env:HUMMING_TEST_DB_PASSWORD = $env:POSTGRES_PASSWORD
    & .\gradlew.bat :humming-server-postgres:postgresIntegrationTest :humming-server-host:postgresHttpIntegrationTest > build/t21/integration.log 2>&1
    if ($LASTEXITCODE -ne 0) { Get-Content build/t21/integration.log -Tail 35; throw 'PostgreSQL integration failed' }
    $taskBefore = (Invoke-TaskDocker @('exec',$taskContainer,'psql','-U','humming','-d','humming_test','-Atc','SELECT count(*) FROM public.humming_drafts')).Trim()
    $null = Invoke-TaskDocker @('restart',$taskContainer)
    Wait-TaskDatabase
    Update-TaskEndpoint
    $taskAfter = (Invoke-TaskDocker @('exec',$taskContainer,'psql','-U','humming','-d','humming_test','-Atc','SELECT count(*) FROM public.humming_drafts')).Trim()
    if ($taskBefore -ne $taskAfter -or [int]$taskAfter -le 0) { throw 'Durability check failed across database restart' }
    "database-restart: rows-before=$taskBefore rows-after=$taskAfter PASS" | Set-Content build/t21/restart.log
    & .\gradlew.bat :humming-server-postgres:postgresIntegrationTest :humming-server-host:postgresHttpIntegrationTest > build/t21/after-restart.log 2>&1
    if ($LASTEXITCODE -ne 0) { Get-Content build/t21/after-restart.log -Tail 35; throw 'PostgreSQL verification after restart failed' }
    Get-Content build/t21/restart.log
    Get-Content build/t21/after-restart.log -Tail 12
} finally {
    if ($taskCreated) { $null = & docker stop $taskContainer }
    foreach ($taskName in $taskEnvNames) { [Environment]::SetEnvironmentVariable($taskName, $taskPrevious[$taskName], 'Process') }
    Pop-Location
}
