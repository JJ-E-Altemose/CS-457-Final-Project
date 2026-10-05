All transitions labeled with * are executed sequentially but shown in a separate not to be more excplict 
### Without splitting these diagrams up, it would be completly unreadable.

# Server side
```mermaid
stateDiagram-v2
    state "Game Ended" as GE
    GE --> WFC

    state "Waiting for clients" as WFC
    [*] --> WFC : Server Started
    
    state "Client 1 Connected Still Waiting" as C1C
    WFC --> C1C : Socket connected
    C1C --> WFC : Socket disconnected

    state "GAME STARTED \nSend GAME_STATE To Clients" as SGSTC
    state "Wait for FLAG || REVEAL requests" as WF
    
    SGSTC --> WF : *
    
    C1C --> SGSTC : Another Socket Connected
    
```
## Client Disconnect & Reconnect
```mermaid
stateDiagram-v2
    state ClientDisconnect {
        
        state "Game Started" as GS
        state "GAME END\n Send game end to still connected client as a win" as KS
        [*] --> GS : Game started
        GS --> KS : A socket disconnected
    }
```

## Round state machine
Draw & Win/Loss both end the game and send back to the lobby creation (Show in diagram)
```mermaid
stateDiagram-v2
    state "Wait for client to send a REVEAL_CELL_REQUEST || FLAG_CELL_REQUEST" as ST
    state "Game End Loss" as GEL
    state "Game End Draw" as GED

    [*] --> ST : Game lobby started
    
    ST --> WFCTSAR : Received REVEAL_CELL_REQUEST
    ST --> FCR : Received FLAG_CELL_REQUEST
    
    state "Handling REVEAL_CELL_REQUEST" as WFCTSAR
    state "Handling REVEAL_CELL_REQUEST" as WFCTSARG
    state "Handling FLAG_CELL_REQUEST" as FCR2
    state "Handling FLAG_CELL_REQUEST" as FCR
    
    state "SAVE FLAG ON GAME BOARD" as SFONGB
    
    state "SAVE FLAG TIME" as SFT
    state "Send INVALID CELL (1)" as AAAAAAAAAAAAAAAAAAAAAAAAAAHHHH
    
    state FCR2 {
        FCR --> SFONGB : Valid cell
        FCR --> AAAAAAAAAAAAAAAAAAAAAAAAAAHHHH : Invalid cell
        AAAAAAAAAAAAAAAAAAAAAAAAAAHHHH --> ST : *
SFONGB --> SFT : Flagged cell is a bomb
SFONGB --> ST : Flagged cell was not a bomb

SFT --> ST : *
 }
    
    

    state "Round End" as RE
    state "All Rounds Completed" as ARC
    state "Round Fail" as RF
    state "Save round time from request, and clear last flag time" as SS
    
    state "Send ROUND_END with SELECT_BOARD request" as IDKMAN
    state "Wait for that client to send SELECT_BOARD response" as IDKMAN2
    state "More rounds to do" as IDKMAN3
    state "Send ROUND_END waiting to player" as IDKMAN4
    

    state WFCTSARG {
RE --> IDKMAN3 : More rounds to do
IDKMAN3 --> IDKMAN4 : Is the faster player or was not randomly selected
IDKMAN4 --> ST : *

IDKMAN3 --> IDKMAN : Is the slower player or random with a tie

IDKMAN --> IDKMAN2 : *
IDKMAN2 --> ST : Valid SELECT_BOARD received SEND GAME STATE
        IDKMAN2 --> IDKMAN2 : Invalid SELECT_BOARD_ACK recieved
        
WFCTSAR --> ST : No ending reached

WFCTSAR --> RF : Player revealed a bomb and died

RF --> GEL : Player flagged less bombs than other player
RF --> GEL : Players revealed same amount of bombs, but other player flagged bomb correctly last


WFCTSAR --> SS : Player completed round

RE --> ARC : All rounds completed

ARC --> GEL : Other player completed rounds faster
ARC --> GED : Players have same time

SS --> ST : 1 Player has not completed there round

SS --> RE : Other player completed round without dieing
SS --> GED : Both players died and flagged the same amount of bombs, at the same time
 }
```

# The following is the FSM for each request

```mermaid
stateDiagram-v2
    state RevealCellRequest {
        state "Handling Request" as HR
        
        [*] --> HR : Recieved a reveal cell request
        
        state "INVALID CELL (1) reply" as E1
        state "ALREADY REVEALED (2) reply" as E2

        state "SEND GAME_END (3) Response" as SGER
        state "SEND ROUND_END (4) Response" as SRER
        state "SEND GAME_STATE (0) Response" as SGSR

        T --> SGER : Revealed a bomb
        T --> SRER : All non-bombs revealed
        
        C --> SGSR : *
        
        state "TERMINAL" as T
        state "CONTINUING" as C
        
        HR --> E1 : Cell out of bounds
        HR --> E2 : Cell was already revealed

        HR --> T : All non bombs revealed
        HR --> T : Revealed a bomb
        
        HR --> C : Did not hit a bomb & haven't revealed all bombs
        
        state "Discard & continue waiting" as CW
        
        E1 --> CW : *
        E2 --> CW : *
}
```

```mermaid
stateDiagram-v2 
    state SelectBoardRequest {
        state "Handling SelectBoardRequest" as HR
        
        [*] --> HR : Received SelectBoardRequest
        HR --> E1 : Board number is not valid
        E1 --> CWFR : *
        
        HR --> CTNR : Board number is valid
        
        state "Continue to next round" as CTNR
        
        state "INVALID BOARD NUMBER (1) Reply" as E1
        state "NOT YOUR TURN (2) Reply" as E2
        state "Continue waiting for Request" as CWFR
        
        HR --> E2 : Client is not the one who should do this right now
        E2 --> CWFR : *
    }
```
# Client  side
```mermaid
stateDiagram-v2 
    state Client {
        [*] --> RBDTU : Started
        
        state "Read game state and wait for input" as RGC
        state "Read boards and display to user waiting for input" as RBDTU
        
        state "Send SELECT_BOARD response" as SSBR
        
        RBDTU --> SSBR : User selected valid option
        SSBR --> RBDTU : Server said option invalid
        
        
        SSBR --> RGC : Server replies with GAME_STATE

state "Send REVEAL_CELL request" as SRCR
state "Send FLAG_CELL request" as SFCR

RGC --> SRCR : User input reveal cell
RGC --> SFCR : User input flag cell
SFCR --> RGC : Response had error code

SRCR --> RGC : Reply had error code 

SRCR --> GO : Server said game ended
SRCR --> RBDTU : Server said choose a game board

state "GAME OVER display type of state (win loss draw)" as GO

 }
```
If an invalid message type is given or incorrect the server or client responds with UNACCEPTABLE
