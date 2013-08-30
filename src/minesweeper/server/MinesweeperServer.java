package minesweeper.server;

import java.net.*; 
import java.io.*;

/**
 * The server is thread safe because every time it is accessed by a thread, the thread needs to 
 * acquire a lock on the board object, which is itself thread-safe (see Board class)since it only
 * has atomic methods.
 *
 */
public class MinesweeperServer {
    
    private int numberOfConnectedClients;
    private final ServerSocket serverSocket;
    /** True if the server should _not_ disconnect a client after a BOOM message. */
    private final boolean debug;
    private final Board board;

    public static final String CLOSE_MSG = "Terminating connection";
    public static final String BOOM_MSG = "BOOM!";
    /**
     * Make a MinesweeperServer that listens for connections on port.
     * @param port port number, requires 0 <= port <= 65535.
     */
    public MinesweeperServer(int port, boolean debug, Board b) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.debug = debug;
        this.numberOfConnectedClients=0;
        this.board=b;
        }
    /**
     * Displays the Board by calling the private methodBoard.toString()
     * @return String representing the board
     */
    private String displayBoard() {
        synchronized(board) {
            return board.toString();
        }
    }
    /**
     * Displays the output of a DIG command
     * @param x : Integer representing the x coordinate of the cell
     * @param y : Integer representing the y coordinate of the cell
     * @return a string representing the BOOM message or the board status
     */
    private String outputDigCell(int x, int y) {
        synchronized(board) {
            String output = board.digCell(x, y);
            if(output.equals(BOOM_MSG)) 
            	return BOOM_MSG;
            else 
            	return displayBoard();
            
        }
    }
    /**
     * Run the server, listening for client connections and handling them.  
     * Never returns unless an exception is thrown.
     * @throws IOException if the main server socket is broken
     * (IOExceptions from individual clients do *not* terminate serve()).
     */
    public void serve() throws IOException {
        while (true) {
            // block until a client connects
             addExtraThread(serverSocket.accept()).start();
        }
    }
    private Thread addExtraThread(final Socket s) {
    	synchronized(this)
    	{
    		numberOfConnectedClients++;
    	}
    	return new Thread(new Runnable()  {
    	
    	public void run() {
        
    		try 
    	{
            handleConnection(s);
        } 
    		catch (IOException e) 
    	{
            e.printStackTrace(); 
        } 
    		finally 
    	{
            try 
        { 
            	synchronized(this) 
            	{
            	numberOfConnectedClients--;
                }
	            s.close();
        } 
            catch (IOException e) 
            {
	           
	            e.printStackTrace();
            }
        }
    }});
    	
    	
    }

    /**
     * Handle a single client connection.  Returns when client disconnects.
     * @param socket socket where the client is connected
     * @throws IOException if connection has an error or terminates unexpectedly
     */
    private void handleConnection(Socket socket) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

        synchronized(this) 
        {
        output.println("Welcome to Minesweeper. " + "\""  +
        numberOfConnectedClients + "\"" + " people are playing including you. Type \'help\' for help." 
        );
        }

        try {
            for (String readline = input.readLine(); readline!=null; readline=input.readLine()) {
                String outcome = handleRequest(readline);
                
                if(outcome != null) 
                {
                     if(outcome.equals(BOOM_MSG) && debug == false) 
                     {
                        output.println(outcome);
                        break;
                     }
                     else if(outcome.equals(CLOSE_MSG)) 
                            break;
                     else 
                        output.println(outcome);
                }
            }
        } 
        finally
        {        
            output.close();
            input.close();
        }
    }

    /**
     * handler for client input 
     * @param String representing the input to be parsed
     * @return String representing the board's status or Boom message or Close message
     */
    private String handleRequest(String input) {
        String validRegex = "(look)|(dig \\d+ \\d+)|(flag \\d+ \\d+)|" +"(deflag \\d+ \\d+)|(help)|(bye)";
        if(!input.matches(validRegex)) 
            return null;
        
        String[] inputArray = input.split(" ");
        if (inputArray[0].equals("bye")) 
            return CLOSE_MSG;   
        else if (inputArray[0].equals("dig")) 
            //return outputDigCell(Integer.parseInt(inputArray[1]), Integer.parseInt(inputArray[2]));  
        	return outputDigCell(Integer.parseInt(inputArray[2]), Integer.parseInt(inputArray[1]));
        else if (inputArray[0].equals("help")) 
           return  "The following commands are available : look, dig, flag, deflag, help, bye"; 
        else  if (inputArray[0].equals("look")) 
                return displayBoard();                   
        else if (inputArray[0].equals("flag")) 
        {
        	//board.SetStatusToFlagged(Integer.parseInt(inputArray[1]),Integer.parseInt(inputArray[2]));
        	board.SetStatusToFlagged(Integer.parseInt(inputArray[2]),Integer.parseInt(inputArray[1]));
            return displayBoard();
        } 
        else if (inputArray[0].equals("deflag")) 
        {
            //board.unflag(Integer.parseInt(inputArray[1]),Integer.parseInt(inputArray[2]));
        	board.unflag(Integer.parseInt(inputArray[2]),Integer.parseInt(inputArray[1]));
            return displayBoard();
        }
       
        throw new UnsupportedOperationException();
    }
    

    /**
     * Start a MinesweeperServer running on the default port (4444).
     * 
     * Usage: MinesweeperServer [DEBUG [(-s SIZE | -f FILE)]]
     * 
     * The DEBUG argument should be either 'true' or 'false'. The server should disconnect a client
     * after a BOOM message if and only if the DEBUG flag is set to 'false'.
     * 
     * SIZE is an optional integer argument specifying that a random board of size SIZE*SIZE should
     * be generated. E.g. "MinesweeperServer false -s 15" starts the server initialized with a
     * random board of size 15*15.
     * 
     * FILE is an optional argument specifying a file pathname where a board has been stored. If
     * this argument is given, the stored board should be loaded as the starting board. E.g.
     * "MinesweeperServer false -f boardfile.txt" starts the server initialized with the board
     * stored in boardfile.txt, however large it happens to be (but the board may be assumed to be
     * square).
     * 
     * The board file format, for use by the "-f" option, is specified by the following grammar:
     * 
     * FILE :== LINE+
     * LINE :== (VAL SPACE)* VAL NEWLINE
     * VAL :== 0 | 1
     * SPACE :== " "
     * NEWLINE :== "\n" 
     * 
     * If neither FILE nor SIZE is given, generate a random board of size 10x10. If no arguments are
     * specified, do the same and additionally assume DEBUG is 'false'. FILE and SIZE may not be
     * specified simultaneously, and if one is specified, DEBUG must also be specified.
     * 
     * The system property minesweeper.customport may be used to specify a listening port other than
     * the default (used by the autograder only).
     */
    public static void main(String[] args) {
        // We parse the command-line arguments for you. Do not change this method.
        boolean debug = false;
        File file = null;
        Integer size = 10; // Default size.
        try {
            if (args.length != 0 && args.length != 1 && args.length != 3)
              throw new IllegalArgumentException();
            if (args.length >= 1) {
                if (args[0].equals("true")) {
                    debug = true;
                } else if (args[0].equals("false")) {
                    debug = false;
                } else {
                    throw new IllegalArgumentException();
                }
            }
            if (args.length == 3) {
                if (args[1].equals("-s")) {
                    try {
                        size = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException();
                    }
                    if (size < 0)
                        throw new IllegalArgumentException();
                } else if (args[1].equals("-f")) {
                    file = new File(args[2]);
                    if (!file.isFile()) {
                        System.err.println("file not found: \"" + file + "\"");
                        return;
                    }
                    size = null;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("usage: MinesweeperServer DEBUG [(-s SIZE | -f FILE)]");
            return;
        }
        // Allow the autograder to change the port number programmatically.
        final int port;
        String portProp = System.getProperty("minesweeper.customport");
        if (portProp == null) {
            port = 4444; // Default port; do not change.
        } else {
            port = Integer.parseInt(portProp);
        }
        try {
        	//System.out.print("trying");
            runMinesweeperServer(debug, file, size, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start a MinesweeperServer running on the specified port, with either a random new board or a
     * board loaded from a file. Either the file or the size argument must be null, but not both.
     * 
     * @param debug The server should disconnect a client after a BOOM message if and only if this
     *        argument is false.
     * @param size If this argument is not null, start with a random board of size size * size.
     * @param file If this argument is not null, start with a board loaded from the specified file,
     *        according to the input file format defined in the JavaDoc for main().
     * @param port The network port on which the server should listen.
     */
    public static void runMinesweeperServer(boolean debug, File file, Integer size, int port)
            throws IOException
    {
        
        if(file == null && size == null) 
            throw new IllegalArgumentException("Must pass a size or file");
        
        Board newBoard;
        if(file != null) 
            newBoard  = new Board(file); // create the board based on the given file
        else if (size != null && size > 0) 
            newBoard = new Board(size); // create the board randomly
        else
            throw new IllegalArgumentException("Illegal size input");
        
        
        MinesweeperServer server = new MinesweeperServer(port, debug, newBoard);
        server.serve();
    }
}
