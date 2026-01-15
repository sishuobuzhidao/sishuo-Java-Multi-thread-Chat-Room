# sishuo-Java-Multi-thread-Chat-Room
A practice project described in the videos when I was self-learning java by watching online java videos

The project was Console-Based. The chatting function was achieved using sockets.
One can open Server.java first, and then open multiple Client.java and enter the Chat Room.\

For clients, log in using a username and a password. The user will then be connected to the Server via a Socket, and can chat.
All messages sent can be seen on the Server side, including private /message ones. Clients can see public messages and targeted private messages.

There are special commands for clients.

(from help.txt)
Here are all the commands you can use:
/exit: exit the chat room
/help: show this list
/list: list all the online users
/message: whisper to somebody
