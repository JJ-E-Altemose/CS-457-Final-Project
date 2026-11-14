package com.viperthesniperstudios.minesweeper;

import com.viperthesniperstudios.protocol.CellByte;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Contains the basic minesweeper implementation for logic and uses Exceptions
 * for terminal conditions like {@link  GameWin game wins} and {@link GameLoss game losses}
 */
public class Minesweeper
{
    protected CellByte[][] board;
    protected int width, height;

    public Minesweeper(int width, int height)
    {
        if(width < 4 || height < 4 || width % 2 == 0 || height % 2 == 0)
        {
            throw new IllegalArgumentException("Invalid dimensions");
        }

        this.width = width;
        this.height = height;
        this.board = new CellByte[width][height];
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return height;
    }

    public void fillBoard(int bombs, long seed)
    {
        for(int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                this.board[x][y] = new CellByte();
            }
        }

        placeBombs(bombs, seed);
    }

    private boolean inRange(int low, int high, int value)
    {
        return value >= low && value <= high;
    }

    private void placeBombs(int bombs, long seed)
    {
        Random random = new Random(seed);
        int placedBombs = 0;
        while (placedBombs < bombs)
        {
            int x = random.nextInt(0, width);
            int y = random.nextInt(0, height);

            if(inRange((width/2)-1,(width/2)+1, x) ||
                    inRange((height/2)-1, (height/2)+1, y))
            {
                continue;
            }

            board[x][y].setBomb(true);
            placedBombs++;
        }

        for(int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                board[x][y].setNearByBombs(getNearbyBombCount(x,y));
            }
        }
    }

    private int getNearbyBombCount(int targetX, int targetY)
    {
        int bombCount = 0;

        for (int x = Math.max(targetX-1, 0); x <= Math.min(targetX+1, height-1); x++)
        {
            for (int y = Math.max(targetY-1, 0); y <= Math.min(targetY+1, height-1); y++)
            {
                if(x == targetX && y == targetY)
                {
                    continue;
                }

                bombCount = board[x][y].isBomb() ? bombCount+1 : bombCount;
            }
        }

        return bombCount;
    }

    public CellByte getCell(int x, int y)
    {
        return board[x][y];
    }

    public void flagCell(int x, int y)
    {
        board[x][y].setFlagged(!board[x][y].isFlagged());
    }

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

    private void revealIfZeroBombs(int targetX, int targetY, Set<Point> checked)
    {
        if(checked.contains(new Point(targetX, targetY)))
        {
            return;
        }
        checked.add(new Point(targetX, targetY));

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
