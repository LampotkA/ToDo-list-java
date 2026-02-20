package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TodoApp {
    private static final String DATA_FILE = "tasks.json";
    private List<Task> tasks;
    private Gson gson;
    private int nextId;

    public TodoApp() {
        this.tasks = new ArrayList<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.nextId = 1;
        loadTasks();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            showMenu();
            int choice = getIntInput(scanner, "Выберите действие: ");

            switch (choice) {
                case 1 -> showTasks();
                case 2 -> addTask(scanner);
                case 3 -> deleteTask(scanner);
                case 4 -> editTask(scanner);
                case 5 -> toggleTaskStatus(scanner);
                case 6 -> saveTasksManual();
                case 0 -> {
                    saveTasks();
                    System.out.println("До свидания!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n=== МЕНЮ ===");
        System.out.println("1. Показать задачи");
        System.out.println("2. Добавить задачу");
        System.out.println("3. Удалить задачу");
        System.out.println("4. Редактировать задачу");
        System.out.println("5. Отметить выполнено/не выполнено");
        System.out.println("6. Сохранить");
        System.out.println("0. Выход");
        System.out.println("============");
    }

    private void showTasks() {
        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст.");
            return;
        }

        System.out.println("\nСписок задач:");
        System.out.println("─".repeat(60));
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(tasks.get(i));
        }
        System.out.println("─".repeat(60));

        int completedCount = 0;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).isCompleted()) {
                completedCount++;
            }
        }
        System.out.printf("Всего задач: %d (выполнено: %d, осталось: %d)%n",
                tasks.size(), completedCount, tasks.size() - completedCount);
    }

    private void addTask(Scanner scanner) {
        System.out.println("\nДобавление новой задачи:");

        String title = getStringInput(scanner, "Введите название задачи: ");
        if (title.trim().isEmpty()) {
            System.out.println("Название не может быть пустым!");
            return;
        }

        String description = getStringInput(scanner, "Введите описание задачи: ");

        Task task = new Task(nextId++, title, description);
        tasks.add(task);
        saveTasks();

        System.out.println("Задача успешно добавлена! (ID: " + task.getId() + ")");
    }

    private void deleteTask(Scanner scanner) {
        if (tasks.isEmpty()) {
            System.out.println("Нет задач для удаления.");
            return;
        }

        showTasks();
        int id = getIntInput(scanner, "Введите ID задачи для удаления: ");

        Task task = findTaskById(id);
        if (task == null) {
            System.out.println("Задача с ID " + id + " не найдена.");
            return;
        }

        String confirm = getStringInput(scanner, "Удалить задачу \"" + task.getTitle() + "\"? (да/нет): ");
        if (confirm.equalsIgnoreCase("да") || confirm.equalsIgnoreCase("yes")) {
            tasks.remove(task);
            saveTasks();
            System.out.println("Задача удалена.");
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    private void editTask(Scanner scanner) {
        if (tasks.isEmpty()) {
            System.out.println("Нет задач для редактирования.");
            return;
        }

        showTasks();
        int id = getIntInput(scanner, "Введите ID задачи для редактирования: ");

        Task task = findTaskById(id);
        if (task == null) {
            System.out.println("Задача с ID " + id + " не найдена.");
            return;
        }

        System.out.println("\nРедактирование задачи #" + id);
        System.out.println("Текущее название: " + task.getTitle());
        String newTitle = getStringInput(scanner, "Новое название (Enter чтобы оставить без изменений): ");

        System.out.println("Текущее описание: " + task.getDescription());
        String newDesc = getStringInput(scanner, "Новое описание (Enter чтобы оставить без изменений): ");

        if (!newTitle.trim().isEmpty()) {
            task.setTitle(newTitle);
        }
        if (!newDesc.trim().isEmpty()) {
            task.setDescription(newDesc);
        }

        saveTasks();
        System.out.println("Задача обновлена.");
    }

    private void toggleTaskStatus(Scanner scanner) {
        if (tasks.isEmpty()) {
            System.out.println("Нет задач.");
            return;
        }

        showTasks();
        int id = getIntInput(scanner, "Введите ID задачи: ");

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

    private void saveTasksManual() {
        if (tasks.isEmpty()) {
            System.out.println("Нет задач для сохранения.");
            return;
        }
        saveTasks();
        System.out.println("Задачи успешно сохранены в файл " + DATA_FILE);
    }

    private Task findTaskById(int id) {
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
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
                int maxId = 0;
                for (int i = 0; i < tasks.size(); i++) {
                    int taskId = tasks.get(i).getId();
                    if (taskId > maxId) {
                        maxId = taskId;
                    }
                }
                nextId = maxId + 1;
            }
        } catch (IOException e) {
            System.out.println("Ошибка загрузки: " + e.getMessage());
            tasks = new ArrayList<>();
        } catch (JsonSyntaxException e) {
            System.out.println("Ошибка: файл данных поврежден. Создан новый список задач.");
            tasks = new ArrayList<>();
        }
    }

    private int getIntInput(Scanner scanner, String prompt) {
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

    private String getStringInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static void main(String[] args) {
        System.out.println("Запуск приложения ToDo List...");
        new TodoApp().run();
    }
}
