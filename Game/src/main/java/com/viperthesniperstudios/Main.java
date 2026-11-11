package com.viperthesniperstudios;

import com.viperthesniperstudios.minesweeper.GameLoss;
import com.viperthesniperstudios.minesweeper.GameWin;
import com.viperthesniperstudios.minesweeper.AbstractMultiRoundMineSweeper;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        //Minesweeper minesweeper = new Minesweeper(7,7,8,42);
        AbstractMultiRoundMineSweeper minesweeper = new AbstractMultiRoundMineSweeper(7,7,8,42, 2);

        try
        {
            minesweeper.play();
        }
        catch (GameWin e)
        {
            System.out.print("\033[H\033[2J");
            System.out.println("Your a winner :0");
        }
        catch (GameLoss e)
        {
            System.out.print("\033[H\033[2J");
            System.out.println("You stinky");
        }
    }
}