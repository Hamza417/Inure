import json

# Load the JSON file
with open('../../app/src/main/resources/trackers.json') as f:
    data = json.load(f)

# Initialize an empty set to store unique categories
categories = set()

# Iterate over each tracker in the JSON data
for tracker in data['trackers'].values():
    # Check if the "categories" field exists for the tracker
    if 'categories' in tracker:
        # If it does, add each category to the set
        for category in tracker['categories']:
            categories.add(category)

# Print all unique categories
for category in categories:
    print(category)
