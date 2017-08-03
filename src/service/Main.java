package service;

import java.sql.SQLException;

public class Main {
    /**
     * The main method which spins up a Server in its own thread.
     * @param args Command line arguments passed to this program (if any).
     */
    public static void main(String[] args) throws SQLException {
        try (Server server = new Server(9001)) {
            // Create UserService Database
            Class.forName("service.UserTable");
            
            // Create a new Server in its own thread.
            System.out.println("Starting Server.");
            
            new Thread(server).start();
    
            // Allow the server 20 seconds to run.
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            UserTable.getInstance().postUser("me@you.com", "111-111-1111", "Me You", "12345", null);
            
            // Spin down the server. It will continue to finish processing any requests it received prior to this command.
            System.out.println("Stopping Server.");
        } catch (ClassNotFoundException e1) {
            // TODO Handle gracefully
            e1.printStackTrace();
        } finally {
//             UserTable.tearDown();
        }
    }
}
