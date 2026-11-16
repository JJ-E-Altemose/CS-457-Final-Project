package com.viperthesniperstudios.server;

import com.viperthesniperstudios.minesweeper.GameLoss;
import com.viperthesniperstudios.minesweeper.GameWin;
import com.viperthesniperstudios.protocol.CellByte;
import com.viperthesniperstudios.protocol.FlagCellRequest;
import com.viperthesniperstudios.protocol.FlagCellResponse;
import com.viperthesniperstudios.protocol.GameEnd;
import com.viperthesniperstudios.protocol.GameState;
import com.viperthesniperstudios.protocol.Message;
import com.viperthesniperstudios.protocol.MessageFactory;
import com.viperthesniperstudios.protocol.RevealCellRequest;
import com.viperthesniperstudios.protocol.RevealCellResponse;
import com.viperthesniperstudios.protocol.RoundEnd;
import com.viperthesniperstudios.protocol.SelectBoardAck;
import com.viperthesniperstudios.protocol.SelectBoardRequest;
import com.viperthesniperstudios.protocol.SelectBoardResponse;
import com.viperthesniperstudios.protocol.TypeID;
import com.viperthesniperstudios.protocol.Unacceptable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientHandler
{
    private static final int MAX_ROUNDS = 3;
    static final int BOARD_WIDTH = 7;
    static final int BOARD_HEIGHT = 7;
    static final int BOARD_BOMBS = 8;

    public long lastFlaggedCorrectlyTime;
    public long lastRevealedTime;
    public long roundStartTime;
    public long totalRoundTime = 0;
    public long lastRoundTime = 0;
    public int flaggedBombCount;
    public int roundsComplete = 0;
    public boolean isAlive = true;

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Minesweeper minesweeper;

    private Random random;

    ClientHandler otherClient;
    private CyclicBarrier barrier;

    public ClientHandler(CyclicBarrier barrier, Random random) throws IOException
    {
        this.barrier = barrier;
        this.random = random;
        this.minesweeper = new Minesweeper(BOARD_WIDTH,BOARD_HEIGHT,BOARD_BOMBS,random.nextLong());
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

    public void handle(Socket socket, ClientHandler otherClient) throws IOException, InterruptedException, BrokenBarrierException
    {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.otherClient = otherClient;

        roundStartTime = System.nanoTime();

        while (true)
        {
            try
            {
                handleMessage(readMessage());
            }
            catch (IOException e)
            {
                isAlive = false;

                try
                {
                    //*koolaid mans through your wall*
                    barrier.await(0, TimeUnit.NANOSECONDS);
                }
                catch (TimeoutException ignored)
                {
                    //tell other thread it may go as it pleases
                }
            }
        }
    }

    private Message readMessage() throws IOException
    {
        return MessageFactory.getMessage(inputStream);
    }

    private void handleMessage(Message message) throws IOException, InterruptedException, BrokenBarrierException
    {
        switch (message.header.typeID)
        {
            case REVEAL_CELL_REQUEST ->
            {
                callHandler(
                        this::handleRevealCellRequest,
                        RevealCellRequest.class,
                        message);
            }
            case SELECT_BOARD_RESPONSE ->
            {
                new SelectBoardAck(SelectBoardAck.Code.NOT_YOUR_TURN)
                        .writeFullMessageToStream(outputStream);
            }
            case FLAG_CELL_REQUEST ->
            {
                callHandler(
                        this::handleFlagCellRequest,
                        FlagCellRequest.class,
                        message);
            }
            case UNACCEPTABLE ->
            {
                //We consider an unacceptable from the client as a time to nope out
                //that we way don't have the client and server going on a nuh uuuh battle
                throw new IllegalStateException("UNACCEPTABLE RECEIVED");
            }
            default ->
            {
                new Unacceptable()
                        .writeFullMessageToStream(outputStream);
            }
        }
    }

    @FunctionalInterface
    public interface Handler <T>
    {
        void handle(T message) throws IOException, InterruptedException, BrokenBarrierException;
    }

    private <T extends Message> void callHandler(Handler<T> handler, Class<T> clazz, Message message) throws IOException, InterruptedException, BrokenBarrierException
    {
        if(clazz.isInstance(message))
        {
            handler.handle(clazz.cast(message));
        }
        else
        {
            throw new IllegalArgumentException("TypeID and class do not match");
        }
    }

    private void handleFlagCellRequest(FlagCellRequest request) throws IOException
    {
        try
        {
            minesweeper.flagCell(request.x, request.y);

            if(minesweeper.getCell(request.x, request.y).isBomb())
            {
                lastFlaggedCorrectlyTime = System.nanoTime();
                flaggedBombCount++;
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            new FlagCellResponse(FlagCellResponse.Code.INVALID)
                    .writeFullMessageToStream(outputStream);
        }
        new FlagCellResponse(FlagCellResponse.Code.SUCCESS)
                .writeFullMessageToStream(outputStream);
    }

    private void handleRevealCellRequest(RevealCellRequest revealCellRequest) throws IOException, InterruptedException, BrokenBarrierException
    {
        try
        {
            lastRevealedTime = System.nanoTime();

            if(!minesweeper.getBoard()[revealCellRequest.x][revealCellRequest.y].isHidden())
            {
                new RevealCellResponse(RevealCellResponse.ReturnCode.ALREADY_REVEALED_ERROR,
                        new GameState(
                                minesweeper.getWidth(),
                                minesweeper.getHeight(),
                                minesweeper.getBoard(),
                                otherClient.minesweeper.getBoard())).writeFullMessageToStream(outputStream);
                return;
            }

            minesweeper.revealCell(revealCellRequest.x, revealCellRequest.y);
        }
        catch (IndexOutOfBoundsException e)
        {
            new RevealCellResponse(RevealCellResponse.ReturnCode.INVALID_CELL_ERROR,
                    null).writeFullMessageToStream(outputStream);
            return;
        }
        catch (GameWin e)
        {
            handleRoundWin();
            return;
        }
        catch (GameLoss e)
        {
            this.isAlive = false;

            new RevealCellResponse(RevealCellResponse.ReturnCode.GAME_END,
                    new GameEnd(GameEnd.Type.LOSS)).writeFullMessageToStream(outputStream);
            try
            {
                //*koolaid mans through your wall*
                barrier.await(0, TimeUnit.NANOSECONDS);
            }
            catch (TimeoutException ignored)
            {
                //tell other thread it may go as it pleases
            }
            return;
        }

        new RevealCellResponse(RevealCellResponse.ReturnCode.GAME_STATE_RETURN,
                    new GameState(
                            minesweeper.getWidth(),
                            minesweeper.getHeight(),
                            minesweeper.getBoard(),
                            otherClient.minesweeper.getBoard()))
                    .writeFullMessageToStream(outputStream);//todo ACTUALLY RETURN OTHER BOARD
    }

    private void handleRoundWin() throws IOException, InterruptedException, BrokenBarrierException
    {
        lastRoundTime = System.nanoTime() - roundStartTime;
        totalRoundTime += lastRoundTime;
        roundsComplete += 1;

        try
        {
            barrier.await();
        }
        catch (BrokenBarrierException e)
        {
            new RevealCellResponse(RevealCellResponse.ReturnCode.GAME_END,
                    new GameEnd(GameEnd.Type.WIN))
                    .writeFullMessageToStream(outputStream);
            return;
        }


        if(roundsComplete < MAX_ROUNDS)
        {
            if(otherClient.lastRoundTime < lastRoundTime)
            {
                handelBoardSelect();
            }
            else if(otherClient.lastRoundTime == lastRoundTime &&
                    System.identityHashCode(this) > System.identityHashCode(otherClient))
            {//Object hashcode gaunted to be different, so who ever has the higher hashcode gets to select
                handelBoardSelect();
            }
            else
            {
                new RevealCellResponse(RevealCellResponse.ReturnCode.ROUND_END,
                        new RoundEnd(RoundEnd.Type.WAITING, null))
                        .writeFullMessageToStream(outputStream);
            }
        }
        else
        {
            decideWinner();
        }

        barrier.await();
    }

    private void decideWinner() throws IOException
    {
        if(otherClient.totalRoundTime < totalRoundTime)
        {
            new RevealCellResponse(RevealCellResponse.ReturnCode.GAME_END,
                    new GameEnd(GameEnd.Type.LOSS))
                    .writeFullMessageToStream(outputStream);
        }
        else if (otherClient.totalRoundTime == totalRoundTime)
        {//TODO EXPAND CASE
            new RevealCellResponse(RevealCellResponse.ReturnCode.GAME_END,
                    new GameEnd(GameEnd.Type.DRAW))
                    .writeFullMessageToStream(outputStream);
        }
        else
        {
            new RevealCellResponse(RevealCellResponse.ReturnCode.GAME_END,
                    new GameEnd(GameEnd.Type.WIN))
                    .writeFullMessageToStream(outputStream);
        }
    }

    private void handelBoardSelect() throws IOException
    {
        minesweeper.setBoard(Minesweeper.createBoardFromSeed(BOARD_WIDTH,BOARD_HEIGHT,BOARD_BOMBS, random.nextLong()));//NEED A NEW BOARD FOR YOUR GAME

        CellByte[][][] boards = new CellByte[][][]
                {
                    Minesweeper.createBoardFromSeed(BOARD_WIDTH,BOARD_HEIGHT,BOARD_BOMBS, random.nextLong()),
                    Minesweeper.createBoardFromSeed(BOARD_WIDTH,BOARD_HEIGHT,BOARD_BOMBS, random.nextLong()),
                    Minesweeper.createBoardFromSeed(BOARD_WIDTH,BOARD_HEIGHT,BOARD_BOMBS, random.nextLong())
                };

        SelectBoardRequest selectBoardRequest =
                new SelectBoardRequest(BOARD_WIDTH,BOARD_HEIGHT, boards);
//tripled nested :0
        new RevealCellResponse(RevealCellResponse.ReturnCode.ROUND_END,
                new RoundEnd(RoundEnd.Type.SELECTING, selectBoardRequest))
                .writeFullMessageToStream(outputStream);

        Message response = MessageFactory.getMessage(inputStream);

        if(response.header.typeID != TypeID.SELECT_BOARD_RESPONSE)
        {
            new Unacceptable().writeFullMessageToStream(outputStream);
        }

        if(response instanceof SelectBoardResponse selectBoardResponse)
        {
            if(selectBoardResponse.boardIndex >= boards.length || selectBoardResponse.boardIndex < 0)
            {
                new SelectBoardAck(SelectBoardAck.Code.INVALID_BOARD_NUMBER)
                        .writeFullMessageToStream(outputStream);
            }

            otherClient.minesweeper.setBoard(boards[selectBoardResponse.boardIndex]);
        }
        else
        {
            throw new RuntimeException("Illegal type");
        }

        new SelectBoardAck(SelectBoardAck.Code.SUCCESS)
                .writeFullMessageToStream(outputStream);
    }
}
