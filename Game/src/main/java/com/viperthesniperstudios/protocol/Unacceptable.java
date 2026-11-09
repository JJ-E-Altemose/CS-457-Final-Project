package com.viperthesniperstudios.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Unacceptable extends Message
{
    protected Unacceptable(MessageHeader header)
    {
        super(header);
    }

    @Override
    public Message readFromStream(InputStream inputStream) throws IOException
    {
        return this;
    }

    @Override
    public Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException
    {
        return this;
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException
    {
        header.writeToStream(outputStream);
    }
}
