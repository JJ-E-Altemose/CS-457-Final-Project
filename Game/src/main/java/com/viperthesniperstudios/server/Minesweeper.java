package com.viperthesniperstudios.server;

import com.viperthesniperstudios.GameState;
import com.viperthesniperstudios.minesweeper.GameLoss;
import com.viperthesniperstudios.minesweeper.GameWin;
import com.viperthesniperstudios.protocol.CellByte;

import java.util.HashSet;
import java.util.Set;

public class Minesweeper extends com.viperthesniperstudios.minesweeper.Minesweeper
{
    public Minesweeper(int width, int height, int bombs, long seed)
    {
        super(width, height);

        fillBoard(bombs, seed);
    }

    @Override
    public void flagCell(int x, int y)
    {
        board[x][y].setFlagged(!board[x][y].isFlagged());
    }

    //todo public no hide no view
    public CellByte[][] getBoard()
    {
        return this.board;
    }


    @Override
    public void revealCell(int x, int y)
    {
        if(board[x][y].isBomb())
        {
            throw new GameLoss("");//todo DIE
        }

        board[x][y].setHidden(false);

        revealIfZeroBombs(x,y, new HashSet<>());

        if(allNonBombsRevealed())
        {
            throw new GameWin();
        }
    }

    private boolean allNonBombsRevealed()
    {
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if(board[x][y].isHidden() && !board[x][y].isBomb())
                {
                    return false;
                }
            }
        }
        return true;
    }

    public record Point(int x, int y){}

    private void revealIfZeroBombs(int targetX, int targetY, Set<com.viperthesniperstudios.minesweeper.Minesweeper.Point> checked)
    {
        if(checked.contains(new com.viperthesniperstudios.minesweeper.Minesweeper.Point(targetX, targetY)))
        {
            return;
        }
        checked.add(new com.viperthesniperstudios.minesweeper.Minesweeper.Point(targetX, targetY));

        if(targetX < 0 || targetX >= width || targetY < 0 || targetY >= height)
        {
            return;
        }

        CellByte cell = board[targetX][targetY];

        cell.setHidden(false);

        if(cell.getNearByBombs() != 0)
        {
            return;
        }

        for (int x = targetX - 1; x <= targetX+1; x++)
        {
            for (int y = targetY - 1; y <= targetY + 1; y++)
            {
                if(x == targetX && y == targetY)
                {
                    continue;
                }

                revealIfZeroBombs(x,y,checked);
            }
        }
    }

    @Override
    public String toString()
    {
        for (int y = 0; y < height; y++)
        {
            System.out.println();
            for (int x = 0; x < width; x++)
            {
                System.out.print(board[x][y]);
            }
        }
        return super.toString();
    }
}
