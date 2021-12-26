package app.simple.inure.apk.structure.xml;

import app.simple.inure.apk.structure.chunk.ChunkHeader;

public class XmlResourceMapHeader extends ChunkHeader {
    public XmlResourceMapHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }
}