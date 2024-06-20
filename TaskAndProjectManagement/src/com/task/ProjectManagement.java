package com.task;

import java.sql.*;
import java.util.Scanner;

public class ProjectManagement {
    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/task_managements";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;

        try {
            // Establish database connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database.");

            while (true) {
                displayMenu();
                int choice = getChoice(scanner);
                handleUserChoice(choice, connection, scanner);
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    // Display the main menu options
    private static void displayMenu() {
        System.out.println("1. Add Project");
        System.out.println("2. View Projects");
        System.out.println("3. Add Task");
        System.out.println("4. View Tasks for a Project");
        System.out.println("5. Update Project");
        System.out.println("6. Update Task");
        System.out.println("7. Delete Project");
        System.out.println("8. Delete Task");
        System.out.println("9. Add Employee");
        System.out.println("10. View Employees");
        System.out.println("11. Exit");
        System.out.print("Enter your choice: ");
    }

    // Get user choice from the menu
    private static int getChoice(Scanner scanner) {
        int choice = -1;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, please enter a number.");
        }
        return choice;
    }

    // Handle user choice based on menu selection
    private static void handleUserChoice(int choice, Connection connection, Scanner scanner) {
        try {
            switch (choice) {
                case 1:
                    addProject(connection, scanner);
                    break;
                case 2:
                    viewProjects(connection);
                    break;
                case 3:
                    addTask(connection, scanner);
                    break;
                case 4:
                    viewTasksForProject(connection, scanner);
                    break;
                case 5:
                    updateProject(connection, scanner);
                    break;
                case 6:
                    updateTask(connection, scanner);
                    break;
                case 7:
                    deleteProject(connection, scanner);
                    break;
                case 8:
                    deleteTask(connection, scanner);
                    break;
                case 9:
                    addEmployee(connection, scanner);
                    break;
                case 10:
                    viewEmployees(connection);
                    break;
                case 11:
                    System.out.println("Exited");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

    // Close database connection
    private static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Disconnected from the database.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Add a new project to the database
    private static void addProject(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter project name:");
        String name = scanner.nextLine();
        System.out.println("Enter project description:");
        String description = scanner.nextLine();
        System.out.println("Enter project start date (YYYY-MM-DD):");
        String startDate = scanner.nextLine();
        System.out.println("Enter project end date (YYYY-MM-DD):");
        String endDate = scanner.nextLine();

        String query = "INSERT INTO projects (name, description, start_date, end_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, startDate);
            statement.setString(4, endDate);
            statement.executeUpdate();
            System.out.println("Project added successfully!");
        }
    }

    // View all projects from the database
    private static void viewProjects(Connection connection) throws SQLException {
        String query = "SELECT * FROM projects";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                System.out.println("Project ID: " + resultSet.getInt("id"));
                System.out.println("Name: " + resultSet.getString("name"));
                System.out.println("Description: " + resultSet.getString("description"));
                System.out.println("Start Date: " + resultSet.getString("start_date"));
                System.out.println("End Date: " + resultSet.getString("end_date"));
                System.out.println("--------------------------------------------------");
            }
        }
    }

    // Add a new task to a project in the database
    private static void addTask(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter project ID:");
        int projectId = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter task name:");
        String name = scanner.nextLine();
        System.out.println("Enter task description:");
        String description = scanner.nextLine();
        System.out.println("Enter assigned team member ID:");
        int assignedTo = scanner.nextInt();
        scanner.nextLine();

        // Check if the employee exists
        if (!employeeExists(connection, assignedTo)) {
            System.out.println("No employee with this ID exists.");
            return;
        }

        System.out.println("Enter task start date (YYYY-MM-DD):");
        String startDate = scanner.nextLine();
        System.out.println("Enter task due date (YYYY-MM-DD):");
        String dueDate = scanner.nextLine();
        System.out.println("Enter task status:");
        String status = scanner.nextLine();

        String query = "INSERT INTO tasks (project_id, name, description, assigned_to, start_date, due_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectId);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setInt(4, assignedTo);
            statement.setString(5, startDate);
            statement.setString(6, dueDate);
            statement.setString(7, status);
            statement.executeUpdate();
            System.out.println("Task added successfully!");
        }
    }

    private static boolean employeeExists(Connection connection, int empId) throws SQLException {
        String query = "SELECT 1 FROM employees WHERE emp_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, empId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // If resultSet.next() returns true, the employee exists
            }
        }
    }

    // View tasks for project from database
    private static void viewTasksForProject(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter project ID to view tasks:");
        int projectId = scanner.nextInt();
        scanner.nextLine();

        String query = "SELECT * FROM tasks WHERE project_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    System.out.println("Task ID: " + resultSet.getInt("id"));
                    System.out.println("Name: " + resultSet.getString("name"));
                    System.out.println("Description: " + resultSet.getString("description"));
                    System.out.println("Assigned To: " + resultSet.getInt("assigned_to"));
                    System.out.println("Start Date: " + resultSet.getString("start_date"));
                    System.out.println("Due Date: " + resultSet.getString("due_date"));
                    System.out.println("Status: " + resultSet.getString("status"));
                    System.out.println("--------------------------------------------------");
                }
            }
        }
    }
    
    // Update Project data from database
    private static void updateProject(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter project ID to update:");
        int projectId = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter new project name:");
        String name = scanner.nextLine();
        System.out.println("Enter new project description:");
        String description = scanner.nextLine();
        System.out.println("Enter new project start date (YYYY-MM-DD):");
        String startDate = scanner.nextLine();
        System.out.println("Enter new project end date (YYYY-MM-DD):");
        String endDate = scanner.nextLine();

        String query = "UPDATE projects SET name = ?, description = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, startDate);
            statement.setString(4, endDate);
            statement.setInt(5, projectId);
            statement.executeUpdate();
            System.out.println("Project updated successfully!");
        }
    }

    // Update Task data from database
    private static void updateTask(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter task ID to update:");
        int taskId = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter new task name:");
        String name = scanner.nextLine();
        System.out.println("Enter new task description:");
        String description = scanner.nextLine();
        System.out.println("Enter new assigned team member ID:");
        int assignedTo = scanner.nextInt();
        scanner.nextLine();

        // Check if the employee exists
        if (!employeeExists(connection, assignedTo)) {
            System.out.println("No employee with this ID exists.");
            return;
        }

        System.out.println("Enter new task start date (YYYY-MM-DD):");
        String startDate = scanner.nextLine();
        System.out.println("Enter new task due date (YYYY-MM-DD):");
        String dueDate = scanner.nextLine();
        System.out.println("Enter new task status:");
        String status = scanner.nextLine();

        String query = "UPDATE tasks SET name = ?, description = ?, assigned_to = ?, start_date = ?, due_date = ?, status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setInt(3, assignedTo);
            statement.setString(4, startDate);
            statement.setString(5, dueDate);
            statement.setString(6, status);
            statement.setInt(7, taskId);
            statement.executeUpdate();
            System.out.println("Task updated successfully!");
        }
    }

    // Delete Project from database
    private static void deleteProject(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter project ID to delete:");
        int projectId = scanner.nextInt();
        scanner.nextLine();

        String query = "DELETE FROM projects WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Project deleted successfully!");
            } else {
                System.out.println("No project found with the given ID.");
            }
        }
    }

    // Delete tasks from database
    private static void deleteTask(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter task ID to delete:");
        int taskId = scanner.nextInt();
        scanner.nextLine();

        String query = "DELETE FROM tasks WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, taskId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Task deleted successfully!");
            } else {
                System.out.println("No task found with the given ID.");
            }
        }
    }
    
    // Add new employees 
    private static void addEmployee(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter employee name:");
        String name = scanner.nextLine();
        System.out.println("Enter employee position:");
        String position = scanner.nextLine();
        System.out.println("Enter employee email:");
        String email = scanner.nextLine();
        System.out.println("Enter employee phone number:");
        String phoneNo = scanner.nextLine();

        String query = "INSERT INTO employees (emp_name, position, email, phone_no) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, position);
            statement.setString(3, email);
            statement.setString(4, phoneNo);
            statement.executeUpdate();
            System.out.println("Employee added successfully!");
        }
    }

    // View employee information and which project and task they are assigned to
    private static void viewEmployees(Connection connection) throws SQLException {
        String query = "SELECT e.emp_id, e.emp_name, e.position, e.email, e.phone_no, " +
                       "t.id AS task_id, t.name AS task_name, p.id AS project_id, p.name AS project_name " +
                       "FROM employees e " +
                       "LEFT JOIN tasks t ON e.emp_id = t.assigned_to " +
                       "LEFT JOIN projects p ON t.project_id = p.id";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                System.out.println("Employee ID: " + resultSet.getInt("emp_id"));
                System.out.println("Name: " + resultSet.getString("emp_name"));
                System.out.println("Position: " + resultSet.getString("position"));
                System.out.println("Email: " + resultSet.getString("email"));
                System.out.println("Phone No: " + resultSet.getString("phone_no"));
                System.out.println("Assigned Task ID: " + resultSet.getInt("task_id"));
                System.out.println("Assigned Task Name: " + resultSet.getString("task_name"));
                System.out.println("Assigned Project ID: " + resultSet.getInt("project_id"));
                System.out.println("Assigned Project Name: " + resultSet.getString("project_name"));
                System.out.println("--------------------------------------------------");
            }
        }
    }
}
