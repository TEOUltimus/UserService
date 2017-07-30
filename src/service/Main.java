package service;

public class Main {
    /**
     * The main method which spins up a Server in its own thread.
     * @param args Command line arguments passed to this program (if any).
     */
    public static void main(String[] args) {
        // Create a new Server in its own thread.
        System.out.println("Starting Server");
        Server server = new Server(9000);
        new Thread(server).start();

        // Allow the server 20 seconds to run.
        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Spin down the server. It will continue to finish processing any requests it received prior to this command.
        System.out.println("Stopping Server");
        server.stop();
    }
}
