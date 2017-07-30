package service;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Request
 * @author Eric Summers
 * 
 * An object that handles a single request made to the Server.
 */
public class Request implements Runnable {
	// The socket which accepted the incoming connection.
    private Socket clientSocket = null;
    
    // Message the server should respond with/
    private String serverText = null;

    /**
     * Creates a Request object to process the request received by the Server.
     * @param socket The socket used.
     * @param text The text to reply with.
     */
    public Request(Socket socket, String text) {
        clientSocket = socket;
        serverText = text;
    }

    /**
     * Responds to the client who made the request.
     */
    public void run() {
    	// Ensure the streams will be closed correctly regardless of success.
        try (InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();) {
            long time = System.currentTimeMillis();
            output.write(("HTTP/1.1 200 OK\n\nWorkerRunnable: " + serverText + " - " + time +"").getBytes());
                        
            System.out.println("Request processed: " + time);
        } catch (IOException e) {
        	// TODO fail gracefully
            e.printStackTrace();
        }
    }
}
