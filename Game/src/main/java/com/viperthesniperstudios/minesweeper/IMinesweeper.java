package com.viperthesniperstudios.minesweeper;

public interface IMinesweeper
{
    public void flagCell(int x, int y);

    /**
     *
     * @param x
     * @param y
     * @throws com.viperthesniperstudios.minesweeper.GameLoss if lost
     * @throws com.viperthesniperstudios.minesweeper.GameWin if won
     */
    public void revealCell(int x, int y);
}
