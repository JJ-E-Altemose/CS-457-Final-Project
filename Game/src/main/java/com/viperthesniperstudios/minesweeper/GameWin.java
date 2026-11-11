package com.viperthesniperstudios.minesweeper;

public class GameWin extends RuntimeException
{
  public GameWin(String message)
  {
    super(message);
  }

  public GameWin(){}
}
