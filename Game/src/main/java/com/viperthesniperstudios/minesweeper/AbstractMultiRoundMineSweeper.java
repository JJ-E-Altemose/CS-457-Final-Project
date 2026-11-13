package com.viperthesniperstudios.minesweeper;

import java.io.IOException;
import java.util.Random;

public abstract class AbstractMultiRoundMineSweeper
{
    /*public static class GameEnd extends RuntimeException
    {
        enum Type
        {
            WIN,
            LOSE,
            DRAW
        }

        Type type;

        public GameEnd(Type type)
        {
            this.type = type;
        }
    }

    int roundCount;
    int width, height, bombs;
    Random random = new Random();

    int BOARD_CHOICE_COUNT = 3;

    Minesweeper game;

    public AbstractMultiRoundMineSweeper(int width, int height, int bombs, long randomSeed, int roundCount, Minesweeper game)
    {
        this.width = width;
        this.height = height;
        this.bombs = bombs;
        this.random = new Random(randomSeed);
        this.roundCount = roundCount;
        this.game = game;
    }

    public abstract void selectBoard(int board);

    public void play() throws IOException
    {//todo ADD BOARD AT START
        //AbstractMinesweeper game = new AbstractMinesweeper(width, height);
        //game.fillBoard(bombs, random.nextLong());

        //IntStream.range(0, boards.length).forEach(i -> boards[i] = new Minesweeper(width, height, bombs, random.nextLong()));

        for (int round = 0; round < roundCount; round++)
        {
            System.out.println("Round " + round);
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            try
            {
                game.play();

                //Minesweeper[] boards = new Minesweeper[3];
                //IntStream.range(0, boards.length).forEach(i -> boards[i] = new Minesweeper(width, height, bombs, random.nextLong()));

                //throw new SelectBoard(boards);
            }
            catch (SelectBoard e)
            {
                game = chooseBoard(e.boards);
            }
            catch (GameWinNewBoard newBoard)
            {
                game = newBoard.minesweeper;
            }
            catch (GameWin ignored) {}//todo TEMP NOT LEGAL
        }

        throw new GameWin();
    }

    private Minesweeper chooseBoard(Minesweeper[] boards) throws IOException
    {
        for (int i = 0; i < boards.length; i++)
        {
            System.out.printf("\nBoard %d:\n", i);
            boards[i].toRevealedString();
        }

        int selected = -1;

        while (selected == -1)
        {
            try
            {
                selected = Integer.parseInt("" + (char) System.in.read());
            }
            catch (NumberFormatException e) {}
        }

        selectBoard(selected);
        System.out.println(selected);//todo ENTER

        return boards[selected];
    }*/
}
