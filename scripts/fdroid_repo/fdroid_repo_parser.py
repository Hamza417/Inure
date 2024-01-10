import os
import xml.etree.ElementTree as elementTree

import requests

# URL of the F-Droid repository XML metadata file
fdroid_repo_url = "https://f-droid.org/repo/index.xml"
izzyondroid_repo_url = "https://apt.izzysoft.de/fdroid/repo/index.xml"

# Directory where the downloaded repository will be saved
download_dir = ""  # specify the directory here

# Create the download directory if it doesn't exist
try:
    os.makedirs(download_dir, exist_ok=True)
except FileNotFoundError:
    print("Directory not specified... Using the current directory.")


# Download and parse F-Droid repository XML metadata
def download_parse_repo(repo_url, repo_name):
    response = requests.get(repo_url)
    if response.status_code == 200:
        xml_content_ = response.content
        with open(os.path.join(download_dir, f"{repo_name}_index.xml"), "wb") as xml_file_:
            xml_file_.write(xml_content_)
        return xml_content_
    else:
        print(f"Failed to download {repo_name} repository metadata.")
        exit(1)


print("Downloading repository metadata...", end="\n\n")
fdroid_xml_content = download_parse_repo(fdroid_repo_url, "fdroid")
izzyondroid_xml_content = download_parse_repo(izzyondroid_repo_url, "izzyondroid")

# Parse F-Droid repository XML metadata
fdroid_tree = elementTree.ElementTree(elementTree.fromstring(fdroid_xml_content))
fdroid_root = fdroid_tree.getroot()

# Parse IzzyOnDroid repository XML metadata
izzyondroid_tree = elementTree.ElementTree(elementTree.fromstring(izzyondroid_xml_content))
izzyondroid_root = izzyondroid_tree.getroot()

# Create a string to store the XML content
xml_content = '<?xml version="1.0" encoding="utf-8"?>\n'
xml_content += '<resources>\n'
count = 0


# Function to parse XML and populate xml_content
def parse_xml(root):
    for application in root.findall(".//application"):
        app_id = application.get("id")
        license_ = application.find(".//license").text

        # Check for duplicates before adding to the xml_content
        global xml_content  # use the global xml_content variable
        if f'{app_id}' not in xml_content:
            xml_content += f'    <string name="{app_id}" translatable="false">{license_}</string>\n'
            global count  # use the global count variable
            count += 1


# Iterate through the applications in the XML
# and get the package name and version code
print("Parsing F-Droid repository XML metadata...")
parse_xml(fdroid_root)

print("Parsing IzzyOnDroid repository XML metadata...", end="\n\n")
parse_xml(izzyondroid_root)

xml_content += '</resources>\n'
print(f"Total applications added: {count}", end="\n\n")

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
        open(f"../../app/src/main/res/xml/package_versions.xml", "r")
        print("File already exists at: ..\\..\\app\\src\\main\\res\\xml\\package_versions.xml")
        print("Deleting..")
        os.remove(f"../../app/src/main/res/xml/package_versions.xml")
    except FileNotFoundError:
        pass

    # Move the file to the values directory
    os.rename(os.path.join(download_dir, "package_versions.xml"),
              f"../../app/src/main/res/xml/package_versions.xml")

    print("File moved successfully.", end="\n\n")

# Delete the downloaded repository XML metadata file
should_delete = input("Do you want to delete the repository metadata file? (y/n): ")
if should_delete.lower() == "y":
    os.remove(os.path.join(download_dir, "fdroid_index.xml"))
    os.remove(os.path.join(download_dir, "izzyondroid_index.xml"))
    print("Repository metadata files deleted successfully.")
else:
    print("Repository metadata files not deleted.")
