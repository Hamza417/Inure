# Get current directory
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Definition
Write-Host "Current directory: $scriptPath"
Set-Location $scriptPath
Write-Host "CD'd to: $scriptPath"

## Add line breaks to the output
Write-Host "`nParsing JSON file..."

$jsonFile = "trackers.json"
$outputFile = "output.txt"

# If output.txt already exists, delete it
Write-Host "Checking if $outputFile already exists..."
if (Test-Path $outputFile)
{
    Write-Host "File already exist: $scriptPath\$outputFile"
    Remove-Item $outputFile
    Write-Host "Deleted: $outputFile `n"
}

# Read the JSON file content
$jsonContent = Get-Content -Path $jsonFile -Raw

# Parse the JSON content
$jsonObject = ConvertFrom-Json $jsonContent
$trackers = $jsonObject.trackers

# Print the trackers to the console
Write-Host "Trackers:"
$trackers.Values | ForEach-Object {
    $trackerObject = $_
    Write-Host $trackerObject.website
}

# Extract the website tags
foreach ($tracker in $trackers)
{
    # Print the website to the console
    Write-Host $tracker.website
    $tracker.website
}

# <string-array name="tweb">
#    <item>https://www.teemo.co</item>
#    <item>https://www.fidzup.com</item>
# </string-array>

# Format and write the output to the text file
$output = @"
<string-array name="trackers">
"@

$output += $websites | ForEach-Object {
    # Write in a newline
    "`n`t<item>$_</item>"
}

$output = @"
$output
</string-array>
"@

$output | Out-File -FilePath $outputFile -Encoding UTF8

Write-Host "Output written to $outputFile"

# Press any key to continue...
Read-Host -Prompt "Press Enter to exit"
