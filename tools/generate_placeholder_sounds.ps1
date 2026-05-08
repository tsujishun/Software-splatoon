param(
    [string]$OutputDir = "assets/audio"
)

$ErrorActionPreference = "Stop"

$sampleRate = 44100
$masterVolume = 0.42
$fadeSeconds = 0.008

$sounds = @(
    @{
        Name = "player_shoot.wav"
        Events = @(
            @{ Duration = 0.035; StartHz = 820; EndHz = 620; Wave = "square"; Amp = 0.95 }
            @{ Duration = 0.025; StartHz = 620; EndHz = 540; Wave = "sine"; Amp = 0.55 }
        )
    }
    @{
        Name = "enemy_shoot.wav"
        Events = @(
            @{ Duration = 0.05; StartHz = 520; EndHz = 380; Wave = "square"; Amp = 0.9 }
            @{ Duration = 0.03; StartHz = 380; EndHz = 320; Wave = "sine"; Amp = 0.5 }
        )
    }
    @{
        Name = "hit.wav"
        Events = @(
            @{ Duration = 0.02; StartHz = 980; EndHz = 860; Wave = "square"; Amp = 1.0 }
            @{ Duration = 0.03; StartHz = 860; EndHz = 720; Wave = "sine"; Amp = 0.8 }
        )
    }
    @{
        Name = "player_splatted.wav"
        Events = @(
            @{ Duration = 0.08; StartHz = 660; EndHz = 520; Wave = "square"; Amp = 0.9 }
            @{ Duration = 0.08; StartHz = 520; EndHz = 380; Wave = "square"; Amp = 0.8 }
            @{ Duration = 0.10; StartHz = 380; EndHz = 220; Wave = "sine"; Amp = 0.7 }
        )
    }
    @{
        Name = "enemy_splatted.wav"
        Events = @(
            @{ Duration = 0.07; StartHz = 760; EndHz = 600; Wave = "square"; Amp = 0.9 }
            @{ Duration = 0.07; StartHz = 600; EndHz = 440; Wave = "square"; Amp = 0.8 }
            @{ Duration = 0.09; StartHz = 440; EndHz = 260; Wave = "sine"; Amp = 0.7 }
        )
    }
    @{
        Name = "countdown_beep.wav"
        Events = @(
            @{ Duration = 0.12; StartHz = 760; EndHz = 760; Wave = "sine"; Amp = 0.8 }
        )
    }
    @{
        Name = "countdown_go.wav"
        Events = @(
            @{ Duration = 0.07; StartHz = 540; EndHz = 540; Wave = "sine"; Amp = 0.7 }
            @{ Duration = 0.07; StartHz = 720; EndHz = 720; Wave = "sine"; Amp = 0.8 }
            @{ Duration = 0.12; StartHz = 980; EndHz = 980; Wave = "square"; Amp = 0.85 }
        )
    }
    @{
        Name = "game_over.wav"
        Events = @(
            @{ Duration = 0.12; StartHz = 560; EndHz = 460; Wave = "sine"; Amp = 0.75 }
            @{ Duration = 0.12; StartHz = 460; EndHz = 340; Wave = "sine"; Amp = 0.7 }
            @{ Duration = 0.18; StartHz = 340; EndHz = 180; Wave = "sine"; Amp = 0.65 }
        )
    }
    @{
        Name = "weapon_switch.wav"
        Events = @(
            @{ Duration = 0.04; StartHz = 660; EndHz = 660; Wave = "square"; Amp = 0.75 }
            @{ Duration = 0.05; StartHz = 880; EndHz = 880; Wave = "square"; Amp = 0.75 }
        )
    }
    @{
        Name = "ink_empty.wav"
        Events = @(
            @{ Duration = 0.06; StartHz = 280; EndHz = 240; Wave = "square"; Amp = 0.85 }
            @{ Duration = 0.06; StartHz = 240; EndHz = 200; Wave = "square"; Amp = 0.75 }
        )
    }
)

function Get-WaveValue {
    param(
        [double]$Phase,
        [string]$Wave
    )

    switch ($Wave) {
        "square" {
            if ([Math]::Sin($Phase) -ge 0) { return 1.0 }
            return -1.0
        }
        "triangle" {
            return 2.0 * [Math]::Abs((($Phase / [Math]::PI) % 2.0) - 1.0) - 1.0
        }
        default {
            return [Math]::Sin($Phase)
        }
    }
}

function Get-Envelope {
    param(
        [double]$Progress,
        [double]$Duration
    )

    $fadeRatio = [Math]::Min(0.45, $fadeSeconds / [Math]::Max(0.001, $Duration))
    if ($Progress -lt $fadeRatio) {
        return $Progress / $fadeRatio
    }
    if ($Progress -gt (1.0 - $fadeRatio)) {
        return (1.0 - $Progress) / $fadeRatio
    }
    return 1.0
}

function New-SampleArray {
    param(
        [array]$Events
    )

    $samples = New-Object System.Collections.Generic.List[System.Int16]

    foreach ($event in $Events) {
        $sampleCount = [Math]::Max(1, [int]($event.Duration * $sampleRate))
        for ($index = 0; $index -lt $sampleCount; $index++) {
            $progress = $index / [double]$sampleCount
            $frequency = $event.StartHz + (($event.EndHz - $event.StartHz) * $progress)
            $timeSeconds = $index / [double]$sampleRate
            $phase = 2.0 * [Math]::PI * $frequency * $timeSeconds
            $value = Get-WaveValue -Phase $phase -Wave $event.Wave
            $value *= $event.Amp * $masterVolume * (Get-Envelope -Progress $progress -Duration $event.Duration)
            $value = [Math]::Max(-1.0, [Math]::Min(1.0, $value))
            $samples.Add([int16]([Math]::Round($value * 32767)))
        }
    }

    return ,$samples.ToArray()
}

function Write-WavFile {
    param(
        [string]$Path,
        [Int16[]]$Samples
    )

    $directory = Split-Path -Parent $Path
    if ($directory) {
        New-Item -ItemType Directory -Force -Path $directory | Out-Null
    }

    $stream = [System.IO.File]::Open($Path, [System.IO.FileMode]::Create, [System.IO.FileAccess]::Write)
    $writer = New-Object System.IO.BinaryWriter($stream)

    try {
        $dataSize = $Samples.Length * 2
        $riffSize = 36 + $dataSize
        $byteRate = $sampleRate * 2

        $writer.Write([System.Text.Encoding]::ASCII.GetBytes("RIFF"))
        $writer.Write([int]$riffSize)
        $writer.Write([System.Text.Encoding]::ASCII.GetBytes("WAVE"))
        $writer.Write([System.Text.Encoding]::ASCII.GetBytes("fmt "))
        $writer.Write([int]16)
        $writer.Write([int16]1)
        $writer.Write([int16]1)
        $writer.Write([int]$sampleRate)
        $writer.Write([int]$byteRate)
        $writer.Write([int16]2)
        $writer.Write([int16]16)
        $writer.Write([System.Text.Encoding]::ASCII.GetBytes("data"))
        $writer.Write([int]$dataSize)

        foreach ($sample in $Samples) {
            $writer.Write([int16]$sample)
        }
    }
    finally {
        $writer.Dispose()
        $stream.Dispose()
    }
}

$resolvedOutput = Join-Path (Get-Location) $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutput | Out-Null

foreach ($sound in $sounds) {
    $samples = New-SampleArray -Events $sound.Events
    $outputPath = Join-Path $resolvedOutput $sound.Name
    Write-WavFile -Path $outputPath -Samples $samples
}

Write-Host "Generated placeholder sounds:"
foreach ($sound in $sounds) {
    Write-Host (" - " + (Join-Path "assets/audio" $sound.Name))
}
