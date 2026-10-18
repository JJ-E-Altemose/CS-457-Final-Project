# Client model
3 loops until GAME_END or ROUND_END
```mermaid
sequenceDiagram
    autonumber
    actor Client1 as Client 1
    participant Server as Game Server
    actor Client2 as Client 2
    
    Client1 ->> Server : Client Connect to server
    Server ->> Client1 : Send game state
    Client1 ->> Server : Flag cell || Reveal cell request
    alt Other Client Still Alive
        alt Invalid move
            Server ->> Client1 : Invalid cell response
        end
        Server ->> Client1 : Valid response (Combined with next 2)
        alt Bomb Cell revealed
            Server ->> Client1: GAME_END
        end
        alt All non bombs revealed
            Server ->> Client1: ROUND_END
        end
    end
    alt Other client Died
        Client2 ->> Server : Reveal cell request & is Bomb
        Server ->> Client2 : GAME_END
        Server ->> Client1 : GAME_END
    end
    Server ->> Client1: GAME_STATE (Continuing)
```
Only for the state of the other client hitting a bomb does the next action depend on the other client, until you get to round ending
# Select Sequence
Other client state does matter here as this is when they have it interact dependent on each other
```mermaid
sequenceDiagram
    autonumber
    actor Client1 as Client 1
    participant Server as Game Server
    actor Client2 as Client 2

    Server ->> Client1 : ROUND_END
    Server ->> Client2 : ROUND_END & SELECT_GAME_BOARD
    Client2 ->> Server : SELECT_GAME_BOARD RESPONSE
    alt Valid
        Server ->> Client2 : GAME_STATE
        Server ->> Client1 : GAME_STATE
    end
    alt Invalid board
        Server ->> Client2 : INVALID BOARD NUMBER
    end
    
```
# All rounds completed
```mermaid
sequenceDiagram
    autonumber
    actor Client1 as Client 1
    participant Server as Game Server
    actor Client2 as Client 2

    Server ->> Client1 : ROUND_END
    Server ->> Client2 : ROUND_END
    Server ->> Client1 : GAME_END
    Server ->> Client2 : GAME_END
```