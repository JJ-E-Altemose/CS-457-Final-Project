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
        Arguments arguments = new Arguments(args);

        Semaphore boardSelectLock = new Semaphore(0);
        //TODO CHange spec for boardSelectLock

        ServerSocket serverSocket = new ServerSocket(arguments.port);
        Socket client1 = serverSocket.accept();
        Socket client2 = serverSocket.accept();


        ClientHandler client1Handler = new ClientHandler();
        ClientHandler client2Handler = new ClientHandler();

        new ClientHandlerThread(client1, client1Handler, client2Handler, boardSelectLock)
                .start();
        new ClientHandlerThread(client2, client2Handler, client1Handler, boardSelectLock)
                .start();
    }

    static class ClientHandlerThread extends Thread
    {
        final Socket client;
        final ClientHandler clientHandler;
        final ClientHandler otherClientHandler;
        final Semaphore boardSelectLock;

        ClientHandlerThread(Socket client, ClientHandler clientHandler, ClientHandler otherClientHandler, Semaphore boardSelectLock)
        {
            this.client = client;
            this.clientHandler = clientHandler;
            this.otherClientHandler = otherClientHandler;
            this.boardSelectLock = boardSelectLock;
        }

        @Override
        public void run()
        {
            try
            {
                clientHandler.handle(client, otherClientHandler, boardSelectLock);
            } catch (IOException | InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    static class Arguments
    {
        int port = -1;

        public Arguments(String[] args)
        {
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
        }
    }
}