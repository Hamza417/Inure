package app.simple.inure.apk.dex;

import com.jaredrummler.apkparser.exception.ParserException;
import com.jaredrummler.apkparser.model.DexClass;
import com.jaredrummler.apkparser.model.DexInfo;
import com.jaredrummler.apkparser.parser.StringPoolEntry;
import com.jaredrummler.apkparser.struct.StringPool;
import com.jaredrummler.apkparser.struct.dex.DexClassStruct;
import com.jaredrummler.apkparser.struct.dex.DexHeader;
import com.jaredrummler.apkparser.utils.Buffers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DexParser {
    
    private static final int NO_INDEX = 0xffffffff;
    
    private final ByteBuffer buffer;
    
    public DexParser(ByteBuffer buffer) {
        this.buffer = buffer.duplicate();
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }
    
    public DexInfo parse() throws ParserException {
        // read magic
        String magic = new String(Buffers.readBytes(buffer, 8));
        if (!magic.startsWith("dex\n")) {
            return null;
        }
        int version = Integer.parseInt(magic.substring(4, 7));
        // now the version is 035
        if (version < 35) {
            // version 009 was used for the M3 releases of the Android platform (November–December 2007),
            // and version 013 was used for the M5 releases of the Android platform (February–March 2008)
            throw new ParserException("Dex file version: " + version + " is not supported");
        }
        
        // read header
        buffer.getInt(); // check sum. skip
        Buffers.readBytes(buffer, DexHeader.K_SHA_1_DIGEST_LEN); // signature skip
        DexHeader.Builder builder = DexHeader.newDexHeader();
        builder.fileSize(Buffers.readUInt(buffer));
        builder.headerSize(Buffers.readUInt(buffer));
        Buffers.readUInt(buffer); // skip?
        builder.linkSize(Buffers.readUInt(buffer));
        builder.linkOff(Buffers.readUInt(buffer));
        builder.mapOff(Buffers.readUInt(buffer));
        builder.stringIdsSize(buffer.getInt());
        builder.stringIdsOff(Buffers.readUInt(buffer));
        builder.typeIdsSize(buffer.getInt());
        builder.typeIdsOff(Buffers.readUInt(buffer));
        builder.protoIdsSize(buffer.getInt());
        builder.protoIdsOff(Buffers.readUInt(buffer));
        builder.fieldIdsSize(buffer.getInt());
        builder.fieldIdsOff(Buffers.readUInt(buffer));
        builder.methodIdsSize(buffer.getInt());
        builder.methodIdsOff(Buffers.readUInt(buffer));
        builder.classDefsSize(buffer.getInt());
        builder.classDefsOff(Buffers.readUInt(buffer));
        builder.dataSize(buffer.getInt());
        builder.dataOff(Buffers.readUInt(buffer));
        builder.version(version);
        DexHeader header = builder.build();
        
        buffer.position((int) header.headerSize);
        
        // read string pool
        long[] stringOffsets = readStringPool(header.stringIdsOff, header.stringIdsSize);
        
        // read types
        int[] typeIds = readTypes(header.typeIdsOff, header.typeIdsSize);
        
        // read classes
        DexClassStruct[] dexClassStructs = readClass(header.classDefsOff, header.classDefsSize);
        
        StringPool stringpool = readStrings(stringOffsets);
        
        String[] types = new String[typeIds.length];
        for (int i = 0; i < typeIds.length; i++) {
            types[i] = stringpool.get(typeIds[i]);
        }
        
        int numClasses = dexClassStructs.length;
        DexClass[] dexClasses = new DexClass[numClasses];
        for (int i = 0; i < numClasses; i++) {
            DexClass.Builder b = DexClass.newDexClass();
            DexClassStruct dexClassStruct = dexClassStructs[i];
            b.classType(types[dexClassStruct.classIdx]);
            if (dexClassStruct.superclassIdx != NO_INDEX) {
                b.superClass(types[dexClassStruct.superclassIdx]);
            }
            b.accessFlags(dexClassStruct.accessFlags);
            dexClasses[i] = b.build();
        }
        return new DexInfo(dexClasses, header);
    }
    
    private DexClassStruct[] readClass(long classDefsOff, int classDefsSize) {
        buffer.position((int) classDefsOff);
        
        DexClassStruct[] dexClassStructs = new DexClassStruct[classDefsSize];
        for (int i = 0; i < classDefsSize; i++) {
            dexClassStructs[i] = DexClassStruct.newDexClassStruct()
                    .classIdx(buffer.getInt())
                    .accessFlags(buffer.getInt())
                    .superclassIdx(buffer.getInt())
                    .interfacesOff(Buffers.readUInt(buffer))
                    .sourceFileIdx(buffer.getInt())
                    .annotationsOff(Buffers.readUInt(buffer))
                    .classDataOff(Buffers.readUInt(buffer))
                    .staticValuesOff(Buffers.readUInt(buffer))
                    .build();
        }
        
        return dexClassStructs;
    }
    
    private int[] readTypes(long typeIdsOff, int typeIdsSize) {
        buffer.position((int) typeIdsOff);
        int[] typeIds = new int[typeIdsSize];
        for (int i = 0; i < typeIdsSize; i++) {
            typeIds[i] = (int) Buffers.readUInt(buffer);
        }
        return typeIds;
    }
    
    private StringPool readStrings(long[] offsets) throws ParserException {
        // read strings.
        // buffer some apk, the strings' offsets may not well ordered. we sort it first
        StringPoolEntry[] entries = new StringPoolEntry[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            entries[i] = new StringPoolEntry(i, offsets[i]);
        }
        String lastStr = null;
        long lastOffset = -1;
        StringPool stringpool = new StringPool(offsets.length);
        for (StringPoolEntry entry : entries) {
            if (entry.offset == lastOffset) {
                stringpool.set(entry.index, lastStr);
                continue;
            }
            buffer.position((int) entry.offset);
            lastOffset = entry.offset;
            String str = readString();
            lastStr = str;
            stringpool.set(entry.index, str);
        }
        return stringpool;
    }
    
    private long[] readStringPool(long stringIdsOff, int stringIdsSize) {
        buffer.position((int) stringIdsOff);
        long[] offsets = new long[stringIdsSize];
        for (int i = 0; i < stringIdsSize; i++) {
            offsets[i] = Buffers.readUInt(buffer);
        }
        
        return offsets;
    }
    
    private String readString() throws ParserException {
        // the length is char len, not byte len
        int strLen = readVarInts();
        return Buffers.readString(buffer, strLen);
    }
    
    private int readVarInts() throws ParserException {
        int i = 0;
        int count = 0;
        short s;
        do {
            if (count > 4) {
                throw new ParserException("read varints error.");
            }
            s = Buffers.readUByte(buffer);
            i |= (s & 0x7f) << (count * 7);
            count++;
        } while ((s & 0x80) != 0);
        
        return i;
    }
}