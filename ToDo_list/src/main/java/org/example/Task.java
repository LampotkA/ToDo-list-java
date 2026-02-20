package org.example;

public class Task {
    private int id;
    private String title;
    private String description;
    private boolean completed;
    private String createdAt;

    public Task(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = false;
        this.createdAt = java.time.LocalDateTime.now().toString();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        String status = completed ? "[✓]" : "[ ]";
        String shortDesc = description;
        if (description.length() > 30) {
            shortDesc = description.substring(0, 30) + "...";
        }
        return String.format("%s #%d: %s - %s", status, id, title, shortDesc);
    }
}