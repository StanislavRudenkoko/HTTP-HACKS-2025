import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static DatabaseManager dbManager = new DatabaseManager();
    private static TrashcanService trashcanService = new TrashcanService(dbManager);
    private static Scanner scanner = new Scanner(System.in);
    private static List<Trashcan> inMemoryTrashcans = new ArrayList<>();
    private static List<Trashcan> originalTrashcans = new ArrayList<>(); // Snapshot from DB
    private static int nextTempId = -1; // For new trashcans (negative IDs)
    private static boolean hasUnsavedChanges = false;

    public static void main(String[] args) {
        boolean running = true;
        boolean firstRun = true;

        while (running) {
            clearScreen();

            if (firstRun) {
                System.out.println("╔════════════════════════════════════════╗");
                System.out.println("║     Welcome to Smart Trashcan App      ║");
                System.out.println("╚════════════════════════════════════════╝");
                System.out.println();
                firstRun = false;
            }

            printMenu();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            boolean isConnected = dbManager.isConnected();

            switch (choice) {
                case "1":
                    connectToDatabase();
                    waitForEnter();
                    break;
                case "2":
                    if (isConnected) {
                        viewAllTrashcans();
                    } else {
                        running = false;
                        disconnectDatabase();
                        System.out.println("Exiting. Goodbye!");
                    }
                    if (running)
                        waitForEnter();
                    break;
                case "3":
                    if (isConnected) {
                        addTrashcan();
                        waitForEnter();
                    } else {
                        System.out.println("[ERROR] Invalid option. Please try again.");
                        waitForEnter();
                    }
                    break;
                case "4":
                    if (isConnected) {
                        updateTrashcan();
                        waitForEnter();
                    } else {
                        System.out.println("[ERROR] Invalid option. Please try again.");
                        waitForEnter();
                    }
                    break;
                case "5":
                    if (isConnected) {
                        deleteTrashcan();
                        waitForEnter();
                    } else {
                        System.out.println("[ERROR] Invalid option. Please try again.");
                        waitForEnter();
                    }
                    break;
                case "6":
                    if (isConnected) {
                        saveTrashcansToDatabase();
                        waitForEnter();
                    } else {
                        System.out.println("[ERROR] Invalid option. Please try again.");
                        waitForEnter();
                    }
                    break;
                case "7":
                    if (isConnected) {
                        if (hasUnsavedChanges) {
                            System.out.print(
                                    "\n[WARNING] You have unsaved changes. Are you sure you want to exit? (yes/no): ");
                            String confirm = scanner.nextLine().trim().toLowerCase();
                            if (!confirm.equals("yes") && !confirm.equals("y")) {
                                waitForEnter();
                                break;
                            }
                        }
                        running = false;
                        disconnectDatabase();
                        System.out.println("Exiting. Goodbye!");
                    } else {
                        System.out.println("[ERROR] Invalid option. Please try again.");
                        waitForEnter();
                    }
                    break;
                default:
                    System.out.println("[ERROR] Invalid option. Please try again.");
                    waitForEnter();
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        boolean isConnected = dbManager.isConnected();

        System.out.println("╔════════════════════════════════╗");
        System.out.println("║       Smart Trashcan App       ║");
        System.out.println("╠════════════════════════════════╣");
        System.out.println("║ 1. Connect to Database         ║");

        if (isConnected) {
            System.out.println("║ 2. View Trashcans              ║");
            System.out.println("║ 3. Add Trashcan                ║");
            System.out.println("║ 4. Update Trashcan             ║");
            System.out.println("║ 5. Delete Trashcan             ║");
            System.out.println("║ 6. Save Changes to Database    ║");
        }

        System.out.println("║ " + (isConnected ? "7" : "2") + ". Exit                        ║");
        System.out.println("╚════════════════════════════════╝");

        if (isConnected) {
            System.out.println("\n[OK] Connected to database");
            if (hasUnsavedChanges) {
                System.out.println("[WARNING] You have unsaved changes");
            }
        } else {
            System.out.println("\n[WARNING] Not connected. Please connect to database first.");
        }
    }

    private static void connectToDatabase() {
        System.out.println("\n--- Connect to Database ---");
        System.out.print("Enter DB URL (e.g., jdbc:postgresql://localhost:5432/dbname): ");
        String url = scanner.nextLine().trim();

        if (url.isEmpty()) {
            System.out.println("[ERROR] Error: Database URL cannot be empty.");
            return;
        }

        System.out.print("Enter DB username: ");
        String user = scanner.nextLine().trim();

        if (user.isEmpty()) {
            System.out.println("[ERROR] Error: Username cannot be empty.");
            return;
        }

        System.out.print("Enter DB password: ");
        String password = scanner.nextLine().trim();

        if (dbManager.connect(url, user, password)) {
            // Load trashcans from database into memory
            loadTrashcansFromDatabase();
        }
    }

    private static void viewAllTrashcans() {
        if (!checkConnection())
            return;

        System.out.println("\n--- View All Trashcans (From Database) ---");

        // Fetch fresh data from database
        List<Trashcan> freshTrashcans = trashcanService.getAllTrashcans();

        // Get the actual next ID from the database sequence (accounts for sequence
        // state)
        int nextSequenceId = trashcanService.getNextId();

        // Create display list with preview IDs for unsaved entries
        List<Trashcan> displayTrashcans = new ArrayList<>(freshTrashcans);
        List<Integer> previewIds = new ArrayList<>();
        int nextPreviewId = nextSequenceId;

        // Map to track original negative ID to preview ID for lookup
        Map<Integer, Integer> idMapping = new HashMap<>();

        for (Trashcan t : inMemoryTrashcans) {
            if (t.getId() < 0) {
                // Create a copy with preview ID for display
                Trashcan displayCopy = new Trashcan(
                        nextPreviewId,
                        t.getName(),
                        t.getLocation(),
                        t.getStatus(),
                        t.getLastUpdated());
                displayTrashcans.add(displayCopy);
                previewIds.add(nextPreviewId);
                idMapping.put(nextPreviewId, t.getId()); // Map preview ID to original negative ID
                nextPreviewId++;
            }
        }

        if (displayTrashcans.isEmpty()) {
            System.out.println("No trashcans found in database.");
            return;
        }

        trashcanService.displayTrashcans(displayTrashcans);
        if (!previewIds.isEmpty()) {
            System.out.print("\n[WARNING] Note: Entries with IDs ");
            for (int i = 0; i < previewIds.size(); i++) {
                System.out.print(previewIds.get(i));
                if (i < previewIds.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println(" are not saved yet. Use option 6 to save to database.");
        }

        // Loop to allow viewing multiple trashcans
        boolean isFirstPrompt = true;
        while (true) {
            if (isFirstPrompt) {
                System.out
                        .print("\nWould you like to view a specific trashcan in detail? (enter ID or 'no' to skip): ");
                isFirstPrompt = false;
            } else {
                System.out.print("\nView another trashcan? (enter ID or 'no' to return to menu): ");
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty() || input.equalsIgnoreCase("no") || input.equalsIgnoreCase("n")) {
                break;
            }

            try {
                int id = Integer.parseInt(input);

                // Find the trashcan - check if it's a preview ID first
                Trashcan foundTrashcan = null;
                Trashcan displayTrashcan = null;

                // Check if it's a preview ID (unsaved entry)
                if (idMapping.containsKey(id)) {
                    // Find the original entry with negative ID
                    int originalId = idMapping.get(id);
                    for (Trashcan t : inMemoryTrashcans) {
                        if (t.getId() == originalId) {
                            foundTrashcan = t;
                            // Create display version with preview ID
                            displayTrashcan = new Trashcan(
                                    id, // Use preview ID for display
                                    t.getName(),
                                    t.getLocation(),
                                    t.getStatus(),
                                    t.getLastUpdated());
                            break;
                        }
                    }
                } else {
                    // Check database entries
                    for (Trashcan t : freshTrashcans) {
                        if (t.getId() == id) {
                            foundTrashcan = t;
                            displayTrashcan = t; // Use as-is for saved entries
                            break;
                        }
                    }
                }

                if (foundTrashcan != null && displayTrashcan != null) {
                    System.out.println();
                    trashcanService.displayTrashcanDetail(displayTrashcan);
                } else {
                    System.out.println("[ERROR] No trashcan found with ID: " + id);
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid input. Please enter a valid ID number or 'no' to skip.");
            }
        }
    }

    private static void addTrashcan() {
        if (!checkConnection())
            return;

        System.out.println("\n--- Add New Trashcan (In-Memory) ---");

        System.out.print("Enter trashcan name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("[ERROR] Error: Name cannot be empty.");
            return;
        }

        System.out.print("Enter location: ");
        String location = scanner.nextLine().trim();
        // Location can be empty/null

        System.out.print("Enter status (0-100, percentage): ");
        String statusInput = scanner.nextLine().trim();
        int status;
        try {
            status = Integer.parseInt(statusInput);
            if (status < 0 || status > 100) {
                System.out.println("[ERROR] Error: Status must be between 0 and 100 (percentage).");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Error: Status must be a valid integer between 0 and 100.");
            return;
        }

        // Create trashcan with temporary negative ID
        Trashcan trashcan = new Trashcan(nextTempId--, name, location, status,
                new Timestamp(System.currentTimeMillis()));
        inMemoryTrashcans.add(trashcan);
        hasUnsavedChanges = true;

        // Calculate preview ID using actual sequence value (accounts for sequence
        // state)
        int nextSequenceId = trashcanService.getNextId();
        // Count unsaved entries to get the correct preview ID
        int unsavedCount = 0;
        for (Trashcan t : inMemoryTrashcans) {
            if (t.getId() < 0) {
                unsavedCount++;
            }
        }
        int previewId = nextSequenceId + unsavedCount - 1; // -1 because we just added this one

        System.out.println(
                "[OK] Trashcan added to memory (ID: " + previewId + "). Use option 6 to save to database.");
    }

    private static void updateTrashcan() {
        if (!checkConnection())
            return;

        System.out.println("\n--- Update Trashcan (In-Memory) ---");

        System.out.print("Enter trashcan ID to update: ");
        String idInput = scanner.nextLine().trim();

        int id;
        try {
            id = Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Error: Invalid ID format. Please enter a number.");
            return;
        }

        // Find trashcan in memory
        Trashcan existingTrashcan = null;
        int index = -1;
        for (int i = 0; i < inMemoryTrashcans.size(); i++) {
            if (inMemoryTrashcans.get(i).getId() == id) {
                existingTrashcan = inMemoryTrashcans.get(i);
                index = i;
                break;
            }
        }

        if (existingTrashcan == null) {
            System.out.println("[ERROR] Error: No trashcan found with ID: " + id);
            return;
        }

        System.out.println("\nCurrent trashcan details:");
        System.out.println("  Name: " + existingTrashcan.getName());
        System.out.println(
                "  Location: " + (existingTrashcan.getLocation() != null ? existingTrashcan.getLocation() : "N/A"));
        System.out.println("  Status: " + existingTrashcan.getStatus() + "%");
        System.out.println("\nEnter new values (press Enter to keep current value):");

        System.out.print("Enter new name [" + existingTrashcan.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = existingTrashcan.getName();
        }

        System.out.print("Enter new location ["
                + (existingTrashcan.getLocation() != null ? existingTrashcan.getLocation() : "N/A") + "]: ");
        String location = scanner.nextLine().trim();
        if (location.isEmpty()) {
            location = existingTrashcan.getLocation();
        }

        System.out.print("Enter new status (0-100, percentage) [" + existingTrashcan.getStatus() + "%]: ");
        String statusInput = scanner.nextLine().trim();
        int status;
        if (statusInput.isEmpty()) {
            status = existingTrashcan.getStatus();
        } else {
            try {
                status = Integer.parseInt(statusInput);
                if (status < 0 || status > 100) {
                    System.out.println("[ERROR] Error: Status must be between 0 and 100 (percentage).");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Error: Status must be a valid integer between 0 and 100.");
                return;
            }
        }

        // Update in memory
        Trashcan updatedTrashcan = new Trashcan(id, name, location, status, new Timestamp(System.currentTimeMillis()));
        inMemoryTrashcans.set(index, updatedTrashcan);
        hasUnsavedChanges = true;
        System.out.println("[OK] Trashcan updated in memory. Use option 6 to save to database.");
    }

    private static void deleteTrashcan() {
        if (!checkConnection())
            return;

        System.out.println("\n--- Delete Trashcan (In-Memory) ---");

        System.out.print("Enter trashcan ID to delete: ");
        String idInput = scanner.nextLine().trim();

        int id;
        try {
            id = Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Error: Invalid ID format. Please enter a number.");
            return;
        }

        // Find trashcan in memory
        Trashcan trashcanToDelete = null;
        for (Trashcan t : inMemoryTrashcans) {
            if (t.getId() == id) {
                trashcanToDelete = t;
                break;
            }
        }

        if (trashcanToDelete == null) {
            System.out.println("[ERROR] Error: No trashcan found with ID: " + id);
            return;
        }

        // Confirm deletion
        System.out.print("Are you sure you want to delete trashcan with ID " + id + " (" + trashcanToDelete.getName()
                + ")? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (confirmation.equals("yes") || confirmation.equals("y")) {
            inMemoryTrashcans.remove(trashcanToDelete);
            hasUnsavedChanges = true;
            System.out.println("[OK] Trashcan removed from memory. Use option 6 to save changes to database.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void saveTrashcansToDatabase() {
        if (!checkConnection())
            return;

        System.out.println("\n--- Save Changes to Database ---");

        if (!hasUnsavedChanges) {
            System.out.println("[OK] No unsaved changes. Everything is up to date.");
            return;
        }

        // Find items to insert (negative IDs)
        List<Trashcan> toInsert = new ArrayList<>();
        // Find items to update (positive IDs that exist in both lists but changed)
        List<Trashcan> toUpdate = new ArrayList<>();
        // Find items to delete (in original but not in memory)
        List<Integer> toDelete = new ArrayList<>();

        // Identify new items (negative IDs)
        for (Trashcan t : inMemoryTrashcans) {
            if (t.getId() < 0) {
                toInsert.add(t);
            }
        }

        // Identify items to update or delete
        for (Trashcan original : originalTrashcans) {
            boolean found = false;
            for (Trashcan current : inMemoryTrashcans) {
                if (current.getId() == original.getId()) {
                    found = true;
                    // Check if it changed (handle null locations)
                    String origLocation = original.getLocation() != null ? original.getLocation() : "";
                    String currLocation = current.getLocation() != null ? current.getLocation() : "";

                    if (!original.getName().equals(current.getName()) ||
                            !origLocation.equals(currLocation) ||
                            original.getStatus() != current.getStatus()) {
                        toUpdate.add(current);
                    }
                    break;
                }
            }
            if (!found) {
                toDelete.add(original.getId());
            }
        }

        // Execute operations
        int totalOperations = 0;

        // Insert new items
        for (Trashcan t : toInsert) {
            Trashcan newTrashcan = new Trashcan(t.getName(), t.getLocation(), t.getStatus(), t.getLastUpdated());
            trashcanService.addTrashcan(newTrashcan);
            totalOperations++;
        }

        // Update existing items
        for (Trashcan t : toUpdate) {
            trashcanService.updateTrashcan(t);
            totalOperations++;
        }

        // Delete removed items
        for (Integer id : toDelete) {
            trashcanService.deleteTrashcan(id);
            totalOperations++;
        }

        if (totalOperations > 0) {
            System.out.println("[OK] Successfully saved " + totalOperations + " change(s) to database:");
            if (!toInsert.isEmpty())
                System.out.println("  - Inserted: " + toInsert.size() + " trashcan(s)");
            if (!toUpdate.isEmpty())
                System.out.println("  - Updated: " + toUpdate.size() + " trashcan(s)");
            if (!toDelete.isEmpty())
                System.out.println("  - Deleted: " + toDelete.size() + " trashcan(s)");
        } else {
            System.out.println("No changes to save.");
        }

        // Reload from database to sync (get new IDs for inserted items)
        loadTrashcansFromDatabase();
        hasUnsavedChanges = false;
        System.out.println("[OK] In-memory list synchronized with database.");
    }

    /**
     * Loads trashcans from database into memory and creates a snapshot.
     */
    private static void loadTrashcansFromDatabase() {
        inMemoryTrashcans.clear();
        originalTrashcans.clear();

        List<Trashcan> dbTrashcans = trashcanService.getAllTrashcans();

        // Create deep copies for both lists
        for (Trashcan t : dbTrashcans) {
            Trashcan copy1 = new Trashcan(t.getId(), t.getName(), t.getLocation(), t.getStatus(), t.getLastUpdated());
            Trashcan copy2 = new Trashcan(t.getId(), t.getName(), t.getLocation(), t.getStatus(), t.getLastUpdated());
            inMemoryTrashcans.add(copy1);
            originalTrashcans.add(copy2);
        }

        hasUnsavedChanges = false;
        nextTempId = -1; // Reset temp ID counter
    }

    private static boolean checkConnection() {
        if (!dbManager.isConnected()) {
            System.out.println("[ERROR] Error: Not connected to any database. Please connect first (Option 1).");
            return false;
        }
        return true;
    }

    private static void disconnectDatabase() {
        dbManager.disconnect();
    }

    /**
     * Clears the console screen.
     * Uses ANSI escape codes for cross-platform support.
     */
    private static void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Unix/Linux/Mac
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (IOException | InterruptedException e) {
            // Fallback: print multiple newlines if clearing fails
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * Waits for user to press Enter before continuing.
     */
    private static void waitForEnter() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
