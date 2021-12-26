package app.simple.inure.apk.structure.resource;

import app.simple.inure.apk.structure.chunk.ChunkHeader;
import app.simple.inure.apk.structure.chunk.ChunkType;
import app.simple.inure.apk.utils.Unsigned;

public class LibraryHeader extends ChunkHeader {
    
    /**
     * A package-id to package name mapping for any shared libraries used
     * in this resource table. The package-id's encoded in this resource
     * table may be different than the id's assigned at runtime. We must
     * be able to translate the package-id's based on the package name.
     */
    
    /**
     * uint32 value, The number of shared libraries linked in this resource table.
     */
    private int count;
    
    public LibraryHeader(int headerSize, long chunkSize) {
        super(ChunkType.TABLE_LIBRARY, headerSize, chunkSize);
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(long count) {
        this.count = Unsigned.ensureUInt(count);
    }
}