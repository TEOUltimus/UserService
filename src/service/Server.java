package service;

import java.net.*;
import java.io.IOException;

/**
 * Server
 * @author Eric Summers
 *
 * A Server object that processes incoming requests each in its own thread.
 */
public class Server implements Runnable {
    // The port the server accepts incoming connections on.
	private int serverPort = 8080;
	
	// The socket the sever uses to accept incoming connections. 
    private ServerSocket serverSocket = null;
    
    // Has the server been instructed to stop accepting incoming requests?
    private volatile boolean isStopped = false;

    /**
     * Creates a new Server object.
     * @param port The port the server should use.
     */
    public Server(int port) {
        serverPort = port;
    }

    /**
     * Runs the server's loop for processing incoming requests.
     */
    public void run() {
        openServerSocket();
        
        while (!isStopped) {
            try {
            	// Spin up a thread to accept an incoming request.
            	new Thread(new Request(serverSocket.accept(), "Multithreaded Server")).start();
            } catch (IOException e) {
            	// Only handle a failure if the server is still running.
                if (!isStopped) {
                    throw new RuntimeException("Error accepting client connection", e);
                }
            }
        }
        
        System.out.println("Server Stopped.") ;
    }

    /**
     * Instructs the server to stop accepting incoming requests.
     */
    public synchronized void stop(){
        isStopped = true;
        
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + serverPort, e);
        }
    }
}
