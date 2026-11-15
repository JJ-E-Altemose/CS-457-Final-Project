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
import java.util.Random;
import java.util.concurrent.Semaphore;

public class ClientHandler
{
    private static int MAX_ROUNDS = 3;

    public long lastFlaggedCorrectlyTime;
    public long lastRevealedTime;
    public long roundStartTime;
    public long totalRoundTime = 0;
    public long lastRoundTime = 0;
    //public long roundEndTime;//TODO ADD SPEC
    public int flaggedBombCount;
    public int roundsComplete = 0;
    public boolean isAlive = true;

    public Semaphore boardSelectLock;

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Minesweeper minesweeper;

    private Random random = new Random(95);//todo FIX RANDOM

    ClientHandler otherClient;

    public ClientHandler() throws IOException
    {
        this.minesweeper = new Minesweeper(7,7,8,random.nextLong());//todo fix config
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

    public void handle(Socket socket, ClientHandler otherClient, Semaphore boardSelectLock) throws IOException, InterruptedException
    {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.otherClient = otherClient;

        roundStartTime = System.nanoTime();//todo UPDATE WITH TOTAL ROUND TIME
        this.boardSelectLock = boardSelectLock;

        while (true)
        {
            handleMessage(readMessage());
        }
    }

    private Message readMessage() throws IOException
    {
        return MessageFactory.getMessage(inputStream);
    }

    private synchronized void handleMessage(Message message) throws IOException, InterruptedException
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
        void handle(T message) throws IOException, InterruptedException;
    }

    private <T extends Message> void callHandler(Handler<T> handler, Class<T> clazz, Message message) throws IOException, InterruptedException
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

    private void handleRevealCellRequest(RevealCellRequest revealCellRequest) throws IOException, InterruptedException
    {
        //todo DROP ALREADY REVEAL, I REALLY CANT BE ASSED
        try
        {
            lastRevealedTime = System.nanoTime();
            minesweeper.revealCell(revealCellRequest.x, revealCellRequest.y);
        }
        catch (IndexOutOfBoundsException e)
        {
            new RevealCellResponse(RevealCellResponse.ReturnCode.INVALID_CELL_ERROR,
                    null).writeFullMessageToStream(outputStream);
        }
        catch (GameWin e)
        {
            handleRoundWin();
            return;
        }
        catch (GameLoss e)
        {
            this.isAlive = false;//todo HANDLE ACTUAL END

            new RevealCellResponse(RevealCellResponse.ReturnCode.GAME_END,
                    new GameEnd(GameEnd.Type.LOSS)).writeFullMessageToStream(outputStream);
        }

        new RevealCellResponse(RevealCellResponse.ReturnCode.GAME_STATE_RETURN,
                    new GameState(
                            minesweeper.getWidth(),
                            minesweeper.getHeight(),
                            minesweeper.getBoard(),
                            minesweeper.getBoard()))
                    .writeFullMessageToStream(outputStream);//todo ACTUALLY RETURN OTHER BOARD
    }

    private void handleRoundWin() throws IOException, InterruptedException
    {
        //The round is now over so we are free to release the lock

        lastRoundTime = System.nanoTime() - roundStartTime;
        totalRoundTime += lastRoundTime;
        roundsComplete += 1;

        while (otherClient.isAlive && (otherClient.roundsComplete < roundsComplete))
        {
            //SPIN WAIT FOR THE W
        }

        if(!otherClient.isAlive)
        {
            throw new GameWin();//todo NO THROW FROM HERE, NESTED RETURN
        }

        //while (otherClient.roundsComplete != roundsComplete) {}
        //incase start somehow stagered so long that player completes round before the other even start
        //should never happen

        if(roundsComplete < MAX_ROUNDS)
        {
            //todo BOTH SELECT CASE CAUSE OF TIMING
            if(otherClient.lastRoundTime > lastRoundTime)
            {
                handelBoardSelect();
                boardSelectLock.release();//LETS OTHER CLIENT CONTINUE
            }
            else
            {
                boardSelectLock.acquire();//WAITS FOR CLIENT TO SELECT
                new RevealCellResponse(RevealCellResponse.ReturnCode.ROUND_END,
                        new RoundEnd(RoundEnd.Type.WAITING, null))
                        .writeFullMessageToStream(outputStream);
            }//todo check



            //todo: wait till other done

            //todo: IF OTHER FASTER THAN YOU SELECT
            //todo: ELSE WAIT
            //new RevealCellResponse(RevealCellResponse.ReturnCode.ROUND_END,
            //        new RoundEnd()).writeFullMessageToStream(outputStream);
        }
        else
        {
            decideWinner();
        }

        //roundLock.acquire();



        //new RevealCellResponse(RevealCellResponse.ReturnCode.GAME_END,
        //        new GameEnd(GameEnd.Type.WIN)).writeFullMessageToStream(outputStream);
        //handelBoardSelect();
        return;

        //todo HANDLE ACTUAL END
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
        {
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

    public synchronized CellByte[][] getBoard()
    {
        return this.minesweeper.getBoard();
    }

    private void handelBoardSelect() throws IOException
    {
        minesweeper.setBoard(Minesweeper.createBoardFromSeed(7,7,8, random.nextLong()));//NEED A NEW BOARD FOR YOUR GAME

        CellByte[][][] boards = new CellByte[][][]
                {
                    Minesweeper.createBoardFromSeed(7,7,8, random.nextLong()),
                    Minesweeper.createBoardFromSeed(7,7,8, random.nextLong()),
                    Minesweeper.createBoardFromSeed(7,7,8, random.nextLong())
                };

        SelectBoardRequest selectBoardRequest =
                new SelectBoardRequest(7,7, boards);
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
        }//todo CODE NOT YOUR TURN

        new SelectBoardAck(SelectBoardAck.Code.SUCCESS)
                .writeFullMessageToStream(outputStream);
    }
}
