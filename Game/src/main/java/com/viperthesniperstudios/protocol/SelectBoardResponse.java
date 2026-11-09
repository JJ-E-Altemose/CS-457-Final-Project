package com.viperthesniperstudios.protocol;

import java.io.IOException;
import java.io.InputStream;

public class SelectBoardResponse extends Message
{
    /**
     * uint8
     */
    int boardIndex;

    protected SelectBoardResponse(MessageHeader header)
    {
        super(header);
    }

    @Override
    public Message readFromStream(InputStream inputStream) throws IOException
    {
        boardIndex = Message.readByteAsUnsignedShort(inputStream);

        return this;
    }

    @Override
    public Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException
    {
        return new SelectBoardResponse(header).readFromStream(inputStream);
    }
}
