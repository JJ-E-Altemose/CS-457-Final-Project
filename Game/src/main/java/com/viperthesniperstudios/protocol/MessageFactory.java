package com.viperthesniperstudios.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MessageFactory
{
    public static Message getMessage(InputStream inputStream) throws IOException
    {
        MessageHeader header = new MessageHeader(inputStream);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputStream.readNBytes((int) (header.length-6)));

        return header.typeID.type.newMessageFromStream(header, byteArrayInputStream);
    }
}
