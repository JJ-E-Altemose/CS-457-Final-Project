package com.viperthesniperstudios.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SelectBoardAck extends Message
{
    enum Code
    {
        SUCCESS,
        INVALID_BOARD_NUMBER,
        NOT_YOUR_TURN
    }

    Code code;

    protected SelectBoardAck(MessageHeader header)
    {
        super(header);
    }

    @Override
    public Message readFromStream(InputStream inputStream) throws IOException
    {
        code = Code.values()[inputStream.read()];

        return this;
    }

    @Override
    public Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException
    {
        return new SelectBoardAck(header).readFromStream(inputStream);
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException
    {
        header.writeToStream(outputStream);

        outputStream.write(code.ordinal());
    }


}
