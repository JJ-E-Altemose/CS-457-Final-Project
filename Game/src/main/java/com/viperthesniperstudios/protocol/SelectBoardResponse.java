package com.viperthesniperstudios.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SelectBoardResponse extends Message
{
    /**
     * uint8
     */
    public int boardIndex;

    protected SelectBoardResponse(MessageHeader header)
    {
        super(header);
    }

    public SelectBoardResponse(int boardIndex)
    {
        super(new MessageHeader(0, TypeID.SELECT_BOARD_RESPONSE));
        this.boardIndex = boardIndex;
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

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException
    {
        header.writeToStream(outputStream);

        outputStream.write(boardIndex);
    }
}
