Community permissions descriptions of some OEM or Developer defined permissions 
for native framework or system apps whose details Android doesn't or cannot provide.

If you're a developer or someone who has information about some permissions and want to 
add their description to this list, you're most welcome to do so. Just add the relevant 
information make a pull request on GitHub, don't forget to add your name or username to 
the list of contributors above the permission details you've been contributed.

## Structure

The permissions are defined in a JSON object with the permission name as the key and
the value as an object containing the following properties:

- `id` : The unique identifier of the permission.
- `label`: The human-readable name of the permission.
- `description`: A brief description of the permission.
- `protectionLevel`: The protection level of the permission.
- `group`: The group of the permission.

### Example

```json
{
    "com.example.permission.SOME_PERMISSION": {
        "label": "Some Permission",
        "description": "This permission allows the app to do something.",
        "protectionLevel": "dangerous",
        "group": "android.permission-group.SOME_GROUP"
    }
}
```

### Schema
```json
{
    "$schema": "http://json-schema.org/draft-07/schema#",
    "type": "object",
    "patternProperties": {
        ".*": {
            "type": "object",
            "properties": {
                "label": {
                    "type": "string"
                },
                "description": {
                    "type": "string"
                },
                "protectionLevel": {
                    "type": "string"
                },
                "group": {
                    "type": "string"
                }
            },
            "required": ["label", "description", "protectionLevel", "group"]
        }
    }
}
```

## How to contribute?

1. Fork the repository or just click on the edit button on he viewer.
2. Add the permission details to the JSON object.
3. Make a pull request.
4. Wait for the review and merge.
5. Done!

## Status
Last updated: 2021-05-01 1:53 AM
Contributors: 
@Hamza417

## License

```
This file is licensed under the GNU General Public License v3.0
```
