package com.viperthesniperstudios.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FlagCellResponse extends Message
{
    public enum Code
    {
        SUCCESS,
        INVALID
        ;

        public static Code getCode(int codeNumber)
        {
            switch (codeNumber)
            {
                case 0:
                    return SUCCESS;
                case 1:
                    return INVALID;
                default:
                    throw new IllegalArgumentException("Invalid code number");
            }
        }
    }

    public Code code;

    protected FlagCellResponse(MessageHeader header)
    {
        super(header);
    }

    public FlagCellResponse(Code code)
    {
        super(new MessageHeader(0, TypeID.FLAG_CELL_RESPONSE));
        this.code = code;
    }

    @Override
    public Message readFromStream(InputStream inputStream) throws IOException
    {
        code = Code.getCode(inputStream.read());

        return this;
    }

    @Override
    public Message newMessageFromStream(MessageHeader header, InputStream inputStream) throws IOException
    {
        return new FlagCellResponse(header).readFromStream(inputStream);
    }

    @Override
    public void writeToStream(OutputStream outputStream) throws IOException
    {
        header.writeToStream(outputStream);

        outputStream.write(code.ordinal());
    }
}
