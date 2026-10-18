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