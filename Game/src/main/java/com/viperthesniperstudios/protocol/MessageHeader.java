package com.viperthesniperstudios.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MessageHeader
{
    public static MessageHeader NO_HEADER = new MessageHeader();

    /**
     * uint32
     */
    public final long length;
    public final short protocolVersion;
    public final short version;
    public final TypeID typeID;

    private MessageHeader()
    {
        this.length = 0;
        this.protocolVersion = 0;
        this.version = 0;
        this.typeID = null;
    }

    MessageHeader(InputStream inputStream) throws IOException
    {
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        length = Integer.toUnsignedLong(dataInputStream.readInt());
        protocolVersion = dataInputStream.readShort();
        version = dataInputStream.readShort();
        typeID = TypeID.getType(dataInputStream.readInt());
    }

    public MessageHeader(int version, TypeID typeID)
    {
        this.length = 0;
        this.protocolVersion = 0;
        this.version = (short) version;
        this.typeID = typeID;
    }

    public void writeToStream(OutputStream outputStream) throws IOException
    {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeShort(version);
        dataOutputStream.writeInt(typeID.id);
    }
}
