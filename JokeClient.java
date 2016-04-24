/*--------------------------------------------------------

1. Name Tom Odon / Date: 4/16/16

2. Java version used, if not the official version for the class:

Java(TM) SE Runtime Environment (build 1.8.0_60-b27)
Java HotSpot(TM) 64-Bit Server VM (build 25.60-b23, mixed mode)

3. Precise command-line compilation examples / instructions:

> javac JokeClient.java

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

Based off of CSC435 InetClient code, modified and commented by Odon

----------------------------------------------------------*/

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/*
 * Main routine has a do-while loop that takes input of "enter" and sends to
 * JokeServer. Requests content from server. 
 */
public class JokeClient {

    //data for client to keep track of. boolean arrays marked as false to 
    //represent unserved content. Updated as content is served. state to keep track
    //of server mod.
    private static String serverName;
    private static String userName;
    private static boolean[] jokesServed;
    private static boolean[] proverbsServed;
    private static int serverState;
    private static int jokeMarker;
    private static int proverbMarker;

    public static void main(String[] args) throws IOException{
        //grab server's IP address for connection 
        String serverName;
        if (args.length < 1)
            serverName = "localhost";  //default
        else serverName = args[0];

        System.out.println("CLIENT Tom Odon's JokeClient, 1.0\n");
        System.out.println("CLIENT Using JokeServer: " + serverName + ", Port: 26600\n");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        //arrays are initialized and set to false, state is defaulted to joke mode.
        jokesServed = new boolean[5];
        proverbsServed = new boolean[5];
        jokeMarker = 0;
        proverbMarker = 0;
        serverState = 0;

        //get client name to add on to content
        System.out.println("Welcome! Please type your name, then press ENTER: \n");
        userName = in.readLine();

        //this loop will continuously run until "quit" command
        try{
            String command;
            do{
                //prompt user for input / Enter command
                System.out.print
                    ("Hey " + userName + ", I've got something to tell you." 
                      +  " Press ENTER to read it. Type \"quit\" to exit. \n");
                System.out.flush();
                command = in.readLine();

                //check for "quit"  in command. If not, get state and content.
                if(command.indexOf("quit") < 0)
                    getServerContent(command);
            } while (command.indexOf("quit") < 0);
            System.out.println("Cancelled by user request.\n");

        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    //static String toText from InetClient not necessary
    
    static void printContent(String content){
        System.out.println(userName + ", " + content);
    }

    /*
    * Checks which joke is available for random selection. If all content
    * has been served, return 0 and reset all content to unserved. Otherwise,
    * randomly choose an index number from the unserved content array.
    */
    static int generateJokeRequest() {
        List<Integer> possibleInts = new ArrayList<Integer>();
        
        //check for which content is available for random selection
        for(int i = 0; i < jokesServed.length; i++){
            if (!jokesServed[i])
                possibleInts.add(i);
        }
        
        //if all jokes/proverbs are served, reset all to false & call again
        if(possibleInts.isEmpty()) {
            Arrays.fill(jokesServed, false);
            return generateJokeRequest();
        }
        
        //generate a random number from available joke indices
        Random rand = new Random();
        int value = rand.nextInt(possibleInts.size());
        int result = possibleInts.get(value);
        jokesServed[result] = true;
        return result;
    }

    /*
    * Checks which proverb is available for random selection. If all content
    * has been served, return 0 and reset all content to unserved. Otherwise,
    * randomly choose an index number from the unserved content array.
    */
    static int generateProverbRequest() {
        List<Integer> possibleInts = new ArrayList<Integer>();
        
        //check for which content is available for random selection
        for(int i = 0; i < proverbsServed.length; i++){
            if (!proverbsServed[i])
                possibleInts.add(i);
        }
        
        //if all jokes/proverbs are served, reset all to false & call again
        if(possibleInts.isEmpty()) {
            Arrays.fill(proverbsServed, false);
            return generateProverbRequest();
        }
        
        //generate a random number from available joke indices
        Random rand = new Random();
        int value = rand.nextInt(possibleInts.size());
        int result = possibleInts.get(value);
        proverbsServed[result] = true;
        return result;
    }    

    //Iterative getRequest method for testing
    static int generateIterativeJokeRequest() {
        int result;

        //if we've sent all jokes, start over at beginning. Otherwise, 
        //request the next unsent joke.
        if(jokeMarker > jokesServed.length - 1){
            jokeMarker = 0;
            Arrays.fill(jokesServed, false);
        }
        result = jokeMarker;
        jokesServed[result] = true;
        jokeMarker += 1;
        return result;
    }

    //Iterative getRequest method for testing
    static int generateIterativeProverbRequest() {
        int result;

        //if we've sent all proverbs, start over at beginning. Otherwise, 
        //request the next unsent proverb.
        if(proverbMarker > proverbsServed.length){
            proverbMarker = 0;
            Arrays.fill(proverbsServed, false);
        }
        result = proverbMarker;
        proverbsServed[result] = true;
        proverbMarker += 1;
        return result;
    }

    /*
    *   Send an empty string to get state, this will help client determine
    *   what kind of content it should request, if any.
    */
    static int getServerState(PrintStream toServer, BufferedReader fromServer)
        throws IOException {
        toServer.println("");
        int serverState = Integer.parseInt(fromServer.readLine());
        return serverState;
    }

    /*
    *   Opens a connection to server, creates IO streams, transmits a command to the 
    *   server followed by an integer with the content to request. 
    */
    static void getServerContent(String command) {
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        try{
            /*Open a connection to server port, using port # 26600 */
            sock = new Socket(serverName, 26600);

            //Create I/O streams to and from socket
            fromServer =
                new BufferedReader(new InputStreamReader(sock.getInputStream()));
            toServer = new PrintStream(sock.getOutputStream());

            //check state, request maintenance message if mode is maintenance
            int serverState = getServerState(toServer, fromServer);
            if (serverState == 2){
                toServer.println("");
            }

            //If mode is not maintenance, transmit command to server
            if(serverState != 2) {
                toServer.println((serverState < 1) ?
                generateJokeRequest() : generateProverbRequest());
            }

            toServer.flush();

            //Read response from server, up to 3 lines. Block while synchronously
            //waiting.
            // **removed for-loop from InetServer as jokes/proverbs should be
            // ony line only **
            textFromServer = fromServer.readLine();

            if (textFromServer != null){
                printContent(textFromServer + "\n");
            }
            
            sock.close();
        } catch (IOException x) {
            System.out.println("Socket error.");
            x.printStackTrace();
        }
    }
}





