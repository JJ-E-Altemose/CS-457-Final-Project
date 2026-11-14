package com.viperthesniperstudios.server;

import com.viperthesniperstudios.protocol.CellByte;

public class Minesweeper extends com.viperthesniperstudios.minesweeper.Minesweeper
{
    public Minesweeper(int width, int height, int bombs, long seed)
    {
        super(width, height);

        fillBoard(bombs, seed);
    }

    static CellByte[][] createBoardFromSeed(int width, int height, int bombs, long seed)
    {
        return new Minesweeper(width, height, bombs, seed).getBoard();
    }

    public void setBoard(CellByte[][] board)
    {
        this.board = board;
    }

    //todo public no hide no view
    public CellByte[][] getBoard()
    {
        return this.board;
    }
}
