package com.viperthesniperstudios.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GameState extends Message
{
    /**
     * uint8
     */
    short width, height;

    CellByte[][] yourBoard, otherBoard;

    protected GameState(MessageHeader header)
    {
        super(header);
    }

    @Override
    public Message readFromStream(InputStream inputStream) throws IOException
    {
        width = Message.readByteAsUnsignedShort(inputStream);
        height = Message.readByteAsUnsignedShort(inputStream);

        yourBoard = readBoard(width, height, inputStream);
        otherBoard = readBoard(width, height, inputStream);

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

    private static void writeBoard(CellByte[][] board, OutputStream outputStream) throws IOException
    {//todo DUPLICATED SELECT_BOARD && GAME_STATE

        for (int i = 0; i < board.length; i++)
        {
            for(int j = 0; j < board[i].length; j++)
            {
                outputStream.write(board[i][j].toByte());
            }
        }
    }

    @Override
    public Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException
    {
        return new GameState(header).readFromStream(inputStream);
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException
    {
        header.writeToStream(outputStream);

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        dataOutputStream.writeShort(width);
        dataOutputStream.writeShort(height);

        writeBoard(yourBoard, outputStream);
        writeBoard(otherBoard, outputStream);
    }
}
