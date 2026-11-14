package com.viperthesniperstudios.client;

import com.viperthesniperstudios.minesweeper.CodeBuffer;
import com.viperthesniperstudios.minesweeper.GameLoss;
import com.viperthesniperstudios.minesweeper.GameWin;
import com.viperthesniperstudios.minesweeper.IMinesweeper;
import com.viperthesniperstudios.minesweeper.Key;
import com.viperthesniperstudios.minesweeper.SelectBoard;
import com.viperthesniperstudios.protocol.CellByte;
import com.viperthesniperstudios.protocol.FlagCellRequest;
import com.viperthesniperstudios.protocol.FlagCellResponse;
import com.viperthesniperstudios.protocol.GameEnd;
import com.viperthesniperstudios.protocol.GameState;
import com.viperthesniperstudios.protocol.Message;
import com.viperthesniperstudios.protocol.MessageFactory;
import com.viperthesniperstudios.protocol.RevealCellRequest;
import com.viperthesniperstudios.protocol.RevealCellResponse;
import com.viperthesniperstudios.protocol.SelectBoardAck;
import com.viperthesniperstudios.protocol.SelectBoardRequest;
import com.viperthesniperstudios.protocol.SelectBoardResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import static com.viperthesniperstudios.protocol.RoundEnd.Type.SELECTING;

public class Minesweeper implements IMinesweeper
{
    protected int width, height;
    protected CellByte[][] board;
    protected CellByte[][] otherBoard;

    protected Socket socket;
    protected InputStream inputStream;
    protected OutputStream outputStream;

    public static final int ROUND_COUNT = 3;


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
                    otherBoard = gameState.otherBoard;
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

    public void play() throws IOException
    {
        for (int round = 0; round < ROUND_COUNT; round++)
        {
            try
            {
                playRound();
            }
            catch (RoundEnd roundEnd)
            {
                if(roundEnd.roundEndMessage.type == SELECTING)
                {
                    if(roundEnd.roundEndMessage.response instanceof SelectBoardRequest selectBoard)
                    {
                        chooseBoard(new CellByte[][][]{selectBoard.board1, selectBoard.board2, selectBoard.board3});
                    }
                    else
                    {
                        throw new RuntimeException("Illegal message type");
                    }
                }
            }
        }
    }

    private void chooseBoard(CellByte[][][] boards) throws IOException
    {
        for (int i = 0; i < boards.length; i++)
        {
            System.out.printf("\nBoard %d:\n", i);
            System.out.println(boardToRevealedString(boards[i]));
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


        new SelectBoardResponse(selected).writeFullMessageToStream(outputStream);

        Message message = MessageFactory.getMessage(inputStream);

        if(message instanceof SelectBoardAck ack)
        {
            if(ack.code != SelectBoardAck.Code.SUCCESS)
            {
                throw new RuntimeException("ERROR WITH BOARD");//todo RETRY
            }
        }
        else
        {
            throw new RuntimeException("INVALID MESSAGE TYPE");
        }
        //selectBoard(selected);
        //System.out.println(selected);//todo ENTER

        //return boards[selected];
    }

    public String boardToRevealedString(CellByte[][] board)
    {
        StringBuilder stringBuilder = new StringBuilder(width*height+height+1);

        for (int y = 0; y < height; y++)
        {
            stringBuilder.append("\n");
            System.out.println();
            for (int x = 0; x < width; x++)
            {
                stringBuilder.append(board[x][y].revealedCharacter());
            }
        }
        return stringBuilder.toString();
    }

    private void playRound() throws IOException
    {
        CodeBuffer codeBuffer = new CodeBuffer(3);

        int cursorX = width/2, cursorY = height/2;

        revealCell(cursorX, cursorY);

        System.out.print("\033[H\033[2J");
        mineSweeperTerminalPrint(cursorX, cursorY);

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

            System.out.print("\033[H\033[2J");

            mineSweeperTerminalPrint(cursorX, cursorY);
        }
    }

    private void mineSweeperTerminalPrint(int cursorX, int cursorY)
    {
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                if(x == cursorX && y == cursorY)
                {
                    System.out.print("v ");
                }
                else
                {
                    System.out.print("  ");
                }
            }
            System.out.println("|"); // adds border

            for (int x = 0; x < width; x++)
            {
                System.out.print(board[x][y] + " ");
            }
            System.out.print("|");

            for (int x = 0; x < width; x++)
            {
                System.out.print(otherBoard[x][y] + " ");
            }

            System.out.println();
        }
    }
}
