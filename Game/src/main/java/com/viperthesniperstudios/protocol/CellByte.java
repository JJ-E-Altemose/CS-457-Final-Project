package com.viperthesniperstudios.protocol;

public class CellByte
{
    int nearByBombs = 0;
    boolean flagged = false, bomb = false, hidden = true;

    public CellByte(int byteB)
    {
        //todo IMPLEMENT
    }

    public CellByte()
    {

    }

    public byte toByte()
    {
        return 0; //todo;
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
        if(isHidden())
            return "?";
        if(isBomb() && !isHidden())
            return "X";
        return "" + getNearByBombs();
    }

    public String revealedCharacter()
    {
        return isBomb() && !isHidden() ? "X" : "" + getNearByBombs();
    }
}
