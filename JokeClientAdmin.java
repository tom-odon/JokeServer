/*--------------------------------------------------------

1. Name Tom Odon / Date: 4/16/16

2. Java version used, if not the official version for the class:

Java(TM) SE Runtime Environment (build 1.8.0_60-b27)
Java HotSpot(TM) 64-Bit Server VM (build 25.60-b23, mixed mode)

3. Precise command-line compilation examples / instructions:

> javac JokeClientAdmin.java

4. Precise examples / instructions to run this program:

In separate shell windows:
> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

There is not a functioning "quit" command yet.
----------------------------------------------------------*/

import java.io.*;
import java.net.*;

public class JokeClientAdmin {
	public static void main(String[] args) {
		String serverName;
		if(args.length < 1)
			serverName = "localhost";
		else
			serverName = args[0];

		System.out.println("Tom Odon's JokeClientAdmin, v1.0 \n");
		System.out.println("Using server: " + serverName + ", Port: 26601");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try{
			String command;
			do{
				System.out.println("Enter a command, then press enter: ");
				System.out.println
				("COMMANDS: p : proverb mode | j : joke mode \n" +
				" | m : maintenance mode | quit : close server");
				System.out.flush();
				command = in.readLine();
				if (command.indexOf("quit") < 0)
					sendCommandToServer(command, serverName);

			} while (command.indexOf("quit") < 0);
			System.out.println("Cancelled by user request");

		} catch (IOException x ){
			x.printStackTrace(); 
		}
	}

	static void sendCommandToServer (String command, String serverName) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;

		try{
			//Open connection to mode serverport
			sock = new Socket(serverName, 26601);

			// Create filter I/O streams for the socket:
			fromServer =
				new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			
			// Send machine name or IP address to server:
			toServer.println(command); 
			toServer.flush();

			// Read response from the server,
			// and block while synchronously waiting:
			textFromServer = fromServer.readLine();
			if (textFromServer != null) 
				System.out.println(textFromServer);
				
			sock.close();

		} catch (IOException x) {
			System.out.println ("Socket error.");
			x.printStackTrace ();
		}
	}

}