package service;

import java.sql.*;

public class Users {
    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    private static final String DB_URL = "jdbc:mysql://localhost/EMP";

    // Database credentials
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
   
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            // STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT id, first, last, age FROM Employees";
            ResultSet rs = stmt.executeQuery(sql);

            // STEP 5: Extract data from result set
            while (rs.next() ){
                // Retrieve by column name
                int id  = rs.getInt("id");
                int age = rs.getInt("age");
                String first = rs.getString("first");
                String last = rs.getString("last");

                // Display values
                System.out.print("ID: " + id);
                System.out.print(", Age: " + age);
                System.out.print(", First: " + first);
                System.out.println(", Last: " + last);
            }
          
            // STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch(SQLException se) {
            // Handle errors for JDBC
            se.printStackTrace();
        } catch(Exception e) {
            // Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            // Finally block used to close resources
            try {
                if (stmt!=null) {
                    stmt.close();
                }
            } catch(SQLException se2) {
            // Nothing we can do
            }
            
            try {
                if (conn!=null){
                    conn.close();
                }
            } catch(SQLException se) {
                se.printStackTrace();
            } // End finally try
        } // End try
       
        System.out.println("Goodbye!");
    } // End main
}
