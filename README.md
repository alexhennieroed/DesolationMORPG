# DesolationMORPG
A MORPG project to teach myself databases and networking

Set in a post-apocalyptic future, Desolation is a Java-based MORPG semi-rougelike that aims
to take the control away from the NPCs and allow the players to create their own society.

The client will use the Light-weight Java Game Library ([LWJGL](https://www.lwjgl.org/)) to provide the visuals.
The server uses [MongoDB](https://www.mongodb.com/) to maintain the list of users and their characters.

The client and server speak to each other using the UDP protocol with Java's DatagramSocket and DatagramPacket classes,
transferring data as strings acting as instruction codes.

Here are the current capabilities of the software:
* Server
  * View all users and whether or not they are online
  * See the total number of users on line and in the database
  * Add users to and remove them from the database
  * Disconnect users and optionally add them and their IP address to a blacklist (in progress)
  * Create new character from a client-provided name (in progress)
  
* Client
  * Connect to the server, then log in or make a new user
  * Maintain a ClientState that is updated according to the current activities
  * Create a new character from a supplied name (in progress)
