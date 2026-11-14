package com.viperthesniperstudios.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RoundEnd extends Message
{
    public RoundEnd(MessageHeader header)
    {
        super(header);
    }

    public RoundEnd(Type type, Message response)
    {
        super(new MessageHeader(0, TypeID.ROUND_END));

        this.type = type;
        this.response = response;
    }

    /**
     * uint1
     */
    public enum Type
    {
        WAITING,
        SELECTING
    }

    public Type type;

    /**
     * 3 Bytes of padding
     */

    public short version;
    public int typeID;
    public Message response;

    @Override
    public Message readFromStream(InputStream inputStream) throws IOException
    {
        type = Message.getEnumFromOrdinal(Type.values(), inputStream.read());

        DataInputStream dataInputStream = new DataInputStream(inputStream);

        inputStream.readNBytes(3);

        version = dataInputStream.readShort();
        typeID = dataInputStream.readInt();

        response = type == Type.SELECTING ?
                new SelectBoardRequest(MessageHeader.NO_HEADER).readFromStream(inputStream)
                : null;

        return this;
    }

    @Override
    public Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException
    {
        return new RoundEnd(header).readFromStream(inputStream);
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException
    {
        header.writeToStream(outputStream);

        outputStream.write(type.ordinal());

        outputStream.write(0);
        outputStream.write(0);
        outputStream.write(0);

        response.writeToStream(outputStream);
    }
}
