package com.viperthesniperstudios.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RevealCellRequest extends Message
{
    /**
     * uint8
     */
    short x, y;

    protected RevealCellRequest(MessageHeader header)
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
        return new RevealCellRequest(header).readFromStream(inputStream);
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
