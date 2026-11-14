package com.viperthesniperstudios.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SelectBoardRequest extends Message
{
    short width, height;
    public CellByte[][] board1, board2, board3;

    protected SelectBoardRequest(MessageHeader header)
    {
        super(header);
    }

    public SelectBoardRequest(int width, int height, CellByte[][][] boards)
    {
        super(new MessageHeader(0, TypeID.SELECT_BOARD_REQUEST));

        if(boards.length != 3)
        {
            throw new RuntimeException("3 Options required");
        }

        this.width = (short) width;
        this.height = (short) height;

        this.board1 = boards[0];
        this.board2 = boards[1];
        this.board3 = boards[2];
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
        return new SelectBoardRequest(header).readFromStream(inputStream);
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException
    {
        header.writeToStream(outputStream);

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        dataOutputStream.write(width);
        dataOutputStream.write(height);

        writeBoard(board1, outputStream);
        writeBoard(board2, outputStream);
        writeBoard(board3, outputStream);
    }
}
