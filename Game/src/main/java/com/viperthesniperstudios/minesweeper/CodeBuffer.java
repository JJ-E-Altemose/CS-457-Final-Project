package com.viperthesniperstudios.minesweeper;

/**
 * Handles a buffer for raw input so can detect things like arrow keys
 */
public class CodeBuffer
{
    int[] buffer;
    int index = 0;

    public CodeBuffer(int size)
    {
        this.buffer = new int[size];
    }

    public int get(int x)// 0 gets last 1 gets before last etc
    {
        return buffer[(index+3+x) % 3];
    }

    public void push(int value)
    {
        index = (index+1) % buffer.length;
        buffer[index] = value;
    }
}
