package app.simple.inure.apk.structure.resource;

import app.simple.inure.apk.structure.chunk.ChunkHeader;
import app.simple.inure.apk.structure.chunk.ChunkType;

public class NullHeader extends ChunkHeader {
    public NullHeader(int headerSize, int chunkSize) {
        super(ChunkType.NULL, headerSize, chunkSize);
    }
}