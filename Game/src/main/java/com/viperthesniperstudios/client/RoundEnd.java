package com.viperthesniperstudios.client;

public class RoundEnd extends RuntimeException
{
    public com.viperthesniperstudios.protocol.RoundEnd roundEndMessage;

    public RoundEnd(com.viperthesniperstudios.protocol.RoundEnd roundEndMessage)
    {
        this.roundEndMessage = roundEndMessage;
    }
}
