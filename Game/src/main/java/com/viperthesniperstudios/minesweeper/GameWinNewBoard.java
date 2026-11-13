package com.viperthesniperstudios.minesweeper;

public class GameWinNewBoard extends RuntimeException
{
    public Minesweeper minesweeper;

    public GameWinNewBoard(Minesweeper minesweeper)
    {
        this.minesweeper = minesweeper;
    }
}
