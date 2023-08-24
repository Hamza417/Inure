import os
import xml.etree.ElementTree as elementTree

import requests

# URL of the F-Droid repository XML metadata file
repo_url = "https://f-droid.org/repo/index.xml"

# Directory where the downloaded repository will be saved
download_dir = ""  # specify the directory here

# Create the download directory if it doesn't exist
try:
    os.makedirs(download_dir, exist_ok=True)
except FileNotFoundError:
    print("Invalid download directory.")

# Ask to re-download the repository XML metadata file
# if it already exists
print("Checking if repository metadata already exists...")
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

    # Print file size
    file_size = int(response.headers.get("content-length", 0))
    print(f"File size: {file_size / 1024 / 1024:.2f} MB")

    if response.status_code == 200:
        xml_content = response.content
        with open(os.path.join(download_dir, "index.xml"), "wb") as xml_file:
            xml_file.write(xml_content)
    else:
        print("Failed to download repository metadata.")
        exit(1)

# Parse the downloaded XML metadata file
tree = elementTree.parse(os.path.join(download_dir, "index.xml"))
root = tree.getroot()

# Create a string to store the XML content
xml_content = '<?xml version="1.0" encoding="utf-8"?>\n'
xml_content += '<resources>\n'

# Iterate through the applications in the XML
# and get the package name and version code
count = 0
for application in root.findall(".//application"):
    app_id = application.get("id")
    version_code = application.find(".//versioncode").text

    xml_content += f'    <string name="{app_id}" translatable="false">{version_code}</string>\n'
    count += 1

xml_content += '</resources>\n'
print(f"Total applications added: {count}")

# Write the package versions to an Android XML resource file
with open(os.path.join(download_dir, "package_versions.xml"), "w") as xml_file:
    xml_file.write(xml_content)

print("Android XML resource file created successfully.")

# Copy the file android values directory
# The path Inure is one level above the trackers folder

# Check if the file exists
decision = input("Do you want to copy the file to Inure? (y/n): ")
if decision.lower() == "y":
    try:
        open(f"../../app/src/main/res/values/package_versions.xml", "r")
        print("File already exists at: ..\\..\\app\\src\\main\\res\\values\\package_versions.xml")
        print("Deleting..")
        os.remove(f"../../app/src/main/res/values/package_versions.xml")
    except FileNotFoundError:
        pass

    # Copy the file to the values directory
    os.rename(os.path.join(download_dir, "package_versions.xml"),
              f"../../app/src/main/res/values/package_versions.xml")

    print("File copied successfully.")

# Delete the downloaded repository XML metadata file
should_delete = input("Do you want to delete the repository metadata file? (y/n): ")
if should_delete.lower() == "y":
    os.remove(os.path.join(download_dir, "index.xml"))
    print("Repository metadata file deleted successfully.")
else:
    print("Repository metadata file not deleted.")
