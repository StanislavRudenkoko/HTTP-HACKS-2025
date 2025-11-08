import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DEFAULT_SCHEMA = "smart_trashcan";
    private Connection connection = null;

    /**
     * Connects to the PostgreSQL database and sets the default schema.
     * 
     * @param url The database URL (e.g., jdbc:postgresql://localhost:5432/dbname)
     * @param user The database username
     * @param password The database password
     * @return true if connection successful, false otherwise
     */
    public boolean connect(String url, String user, String password) {
        // Close existing connection if any
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Warning: Error closing existing connection: " + e.getMessage());
            }
        }

        try {
            // Establish connection
            connection = DriverManager.getConnection(url, user, password);
            
            // Set the default schema to smart_trashcan
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET search_path TO " + DEFAULT_SCHEMA);
            }
            
            System.out.println("[OK] Successfully connected to database!");
            System.out.println("[OK] Default schema set to: " + DEFAULT_SCHEMA);
            return true;
            
        } catch (SQLException e) {
            connection = null;
            String errorMessage = "[ERROR] Failed to connect to database: ";
            
            // Provide more specific error messages
            if (e.getMessage().contains("Connection refused")) {
                errorMessage += "Connection refused. Please check if the database server is running and the URL is correct.";
            } else if (e.getMessage().contains("password authentication failed")) {
                errorMessage += "Authentication failed. Please check your username and password.";
            } else if (e.getMessage().contains("does not exist")) {
                errorMessage += "Database does not exist. Please verify the database name in the URL.";
            } else {
                errorMessage += e.getMessage();
            }
            
            System.err.println(errorMessage);
            return false;
        }
    }

    /**
     * Disconnects from the database.
     * 
     * @return true if disconnection successful, false otherwise
     */
    public boolean disconnect() {
        if (connection == null) {
            System.out.println("No active connection to close.");
            return false;
        }

        try {
            connection.close();
            connection = null;
            System.out.println("[OK] Database connection closed successfully.");
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] Error closing database connection: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns the current database connection.
     * 
     * @return The Connection object, or null if not connected
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Tests the database connection by executing a simple query.
     * Prints success or failure message.
     */
    public void testConnection() {
        if (connection == null) {
            System.out.println("[ERROR] Connection test failed: No active database connection.");
            return;
        }

        try {
            // Check if connection is still valid (timeout: 2 seconds)
            if (connection.isValid(2)) {
                // Try a simple query to verify schema access
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeQuery("SELECT 1");
                }
                System.out.println("[OK] Connection test successful! Database is accessible.");
            } else {
                System.out.println("[ERROR] Connection test failed: Connection is no longer valid.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Connection test failed: " + e.getMessage());
            System.err.println("  Error code: " + e.getErrorCode());
            System.err.println("  SQL state: " + e.getSQLState());
        }
    }

    /**
     * Checks if there is an active database connection.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        if (connection == null) {
            return false;
        }
        try {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}

