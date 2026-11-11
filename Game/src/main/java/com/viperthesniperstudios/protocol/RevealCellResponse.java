package com.viperthesniperstudios.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RevealCellResponse extends Message
{
    enum ReturnCode
    {
        GAME_STATE_RETURN,
        INVALID_CELL_ERROR,
        ALREADY_REVEALED_ERROR,
        GAME_END,
        ROUND_END
    }

    ReturnCode returnCode;
    //3 Bytes of padding
    short version;
    int typeID;
    Message response;

    protected RevealCellResponse(MessageHeader header)
    {
        super(header);
    }

    @Override
    public Message readFromStream(InputStream inputStream) throws IOException
    {
        returnCode = Message.getEnumFromOrdinal(ReturnCode.values(), inputStream.read());

        DataInputStream dataInputStream = new DataInputStream(inputStream);

        inputStream.readNBytes(3);

        version = dataInputStream.readShort();
        typeID = dataInputStream.readInt();

        switch (returnCode)
        {//todo FIX no header
            case GAME_STATE_RETURN -> response = new GameState(MessageHeader.NO_HEADER).readFromStream(inputStream);
            case GAME_END -> response = new GameEnd(MessageHeader.NO_HEADER).readFromStream(inputStream);
            case ROUND_END -> response = new RoundEnd(MessageHeader.NO_HEADER).readFromStream(inputStream);
            default -> response = null;
        }

        return this;
    }

    @Override
    public Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException
    {
        return new RevealCellResponse(header).readFromStream(inputStream);
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException
    {
        header.writeToStream(outputStream);

        outputStream.write(returnCode.ordinal());

        //PADDING
        outputStream.write(0);
        outputStream.write(0);
        outputStream.write(0);

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        //TODO CHECK NESTING
        //dataOutputStream.writeShort(version);
        //dataOutputStream.writeInt(typeID);

        if(returnCode != ReturnCode.INVALID_CELL_ERROR &&
           returnCode != ReturnCode.ALREADY_REVEALED_ERROR)
        {
            response.writeToStream(outputStream);
        }
    }
}
