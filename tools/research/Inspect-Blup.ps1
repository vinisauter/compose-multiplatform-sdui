param(
    [string]$InstallRoot = 'C:\Program Files (x86)\Blup',
    [string]$OutputPath
)
$ErrorActionPreference = 'Stop'
# Read-only, bounded feature probes. Never dump configuration, credentials or arbitrary strings.
$exe = Join-Path $InstallRoot 'Blup.exe'
$aot = Join-Path $InstallRoot 'data/app.so'
$manifest = Join-Path $InstallRoot 'data/flutter_assets/AssetManifest.json'
$assets = (Get-Content -LiteralPath $manifest -Raw | ConvertFrom-Json).PSObject.Properties.Name
$bytes = [IO.File]::ReadAllBytes($aot)
$ascii = [Text.Encoding]::ASCII.GetString($bytes)
$terms = @('AutoLayout', 'Import from Figma', 'Debug Console', 'Minimap', 'NodePoint', 'Choose from Assets', 'onClick', 'pubspec.yaml')
$probes = foreach ($term in $terms) {
    [ordered]@{ term = $term; firstByteOffset = $ascii.IndexOf($term, [StringComparison]::OrdinalIgnoreCase) }
}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zipPath = Join-Path $InstallRoot 'data/flutter_assets/assets/extensions.zip'
$zip = [IO.Compression.ZipFile]::OpenRead($zipPath)
try {
    $extensions = @($zip.Entries | Where-Object { $_.FullName -match '^extensions/[^/]+/package.json$' } | ForEach-Object { $_.FullName })
} finally { $zip.Dispose() }
$report = [ordered]@{
    method = 'Static metadata, asset manifest keys, ZIP entry names and case-insensitive ASCII literal probes; no execution/decompilation'
    executable = [ordered]@{ sha256 = (Get-FileHash -LiteralPath $exe).Hash; bytes = (Get-Item -LiteralPath $exe).Length; fileVersion = (Get-Item -LiteralPath $exe).VersionInfo.FileVersion }
    aot = [ordered]@{ sha256 = (Get-FileHash -LiteralPath $aot).Hash; bytes = $bytes.Length; probes = @($probes) }
    assets = [ordered]@{ manifestSha256 = (Get-FileHash -LiteralPath $manifest).Hash; count = @($assets).Count; packageNames = @($assets | Where-Object { $_ -match '^packages/([^/]+)/' } | ForEach-Object { ($_ -split '/')[1] } | Sort-Object -Unique) }
    editorExtensions = $extensions
    limitations = @('String presence does not establish reachability or runtime behavior', 'Absent literal does not establish absent capability', 'Asset/plugin names do not establish server architecture or enabled integrations')
}
$json = $report | ConvertTo-Json -Depth 8
if ($OutputPath) { [IO.File]::WriteAllText([IO.Path]::GetFullPath($OutputPath), $json, [Text.UTF8Encoding]::new($false)) } else { $json }
