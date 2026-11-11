package com.viperthesniperstudios.minesweeper;

public class GameWinNewBoard extends RuntimeException
{
    AbstractMinesweeper minesweeper;

    public GameWinNewBoard(AbstractMinesweeper minesweeper)
    {
        this.minesweeper = minesweeper;
    }
}
