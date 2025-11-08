import java.sql.Timestamp;

public class Trashcan {
    private int id;
    private String name;
    private String location;
    private String status;
    private Timestamp lastUpdated;

    // Default constructor
    public Trashcan() {
    }

    // Constructor without id (for creating new trashcans)
    public Trashcan(String name, String location, String status, Timestamp lastUpdated) {
        this.name = name;
        this.location = location;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    // Full constructor
    public Trashcan(int id, String name, String location, String status, Timestamp lastUpdated) {
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

    public String getStatus() {
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

    public void setStatus(String status) {
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

        // Truncate strings to fit table columns if needed
        String truncatedName = name != null && name.length() > 19 
            ? name.substring(0, 16) + "..." 
            : (name != null ? name : "N/A");
        String truncatedLocation = location != null && location.length() > 22 
            ? location.substring(0, 19) + "..." 
            : (location != null ? location : "N/A");
        String truncatedStatus = status != null && status.length() > 5 
            ? status.substring(0, 5) 
            : (status != null ? status : "N/A");

        // Format to match table: | ID | Name (19) | Location (22) | Status (5) | Last Updated (21) |
        return String.format("| %-3d| %-19s| %-22s| %-5s| %-21s|",
            id,
            truncatedName,
            truncatedLocation,
            truncatedStatus,
            formattedTimestamp);
    }
}

