import xml.etree.ElementTree as ET


def extract_permission_names(xml_file, output_file):
    # Check if file exists
    try:
        with open(xml_file, 'r'):
            print(f"Extracting permissions from {xml_file}")
            pass
    except FileNotFoundError:
        print(f"File {xml_file} not found")
        return

    # Parse the XML file
    tree = ET.parse(xml_file)

    # Get the root element
    root = tree.getroot()

    # Find all permission elements
    permission_elements = root.findall(".//permission")

    # Open the output file in write mode
    with open(output_file, 'w') as f:
        # Extract and write the name attribute of each permission element to the file
        for permission in permission_elements:
            # Access the attribute using the full namespace URL
            permission_name = permission.get('{http://schemas.android.com/apk/res/android}name')
            if permission_name is not None:
                f.write(permission_name + '\n')
            else:
                # If android:name attribute is not found, print the entire permission element
                print("Permission name not found for element:")
                ET.dump(permission)
                print()  # Add a newline for better readability


# Use the function
extract_permission_names('AndroidManifest.xml', 'permissions.txt')
