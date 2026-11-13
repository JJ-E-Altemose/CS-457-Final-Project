package com.viperthesniperstudios.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GameEnd extends Message
{
    /**
     * uint2
     */
    public Type type;

    protected GameEnd(MessageHeader header)
    {
        super(header);
    }

    public GameEnd(Type type)
    {
        super(new MessageHeader(0, TypeID.GAME_END));
        this.type = type;
    }

    @Override
    public Message readFromStream(InputStream inputStream) throws IOException
    {
        this.type = Message.getEnumFromOrdinal(Type.values(), inputStream.read());

        return this;
    }

    @Override
    public Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException
    {
        return new GameEnd(header).readFromStream(inputStream);
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException
    {
        header.writeToStream(outputStream);

        outputStream.write(type.ordinal());
    }

    public enum Type
    {
        WIN,
        LOSS,
        DRAW
    }
}
