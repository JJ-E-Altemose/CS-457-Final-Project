package com.viperthesniperstudios.minesweeper;

public class GameLoss extends RuntimeException
{
    public GameLoss(String message)
    {
        super(message);
    }

    public GameLoss(){}
}
