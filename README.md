# DesolationMORPG
A MORPG project to teach myself databases and networking

Set in a post-apocalyptic future, Desolation is a Java-based MORPG semi-rougelike that aims
to take the control away from the NPCs and allow the players to create their own society.

The client will use the Light-weight Java Game Library ([LWJGL](https://www.lwjgl.org/)) to provide the visuals.
The server uses [MongoDB](https://www.mongodb.com/) to maintain the list of users and their characters.

The client and server speak to each other using the UDP protocol with Java's DatagramSocket and DatagramPacket classes,
transferring data as strings acting as instruction codes.

## Current Capabilites
Here are the current capabilities of the software:
* Server
  * View all users and whether or not they are online
  * See the total number of users on line and in the database
  * Add users to and remove them from the database
  * Create new character from a client-provided name
  * Disconnect users and optionally add them and their IP address to a blacklist (in progress)
  * Delete disconnected users (in progress)
  
* Client
  * Connect to the server, then log in or make a new user
  * Maintain a ClientState that is updated according to the current activities
  * Create a new character from a supplied name

## Game Information (not final)
* Game World
  * The world will be a giant island resembling Pangea
  * The island will be split into sectors, each one needing to be unlocked by defeating the boss in the previous sector
  * Once opened sectors will be open forever, but the bosses will still be available for fighting
  * Each sector will have a town occupied sparsely by friendly NPCs and ready for players to leave their mark
  * The goal is to reach the main sector and defeat the final boss there
  
* NPCs
  * Friendly NPCs will be hard to find, but will provide quests that help build the lore of the game
  * Enemy NPCs will spawn in specified regions based on difficulty and type

* Player Characters
  * PCs will each have a stash to store items they can't fit in their inventory
  * PCs have traits and skills. Traits are character stats (health, strength, speed, etc.), while skills are attributes that must be used in order to increase their usefulness (smithing, cooking, swordsmanship, etc.)
  * Skills can help PCs when participating in their chosen Profession, although a profession is never actually selected
  * Because there are no specific classes, every build starts with the same base configuration ready to be molded into unique perfection
 
