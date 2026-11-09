package com.viperthesniperstudios.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RoundEnd extends Message
{
    public RoundEnd(MessageHeader header)
    {
        super(header);
    }

    /**
     * uint1
     */
    enum Type
    {
        WAITING,
        SELECTING
    }

    Type type;

    /**
     * 3 Bytes of padding
     */

    short version;
    short typeID;
    Message response;

    @Override
    public Message readFromStream(InputStream inputStream) throws IOException
    {
        type = Message.getEnumFromOrdinal(Type.values(), inputStream.read());

        DataInputStream dataInputStream = new DataInputStream(inputStream);

        inputStream.readNBytes(3);

        version = dataInputStream.readShort();
        typeID = dataInputStream.readShort();

        response = type == Type.WAITING ?
                new SelectBoardResponse(MessageHeader.NO_HEADER).readFromStream(inputStream)
                : null;

        return this;
    }

    @Override
    public Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException
    {
        return new RoundEnd(header).readFromStream(inputStream);
    }
}
