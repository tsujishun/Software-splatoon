param(
    [string]$ProjectRoot = ".",
    [string[]]$TargetDirs = @("core/src", "lwjgl3/src")
)

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[Console]::OutputEncoding = $utf8NoBom
$OutputEncoding = $utf8NoBom

function Get-ApproxCodeLineCount {
    param(
        [string[]]$Lines
    )

    $count = 0
    foreach ($line in $Lines) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed)) {
            continue
        }

        if (
            $trimmed.StartsWith("//") -or
            $trimmed.StartsWith("/*") -or
            $trimmed.StartsWith("*") -or
            $trimmed.StartsWith("*/")
        ) {
            continue
        }

        $count++
    }

    return $count
}

function Get-ProjectRelativePath {
    param(
        [string]$RootPath,
        [string]$FilePath
    )

    $normalizedRoot = $RootPath.TrimEnd('\', '/')
    if ($FilePath.StartsWith($normalizedRoot)) {
        return $FilePath.Substring($normalizedRoot.Length).TrimStart('\', '/').Replace('\', '/')
    }

    return $FilePath.Replace('\', '/')
}

$resolvedRoot = (Resolve-Path $ProjectRoot).Path
$javaFiles = @()

foreach ($targetDir in $TargetDirs) {
    $fullTargetDir = Join-Path $resolvedRoot $targetDir
    if (-not (Test-Path $fullTargetDir)) {
        continue
    }

    $javaFiles += Get-ChildItem -Path $fullTargetDir -Recurse -Filter "*.java" -File |
        Where-Object {
            $_.FullName -notmatch '[\\/](build|\.gradle|\.git)[\\/]'
        }
}

$results = foreach ($file in $javaFiles | Sort-Object FullName) {
    $lines = Get-Content -LiteralPath $file.FullName
    $relativePath = Get-ProjectRelativePath -RootPath $resolvedRoot -FilePath $file.FullName
    $module = if ($relativePath.StartsWith("core/")) { "core" } else { "lwjgl3" }
    $totalLines = $lines.Count
    $nonBlankLines = ($lines | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }).Count
    $approxCodeLines = Get-ApproxCodeLineCount -Lines $lines

    [PSCustomObject]@{
        FileName = $file.Name
        RelativePath = $relativePath
        Module = $module
        TotalLines = $totalLines
        NonBlankLines = $nonBlankLines
        ApproxCodeLines = $approxCodeLines
    }
}

$results | ConvertTo-Json -Depth 3
