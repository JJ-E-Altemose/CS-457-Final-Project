package com.viperthesniperstudios.client;

import com.viperthesniperstudios.minesweeper.GameDraw;
import com.viperthesniperstudios.minesweeper.GameLoss;
import com.viperthesniperstudios.minesweeper.GameWin;
import com.viperthesniperstudios.minesweeper.IMinesweeper;
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
import com.viperthesniperstudios.protocol.TypeID;
import com.viperthesniperstudios.protocol.Unacceptable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

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

            FlagCellResponse flagCellResponse = getMessageOfType(TypeID.FLAG_CELL_RESPONSE);

            if(flagCellResponse.code == FlagCellResponse.Code.INVALID)
            {
                throw new IndexOutOfBoundsException();
            }

            board[x][y].setFlagged(true);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private <T extends Message> T getMessageOfType(TypeID typeID) throws IOException
    {
        Message message = MessageFactory.getMessage(inputStream);

        if(message.header.typeID != typeID)
        {
            new Unacceptable().writeFullMessageToStream(outputStream);
        }

        Class<T> clazz = Arrays.stream(TypeID.values())
                .filter(e -> e == typeID)
                .findFirst().get()
                .getMessageClass();

        return clazz.cast(message);
    }

    class WaitingOnOtherPlayer extends Thread
    {
        AtomicBoolean isCanceled;

        public WaitingOnOtherPlayer(AtomicBoolean isCanceled)
        {
            this.isCanceled = isCanceled;
        }

        @Override
        public void run()
        {
            try
            {
                Thread.sleep(250);
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            if(!isCanceled.get())
            {
                System.out.println("Waiting on other client");
            }
        }
    }

    @Override
    public void revealCell(int x, int y)
    {
        try
        {
            AtomicBoolean isCanceled = new AtomicBoolean(false);

            new WaitingOnOtherPlayer(isCanceled).start();

            new RevealCellRequest((short) x, (short) y).writeFullMessageToStream(outputStream);

            Message response = MessageFactory.getMessage(inputStream);

            isCanceled.set(true);

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
                    board = gameState.yourBoard;
                    otherBoard = gameState.otherBoard;
                }
            }
            case INVALID_CELL_ERROR ->
            {
                throw new RuntimeException("INVALID CELL SOMEHOW");
                //If this happens something has gone seriously wrong
            }
            case ALREADY_REVEALED_ERROR ->
            {
                if(revealCellResponse.response instanceof GameState gameState)
                {
                    board = gameState.yourBoard;
                    otherBoard = gameState.otherBoard;
                }
                return; //Cool the users a goof ball and revealed something twice, I dont care
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
                            throw new GameDraw();
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
                else
                {
                    System.out.println("\n\n\n\n\n\n");
                    //purge a few lines make it easier if ye scroll on accident
                    System.out.print("\033[H\033[2J");
                    System.out.println("Waiting for other player to pick a board for you");
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


        while(true)
        {
            new SelectBoardResponse(selected).writeFullMessageToStream(outputStream);

            Message message = MessageFactory.getMessage(inputStream);

            if(message instanceof SelectBoardAck ack)
            {
                if(ack.code == SelectBoardAck.Code.SUCCESS)
                {
                    break;
                }
            }
            else
            {
                throw new RuntimeException("INVALID MESSAGE TYPE");
            }
        }
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

            System.out.println("\n\n\n\n\n\n");
            //purge a few lines make it easier if ye scroll on accident
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
