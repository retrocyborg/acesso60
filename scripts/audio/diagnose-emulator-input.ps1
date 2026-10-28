[CmdletBinding()]
param(
    [string]$Serial = 'emulator-5554',
    [ValidateRange(1, 30)]
    [int]$DurationSeconds = 5,
    [string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'

$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
if (-not (Test-Path -LiteralPath $adb)) {
    throw "adb nao encontrado em $adb"
}

if (-not $OutputDirectory) {
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $OutputDirectory = Join-Path $env:TEMP "acesso60-audio-diagnostics\$stamp"
}
$OutputDirectory = [System.IO.Path]::GetFullPath($OutputDirectory)
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

$deviceState = (& $adb -s $Serial get-state 2>&1 | Out-String).Trim()
if ($deviceState -ne 'device') {
    throw "Dispositivo $Serial indisponivel: $deviceState"
}

$remoteDirectory = '/sdcard/Android/data/com.acesso60/files/audio-diagnostics'
$remoteWav = "$remoteDirectory/usb4-test.wav"
$remoteSummary = "$remoteDirectory/capture-summary.json"
$localWav = Join-Path $OutputDirectory 'usb4-test.wav'
$androidSummary = Join-Path $OutputDirectory 'capture-summary-android.json'
$beforeLog = Join-Path $OutputDirectory 'audio-before.txt'
$afterLog = Join-Path $OutputDirectory 'audio-after.txt'
$logcatFile = Join-Path $OutputDirectory 'logcat-audio.txt'
$summaryFile = Join-Path $OutputDirectory 'summary.json'

& $adb -s $Serial emu avd hostmicon | Out-Null
& $adb -s $Serial logcat -c
& $adb -s $Serial shell dumpsys media.audio_flinger | Out-File -Encoding utf8 $beforeLog

Write-Host "Fale no USB Audio Device 4 durante os proximos $DurationSeconds segundos."
& $adb -s $Serial shell rm -f $remoteWav $remoteSummary
& $adb -s $Serial shell am start -W `
    -n 'com.acesso60/.diagnostics.AudioCaptureDiagnosticActivity' `
    --ei duration_seconds $DurationSeconds | Out-Null

$deadline = (Get-Date).AddSeconds($DurationSeconds + 15)
do {
    Start-Sleep -Milliseconds 500
    & $adb -s $Serial shell test -f $remoteSummary
    $summaryReady = $LASTEXITCODE -eq 0
} while (-not $summaryReady -and (Get-Date) -lt $deadline)
if (-not $summaryReady) {
    throw 'O diagnostico Android nao produziu o resumo dentro do prazo.'
}

& $adb -s $Serial pull $remoteSummary $androidSummary | Out-Null
$androidCapture = Get-Content -Raw -LiteralPath $androidSummary | ConvertFrom-Json
if ($androidCapture.status -ne 'captured') {
    throw "Captura Android falhou: $($androidCapture.error)"
}
& $adb -s $Serial pull $remoteWav $localWav | Out-Null
if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $localWav)) {
    throw 'Falha ao copiar o WAV do Android.'
}
& $adb -s $Serial shell dumpsys media.audio_flinger | Out-File -Encoding utf8 $afterLog
& $adb -s $Serial logcat -d -v threadtime | Out-File -Encoding utf8 $logcatFile

function Measure-PcmWave {
    param([Parameter(Mandatory)][string]$Path)

    $stream = [System.IO.File]::OpenRead($Path)
    $reader = [System.IO.BinaryReader]::new($stream)
    try {
        $riff = -join ($reader.ReadChars(4))
        [void]$reader.ReadUInt32()
        $wave = -join ($reader.ReadChars(4))
        if ($riff -ne 'RIFF' -or $wave -ne 'WAVE') {
            throw 'Arquivo capturado nao e RIFF/WAVE.'
        }

        $channels = $null
        $sampleRate = $null
        $bitsPerSample = $null
        $audioFormat = $null
        $dataOffset = $null
        $dataLength = $null

        while ($stream.Position -le ($stream.Length - 8)) {
            $chunkId = -join ($reader.ReadChars(4))
            $chunkLength = $reader.ReadUInt32()
            $chunkStart = $stream.Position
            if ($chunkId -eq 'fmt ') {
                $audioFormat = $reader.ReadUInt16()
                $channels = $reader.ReadUInt16()
                $sampleRate = $reader.ReadUInt32()
                [void]$reader.ReadUInt32()
                [void]$reader.ReadUInt16()
                $bitsPerSample = $reader.ReadUInt16()
            } elseif ($chunkId -eq 'data') {
                $dataOffset = $stream.Position
                $dataLength = $chunkLength
                break
            }
            $stream.Position = $chunkStart + $chunkLength + ($chunkLength % 2)
        }

        if ($audioFormat -ne 1 -or $bitsPerSample -ne 16 -or $null -eq $dataOffset) {
            throw "Formato WAV nao suportado: format=$audioFormat bits=$bitsPerSample"
        }

        $stream.Position = $dataOffset
        $sampleCount = [int64]($dataLength / 2)
        $sumSquares = 0.0
        $peak = 0
        $nonZero = 0L
        for ($index = 0L; $index -lt $sampleCount; $index++) {
            $sample = [int]$reader.ReadInt16()
            $absolute = [Math]::Abs($sample)
            if ($absolute -gt $peak) { $peak = $absolute }
            if ($sample -ne 0) { $nonZero++ }
            $sumSquares += [double]$sample * [double]$sample
        }
        $rms = if ($sampleCount -gt 0) { [Math]::Sqrt($sumSquares / $sampleCount) } else { 0.0 }

        [ordered]@{
            audio_format = $audioFormat
            channels = $channels
            sample_rate_hz = $sampleRate
            bits_per_sample = $bitsPerSample
            samples = $sampleCount
            duration_seconds = if ($sampleRate -gt 0 -and $channels -gt 0) {
                [Math]::Round($sampleCount / ($sampleRate * $channels), 3)
            } else { 0 }
            peak = $peak
            peak_normalized = [Math]::Round($peak / 32768.0, 8)
            rms = [Math]::Round($rms, 4)
            rms_normalized = [Math]::Round($rms / 32768.0, 8)
            non_zero_samples = $nonZero
            has_non_zero_samples = $nonZero -gt 0
        }
    } finally {
        $reader.Dispose()
        $stream.Dispose()
    }
}

$measurement = Measure-PcmWave -Path $localWav
$summary = [ordered]@{
    captured_at = (Get-Date).ToString('o')
    serial = $Serial
    requested_duration_seconds = $DurationSeconds
    wav_path = $localWav
    wav_size_bytes = (Get-Item -LiteralPath $localWav).Length
    wav_sha256 = (Get-FileHash -LiteralPath $localWav -Algorithm SHA256).Hash
    measurement = $measurement
    evidence = [ordered]@{
        android_capture_summary = $androidSummary
        audio_before = $beforeLog
        audio_after = $afterLog
        logcat = $logcatFile
    }
}
$summary | ConvertTo-Json -Depth 5 | Set-Content -Encoding utf8 $summaryFile

Write-Output ($summary | ConvertTo-Json -Depth 5)
Write-Output "Evidencias: $OutputDirectory"
