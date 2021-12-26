package app.simple.inure.apk.structure.resource;

import app.simple.inure.apk.structure.chunk.ChunkHeader;
import app.simple.inure.apk.structure.chunk.ChunkType;
import app.simple.inure.apk.utils.Unsigned;

public class ResourceTableHeader extends ChunkHeader {
    // The number of ResTable_package structures. uint32
    private int packageCount;
    
    public ResourceTableHeader(int headerSize, int chunkSize) {
        super(ChunkType.TABLE, headerSize, chunkSize);
    }
    
    public long getPackageCount() {
        return Unsigned.toLong(packageCount);
    }
    
    public void setPackageCount(long packageCount) {
        this.packageCount = Unsigned.toUInt(packageCount);
    }
}