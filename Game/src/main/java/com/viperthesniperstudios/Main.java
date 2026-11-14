package com.viperthesniperstudios;

import com.viperthesniperstudios.client.Minesweeper;
import com.viperthesniperstudios.minesweeper.GameLoss;
import com.viperthesniperstudios.minesweeper.GameWin;
import com.viperthesniperstudios.minesweeper.AbstractMultiRoundMineSweeper;
import com.viperthesniperstudios.protocol.CellByte;
import com.viperthesniperstudios.server.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        Semaphore s = new Semaphore(1);

        s.acquire();
        Thread t = new Thread(() -> {
            try
            {
                ServerSocket serverSocket = new ServerSocket(5992);
                s.release();
                Socket client = serverSocket.accept();


                ClientHandler clientHandler = new ClientHandler(client);

                clientHandler.handle();
            } catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        });
        t.start();
        s.acquire();
        s.release();


        Minesweeper minesweeper = new Minesweeper(7,7, new Socket("127.0.0.1", 5992));
        //AbstractMultiRoundMineSweeper minesweeper = new AbstractMultiRoundMineSweeper(7,7,8,42, 2);

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