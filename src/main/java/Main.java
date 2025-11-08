import java.sql.*;

public class Main {
    public static void main(String[] args) {
        // Update these for your setup
        String url = "jdbc:postgresql://localhost:5432/trash_db"; // replace with your DB name
        String user = "postgres"; // your DB username
        String password = "psql"; // your DB password

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to the database!");

            // Set the search_path to the smart_trashcan schema
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET search_path TO smart_trashcan");
            }

            // Query without subscriber_phone
            String query = "SELECT id, name, location, status, last_updated FROM trashcans";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                System.out.println("ID | Name        | Location     | Status | Last Updated");
                System.out.println("-----------------------------------------------------------");

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String location = rs.getString("location");
                    String status = rs.getString("status");
                    Timestamp lastUpdated = rs.getTimestamp("last_updated");

                    System.out.printf("%2d | %-10s | %-10s | %-5s | %-20s%n",
                            id, name, location, status, lastUpdated);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
