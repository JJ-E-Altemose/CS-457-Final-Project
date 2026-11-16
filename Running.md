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

For the logs of running I made the boards static so you could see they both worked but not be insanely
long due to the output of the code. Also one board will be all hidden just do to one of the clients 
starting later than the other, this is ok behavior its ment to show the state of the other client, 
which at that time is all hidden, you can force a update by revealing a cell already revealed if you
wish to update it.