import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static Connection connection = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    connectToDatabase();
                    break;
                case "2":
                    if (checkConnection()) viewTrashcans();
                    break;
                case "3":
                    System.out.println("Add Trashcan feature coming soon!");
                    break;
                case "4":
                    System.out.println("Update Trashcan feature coming soon!");
                    break;
                case "5":
                    System.out.println("Delete Trashcan feature coming soon!");
                    break;
                case "6":
                    System.out.println("View Subscribers feature coming soon!");
                    break;
                case "7":
                    System.out.println("Add Subscriber feature coming soon!");
                    break;
                case "8":
                    System.out.println("Delete Subscriber feature coming soon!");
                    break;
                case "9":
                    running = false;
                    disconnectDatabase();
                    System.out.println("Exiting. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }

            System.out.println(); // Empty line for spacing
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║       Smart Trashcan App       ║");
        System.out.println("╠════════════════════════════════╣");
        System.out.println("║ 1. Connect to Database         ║");
        System.out.println("║ 2. View Trashcans              ║");
        System.out.println("║ 3. Add Trashcan                ║");
        System.out.println("║ 4. Update Trashcan             ║");
        System.out.println("║ 5. Delete Trashcan             ║");
        System.out.println("║ 6. View Subscribers            ║");
        System.out.println("║ 7. Add Subscriber              ║");
        System.out.println("║ 8. Delete Subscriber           ║");
        System.out.println("║ 9. Exit                        ║");
        System.out.println("╚════════════════════════════════╝");
    }

    private static void connectToDatabase() {
        System.out.print("Enter DB URL (jdbc:postgresql://host:port/dbname): ");
        String url = scanner.nextLine();
        System.out.print("Enter DB username: ");
        String user = scanner.nextLine();
        System.out.print("Enter DB password: ");
        String password = scanner.nextLine();

        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database!");
        } catch (SQLException e) {
            System.out.println("Failed to connect: " + e.getMessage());
        }
    }

    private static boolean checkConnection() {
        if (connection == null) {
            System.out.println("Not connected to any database. Please connect first.");
            return false;
        }
        return true;
    }

    private static void disconnectDatabase() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    private static void viewTrashcans() {
        if (!checkConnection()) return;

        // TODO: Replace this with actual DB query using TrashcanService
        System.out.println("+----+-------------------+----------------------+-------+---------------------+");
        System.out.println("| ID | Name              | Location             | Status| Last Updated        |");
        System.out.println("+----+-------------------+----------------------+-------+---------------------+");
        System.out.println("| 1  | NW1-F1-Entrance   | NW1 floor 1, entrance| empty | 2025-11-07 12:00    |");
        System.out.println("| 2  | NE2-F2-Labs       | NE2 floor 2, labs    | half  | 2025-11-07 12:05    |");
        System.out.println("| 3  | SW3-F1-Cafeteria | SW3 floor 1, cafeteria| full | 2025-11-07 12:10   |");
        System.out.println("+----+-------------------+----------------------+-------+---------------------+");
    }
}
