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
    public short width, height;

    public CellByte[][] yourBoard, otherBoard;

    protected GameState(MessageHeader header)
    {
        super(header);
    }

    public GameState(int width, int height, CellByte[][] yourBoard, CellByte[][] otherBoard)
    {
        super(new MessageHeader(0, TypeID.GAME_STATE));
        this.width = (short) width;
        this.height = (short) height;

        this.yourBoard = yourBoard;
        this.otherBoard = otherBoard;
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

        dataOutputStream.write(width);
        dataOutputStream.write(height);

        writeBoard(yourBoard, outputStream);
        writeBoard(otherBoard, outputStream);
    }
}
