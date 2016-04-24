/*--------------------------------------------------------

1. Name Tom Odon / Date: 4/16/16

2. Java version used, if not the official version for the class:

Java(TM) SE Runtime Environment (build 1.8.0_60-b27)
Java HotSpot(TM) 64-Bit Server VM (build 25.60-b23, mixed mode)

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java

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
v1.0- Initial Setup based off of InetServer, joke-pseudo, joke-thread files on 
CSC435 course site. Intent to fashion a shutdown command from Admin client was
put off until later. Has not been implemented.

----------------------------------------------------------*/
import java.io.*;
import java.net.*;
import java.util.LinkedList;

/*
 * Worker class. Constructor takes in a Socket argument and assigns to
 * local member 'sock'. Subclass of thread class to allow multithreading.
 */
class Worker extends Thread {
    Socket sock;
    private static LinkedList<ServerContent> jokeList;
    private static LinkedList<ServerContent> proverbList;
    private static Mode workerMode;

    //Content for jokes and proverbs
    private static final String jokeA = 
        "If my wife made whiskey, I'd love her still.";
    private static final String jokeB = 
        "I shot a man with a paintball gun, just to watch him dye.";
    private static final String jokeC = 
        "I make apocalypse jokes like there's no tomorrow.";
    private static final String jokeD = 
        "no matter how kind you are, German children are kinder.";
    private static final String jokeE = 
        "6:30 is the best time on a clock, hands down.";
    
    private static final String proverbA = 
        "you cannot get to the top by sitting on your bottom.";
    private static final String proverbB = 
        "the pen is mightier than the sword.";
    private static final String proverbC = 
        "hope for the best, but prepare for the worst.";
    private static final String proverbD = 
        "necessity is the mother of invention.";
    private static final String proverbE = 
        "don't judge a book by its cover.";

    private static final String maintMessage = 
        "The server is temporarily unavailable—check-back shortly.";
    
    Worker(Socket s) {
        sock = s;

        //Each thread contains its list of jokes. Could be kept at server level too.
        jokeList = new LinkedList<ServerContent>();
        jokeList.add(new ServerContent(jokeA, "JA"));
        jokeList.add(new ServerContent(jokeB, "JB"));
        jokeList.add(new ServerContent(jokeC, "JC"));
        jokeList.add(new ServerContent(jokeD, "JD"));
        jokeList.add(new ServerContent(jokeE, "JE"));

        proverbList = new LinkedList<ServerContent>();
        proverbList.add(new ServerContent(proverbA, "PA"));
        proverbList.add(new ServerContent(proverbB, "PB"));
        proverbList.add(new ServerContent(proverbC, "PC"));
        proverbList.add(new ServerContent(proverbD, "PD"));
        proverbList.add(new ServerContent(proverbE, "PE"));

        workerMode = Mode.getInstance();
    }

    /*
     * Overrides the 'run' method of Thread, this is executed for every thread.
     */
    public void run(){
        PrintStream out = null;
        BufferedReader in = null;
        
                
        try{
            in = new BufferedReader
                (new InputStreamReader(sock.getInputStream()));
            out = new PrintStream(sock.getOutputStream());
            
            
            //ELLIOTT: Note that this branch might not execute whene expected 
            try{

                //receives client's request for state
                String clientStateRequest = in.readLine();

                //readLine terminates at EOL char, sends state
                if(clientStateRequest.equals("")){
                    int mode = workerMode.getMode();
                    out.println(mode);     
                    System.out.println("State transmitted: " + mode +"\n");
                }

                /*
                *   receives client's request for content, either an integer
                *   corresponding to a joke/proverb or nothing, which allows
                *   maintenance message to be stransmitted.    
                */
                String clientContentRequest = in.readLine();
                System.out.println("clientContentRequest received: " + clientContentRequest);
                serveContent(clientContentRequest, out);
                System.out.println("clientContentRequest served");
           
            //Exception thrown if this doesn't work?
            } catch(IOException x) {
                System.out.println("User input error");
                x.printStackTrace();
            }
            sock.close(); //close the connection but not the server

        } catch (IOException ioe) {
            System.out.println(ioe);
            }
    }

    /*
    *   Sends requested content to server based on previously transmitted state.
    *   Checks state, transmits message.
    */
    static void serveContent(String clientContentRequest, PrintStream out){

        int state = workerMode.getMode();
        if(clientContentRequest.equals("")){
            out.println("SERVER " + maintMessage);
            return;
        } else {
            int req = Integer.parseInt(clientContentRequest);  
            if (req < 5) {
                ServerContent content = null;
                content = 
                    (state < 1) ? jokeList.get(req) : proverbList.get(req);
                out.println(content.getContent());
                System.out.println("Content served \n");
                return;
            }
            System.out.println("serveContent error. \n");    
        }
    }
}

/*
*   This server listens for AdminClient connections and spawns a
*   ModeWorker when a connection is formed.
*/
class ModeServer implements Runnable {
public static boolean adminControlSwitch = true;
    
    public void run() {
        //create a server socket listening at port 26601
        int modeQLen = 6;
        int port = 26601;
        Socket modeSock;
    
   
        try{        
            ServerSocket modeServSock = new ServerSocket(port, modeQLen);

            System.out.println
            ("Tom Odon's JokeServer ModeServer starting up, listening at port 26601.\n");

            /* loop while server is still running:
            *  blcok while waiting for a connection to 26601
            *  spawn a new modeWorker thread, pass the connection.
            */
            while(adminControlSwitch){
                modeSock = modeServSock.accept();
                new ModeWorker(modeSock).start();
            }
        } catch (IOException ioe) {System.out.println(ioe);}
    }
}

/*
*   Class that listens to the AdminClient for changes in server mode state,
*   transmits those to the singleton Mode class for all classes to observe.
*/
class ModeWorker extends Thread {
    //process the data coming in over the connection.
    Socket workerSock;
    String state;
    ModeWorker(Socket s) {
        workerSock = s;
    }
    //if the data is a mode change, then change the mode.
    //OPTIONAL: shutdown command to change  main to false
    //close connection
    //terminate thread
    public void run(){
        PrintStream out = null;
        BufferedReader in = null;
        Mode workerMode = Mode.getInstance();
        state = "joke";
        try{
            in = new BufferedReader
            (new InputStreamReader(workerSock.getInputStream()));
            out = new PrintStream(workerSock.getOutputStream());
            try{
                String command;
                command = in.readLine();
                switch(command){
                    case "j": workerMode.setMode(0);
                    break;
                    case "p":  workerMode.setMode(1);
                    break;
                    case "m":  workerMode.setMode(2);
                    break;
                    default: workerMode.setMode(0);
                }
                changeStateMessage(command);
                out.println("Mode has been changed to " + state + ".");
            } catch (IOException x) {
                System.out.println("Server read error");
                x.printStackTrace();
                }
            workerSock.close();
        } catch (IOException ioe){ System.out.println(ioe);}
    }

    public void changeStateMessage(String command) {
         switch(command){
            case "j": state = "joke";
                break;
            case "p":  state = "proverb";
                break;
            case "m":  state = "maintenance";
                break;
            default: state = "joke";
        }
    }
}

/*
*   Singleton class which controls server state.
*/
class Mode {
    private int mode;

    private static Mode uniqueInstance;
    private Mode(){
        this.mode = 0;
    }

    public static Mode getInstance(){
        if(uniqueInstance == null)
            uniqueInstance = new Mode();

        return uniqueInstance;
    }

    public void setMode(int command){
        this.mode = command; 
    }

    public int getMode() {
        int result = this.mode;
        return result;
    }
}

/*
 * Class to represent Jokes and Proverb content offered by server. 
 * Connected via core Java LinkedList, which is passed to a thread when created.
 */
class ServerContent{
	private String content;
	private String id;
	private boolean served;
	
	
	public ServerContent(String item, String identifier){
		this.content = item;
		this.id = identifier;
		this.served = false;
	}
	
	public String getContent(){
		served = true;
		return this.toString();
	}
	
	public boolean hasBeenServed(){
		boolean result = served;
		return result;
	}
	
	@Override
	public String toString(){
		String result = content + " " + "(" + id + ")";
		return result;
	}

}

/*
*   Main server routine. Listens for connections and spawns off worker
*   threads. Also initializes the ModeServer to listen to AdminClient
*/
public class JokeServer{
	   
    public static void main(String[] args) throws IOException {
        int q_len = 6; //Organization for simultaneous connection requests
        int port = 26600;
        Socket sock;
        
        //create a Mode thread, go get MODE instructions, using asynchronous call
        ModeServer modeServer = new ModeServer();
        Thread t = new Thread(modeServer);
        t.start();

        //Create server socket listening @ port 26600
        ServerSocket servsock = new ServerSocket(port, q_len);

        System.out.println
            ("Tom Odon's Joke Server 1.0 starting up, listening at port 26600.\n");

        //Block while waiting for connection
        while(true){
            
            //listen for a connection and accept
            sock = servsock.accept();    
            
            //Creates new Worker class, calls Thread's start() method 
            //to initialize execution. Each Worker has its own joke and proverb List.
            new Worker(sock).start();    
                                         
        }
    }
}
