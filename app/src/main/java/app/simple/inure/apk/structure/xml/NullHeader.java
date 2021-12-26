package app.simple.inure.apk.structure.xml;

import app.simple.inure.apk.structure.chunk.ChunkHeader;

public class NullHeader extends ChunkHeader {
    public NullHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }
}