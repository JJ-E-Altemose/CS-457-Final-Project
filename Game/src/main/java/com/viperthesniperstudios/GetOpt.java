package com.viperthesniperstudios;

public class GetOpt
{
    public String[] args;

    String flags;


    public int optind;
    public String optarg;

    public GetOpt(String[] args, String flags)
    {
        this.flags = flags;
        this.args = args;
        this.optind = 0;
    }

    public char getOption()
    {
        if(optind >= args.length)
        {
            return (char) -1;
        }

        String argument = args[optind];

        if(argument.length() != 2)
        {
            return '?';
        }

        char flag = argument.charAt(1);

        if(argument.charAt(0) != '-' || !flags.contains("" + flag))
        {
            return '?';
        }

        boolean isInputRequired = flags.indexOf(flag) - 1 > 0 &&
                flags.charAt(flags.indexOf(flag) - 1) == ':';

        if(isInputRequired)
        {
            if(optind+1 >= args.length)
            {
                System.out.println("Argument " + flag + " must have a value");
                System.exit(1);
            }

            optarg = args[optind + 1];
            optind += 2;
        }
        else
        {
            optind += 1;
        }

        return flag;
    }
}
