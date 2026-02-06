package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Task {
    public boolean isCompleted;
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
        return String.format("%s #%d: %s - %s", status, id, title,
                description.length() > 30 ? description.substring(0, 30) + "..." : description);
    }
}

public class TodoApp {
    private static final String DATA_FILE = "tasks.json";
    private List<Task> tasks;
    private Scanner scanner;
    private Gson gson;
    private int nextId;

    public TodoApp() {
        this.tasks = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.nextId = 1;
        loadTasks();
    }

    public void run() {
        while (true) {
            showMenu();
            int choice = getIntInput("Выберите действие: ");

            switch (choice) {
                case 1 -> showTasks();
                case 2 -> addTask();
                case 3 -> deleteTask();
                case 4 -> editTask();
                case 5 -> toggleTaskStatus();
                case 0 -> {
                    saveTasks();
                    System.out.println("До свидания!");
                    return;
                }
                default -> System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    private void showMenu() {
        System.out.println("1. Показать задачи");
        System.out.println("2. Добавить задачу");
        System.out.println("3. Удалить задачу");
        System.out.println("4. Редактировать задачу");
        System.out.println("5. Отметить выполнено/не выполнено");
        System.out.println("0. Выход");
    }

    private void showTasks() {
        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст.");
            return;
        }

        System.out.println("\nСписок задач:");
        System.out.println("─".repeat(60));
        for (Task task : tasks) {
            System.out.println(task);
        }
        System.out.println("─".repeat(60));
        System.out.printf("Всего задач: %d (выполнено: %d, осталось: %d)%n",
                tasks.size(),
                tasks.stream().filter(Task::isCompleted).count(),
                tasks.stream().filter(t -> !t.isCompleted).count());
    }

    private void addTask() {
        System.out.println("\nДобавление новой задачи:");

        String title = getStringInput("Введите название задачи: ");
        if (title.trim().isEmpty()) {
            System.out.println("Название не может быть пустым!");
            return;
        }

        String description = getStringInput("Введите описание задачи: ");

        Task task = new Task(nextId++, title, description);
        tasks.add(task);
        saveTasks();

        System.out.println("Задача успешно добавлена! (ID: " + task.getId() + ")");
    }

    private void deleteTask() {
        if (tasks.isEmpty()) {
            System.out.println("Нет задач для удаления.");
            return;
        }

        showTasks();
        int id = getIntInput("Введите ID задачи для удаления: ");

        Task task = findTaskById(id);
        if (task == null) {
            System.out.println("Задача с ID " + id + " не найдена.");
            return;
        }

        String confirm = getStringInput("Удалить задачу \"" + task.getTitle() + "\"? (да/нет): ");
        if (confirm.equalsIgnoreCase("да") || confirm.equalsIgnoreCase("yes")) {
            tasks.remove(task);
            saveTasks();
            System.out.println("Задача удалена.");
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    private void editTask() {
        if (tasks.isEmpty()) {
            System.out.println("Нет задач для редактирования.");
            return;
        }

        showTasks();
        int id = getIntInput("Введите ID задачи для редактирования: ");

        Task task = findTaskById(id);
        if (task == null) {
            System.out.println("Задача с ID " + id + " не найдена.");
            return;
        }

        System.out.println("\nРедактирование задачи #" + id);
        System.out.println("Текущее название: " + task.getTitle());
        String newTitle = getStringInput("Новое название (Enter чтобы оставить без изменений): ");

        System.out.println("Текущее описание: " + task.getDescription());
        String newDesc = getStringInput("Новое описание (Enter чтобы оставить без изменений): ");

        if (!newTitle.trim().isEmpty()) {
            task.setTitle(newTitle);
        }
        if (!newDesc.trim().isEmpty()) {
            task.setDescription(newDesc);
        }

        saveTasks();
        System.out.println("Задача обновлена.");
    }

    private void toggleTaskStatus() {
        if (tasks.isEmpty()) {
            System.out.println("Нет задач.");
            return;
        }

        showTasks();
        int id = getIntInput("Введите ID задачи: ");

        Task task = findTaskById(id);
        if (task == null) {
            System.out.println("Задача не найдена.");
            return;
        }

        task.setCompleted(!task.isCompleted());
        saveTasks();

        String status = task.isCompleted() ? "выполнена" : "не выполнена";
        System.out.println("Задача отмечена как " + status + ".");
    }

    private Task findTaskById(int id) {
        return tasks.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
    }

    private void saveTasks() {
        try (Writer writer = new FileWriter(DATA_FILE)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void loadTasks() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        try (Reader reader = new FileReader(DATA_FILE)) {
            Type taskListType = new TypeToken<ArrayList<Task>>(){}.getType();
            List<Task> loaded = gson.fromJson(reader, taskListType);
            if (loaded != null) {
                tasks = loaded;
                nextId = tasks.stream().mapToInt(Task::getId).max().orElse(0) + 1;
            }
        } catch (IOException e) {
            System.out.println("Ошибка загрузки: " + e.getMessage());
        }
    }

    private int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static void main(String[] args) {
        System.out.println("Запуск приложения ToDo List...");
        new TodoApp().run();
    }
}