package com.viperthesniperstudios.protocol;

import java.util.Arrays;

public enum TypeID
{
    GAME_END(0, new GameEnd(MessageHeader.NO_HEADER)),
    ROUND_END(1, new RoundEnd(MessageHeader.NO_HEADER)),
    GAME_STATE(2, new GameState(MessageHeader.NO_HEADER)),
    REVEAL_CELL_REQUEST(3, new RevealCellRequest(MessageHeader.NO_HEADER)),
    REVEAL_CELL_RESPONSE(4, new RevealCellResponse(MessageHeader.NO_HEADER)),
    SELECT_BOARD_RESPONSE(7, new SelectBoardResponse(MessageHeader.NO_HEADER)),
    SELECT_BOARD_REQUEST(8, new SelectBoardRequest(MessageHeader.NO_HEADER)),
    SELECT_BOARD_ACK(9, new SelectBoardAck(MessageHeader.NO_HEADER)),
    FLAG_CELL_REQUEST(10, new FlagCellRequest(MessageHeader.NO_HEADER)),
    FLAG_CELL_RESPONSE(11, new FlagCellResponse(MessageHeader.NO_HEADER)),
    UNACCEPTABLE(0xFFFFFFFF, new Unacceptable(MessageHeader.NO_HEADER))
    ;

    TypeID(int id, Message type)
    {
        this.id = id;
        this.type = type;
    }

    int id;
    Message type;

    public int getID()
    {
        return id;
    }

    public <T extends Message> Class<T> getMessageClass()
    {
        return (Class<T>) type.getClass();
    }

    public static TypeID getType(int id)
    {
        return Arrays.stream(TypeID.values())
                .filter(e -> e.id == id)
                .findFirst()
                .orElse(UNACCEPTABLE);
    }
}
