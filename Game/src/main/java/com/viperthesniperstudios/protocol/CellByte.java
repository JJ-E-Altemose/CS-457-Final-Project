package com.viperthesniperstudios.protocol;

public class CellByte
{
    int nearByBombs = 0;
    boolean flagged = false, bomb = false, hidden = true;

    public CellByte(int byteB)
    {
        nearByBombs = (byteB & 0b1111_0000) >> 4;
        flagged = (byteB & 0b0000_0001) != 0;
        bomb = (byteB & 0b0000_0100) != 0;
        hidden = (byteB & 0b0000_1000) != 0;

        //todo IMPLEMENT
    }

    public CellByte() {}

    public CellByte(int nearByBombs, boolean flagged, boolean bomb, boolean hidden)
    {
        this.nearByBombs = nearByBombs;
        this.flagged = flagged;
        this.bomb = bomb;
        this.hidden = hidden;
    }

    public byte toByte()
    {
        byte value = (byte) (((byte) nearByBombs) << 4);
        value |= flagged ? 0b0000_0001 : 0b0000_0000;
        value |= bomb ? 0b0000_0100 : 0b0000_0000;
        value |= hidden ? 0b0000_1000 : 0b0000_0000;

        return value; //todo CHECK;
    }

    public void setFlagged(boolean value)
    {
        this.flagged = value;
    }

    public boolean isFlagged()
    {
        return this.flagged;
    }

    public void setBomb(boolean value)
    {
        this.bomb = value;
    }

    public boolean isBomb()
    {
        return this.bomb;
    }

    public void setHidden(boolean value)
    {
        this.hidden = value;
    }

    public boolean isHidden()
    {
        return this.hidden;
    }

    public int getNearByBombs()
    {
        return nearByBombs;
    }

    public void setNearByBombs(int nearByBombs)
    {
        this.nearByBombs = nearByBombs;
    }

    @Override
    public String toString()
    {
        if(isFlagged() && isHidden())
            return "F";
        if(isHidden())
            return "■";
        if(isBomb() && !isHidden())
            return "\uD83D\uDCA3";
        return "" + getNearByBombs();
    }

    public String revealedCharacter()
    {
        return isFlagged() ? "F" : (isBomb() && !isHidden() ? "X" : "" + getNearByBombs());
    }
}
