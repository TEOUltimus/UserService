package service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.*;

public class UserTable implements AutoCloseable {
    // JDBC driver name and SQL URL
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    private static final String SQL_URL = "jdbc:mysql://localhost/";
    
    // Name of the database and table to create
    private static final String DB_NAME = "UserService";
    private static final String TABLE_NAME = "Users";
    
    private static final String CREATE_DB_QUERY = "CREATE DATABASE %s";
    private static final String DESTROY_DB_QUERY = "DROP DATABASE %s";
    
    private static final String CREATE_USER_TABLE_QUERY = "CREATE TABLE %s (%s, %s, %s, %s, %s, %s, %s, %s, %s)";
    private static final String GET_USERS_QUERY = "SELECT * from %s %s ORDER BY id DESC";
    private static final String INSERT_USER_QUERY = "INSERT INTO %s"
            + "(email, phone_number, full_name, password, salt, user_key, metadata)"
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String USER_ID = "id int NOT NULL AUTO_INCREMENT PRIMARY KEY";
    private static final String USER_EMAIL = "email varchar(200) NOT NULL UNIQUE";
    private static final String USER_PHONE = "phone_number varchar(200) NOT NULL UNIQUE";
    private static final String USER_NAME = "full_name varchar(200)";
    private static final String USER_PWD = "password varchar(100) NOT NULL";
    private static final String USER_SALT = "salt varchar(16) NOT NULL";
    private static final String USER_KEY = "user_key varchar(100) NOT NULL UNIQUE";
    private static final String USER_ACCT_KEY = "account_key varchar(100) UNIQUE";
    private static final String USER_METADATA = "metadata varchar(2000)";

    // Database credentials and URL
    private static String username = "";
    private static String password = "";
    private static String dbUrl = SQL_URL + DB_NAME;
    
    private static final ReadWriteLock masterLock = new ReentrantReadWriteLock();

    static {
        Lock lock = masterLock.writeLock();
        try {
            // Register Driver
            Class.forName(JDBC_DRIVER);
            
            // Prevent attempts to access database during its creation
            lock.lock();
            try (Connection conn = DriverManager.getConnection(SQL_URL, username, password);
                    Statement stmt = conn.createStatement();) {
                stmt.executeUpdate(String.format(CREATE_DB_QUERY, DB_NAME));
                System.out.printf("Created database %s.\n", DB_NAME);
                
                try (Connection conn2 = DriverManager.getConnection(dbUrl, username, password);
                        Statement stmt2 = conn2.createStatement();) {
                    stmt2.executeUpdate(String.format(CREATE_USER_TABLE_QUERY,
                            TABLE_NAME, 
                            USER_ID, 
                            USER_EMAIL, 
                            USER_PHONE, 
                            USER_NAME, 
                            USER_PWD,
                            USER_SALT,
                            USER_KEY, 
                            USER_ACCT_KEY, 
                            USER_METADATA
                    ));
                    
                    System.out.printf("Created table %s.\n", TABLE_NAME);
                }
            }
        } catch (ClassNotFoundException e) {
            // TODO Fail gracefully
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            lock.tryLock();
            lock.unlock();
        }
    }
    
    public synchronized static UserTable getInstance() {
        return new UserTable(masterLock.readLock());
    }
    
    public static void tearDown() {
        Lock lock = masterLock.writeLock();
        try {
            // TODO trylock with timeout?
            lock.lock();
        } finally {
            lock.tryLock();
            
            try (Connection conn = DriverManager.getConnection(SQL_URL, username, password);
                    Statement stmt = conn.createStatement();) {
                stmt.executeUpdate(String.format(DESTROY_DB_QUERY, DB_NAME));
                System.out.printf("Destroyed database %s.\n", DB_NAME);
            } catch (SQLException e) {
                // TODO Fail Gracefully
                e.printStackTrace();
            }
            
            lock.unlock();
        }
    }
    
    private Connection conn;
    private Lock lock;
    private Random random;
    private MessageDigest dig;
    
    private UserTable(Lock l) {
        lock = l;
        lock.lock();
        random = new SecureRandom();
        
        try {
            dig = MessageDigest.getInstance("SHA-256");
            conn = DriverManager.getConnection(dbUrl, username, password);
        } catch (SQLException | NoSuchAlgorithmException e) {
            // TODO Fail gracefully
            e.printStackTrace();
        }
    }
    
    public String getUsers(Map<String, String> userDetails) {
        try (Statement stmt = conn.prepareStatement("");) {
            return "";
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }
    
    public void postUser(String email, String phone, String name, String pwd, String metadata) throws SQLException {
        try (PreparedStatement stmt = insertQuery(pwd, new byte[16]);) {
            stmt.setString(1, email);
            stmt.setString(2, name);
            stmt.setString(3, phone);
            if (metadata != null) {
                stmt.setString(7, metadata);
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }
            
            stmt.executeUpdate();
        } /*catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
    }
    
    private PreparedStatement insertQuery(String pwd, byte[] salt) throws SQLException {
        random.nextBytes(salt);
        byte[] saltedHash = new byte[pwd.length() + salt.length];
        
        int i = 0;
        for (; i < pwd.length(); i++) {
            saltedHash[i] = (byte) pwd.charAt(i);
        }
        
        for (int j = 0; i < saltedHash.length; j++) {
            saltedHash[i++] = salt[j];
        }
        
        dig.update(saltedHash, 0, saltedHash.length);
        
        PreparedStatement stmt = conn.prepareStatement(String.format(INSERT_USER_QUERY, TABLE_NAME));
        stmt.setString(4, new String(saltedHash));
        stmt.setString(5, new String(salt));
        stmt.setString(6, UUID.randomUUID().toString());
        
        return stmt;
    }

    @Override
    public void close() throws Exception {
        lock.tryLock();
        conn.close();
        lock.unlock();
    }
    
    @Override
    public void finalize() throws Exception {
        close();
    }
}
