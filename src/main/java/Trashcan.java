import java.sql.Timestamp;

public class Trashcan {
    private int id;
    private String name;
    private String location;
    private int status; // Percentage (0-100)
    private Timestamp lastUpdated;

    // Default constructor
    public Trashcan() {
    }

    // Constructor without id (for creating new trashcans)
    public Trashcan(String name, String location, int status, Timestamp lastUpdated) {
        this.name = name;
        this.location = location;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    // Full constructor
    public Trashcan(int id, String name, String location, int status, Timestamp lastUpdated) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public int getStatus() {
        return status;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // toString() method formatted for terminal table display
    @Override
    public String toString() {
        // Format timestamp to match table display: "2025-11-07 12:00"
        String formattedTimestamp = "N/A";
        if (lastUpdated != null) {
            String timestampStr = lastUpdated.toString();
            // Handle format: "2025-11-07 12:00:00.0" -> "2025-11-07 12:00"
            if (timestampStr.length() >= 16) {
                formattedTimestamp = timestampStr.substring(0, 16);
            } else {
                formattedTimestamp = timestampStr;
            }
        }

        // Truncate strings to fit table columns if needed (with smart truncation)
        int nameWidth = 45;
        int locationWidth = 40;

        String truncatedName = truncateString(name != null ? name : "N/A", nameWidth);
        String truncatedLocation = truncateString(location != null ? location : "N/A", locationWidth);
        String statusDisplay = String.format("%d%%", status);

        // Format to match table: | ID (4) | Name (44) | Location (39) | Status (8) |
        // Last Updated (19) |
        return String.format("| %-4d| %-44s| %-40s| %-8s| %-19s|",
                id,
                truncatedName,
                truncatedLocation,
                statusDisplay,
                formattedTimestamp);
    }

    /**
     * Truncates a string to fit within the specified width, adding "..." if
     * truncated.
     * 
     * @param str   The string to truncate
     * @param width The maximum width
     * @return The truncated string
     */
    private String truncateString(String str, int width) {
        if (str == null) {
            return "N/A";
        }
        if (str.length() <= width) {
            return str;
        }
        // Truncate and add ellipsis
        return str.substring(0, width - 3) + "...";
    }

    /**
     * Returns a detailed string representation of the trashcan without truncation.
     * 
     * @return Detailed string representation
     */
    public String toDetailedString() {
        String formattedTimestamp = "N/A";
        if (lastUpdated != null) {
            String timestampStr = lastUpdated.toString();
            if (timestampStr.length() >= 19) {
                formattedTimestamp = timestampStr.substring(0, 19);
            } else {
                formattedTimestamp = timestampStr;
            }
        }

        // Box width is 64 characters total
        // Format breakdown: "║ " (2) + label (14) + content (47) + "║" (1) = 64
        // Remove the space before ║ to fix alignment
        int boxWidth = 64;
        int labelWidth = 14;
        // Total: 2 (left) + 14 (label) + content + 1 (right) = 64
        // Therefore: content = 64 - 2 - 14 - 1 = 47
        int contentWidth = boxWidth - 2 - labelWidth - 1; // 47

        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    TRASHCAN DETAILS                            ║\n");
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ %-" + labelWidth + "s%-" + contentWidth + "s  ║\n", "ID:",
                id != 0 ? String.valueOf(id) : "N/A"));
        sb.append(String.format("║ %-" + labelWidth + "s%-" + contentWidth + "s  ║\n", "Name:",
                name != null ? name : "N/A"));
        sb.append(String.format("║ %-" + labelWidth + "s%-" + contentWidth + "s  ║\n", "Location:",
                location != null ? location : "N/A"));
        sb.append(String.format("║ %-" + labelWidth + "s%-" + contentWidth + "s  ║\n", "Status:",
                String.format("%d%%", status)));
        sb.append(String.format("║ %-" + labelWidth + "s%-" + contentWidth + "s  ║\n", "Last Updated:",
                formattedTimestamp));
        sb.append("╚════════════════════════════════════════════════════════════════╝");

        return sb.toString();
    }
}
