public class Subscriber {
    private int id;
    private int trashcanId;
    private String phone;

    // Default constructor
    public Subscriber() {
    }

    // Constructor without id (for creating new subscribers)
    public Subscriber(int trashcanId, String phone) {
        this.trashcanId = trashcanId;
        this.phone = phone;
    }

    // Full constructor
    public Subscriber(int id, int trashcanId, String phone) {
        this.id = id;
        this.trashcanId = trashcanId;
        this.phone = phone;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getTrashcanId() {
        return trashcanId;
    }

    public String getPhone() {
        return phone;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTrashcanId(int trashcanId) {
        this.trashcanId = trashcanId;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // toString() method formatted for terminal table display
    @Override
    public String toString() {
        // Format for a simple table: | ID | Trashcan ID | Phone |
        return String.format("| %-3d| %-12d| %-15s|",
            id,
            trashcanId,
            phone != null ? phone : "N/A");
    }
}

