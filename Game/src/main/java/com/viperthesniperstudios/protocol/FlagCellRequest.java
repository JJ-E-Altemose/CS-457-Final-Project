package com.viperthesniperstudios.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FlagCellRequest extends Message
{
    public short x, y;

    protected FlagCellRequest(MessageHeader header)
    {
        super(header);
    }

    @Override
    public Message readFromStream(InputStream inputStream) throws IOException
    {
        x = Message.readByteAsUnsignedShort(inputStream);
        y = Message.readByteAsUnsignedShort(inputStream);

        return this;
    }

    @Override
    public Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException
    {
        return new FlagCellRequest(header).readFromStream(inputStream);
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException
    {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        header.writeToStream(outputStream);

        dataOutputStream.writeShort(x);
        dataOutputStream.writeShort(y);
    }
}
