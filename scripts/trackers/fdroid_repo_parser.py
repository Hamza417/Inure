import os
import xml.etree.ElementTree as ET

import requests

# URL of the F-Droid repository XML metadata file
repo_url = "https://f-droid.org/repo/index.xml"

# Directory where the downloaded repository will be saved
download_dir = "fdroid_repo"

# Create the download directory if it doesn't exist
os.makedirs(download_dir, exist_ok=True)

# Ask to re-download the repository XML metadata file
# if it already exists
if os.path.exists(os.path.join(download_dir, "index.xml")):
    should_redownload = input("Repository metadata already downloaded. Re-download? (y/n): ")
    if should_redownload.lower() == "y":
        os.remove(os.path.join(download_dir, "index.xml"))

# Download the repository XML metadata file
if os.path.exists(os.path.join(download_dir, "index.xml")):
    print("Repository metadata already downloaded.")
    # exit(0)
else:
    print("Downloading repository metadata...")
    response = requests.get(repo_url)
    if response.status_code == 200:
        xml_content = response.content
        with open(os.path.join(download_dir, "index.xml"), "wb") as xml_file:
            xml_file.write(xml_content)
    else:
        print("Failed to download repository metadata.")
        exit(1)

# Parse the downloaded XML metadata file
tree = ET.parse(os.path.join(download_dir, "index.xml"))
root = tree.getroot()

# Create a string to store the XML content
xml_content = '<?xml version="1.0" encoding="utf-8"?>\n'
xml_content += '<resources>\n'

# Iterate through the applications in the XML
for application in root.findall(".//application"):
    app_id = application.get("id")
    version_code = application.find(".//versioncode").text

    xml_content += f'    <string name="{app_id}">{version_code}</string>\n'

xml_content += '</resources>\n'

# Write the package versions to an Android XML resource file
with open(os.path.join(download_dir, "package_versions.xml"), "w") as xml_file:
    xml_file.write(xml_content)

print("Android XML resource file created successfully.")

# Delete the downloaded repository XML metadata file
os.remove(os.path.join(download_dir, "index.xml"))
print("Repository metadata file deleted successfully.")
