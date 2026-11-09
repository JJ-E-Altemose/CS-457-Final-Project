package com.viperthesniperstudios.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Message
{
    /**
     * uint32
     */
    public final MessageHeader header;

    protected Message(MessageHeader header)
    {
        this.header = header;
    }

    public static short readByteAsUnsignedShort(InputStream inputStream) throws IOException
    {
        int in = inputStream.read();

        if(in == -1)
        {
            throw new IOException("Reached EOF when attempting to read byte");
        }

        return (short) Byte.toUnsignedInt((byte) in);
    }

    public static <T extends Enum<T>> T getEnumFromOrdinal(T[] enumeration, int ordinal)
    {
        if(ordinal < 0 || ordinal > enumeration.length)
        {
            throw new IllegalArgumentException("Invalid ordinal");
        }

        return enumeration[ordinal];
    }

    public abstract Message readFromStream(InputStream inputStream) throws IOException;

    public abstract Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException;

    public abstract void writeToStream(OutputStream outputStream) throws IOException;
}
