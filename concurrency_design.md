There will be 3 threads
One for accepting clients and creating the gamestate thread/object combos ie accept then create client thread


the other 2 are for the clients and interact with each other through thread objects


# Game State
## fields
> - Board data
> - Last flagged correctly time
> - Flagged bomb #
> - Rounds complete
> - isAlive

Has getter methods that are public, and since this object is also the thread
it can mutate its own state freely through private methods that are syncronized, 
but the other thread can only access, \
Each thread accesses the other object when a request comes in and handles the state
based of that and the accesses to game state

Because of java object synchronization, this is very simple and not complex

# Changes
Since one client never writes to a another except for board select, nothing needs
to be synchronized, the only part that does is the the board select and round end
which can easily be achieved with spin locks, but I wanted to use something else so 
I used a cyclic barrier instead, which waits for both threads to reach a point and
they wait till both have ended a round, and they wait till both have finished processing
ending a round. 

If the client disconnects or looses the barrier is broken, and the one potentially waiting at 
the lock wins