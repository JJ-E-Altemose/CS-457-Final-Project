you should use stty -icanon for convince and if you want to use the arrow keys

Use arrow keys or wasd to move cursor. \
then use r or f to reveal or flag a cell.
Selecting a board enter a number 

to run the client and server first build with mvn package
(or use the one provided) then run the commands \

Server: \
java -cp CompetitveMinesweeper-1.0-SNAPSHOT.jar com.viperthesniperstudios.Server -p 5992

Client: \
java -cp CompetitveMinesweeper-1.0-SNAPSHOT.jar com.viperthesniperstudios.Client -p 5992 -i 127.0.0.1
