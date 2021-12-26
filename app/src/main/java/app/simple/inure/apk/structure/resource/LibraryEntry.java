package app.simple.inure.apk.structure.resource;

public class LibraryEntry {
    // uint32. The package-id this shared library was assigned at build time.
    private final int packageId;
    
    //The package name of the shared library. \0 terminated. max 128
    private final String name;
    
    public LibraryEntry(int packageId, String name) {
        this.packageId = packageId;
        this.name = name;
    }
}