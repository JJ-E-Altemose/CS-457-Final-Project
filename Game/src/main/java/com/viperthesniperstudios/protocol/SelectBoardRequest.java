package com.viperthesniperstudios.protocol;

import java.io.IOException;
import java.io.InputStream;

public class SelectBoardRequest extends Message
{
    short width, height;
    CellByte[][] board1, board2, board3;

    protected SelectBoardRequest(MessageHeader header)
    {
        super(header);
    }

    @Override
    public Message readFromStream(InputStream inputStream) throws IOException
    {
        width = Message.readByteAsUnsignedShort(inputStream);
        height = Message.readByteAsUnsignedShort(inputStream);

        board1 = readBoard(width, height, inputStream);
        board2 = readBoard(width, height, inputStream);
        board3 = readBoard(width, height, inputStream);

        return this;
    }

    private static CellByte[][] readBoard(short width, short height, InputStream inputStream) throws IOException
    {//todo DUPLICATED SELECT_BOARD && GAME_STATE
        CellByte[][] board = new CellByte[width][height];

        for (int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                board[i][j] = new CellByte(inputStream.read());
            }
        }

        return board;
    }

    @Override
    public Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException
    {
        return new SelectBoardRequest(header).readFromStream(inputStream);
    }
}
