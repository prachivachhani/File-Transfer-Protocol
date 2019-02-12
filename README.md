This project is a network-based socket programming for File Transfer application running on the Application layer using both TCP as well as UDP Protocols for the underlying Transport Layer Application. It is a client-server application with different connections for control and data transfer. The server code can handle 4 basic ftp commands: get, put, cd, ls.  The client code takes 3 input from the user; the host name or IP address, the port number and a command to be executed on the server.
The interaction flow between the client and server is as follows: 
1. The server starts and waits for a connection to be established by the client  
2. When a command is received, the server will: 
    • Execute the command and send the results back to the client    
3.   Finally, the client will receive the results and display it to the user
Server also handles the exceptions that may occur during the interaction.
