When reading a message, the first int is taken as the length, then buffer is created of that size, and the appropriate object
is created from the TYPEID. (ObjectStreams would have been faster and nicer and eaiser but wasn't 100% sure ok so... let the chaos commence)

as an example getting
00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 00 00 00 FF FF FF FF
Means the first message is of length 17 and its type is 0 so its a GAME_END response, 
and the second is length 16, and its type if FF FF FF FF so it is a UNACCCEPTABLE response

All messages are prefixed with a length, a protocol version

Protocol V0
All messages get a Version then an TYPEID
# Protocol V0 Header
| Length 0b -> 4b | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b |
|-----------------|----------------------------|--------------------|-------------------|

# GAME_END (0) Response V0 (Server --> Client)

|       | Length 0b -> 4b | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b | Type 16b -> 17b |
|-------|-----------------|---------------------------|--------------------|------------------|-----------------|
| Value | 17              | 0                         | 0                  | 0                | 0->2            |
| HEX   | 00 00 00 11     | 00 00                     | 00 00              | 00 00 00 00      | 00->02          |

> Type
> - 0 WIN
> - 1 LOSS
> - 2 DRAW

Instead of the client saying im going now, we tell the client its allowed to go now, and if the client disconnects at any other time than after this, they lose, bad internet sucks to be yous.
And because I will be using java it will all be exceptions thrown from accessing stream I can catch and easily just send a GAME_END to the other client

# ROUND_END (1) Response (Server --> Client)
Tells the client the round has now ended, and weather or not to expect the select board response

|       | Length 0b -> 4b | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b | Type 16b -> 17b | Padding 17b -> 20b | Version 20b -> 22b | TYPEID 22b -> 26b | *SELECT BOARD RESPONSE* |
|-------|-----------------|---------------------------|--------------------|-------------------|-----------------|-------------------|--------------------|-------------------|-------------------------|
| Value | 26 -> 195103    | 0                         | 0                  | 1                 | 0->1            | 0                 | 0                  | ?                 | *Nested Response*       |
| HEX   | 00 02 FA 1D     | 00 00                     | 00 00              | 00 00 00 01       | 00->01          | 00 00 00          | 00 00              | 00 00 00 0?       | *Nested Response*       |

> Type
> - 0 WAITING
> - 1 SELECTING

# GAME_STATE (2) Response (Server --> Client)
Tells the client the current state of their board, and their opponents board

|       | Length 0b -> 4b            | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b | Width 16b -> 17b | Height 17b -> 18b | Your Board 19b -> 19b+(Width*Height) | Other board 19b+(Width*Height) -> 19b+(Width*Height*2) |
|-------|----------------------------|---------------------------|--------------------|-------------------|------------------|-------------------|--------------------------------------|--------------------------------------------------------|
| Value | 18 -> 130068               | 0                         | 0                  | 2                 | 0->255           | 0->255            | Contains Cell Bytes                  | Contains Cell Bytes                                    |
| HEX   | 00 00 00 12 -> 00 01 FC 14 | 00 00                     | 00 00              | 00 00 00 02       | 00->FF           | 00->FF            | ?                                    | ? |

## Cell byte
| Num neighbor bombs 0->4 | Hidden 4->5 | Bomb 5->6 | Reserved 6->7 | Flagged 7->8 |
|------------------------|-------------|-----------|---------------|--------------|

Example 
0010 1101
has 2 bomb neighbors, is hidden, is a bomb, and is flagged


Your board will not show actual bomb locations, but will for opponent

# REVEAL_CELL (3) Request (Client --> Server)
Tells the server that the client wants to reveal a cell, so it can send them the next state, or tell them they have failed :(

|       | Length 0b -> 4b | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b | X 16b -> 17b | Y 17b -> 18b |
|-------|-----------------|---------------------------|--------------------|-------------------|--------------|--------------|
| Value | 18              | 0                         | 0                  | 3                 | 0->255       | 0 -> 255     |
| HEX   | 00 00 00 12     | 00 00                     | 00 00              | 00 00 00 03       | 00->FF       | 0 -> FF      |

# REVEAL_CELL (4) Response (Server --> Client)
Tells the client the result of their request

|       | Length 0b -> 4b             | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b | Return Code 16b -> 17b | Padding 17b -> 20b | Version 20b -> 22b | TYPEID 22b -> 26b                  | *Nested according to return code* |
|-------|-----------------------------|---------------------------|--------------------|-------------------|------------------------|--------------------|--------------------|------------------------------------|-----------------------------------|
| Value | 17->195111                  | 0                         | 0                  | 4                 | 0->4                   | 0                  | 0                  | 0, 1 or 2 According to return code | According to return code          |
| HEX   | 00 00 00 17 --> 00 02 FA 27 | 00 00                     | 00 00              | 00 00 00 04       | 0->4                   | 00 00 00 | 00 00              | 00 00 00 00 -> 00 00 00 02         | ?                                 |

## Return code
> - 0 GAME_STATE return
> - 1 Invalid cell error
> - 2 Already revealed error
> - 3 GAME_END
> - 4 ROUND_END

# SELECT_BOARD (7) Response (Client --> Server)
Tells the server what board the client selected

|       | Length 0b -> 4b | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b | Board 16b -> 17b |
|-------|-----------------|---------------------------|--------------------|-------------------|------------------|
| Value | 17              | 0                         | 0                  | 7                 | 0->2             |
| HEX   | 00 00 00 11     | 00 00                     | 00 00              | 00 00 00 07       | 00->02           |

## Board
> The number of the board to select between the 3 starting at zero

# SELECT_BOARD (8) Request (Server --> Client)
Tells the client the boards they can select from (Yucki Inverted request)

|       | Length 0b -> 4b            | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b | Width 16b -> 17b | Height 17b -> 18b | Boards options 0, 1, and 2 18b -> 18b+(width*height*3) |
|-------|----------------------------|---------------------------|--------------------|-------------------|------------------|-------------------|--------------------------------------------------------|
| Value | 18 -> 195093               | 0                         | 0                  | 8                 | 0->255           | 0->255            | Cell Bytes                                             |
| HEX   | 00 00 00 12 -> 00 02 FA 15 | 00 00                     | 00 00              | 00 00 00 08       | 00->FF           | 00->FF            | ?                                                      |

Uses the same cell bytes as before, but all the cells contain there true state

# SELECT_BOARD_ACK (9) Request (Server --> Client)
Tells the client their selection was accepted

|       | Length 0b -> 4b | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b | Code 16b->17b |
|-------|-----------------|---------------------------|--------------------|-------------------|---------------|
| Value | 17              | 0                         | 0                  | 9                 | 0->1          |
| HEX   | 00 00 00 11     | 00 00                     | 00 00              | 00 00 00 09      | 00->01        |

## Code
> - 0 Success
> - 1 Invalid board number
> - 2 Not your turn 

# FLAG_CELL (10) Request (Client --> Server)
Tells the server that the client wants to flag a cell, so the server can handle draw conditions 

|       | Length 0b -> 4b | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b | X 16b -> 17b | Y 17b -> 18b |
|-------|-----------------|---------------------------|--------------------|-------------------|--------------|--------------|
| Value | 18              | 0                         | 0                  | 10                | 0->255       | 0 -> 255     |
| HEX   | 00 00 00 12     | 00 00                     | 00 00              | 00 00 00 0A       | 00->FF       | 0 -> FF      |

# FLAG_CELL (11) Response (Server --> Client)
Tells client if it was a success or not

|       | Length 0b -> 4b | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b | Code 16b->17b |
|-------|-----------------|---------------------------|--------------------|-------------------|---------------|
| Value | 17              | 0                         | 0                  | 11                | 0->1          |
| HEX   | 00 00 00 11     | 00 00                     | 00 00              | 00 00 00 0B       | 00->01        |

## Code
> - 0 Success
> - 1 Invalid

# UNACCEPTABLE 0xFFFFFFFF  (Server --> Client & Client --> Server)
Tells the other end that what was sent is not acceptable

|       | Length 0b -> 4b | Protocol Version 8b -> 10b | Version 10b -> 12b | TYPEID 12b -> 16b |
|-------|-----------------|---------------------------|--------------------|-------------------|
| Value | 16              | 0                         | 0                  | -1                |
| HEX   | 00 00 00 10     | 00 00                     | 00 00              | FF FF FF FF       |

_________________
The gap in numbers is due to me removing the even more complicated part, and I have been working on this for... you dont wanna know how long