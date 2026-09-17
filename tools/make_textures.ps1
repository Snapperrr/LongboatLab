param([Parameter(Mandatory=$true)][string]$MinecraftJar)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem
$projectRoot = Split-Path $PSScriptRoot -Parent
$outputDirectory = Join-Path $projectRoot 'src/main/resources/assets/longboatlab/textures/entity'
[System.IO.Directory]::CreateDirectory($outputDirectory) | Out-Null
$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $MinecraftJar))
try {
    foreach ($wood in @('oak','spruce','birch','jungle','acacia','dark_oak','mangrove','cherry','bamboo')) {
        $entry = $archive.GetEntry("assets/minecraft/textures/block/${wood}_planks.png")
        if ($null -eq $entry) { throw "Missing plank texture: $wood" }
        $stream = $entry.Open()
        $tile = [System.Drawing.Bitmap]::FromStream($stream)
        $atlas = [System.Drawing.Bitmap]::new(2048, 1024)
        $graphics = [System.Drawing.Graphics]::FromImage($atlas)
        try {
            for ($tileY = 0; $tileY -lt 1024; $tileY += 16) {
                for ($tileX = 0; $tileX -lt 2048; $tileX += 16) {
                    $graphics.DrawImageUnscaled($tile, $tileX, $tileY)
                }
            }
            $atlas.Save((Join-Path $outputDirectory "$wood.png"), [System.Drawing.Imaging.ImageFormat]::Png)
        } finally {
            $graphics.Dispose()
            $atlas.Dispose()
            $tile.Dispose()
            $stream.Dispose()
        }
    }
} finally { $archive.Dispose() }
