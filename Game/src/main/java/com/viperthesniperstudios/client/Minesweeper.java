package com.viperthesniperstudios.client;

import com.viperthesniperstudios.minesweeper.CodeBuffer;
import com.viperthesniperstudios.minesweeper.GameLoss;
import com.viperthesniperstudios.minesweeper.GameWin;
import com.viperthesniperstudios.minesweeper.IMinesweeper;
import com.viperthesniperstudios.minesweeper.Key;
import com.viperthesniperstudios.protocol.CellByte;
import com.viperthesniperstudios.protocol.FlagCellRequest;
import com.viperthesniperstudios.protocol.FlagCellResponse;
import com.viperthesniperstudios.protocol.GameEnd;
import com.viperthesniperstudios.protocol.GameState;
import com.viperthesniperstudios.protocol.Message;
import com.viperthesniperstudios.protocol.MessageFactory;
import com.viperthesniperstudios.protocol.RevealCellRequest;
import com.viperthesniperstudios.protocol.RevealCellResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class Minesweeper implements IMinesweeper
{
    protected int width, height;
    protected CellByte[][] board;

    protected Socket socket;
    protected InputStream inputStream;
    protected OutputStream outputStream;


    public Minesweeper(int width, int height, Socket socket) throws IOException
    {
        if(width < 4 || height < 4 || width % 2 == 0 || height % 2 == 0)
        {
            throw new IllegalArgumentException("Invalid dimensions");
        }

        this.width = width;
        this.height = height;
        this.board = new CellByte[width][height];

        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    @Override
    public void flagCell(int x, int y)
    {
        try
        {
            new FlagCellRequest((short) x, (short) y).writeFullMessageToStream(outputStream);

            Message response = MessageFactory.getMessage(inputStream);

            if(response instanceof FlagCellResponse flagCellResponse)
            {
                if(flagCellResponse.code == FlagCellResponse.Code.INVALID)
                {
                    throw new IndexOutOfBoundsException();//todo UGLY NESTING
                }

                board[x][y].setFlagged(true);
                return;
            }
            throw new IOException("Illegal message type");
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);//todo FIX TEMP
            //throw new Disconnect();
        }
//todo IMPLEMENT
    }

    @Override
    public void revealCell(int x, int y)
    {
        try
        {
            new RevealCellRequest((short) x, (short) y).writeFullMessageToStream(outputStream);

            Message response = MessageFactory.getMessage(inputStream);

            if(response instanceof RevealCellResponse revealCellResponse)
            {
                handleRevealCellResponse(revealCellResponse);

                return;
            }

            throw new IOException("Illegal message type");
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
//todo IMPLEMENT
    }

    private void handleRevealCellResponse(RevealCellResponse revealCellResponse)
    {
        switch (revealCellResponse.returnCode)
        {
            case GAME_STATE_RETURN ->
            {//todo missing branch, size mismatch cast fail
                if(revealCellResponse.response instanceof GameState gameState)
                {
                    board = gameState.yourBoard;//todo HANDLE OTHER BOARD;
                }
            }
            case INVALID_CELL_ERROR ->
            {
                throw new RuntimeException("INVALID CELL SOMEHOW");
            }
            case ALREADY_REVEALED_ERROR ->
            {
                //I dont wanna
            }
            case GAME_END ->
            {
                if(revealCellResponse.response instanceof GameEnd gameEnd)
                {
                    switch (gameEnd.type)
                    {
                        case WIN ->
                        {
                            throw new GameWin();
                        }
                        case LOSS ->
                        {
                            throw new GameLoss();
                        }
                        case DRAW ->
                        {
                            throw new RuntimeException("NOT YET IMPL");//TODO IMPL
                        }
                    }
                }
            }
            case ROUND_END ->
            {
                if(revealCellResponse.response instanceof com.viperthesniperstudios.protocol.RoundEnd roundEndMessage)
                {
                    throw new RoundEnd(roundEndMessage);
                }
            }
        }
    }

    public String toRevealedString()
    {
        for (int y = 0; y < height; y++)
        {
            System.out.println();
            for (int x = 0; x < width; x++)
            {
                System.out.print(board[x][y].revealedCharacter());
            }
        }
        return super.toString();
    }

    public void play() throws IOException
    {
        CodeBuffer codeBuffer = new CodeBuffer(3);

        int cursorX = width/2, cursorY = height/2;

        revealCell(cursorX, cursorY);

        Key lastKey = new Key('\0');
        System.out.print("\033[H\033[2J");
        mineSweeperTerminalPrint(cursorX, cursorY);
        String afterClearPrint = "";

        while (true)
        {
            Key key = Key.getKey(codeBuffer, System.in);

            if(key.isSpecial)
            {
                switch (key.type)
                {
                    case DOWN_ARROW -> cursorY = (cursorY+1) % height;
                    case UP_ARROW -> cursorY = (cursorY+ height-1) % height;
                    case LEFT_ARROW -> cursorX = (cursorX+ width-1) % width;
                    case RIGHT_ARROW -> cursorX = (cursorX+1) % width;
                }

                /*if(key.type == Key.Type.ENTER)
                {
                    if(lastKey.character == 'r')
                    {
                        revealCell(cursorX, cursorY);
                        afterClearPrint += "\nReveling cell\n";
                    }

                    if(lastKey.character == 'f')
                    {
                        flagCell(cursorX, cursorY);
                        afterClearPrint += "\nFlagging cell\n";
                    }
                }*/

                /*if(key.type == Key.Type.ENTER)
                {
                    revealCell(cursorX, cursorY);
                }*///TODO UN REMOVE
            }
            else if(key.character == '/')
            {
                flagCell(cursorX, cursorY);
            }
            else {
                switch (key.character)
                {
                    case 's' -> cursorY = (cursorY+1) % height;
                    case 'w' -> cursorY = (cursorY+ height-1) % height;
                    case 'a' -> cursorX = (cursorX+ width-1) % width;
                    case 'd' -> cursorX = (cursorX+1) % width;
                }

                if(key.character == 'r')
                {
                    revealCell(cursorX, cursorY);
                }
                if(key.character == 'f')
                {
                    flagCell(cursorX, cursorY);
                }
            }
            /*
            else
            {
                if(key.character == 'r')
                {
                    afterClearPrint += "\nPress Enter to reveal cell\n";
                }

                if(key.character == 'f')
                {
                    afterClearPrint += "\nPress Enter to flag cell\n";
                }
            }*/
            System.out.print("\033[H\033[2J");

            mineSweeperTerminalPrint(cursorX, cursorY);
            System.out.println(afterClearPrint);
            afterClearPrint = "";

            lastKey = key;
        }
    }

    private void mineSweeperTerminalPrint(int cursorX, int cursorY)
    {
        for (int y = 0; y < height; y++)
        {
            System.out.println();

            for (int x = 0; x < width; x++)
            {
                if(x == cursorX && y == cursorY)
                {
                    System.out.print("v ");
                }
                System.out.print("  ");
            }
            System.out.println();

            for (int x = 0; x < width; x++)
            {
                System.out.print(board[x][y] + " ");
            }
        }
    }
}
