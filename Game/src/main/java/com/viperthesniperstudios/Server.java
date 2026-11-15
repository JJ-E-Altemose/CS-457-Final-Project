package com.viperthesniperstudios;

import com.viperthesniperstudios.server.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Server
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        int port = -1;
        String hostname = null;


        GetOpt getOpt = new GetOpt(args, "h:p");
        char flag = '?';

        while ((flag = getOpt.getOption()) != (char) -1)
        {
            switch (flag)
            {
                case 'h':
                    System.out.println("use -p to set the port ie -p 5992, and -h the help message which is this");
                    System.exit(1);
                case 'p':

                    if(port != -1)
                    {
                        System.out.println("port cannot be set more than once");
                        System.exit(1);
                    }

                    port = Integer.parseInt(getOpt.optarg);
                    break;
                case 'i':

                    if(hostname != null)
                    {
                        System.out.println("hostname cannot be set more than once");
                        System.exit(1);
                    }

                    hostname = getOpt.optarg;
                    break;
                case '?':
                    System.out.println("Unknown argument " + args[getOpt.optind]);
                    System.out.println("use -p to set the port ie -p 5992, and -h the help message which is this");
                    System.exit(1);
            }
        }

        if(port == -1)
        {
            System.out.println("You must specify port");
            System.exit(1);
        }

        Semaphore s = new Semaphore(1);

        Semaphore boardSelectLock = new Semaphore(0);

        s.acquire();
        int finalPort = port;
        Thread t = new Thread(() -> {
            try
            {
                ServerSocket serverSocket = new ServerSocket(finalPort);
                s.release();
                Socket client1 = serverSocket.accept();
                Socket client2 = serverSocket.accept();

                ClientHandler client1Handler = new ClientHandler();
                ClientHandler client2Handler = new ClientHandler();

                new Thread(() ->
                {
                    try
                    {
                        client1Handler.handle(client1, client2Handler, boardSelectLock);
                    } catch (IOException | InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                }).start();

                client2Handler.handle(client2, client1Handler, boardSelectLock);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        });
        t.start();
        s.acquire();
        s.release();

/*
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
        }*/
    }
}