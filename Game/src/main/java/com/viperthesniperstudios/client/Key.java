package com.viperthesniperstudios.client;

import com.viperthesniperstudios.Main;

import java.io.IOException;
import java.io.InputStream;

public class Key
{
    public final char character;
    public final boolean isSpecial;
    public final Type type;

    public enum Type
    {
        LEFT_ARROW,
        RIGHT_ARROW,
        UP_ARROW,
        DOWN_ARROW,
        ENTER
    }

    public Key(char character)
    {
        this.character = character;
        this.isSpecial = false;
        this.type = null;
    }

    public Key(Type type)
    {
        this.character = '\0';
        this.isSpecial = true;
        this.type = type;
    }

    @Override
    public String toString()
    {
        if(isSpecial)
        {
            return type.toString();
        }

        return "" + character;
    }

    public static Key getKey(CodeBuffer codeBuffer, InputStream in) throws IOException
    {
        codeBuffer.push(in.read());

        if(codeBuffer.get(0) == 10)
        {
            return new Key(Key.Type.ENTER);
        }

        if(codeBuffer.get(1) == 27 && codeBuffer.get(2) == 91)
        {
            switch (codeBuffer.get(0))
            {
                case 65:
                    return new Key(Key.Type.UP_ARROW);
                case 66:
                    return new Key(Key.Type.DOWN_ARROW);
                case 67:
                    return new Key(Key.Type.RIGHT_ARROW);
                case 68:
                    return new Key(Key.Type.LEFT_ARROW);
            }
        }

        return new Key((char) codeBuffer.get(0));
    }
}
