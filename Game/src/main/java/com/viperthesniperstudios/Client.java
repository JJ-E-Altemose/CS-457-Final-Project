package com.viperthesniperstudios;

import com.viperthesniperstudios.client.Minesweeper;
import com.viperthesniperstudios.minesweeper.GameDraw;
import com.viperthesniperstudios.minesweeper.GameLoss;
import com.viperthesniperstudios.minesweeper.GameWin;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client
{
    public static void main(String[] args) throws IOException
    {
        Arguments arguments = new Arguments(args);

        InetAddress address;
        try
        {
            address = InetAddress.getByName(arguments.hostname);
        }
        catch (UnknownHostException e)
        {
            System.out.println("Unable to resolve hostname");
            System.exit(1);
            return;
        }

        Minesweeper minesweeper = new Minesweeper(7,7, new Socket(address, arguments.port));

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
            System.out.println("You lost");
        }
        catch (GameDraw e)
        {
            System.out.print("\033[H\033[2J");
            System.out.println("You somehow had a draw, like that's insane");
        }
    }

    static class Arguments
    {
        public int port = -1;
        public String hostname = null;

        public Arguments(String[] args)
        {
            GetOpt getOpt = new GetOpt(args, "h:p:i");
            char flag = '?';

            while ((flag = getOpt.getOption()) != (char) -1)
            {
                switch (flag)
                {
                    case 'h':
                        System.out.println("Use -p and -i to set the port and host respectively, -h for usage message");
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
                        System.out.println("Use -p and -i to set the port and host respectively, -h for usage message");
                        System.exit(1);
                }
            }

            if(port == -1)
            {
                System.out.println("You must specify port");
                System.exit(1);
            }
            if(hostname == null)
            {
                System.out.println("You must specify hostname");
                System.exit(1);
            }
        }
    }
}
