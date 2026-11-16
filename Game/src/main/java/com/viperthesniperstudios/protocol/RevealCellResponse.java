package com.viperthesniperstudios.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RevealCellResponse extends Message
{
    public enum ReturnCode
    {
        GAME_STATE_RETURN,
        INVALID_CELL_ERROR,
        ALREADY_REVEALED_ERROR,
        GAME_END,
        ROUND_END
    }

    public ReturnCode returnCode = null;
    //3 Bytes of padding
    public short version = 0;
    public int typeID = 0;
    public Message response = null;

    public RevealCellResponse(MessageHeader header)
    {
        super(header);
    }

    public RevealCellResponse(ReturnCode returnCode, Message response)
    {
        super(new MessageHeader(0, TypeID.REVEAL_CELL_RESPONSE));//todo why we passing the header wth is the point?

        this.returnCode = returnCode;

        if(response != null)
        {
            this.typeID = response.header.typeID.id;
            this.response = response;
        }
        else
        {
            this.typeID = 0;
        }
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
        {
            case GAME_STATE_RETURN -> response = new GameState(MessageHeader.NO_HEADER).readFromStream(inputStream);
            case GAME_END -> response = new GameEnd(MessageHeader.NO_HEADER).readFromStream(inputStream);
            case ROUND_END -> response = new RoundEnd(MessageHeader.NO_HEADER).readFromStream(inputStream);
            case ALREADY_REVEALED_ERROR -> response = new GameState(MessageHeader.NO_HEADER).readFromStream(inputStream);
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

        if(returnCode != ReturnCode.INVALID_CELL_ERROR)
        {
            response.writeToStream(outputStream);
        }
        else
        {
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeShort(0);
            dataOutputStream.writeInt(0);
        }
    }
}
