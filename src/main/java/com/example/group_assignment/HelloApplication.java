package com.example.group_assignment;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.collections.*;
import javafx.scene.text.*;
import javafx.scene.image.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.*;
import java.net.URI;
import java.time.LocalDate;
import javafx.scene.chart.*;
import java.util.Map;
import java.util.HashMap;
import javafx.scene.shape.Circle;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import java.util.Optional;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.stage.Modality;
import java.sql.*;
import java.util.stream.Collectors;

import javafx.scene.control.TextFormatter;



public class HelloApplication extends Application {

    private ObservableList<Course> courses = FXCollections.observableArrayList();
    private ObservableList<Student> students = FXCollections.observableArrayList();
    private ObservableList<Assessment> assessments = FXCollections.observableArrayList();
    private ObservableList<Announcement> announcements = FXCollections.observableArrayList();
    private ObservableList<Message> messages = FXCollections.observableArrayList();
    private ObservableList<Notification> notifications = FXCollections.observableArrayList();
    private ObservableList<Video> videos = FXCollections.observableArrayList();
    private ObservableList<Document> documents = FXCollections.observableArrayList();

    private Stage primaryStage;
    private String currentUserRole = "Student"; // Default role
    private String currentUserName = "Guest";

    public boolean isValidFullName(String fullName) {
        // Allows only letters and spaces
        return fullName != null && fullName.matches("[A-Za-z ]+");
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

    public boolean isValidPassword(String password) {
        // At least 8 characters, contains at least one letter
        return password != null && password.length() >= 4;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        // Test database connection
        if (DatabaseConnection.testConnection()) {
            System.out.println("Connected to the database successfully!");
            loadInitialDataFromDatabase();
            // You can proceed to load data from the database here if needed
        } else {
            showAlert("Database Error", "Unable to connect to the database. Please check your connection settings.");
            // Optionally, exit the app or continue with sample data
        }
        videos.addAll(
                new Video("Introduction to Java", "30:32", LocalDate.now().minusDays(2)),
                new Video("Object-Oriented Programming", "12:18", LocalDate.now().minusDays(5)),
                new Video("GUI Development with JavaFX", "25:45", LocalDate.now().minusDays(7))
        );

        documents.addAll(
                new Document("Syllabus.pdf", "PDF", "2.4 MB","file:///C:/Users/Mathapelo Letsoela/Documents/notes/YEAR1/programming"),
                new Document("Assignment1.docx", "Word", "1.8 MB","file:///C:/Users/Mathapelo/Path/To/Your/File.pdf"),
                new Document("LectureNotes.pdf", "PDF", "3.2 MB", "file:///C:/Users/Mathapelo Letsoela/documents/notes/YEAR1/humen computer interaction h")
        );
        documents.add(new Document("Syllabus.pdf", "PDF", "2.4 MB", "file:///C:/path/to/Syllabus.pdf"));
        showLoginPage();
    }
    private void loadInitialDataFromDatabase() {
        loadUsersFromDatabase();
        loadCoursesFromDatabase();
        loadAssessmentsFromDatabase();
        loadAnnouncementsFromDatabase();
        loadEnrollmentsFromDatabase();
        loadSubmissionsFromDatabase();

    }

    private void loadUsersFromDatabase() {
        students.clear();
        String query = "SELECT * FROM users WHERE role = 'student'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                students.add(new Student(
                        String.valueOf(rs.getInt("user_id")),
                        rs.getString("full_name"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCoursesFromDatabase() {
        courses.clear();
        String query = "SELECT * FROM courses";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
               courses.add(new Course(
                        rs.getString("course_name"),
                        rs.getString("description"),
                        0.0,
                        0.0
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAssessmentsFromDatabase() {
        assessments.clear();
        String query = "SELECT * FROM assessments";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                assessments.add(new Assessment(
                        rs.getString("title"),
                        rs.getString("type"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getInt("total_marks")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAnnouncementsFromDatabase() {
        announcements.clear();
        String query = "SELECT a.*, u.full_name FROM announcements a JOIN users u ON a.posted_by = u.user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                announcements.add(new Announcement(
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("full_name"),
                        rs.getTimestamp("posted_on").toLocalDateTime().toLocalDate()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }  private void loadEnrollmentsFromDatabase() {
        announcements.clear();
            // Example: We'll just print enrolled students for each course
            String query = "SELECT e.*, u.full_name, c.course_name FROM enrollments e "
                    + "JOIN users u ON e.user_id = u.user_id "
                    + "JOIN courses c ON e.course_id = c.course_id";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String studentName = rs.getString("full_name");
                    String courseName = rs.getString("course_name");
                    // Here, you could create Enrollment objects, or just print
                    System.out.println("Student " + studentName + " enrolled in " + courseName);
                    // Or add to a list if you have an Enrollment class:
                    // enrollments.add(new Enrollment(studentId, courseId));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
    private void loadSubmissionsFromDatabase() {
        announcements.clear();
            String query = "SELECT s.*, u.full_name, a.title FROM submissions s "
                    + "JOIN users u ON s.user_id = u.user_id "
                    + "JOIN assessments a ON s.assessment_id = a.assessment_id";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String studentName = rs.getString("full_name");
                    String assessmentTitle = rs.getString("title");
                    java.sql.Timestamp submittedOn = rs.getTimestamp("submitted_on");
                    int score = rs.getInt("score");
                    System.out.println("Student " + studentName + " submitted " + assessmentTitle
                            + " on " + submittedOn.toLocalDateTime() + " with score " + score);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    private void showLoginPage() {
        // Create the main container
        StackPane root = new StackPane();
        root.setPadding(new Insets(20));

        // Background gradient
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1a2a6c")),
                new Stop(1, Color.web("#b21f1f"))
        );
        root.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        // Create the login form container
        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(30, 50, 30, 50));
        loginBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 10;");

        // Enhanced DropShadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(15);
        dropShadow.setOffsetX(5);
        dropShadow.setOffsetY(5);
        dropShadow.setColor(Color.color(0.4, 0.5, 0.5));
        loginBox.setEffect(dropShadow);

        // Application title with reflection effect
        Label titleLabel = new Label("Sebakeng College");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#333333"));
        Reflection reflection = new Reflection();
        reflection.setFraction(0.7);
        reflection.setTopOffset(5);
        titleLabel.setEffect(reflection);

        // Login form fields
        GridPane formGrid = new GridPane();
        formGrid.setVgap(15);
        formGrid.setHgap(10);
        formGrid.setAlignment(Pos.CENTER);

        // Username field with glow effect
        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setPrefWidth(200);
        emailField.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");

        // Password field with glow effect
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefWidth(200);
        passwordField.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");

        // Role selection
        Label roleLabel = new Label("Login as:");
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        ToggleGroup roleGroup = new ToggleGroup();

        HBox roleBox = new HBox(15);
        roleBox.setAlignment(Pos.CENTER);

        RadioButton studentRadio = new RadioButton("Student");
        studentRadio.setToggleGroup(roleGroup);
        studentRadio.setSelected(true);

        RadioButton teacherRadio = new RadioButton("Lecturer");
        teacherRadio.setToggleGroup(roleGroup);

        RadioButton adminRadio = new RadioButton("Admin");
        adminRadio.setToggleGroup(roleGroup);

        roleBox.getChildren().addAll(studentRadio, teacherRadio, adminRadio);

        // Add elements to form grid
        formGrid.addRow(0, emailLabel, emailField);
        formGrid.addRow(1, passwordLabel, passwordField);
        formGrid.addRow(2, roleLabel, roleBox);

        // Login button with fade transition and glow effect
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        loginButton.setPrefWidth(150);

        // Enhanced FadeTransition for login button
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), loginButton);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.3);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();

        // Add glow effect on hover
        Glow glow = new Glow(0.8);
        loginButton.setOnMouseEntered(e -> loginButton.setEffect(glow));
        loginButton.setOnMouseExited(e -> loginButton.setEffect(null));

        // Sign up button
        Button signUpButton = new Button("Don't have an account? Sign Up");
        signUpButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2196F3; -fx-underline: true; -fx-border-width: 0;");

        // Add functionality to buttons
        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            currentUserRole = ((RadioButton) roleGroup.getSelectedToggle()).getText();
            currentUserName = email;

            if (email.isEmpty() || password.isEmpty()) {
                showAlert("Error", "Please enter both username and password");
                return;
            }

            // Simple validation for demo purposes
            if (password.length() < 4) {
                showAlert("Error", "Password must be at least 4 characters");
                return;
            }

            // Authenticate against database
            if (authenticateUser(email, password)) {
                currentUserName = email;
                // Proceed to main application
                initializeMainApplication();
            }else{
                showAlert("Error" , "Invalid username or password");
            }

        });

        // Add Enter key support for login
        EventHandler<KeyEvent> loginHandler = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire();
            }
        };
        emailField.setOnKeyPressed(loginHandler);
        passwordField.setOnKeyPressed(loginHandler);

        signUpButton.setOnAction(e -> showSignUpPage());

        // Add all elements to login box
        loginBox.getChildren().addAll(titleLabel, formGrid, loginButton, signUpButton);

        // Add login box to root
        root.getChildren().add(loginBox);

        // Create scene and show stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Learning Management System - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Returns true if a matching user was found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private void showSignUpPage() {
        // Create the sign up form container
        VBox signUpBox = new VBox(20);
        signUpBox.setAlignment(Pos.CENTER);
        signUpBox.setPadding(new Insets(30, 50, 30, 50));
        signUpBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 10;");

        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(5);
        dropShadow.setOffsetY(5);
        dropShadow.setColor(Color.color(0, 0, 0, 0.5));
        signUpBox.setEffect(dropShadow);

        // Sign up title
        Label titleLabel = new Label("Create New Account");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#333333"));

        // Sign up form fields
        GridPane formGrid = new GridPane();
        formGrid.setVgap(15);
        formGrid.setHgap(10);
        formGrid.setAlignment(Pos.CENTER);

        // Full name field
        Label fullNameLabel = new Label("Full Name:");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Enter your full name");
        fullNameField.setPrefWidth(250);

        // Email field
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setPrefWidth(250);

        // Username field
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setPrefWidth(250);

        // Password field
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Create a password");
        passwordField.setPrefWidth(250);

        // Confirm Password field
        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter your password");
        confirmPasswordField.setPrefWidth(250);

        // Role selection
        Label roleLabel = new Label("Register as:");
        ToggleGroup roleGroup = new ToggleGroup();

        HBox roleBox = new HBox(15);
        roleBox.setAlignment(Pos.CENTER);

        RadioButton studentRadio = new RadioButton("Student");
        studentRadio.setToggleGroup(roleGroup);
        studentRadio.setSelected(true);

        RadioButton lecturerRadio = new RadioButton("Lecturer");
        lecturerRadio.setToggleGroup(roleGroup);

        roleBox.getChildren().addAll(studentRadio, lecturerRadio);

        // Add elements to form grid
        formGrid.addRow(0, fullNameLabel, fullNameField);
        formGrid.addRow(1, emailLabel, emailField);
        formGrid.addRow(2, usernameLabel, usernameField);
        formGrid.addRow(3, passwordLabel, passwordField);
        formGrid.addRow(4, confirmPasswordLabel, confirmPasswordField);
        formGrid.addRow(5, roleLabel, roleBox);

        // Sign up button with fade transition
        Button signUpButton = new Button("Sign Up");
        signUpButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        signUpButton.setPrefWidth(150);

        // Add fade transition to sign up button
        FadeTransition signUpFade = new FadeTransition(Duration.seconds(1), signUpButton);
        signUpFade.setFromValue(1.0);
        signUpFade.setToValue(0.7);
        signUpFade.setCycleCount(FadeTransition.INDEFINITE);
        signUpFade.setAutoReverse(true);
        signUpFade.play();

        // Back to login button
        Button backButton = new Button("Back to Login");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2196F3; -fx-underline: true; -fx-border-width: 0;");

        // Add functionality to buttons
        signUpButton.setOnAction(e -> {
            String fullName = fullNameField.getText();
            String email = emailField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String role = ((RadioButton) roleGroup.getSelectedToggle()).getText();

            if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                showAlert("Error", "Please fill in all required fields");
                return;
            }

            if (!isValidFullName(fullName)) {
                showAlert("Invalid Input", "Full Name should contain only letters and spaces");
                return;
            }
            if (!isValidEmail(email)) {
                showAlert("Invalid Input", "Please enter a valid email address");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showAlert("Error", "Passwords do not match");
                return;
            }

            if(isValidPassword(password)){
                showAlert("Error", "Password must be at least 4 characters");
                return;
            }

            if (password.length() >= 4) {
                showAlert("Error", "Password must be at least 4 characters");
                return;
            }
            try {// Save to database
                String query = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, ?)";

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                    pstmt.setString(1, fullName);
                    pstmt.setString(2, email);
                    pstmt.setString(3, password);
                    pstmt.setString(4, role.toLowerCase());

                    int affectedRows = pstmt.executeUpdate();

                    if (affectedRows > 0) {
                        try (ResultSet rs = pstmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                String userId = "S" + rs.getInt(1);
                                Student newStudent = new Student(userId, fullName, email);
                                students.add(newStudent);

                                showNotification("Registration Successful",
                                        "Account created for " + fullName + " (" + role + ")");
                                showLoginPage();
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                showAlert("Error", "Failed to create account: " + ex.getMessage());
            }

            // Add to students list (for demo purposes)
            students.add(new Student(
                    "S" + (students.size() + 1000), // Generate ID
                    fullName,
                    email
            ));

            showNotification("Registration Successful", "Account created for " + fullName + " (" + role + ")");
            showLoginPage();
        });

        backButton.setOnAction(e -> showLoginPage());

        // Add all elements to sign up box
        signUpBox.getChildren().addAll(titleLabel, formGrid, signUpButton, backButton);

        // Replace the current scene content with sign up form
        StackPane root = (StackPane) primaryStage.getScene().getRoot();
        root.getChildren().clear();
        root.getChildren().add(signUpBox);
    }

    private void initializeMainApplication() {
        try {
            BorderPane root = new BorderPane();
            root.setStyle("-fx-font-family: 'Segoe UI'; -fx-base: #f0f0f0;");

            // Enhanced Menu Bar (4 marks)
            root.setTop(createEnhancedMenuBar());
            // Create a HBox to hold the menu bar and logout button
            HBox topContainer = new HBox();
            topContainer.setAlignment(Pos.TOP_RIGHT);
            topContainer.setPadding(new Insets(5));
            topContainer.setSpacing(10);

            // Create your existing menu bar
            MenuBar menuBar = createEnhancedMenuBar();
            // Make menuBar grow to fill space if needed
            HBox.setHgrow(menuBar, Priority.ALWAYS);
            menuBar.setMaxWidth(Double.MAX_VALUE);

            // Create a spacer Region to push the button to the right
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Create the logout button
            Button logoutBtn = new Button("Logout");
            logoutBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
            logoutBtn.setOnAction(e -> {
                showLoginPage(); // go back to login page
            });

            // Add menu bar, spacer, and logout button to the top container
            topContainer.getChildren().addAll(menuBar, spacer, logoutBtn);

            // Set this container as the top of your BorderPane
            root.setTop(topContainer);

            TabPane tabPane = new TabPane();
            tabPane.setStyle("-fx-tab-min-width: 100px;");

            // Only show tabs appropriate for the user's role
            if (currentUserRole.equals("admin") || currentUserRole.equals("lecturer")) {
                tabPane.getTabs().addAll(
                        createCoursesTab(),
                        createStudentsTab(),
                        createContentTab(),
                        createAssessmentsTab(),
                        createCommunicationTab(),
                        createCertificationTab(),
                        createReportingTab()
                          // Add this

                );
            } else { // Student
                tabPane.getTabs().addAll(
                        createCoursesTab(),
                        createContentTab(),
                        createCertificationTab(),  // If students can see certificates
                        createAssessmentsTab(),
                        createCommunicationTab(),
                        createReportingTab()
                );
            }

            root.setCenter(tabPane);

            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setTitle("Sebakeng College - " + currentUserRole + " (" + currentUserName + ")");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Show welcome notification
            showNotification("Welcome to Sebakeng College", "Welcome " + currentUserName + "! System initialized successfully");

        } catch (Exception e) {
            showAlert("Application Error", "Failed to start application: " + e.getMessage());
        }
    }

    // ==================== Menu Bar (4 marks) ====================
    private MenuBar createEnhancedMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: linear-gradient(to bottom, #3c7fb1, #2a5d84);");

        // File Menu with icons
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New", new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2965/2965288.png", 16, 16, true, true)));
        MenuItem openItem = new MenuItem("Open", new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2965/2965300.png", 16, 16, true, true)));
        MenuItem saveItem = new MenuItem("Save", new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2965/2965303.png", 16, 16, true, true)));
        MenuItem exitItem = new MenuItem("Exit", new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2965/2965309.png", 16, 16, true, true)));

        // Add functionality to file menu items
        newItem.setOnAction(e -> showNotification("New", "Create new file"));
        openItem.setOnAction(e -> showNotification("Open", "Open existing file"));
        saveItem.setOnAction(e -> showNotification("Save", "Save current file"));
        exitItem.setOnAction(e -> System.exit(0));

        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);

        // Edit Menu with icons
        Menu editMenu = new Menu("Edit");
        MenuItem cutItem = new MenuItem("Cut", new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2965/2965318.png", 16, 16, true, true)));
        MenuItem copyItem = new MenuItem("Copy", new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2965/2965319.png", 16, 16, true, true)));
        MenuItem pasteItem = new MenuItem("Paste", new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2965/2965320.png", 16, 16, true, true)));

        // Add functionality to edit menu items
        cutItem.setOnAction(e -> showNotification("Cut", "Cut selected content"));
        copyItem.setOnAction(e -> showNotification("Copy", "Copy selected content"));
        pasteItem.setOnAction(e -> showNotification("Paste", "Paste from clipboard"));

        editMenu.getItems().addAll(cutItem, copyItem, pasteItem);

        // View Menu with icons
        Menu viewMenu = new Menu("View");
        CheckMenuItem darkModeItem = new CheckMenuItem("Dark Mode");
        CheckMenuItem fullscreenItem = new CheckMenuItem("Full Screen");

        // Add functionality to view menu items
        darkModeItem.setOnAction(e -> {
            if (darkModeItem.isSelected()) {
                primaryStage.getScene().getRoot().setStyle("-fx-base: #333333; -fx-background: #222222;");
                showNotification("Dark Mode", "Dark mode enabled");
            } else {
                primaryStage.getScene().getRoot().setStyle("-fx-base: #f0f0f0; -fx-background: #ffffff;");
                showNotification("Dark Mode", "Dark mode disabled");
            }
        });

        fullscreenItem.setOnAction(e -> {
            if (fullscreenItem.isSelected()) {
                primaryStage.setFullScreen(true);
                showNotification("Full Screen", "Full screen mode enabled");
            } else {
                primaryStage.setFullScreen(false);
                showNotification("Full Screen", "Full screen mode disabled");
            }
        });

        viewMenu.getItems().addAll(darkModeItem, fullscreenItem);

        // Tools Menu with icons
        Menu toolsMenu = new Menu("Tools");
        MenuItem settingsItem = new MenuItem("Settings", new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2965/2965358.png", 16, 16, true, true)));
        MenuItem backupItem = new MenuItem("Backup", new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2965/2965368.png", 16, 16, true, true)));

        // Add functionality to tools menu items
        settingsItem.setOnAction(e -> showSettingsDialog());
        backupItem.setOnAction(e -> showNotification("Backup", "System backup created successfully"));

        toolsMenu.getItems().addAll(settingsItem, backupItem);

        // Help Menu with icons
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About", new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2965/2965370.png", 16, 16, true, true)));
        MenuItem docsItem = new MenuItem("Documentation", new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2965/2965371.png", 16, 16, true, true)));

        // Add functionality to help menu items
        aboutItem.setOnAction(e -> showAlert("About", "Enhanced Learning Management System\nVersion 1.0\n© 2023"));
        docsItem.setOnAction(e -> showNotification("Documentation", "Opening documentation in browser"));

        helpMenu.getItems().addAll(aboutItem, docsItem);

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, toolsMenu, helpMenu);
        return menuBar;
    }

    private void showSettingsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Settings");
        dialog.setHeaderText("System Configuration");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField serverField = new TextField();
        serverField.setPromptText("Server URL");
        TextField portField = new TextField();
        portField.setPromptText("Port");
        CheckBox sslCheck = new CheckBox("Use SSL");

        grid.add(new Label("Server:"), 0, 0);
        grid.add(serverField, 1, 0);
        grid.add(new Label("Port:"), 0, 1);
        grid.add(portField, 1, 1);
        grid.add(sslCheck, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait();
    }

    // ==================== Course Management (10 marks) ====================
    private Tab createCoursesTab() {
        Tab coursesTab = new Tab("Courses");
        coursesTab.setClosable(false);

        VBox coursesContent = new VBox(15);
        coursesContent.setPadding(new Insets(15));
        coursesContent.setStyle("-fx-background-color: #f5f5f5;");

        // Header with title and search
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Course Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2a5d84;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search courses...");
        searchField.setPrefWidth(250);

        headerBox.getChildren().addAll(titleLabel, searchField);

        // Course statistics section
        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: white; -fx-background-radius: 5; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-border-width: 1px;");

        Label statsTitle = new Label("Course Statistics");
        statsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Add your stat boxes here
        HBox statsRow = new HBox(20);
        VBox totalCourses = createStatBox("Total Courses", String.valueOf(courses.size()), "#2196F3");
        VBox activeCourses = createStatBox("Active Courses", "3", "#4CAF50"); // Example value
        VBox avgCompletion = createStatBox("Avg Completion", "72%", "#FF9800");

        statsRow.getChildren().addAll(totalCourses, activeCourses, avgCompletion);
        statsBox.getChildren().addAll(statsTitle, statsRow);

        // Pagination setup
        int itemsPerPage = 5;
        int totalPages = (int) Math.ceil(courses.size() / (double) itemsPerPage);
        if (totalPages == 0) totalPages = 1; // Handle case when no courses exist

        Pagination coursePagination = new Pagination(totalPages, 0);
        coursePagination.setPageFactory(pageIndex -> {
            VBox pageBox = new VBox(10);
            int startIdx = pageIndex * itemsPerPage;
            int endIdx = Math.min(startIdx + itemsPerPage, courses.size());

            for (int i = startIdx; i < endIdx; i++) {
                pageBox.getChildren().add(createCourseCard(courses.get(i)));
            }

            ScrollPane sp = new ScrollPane(pageBox);
            sp.setFitToWidth(true);
            sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            return sp;
        });

        VBox coursesContainer = new VBox(10, coursePagination);

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter courses based on search
            List<Course> filteredCourses = courses.stream()
                    .filter(c -> c.getName().toLowerCase().contains(newValue.toLowerCase()) ||
                            c.getDescription().toLowerCase().contains(newValue.toLowerCase()))
                    .collect(Collectors.toList());

            int newTotalPages = (int) Math.ceil(filteredCourses.size() / (double) itemsPerPage);
            if (newTotalPages == 0) newTotalPages = 1;

            coursePagination.setPageCount(newTotalPages);
            coursePagination.setCurrentPageIndex(0); // Reset to first page

            coursePagination.setPageFactory(pageIndex -> {
                VBox pageBox = new VBox(10);
                int startIdx = pageIndex * itemsPerPage;
                int endIdx = Math.min(startIdx + itemsPerPage, filteredCourses.size());
                for (int i = startIdx; i < endIdx; i++) {
                    pageBox.getChildren().add(createCourseCard(filteredCourses.get(i)));
                }
                ScrollPane sp = new ScrollPane(pageBox);
                sp.setFitToWidth(true);
                sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
                return sp;
            });
        });

        // Only show add course form for teachers/admins
        if (currentUserRole.equals("Teacher") || currentUserRole.equals("Admin")) {
            // Add Course Form
            VBox addCourseBox = new VBox(15);
            addCourseBox.setPadding(new Insets(15));
            addCourseBox.setStyle("-fx-background-color: white; -fx-background-radius: 5; " +
                    "-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-border-width: 1px;");

            Label addTitle = new Label("Add New Course");
            addTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2a5d84;");

            GridPane courseForm = new GridPane();
            courseForm.setVgap(10);
            courseForm.setHgap(10);

            TextField nameField = new TextField();
            nameField.setPromptText("Course Name");
            TextArea descriptionField = new TextArea();
            descriptionField.setPromptText("Description");
            descriptionField.setPrefHeight(60);
            DatePicker startDatePicker = new DatePicker();
            startDatePicker.setPromptText("Start Date");
            DatePicker endDatePicker = new DatePicker();
            endDatePicker.setPromptText("End Date");
            TextField lecturerField = new TextField();
            lecturerField.setPromptText("Lecturer Name");

            courseForm.addRow(0, new Label("Course Name:"), nameField);
            courseForm.addRow(1, new Label("Description:"), descriptionField);
            courseForm.addRow(2, new Label("Start Date:"), startDatePicker);
            courseForm.addRow(3, new Label("End Date:"), endDatePicker);
            courseForm.addRow(4, new Label("Lecturer:"), lecturerField);

            Button saveBtn = new Button("Save Course");
            saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

            saveBtn.setOnAction(e -> {
                if (nameField.getText().isEmpty()) {
                    showAlert("Error", "Course name is required");
                    return;
                }

                Course newCourse = new Course(
                        nameField.getText(),
                        descriptionField.getText(),
                        0.0, // initial progress
                        0.0  // initial average grade
                );
                newCourse.saveToDatabase();
                courses.add(newCourse);

                // Recalculate pagination when data changes
                int updatedTotalPages = (int) Math.ceil(courses.size() / (double) itemsPerPage);
                if (updatedTotalPages == 0) updatedTotalPages = 1;

                coursePagination.setPageCount(updatedTotalPages);
                coursePagination.setCurrentPageIndex(0);

                // Refresh the current page
                coursePagination.setPageFactory(pageIndex -> {
                    VBox pageBox = new VBox(10);
                    int startIdx = pageIndex * itemsPerPage;
                    int endIdx = Math.min(startIdx + itemsPerPage, courses.size());
                    for (int i = startIdx; i < endIdx; i++) {
                        pageBox.getChildren().add(createCourseCard(courses.get(i)));
                    }
                    ScrollPane sp = new ScrollPane(pageBox);
                    sp.setFitToWidth(true);
                    sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
                    return sp;
                });

                // Clear form
                nameField.clear();
                descriptionField.clear();
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
                lecturerField.clear();

                showNotification("Course Saved", newCourse.getName() + " has been added");
            });

            addCourseBox.getChildren().addAll(addTitle, courseForm, saveBtn);
            coursesContent.getChildren().addAll(headerBox, statsBox, coursesContainer, addCourseBox);
        } else {
            coursesContent.getChildren().addAll(headerBox, statsBox, coursesContainer);
        }

        // Add announcements section for all users
        coursesContent.getChildren().add(createAnnouncementsSection());

        coursesTab.setContent(coursesContent);
        return coursesTab;
    }

    private VBox createStatBox(String title, String value, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 5;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    // Helper method to create course cards
    private VBox createCourseCard(Course course) {
        VBox courseCard = new VBox(10);
        courseCard.setPadding(new Insets(15));
        courseCard.setStyle("-fx-background-color: white; -fx-background-radius: 5; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-border-width: 1px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");

        // Course title with icon
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        ImageView courseIcon = new ImageView(new Image("https://cdn-icons-png.flaticon.com/512/2232/2232688.png"));
        courseIcon.setFitHeight(24);
        courseIcon.setFitWidth(24);

        Label courseName = new Label(course.getName());
        courseName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        titleBox.getChildren().addAll(courseIcon, courseName);

        // Course description
        Label description = new Label(course.getDescription());
        description.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
        description.setWrapText(true);

        // Progress bar with percentage and indicator
        HBox progressBox = new HBox(10);
        progressBox.setAlignment(Pos.CENTER_LEFT);

        ProgressBar progressBar = new ProgressBar(course.getProgress());
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: linear-gradient(to right, #4CAF50, #8BC34A); " +
                "-fx-background-insets: 0; -fx-border-radius: 5; -fx-padding: 2px;");

        ProgressIndicator progressIndicator = new ProgressIndicator(course.getProgress());
        progressIndicator.setPrefSize(30, 30);
        progressIndicator.setStyle("-fx-progress-color: #4CAF50;");

        Label progressLabel = new Label(String.format("%.0f%%", course.getProgress() * 100));
        progressLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        progressBox.getChildren().addAll(new Label("Progress:"), progressBar, progressIndicator, progressLabel);

        // Average grade
        Label gradeLabel = new Label("Average Grade: " + String.format("%.1f", course.getAverageGrade()) + "%");
        gradeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2196F3; -fx-font-weight: bold;");

        // Only show edit/delete buttons for teachers/admins
        if (currentUserRole.equals("Lecturer") || currentUserRole.equals("Admin")) {
            HBox buttonBox = new HBox(10);
            Button editBtn = new Button("Edit");
            Button deleteBtn = new Button("Delete");

            editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
            deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");

            editBtn.setOnAction(e -> showCourseEditDialog(course));
            deleteBtn.setOnAction(e -> {
                if (showConfirmation("Delete Course", "Are you sure you want to delete " + course.getName() + "?")) {
                    course.deleteFromDatabase();
                    courses.remove(course);
                    showNotification("Course Deleted", course.getName() + " has been removed");
                }
            });

            buttonBox.getChildren().addAll(editBtn, deleteBtn);
            courseCard.getChildren().add(buttonBox);
        }

        courseCard.getChildren().addAll(titleBox, description, progressBox, gradeLabel);
        return courseCard;
    }

    private void showCourseEditDialog(Course course) {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        dialog.setHeaderText("Edit Course Details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(course.getName());
        TextArea descriptionField = new TextArea(course.getDescription());
        descriptionField.setPrefHeight(60);
        TextField progressField = new TextField(String.valueOf(course.getProgress()));
        TextField gradeField = new TextField(String.valueOf(course.getAverageGrade()));

        grid.add(new Label("Course Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Progress (0-1):"), 0, 2);
        grid.add(progressField, 1, 2);
        grid.add(new Label("Average Grade:"), 0, 3);
        grid.add(gradeField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String originalName = course.getName(); // Store original name for update
                    course.setName(nameField.getText());
                    course.setDescription(descriptionField.getText());
                    course.setProgress(Double.parseDouble(progressField.getText()));
                    course.setAverageGrade(Double.parseDouble(gradeField.getText()));

                   course.updateInDatabase();
                    return course;
                } catch (NumberFormatException e) {
                    showAlert("Error", "Please enter valid numbers for progress and grade");
                    return null;
                }
            }
            return null;
        });

        Optional<Course> result = dialog.showAndWait();
        result.ifPresent(updatedCourse -> {
            showNotification("Course Updated", updatedCourse.getName() + " has been updated");
        });
    }

    private VBox createAnnouncementsSection() {
        VBox announcementsBox = new VBox(10);
        announcementsBox.setPadding(new Insets(15));
        announcementsBox.setStyle("-fx-border-color: #dddddd; -fx-border-width: 1px; -fx-padding: 10px;");

        ListView<Announcement> announcementsList = new ListView<>(announcements);
        announcementsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Announcement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                    HBox metaBox = new HBox(10);
                    Label authorLabel = new Label("By: " + item.getAuthor());
                    Label dateLabel = new Label(item.getDate().toString());
                    authorLabel.setStyle("-fx-font-size: 10px;");
                    dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                    metaBox.getChildren().addAll(authorLabel, dateLabel);

                    Label contentLabel = new Label(item.getContent());
                    contentLabel.setWrapText(true);

                    box.getChildren().addAll(titleLabel, metaBox, contentLabel);
                    setGraphic(box);
                }
            }
        });

        // Only show post announcement controls for teachers/admins
        if (currentUserRole.equals("Lecturer") || currentUserRole.equals("Admin")) {
            TextField announcementTitle = new TextField();
            announcementTitle.setPromptText("Title");
            TextArea announcementContent = new TextArea();
            announcementContent.setPromptText("Content");
            announcementContent.setPrefHeight(60);

            Button postBtn = new Button("Post Announcement");
            postBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

            postBtn.setOnAction(e -> {
                if (announcementTitle.getText().isEmpty() || announcementContent.getText().isEmpty()) {
                    showAlert("Error", "Title and content are required");
                    return;
                }

                Announcement newAnnouncement = new Announcement(
                        announcementTitle.getText(),
                        announcementContent.getText(),
                        currentUserRole.equals("Lecturer") ? "Lecturer" : "Admin",
                        LocalDate.now()
                );

                announcements.add(newAnnouncement);
                announcementTitle.clear();
                announcementContent.clear();

                showNotification("Announcement Posted", "New announcement has been posted");
            });

            announcementsBox.getChildren().addAll(
                    new Label("Course Announcements"),
                    announcementsList,
                    new Separator(),
                    announcementTitle,
                    announcementContent,
                    postBtn
            );
        } else {
            announcementsBox.getChildren().addAll(
                    new Label("Course Announcements"),
                    announcementsList
            );
        }

        return announcementsBox;
    }

    // ==================== Content Management (11 marks) ====================
    private Tab createContentTab() {
        Tab contentTab = new Tab("Content");
        contentTab.setClosable(false);

        // Add tabbed interface for different content types
        TabPane contentTabs = new TabPane();

        Tab videosTab = new Tab("Videos", createVideoContent());
        Tab docsTab = new Tab("Documents", createDocumentContent());
        Tab forumTab = new Tab("Discussion", createForumContent());

        contentTabs.getTabs().addAll(videosTab, docsTab, forumTab);

        if (currentUserRole.equals("Lecturer") || currentUserRole.equals("Admin")) {
            Tab uploadTab = new Tab("Upload", createUploadSection());
            contentTabs.getTabs().add(uploadTab);
        }

        VBox contentBox = new VBox(15, contentTabs);
        contentBox.setPadding(new Insets(15));
        contentTab.setContent(contentBox);

        return contentTab;
    }

    private VBox createVideoContent() {
        VBox videoBox = new VBox(10);
        videoBox.setPadding(new Insets(10));

        TableView<Video> videoTable = new TableView<>();
        videoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Video, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Video, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));

        TableColumn<Video, String> dateCol = new TableColumn<>("Upload Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));

        videoTable.getColumns().addAll(titleCol, durationCol, dateCol);
        videoTable.setItems(videos);

        Button playBtn = new Button("Play Selected");
        playBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        playBtn.setOnAction(e -> {
            Video selected = videoTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showVideoPlayer(selected);
            } else {
                showAlert("Error", "Please select a video to play");
            }
        });

        videoBox.getChildren().addAll(
                new Label("Available Video Lectures"),
                videoTable,
                playBtn
        );
        return videoBox;
    }
    private void showVideoPlayer(Video video) {
        Stage videoStage = new Stage();
        videoStage.setTitle("Video Player: " + video.getTitle());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(video.getTitle());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Create actual media player
        MediaView mediaView = new MediaView();

        // Create controls
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);

        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Button stopButton = new Button("Stop");
        Slider volumeSlider = new Slider(0, 1, 0.5);
        Slider timeSlider = new Slider();

        controls.getChildren().addAll(playButton, pauseButton, stopButton,
                new Label("Volume:"), volumeSlider);

        // File chooser for selecting video
        Button chooseFileButton = new Button("Choose Video File");
        chooseFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Video File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.flv", "*.m4v")
            );
            File selectedFile = fileChooser.showOpenDialog(videoStage);
            if (selectedFile != null) {
                Media media = new Media(selectedFile.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);

                // Bind media player properties to controls
                volumeSlider.valueProperty().bindBidirectional(mediaPlayer.volumeProperty());

                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (!timeSlider.isValueChanging()) {
                        timeSlider.setValue(newTime.toSeconds() / mediaPlayer.getTotalDuration().toSeconds() * 100);
                    }
                });

                timeSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                    if (!isChanging) {
                        mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(timeSlider.getValue() / 100.0));
                    }
                });

                playButton.setOnAction(ev -> mediaPlayer.play());
                pauseButton.setOnAction(ev -> mediaPlayer.pause());
                stopButton.setOnAction(ev -> mediaPlayer.stop());
            }
        });

        Label infoLabel = new Label(String.format("Duration: %s | Uploaded: %s",
                video.getDuration(), video.getUploadDate()));

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> videoStage.close());

        vbox.getChildren().addAll(
                titleLabel,
                chooseFileButton,
                mediaView,
                controls,
                timeSlider,
                infoLabel,
                closeBtn
        );

        Scene scene = new Scene(vbox, 800, 600);
        videoStage.setScene(scene);
        videoStage.show();
    }
    private VBox createDocumentContent() {
        VBox docBox = new VBox(10);
        docBox.setPadding(new Insets(10));

        TableView<Document> docTable = new TableView<>();
        docTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Document, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Document, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Document, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));

        docTable.getColumns().addAll(nameCol, typeCol, sizeCol);
        docTable.setItems(documents);

        HBox docButtons = new HBox(10);
        Button viewBtn = new Button("View");
        Button downloadBtn = new Button("Download");
        viewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        downloadBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");

        viewBtn.setOnAction(e -> {
            Document selected = docTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showDocumentViewer(selected);
            } else {
                showAlert("Error", "Please select a document to view");
            }
        });

        downloadBtn.setOnAction(e -> {
            Document selected = docTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showDownloadProgress(selected);
            } else {
                showAlert("Error", "Please select a document to download");
            }
        });

        docButtons.getChildren().addAll(viewBtn, downloadBtn);

        docBox.getChildren().addAll(
                new Label("Available Documents"),
                docTable,
                docButtons
        );
        return docBox;
    }

    private void showDocumentViewer(Document doc) {
        Stage docStage = new Stage();
        docStage.setTitle("Document Viewer: " + doc.getName());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(doc.getName());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        String filePath = doc.getFilePath(); // e.g., "file:///C:/path/to/file.pdf"

        try {
            URI uri = new URI(filePath);
            File file = new File(uri);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert("Error", "Desktop operations are not supported on this system.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unable to open document: " + e.getMessage());
        }
        Label infoLabel = new Label(String.format("Type: %s | Size: %s",
                doc.getType(), doc.getSize()));

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> docStage.close());

        vbox.getChildren().addAll(titleLabel, infoLabel, closeBtn);

        Scene scene = new Scene(vbox);
        docStage.setScene(scene);
        docStage.show();
    }

    private void showDownloadProgress(Document doc) {
        Stage downloadStage = new Stage();
        downloadStage.setTitle("Downloading: " + doc.getName());

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Downloading: " + doc.getName());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);

        Label progressLabel = new Label("0%");

        // Simulate download progress
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(3), new KeyValue(progressBar.progressProperty(), 1))
        );

        timeline.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            double progress = newTime.toSeconds() / 3.0;
            progressLabel.setText(String.format("%.0f%%", progress * 100));
        });

        timeline.setOnFinished(e -> {
            showNotification("Download Complete", doc.getName() + " has been downloaded");
            downloadStage.close();
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> {
            timeline.stop();
            downloadStage.close();
        });

        vbox.getChildren().addAll(titleLabel, progressBar, progressLabel, cancelBtn);

        Scene scene = new Scene(vbox);
        downloadStage.setScene(scene);
        downloadStage.show();
        timeline.play();
    }

    private VBox createForumContent() {
        VBox forumBox = new VBox(10);
        forumBox.setPadding(new Insets(10));

        ListView<Message> messagesList = new ListView<>(messages);
        messagesList.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    HBox header = new HBox(10);
                    Label authorLabel = new Label(item.getAuthor());
                    authorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    Label dateLabel = new Label(item.getTimestamp());
                    dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                    header.getChildren().addAll(authorLabel, dateLabel);

                    Label contentLabel = new Label(item.getContent());
                    contentLabel.setWrapText(true);

                    box.getChildren().addAll(header, contentLabel);
                    setGraphic(box);
                }
            }
        });

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Type your message here...");
        messageArea.setPrefHeight(80);

        Button sendBtn = new Button("Send Message");
        sendBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        sendBtn.setOnAction(e -> {
            if (messageArea.getText().isEmpty()) {
                showAlert("Error", "Message cannot be empty");
                return;
            }

            Message newMessage = new Message(
                    currentUserName,
                    LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")),
                    messageArea.getText()
            );

            messages.add(newMessage);
            messageArea.clear();
            messagesList.scrollTo(messages.size() - 1);
        });

        // Add keyboard shortcut (Ctrl+Enter) to send message
        messageArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
                sendBtn.fire();
            }
        });

        forumBox.getChildren().addAll(
                new Label("Discussion Forum"),
                messagesList,
                messageArea,
                sendBtn
        );

        return forumBox;
    }

    private VBox createUploadSection() {
        VBox uploadBox = new VBox(15);
        uploadBox.setPadding(new Insets(15));

        ChoiceBox<String> contentType = new ChoiceBox<>(
                FXCollections.observableArrayList("Video", "Document", "Image", "Other")
        );
        contentType.setValue("Document");

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefHeight(60);

        // Add file type filtering
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF Files", "*.pdf");
        FileChooser.ExtensionFilter videoFilter = new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mov", "*.avi");
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg");

        Button browseBtn = new Button("Browse Files");
        Button uploadBtn = new Button("Upload");
        uploadBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        ProgressBar uploadProgress = new ProgressBar(0);
        uploadProgress.setPrefWidth(300);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");

        browseBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File ");

            // Set filters based on selected content type
            String selectedType = contentType.getValue();
            if (selectedType.equals("Document")) {
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf", "*.docx", "*.txt"));
            } else if (selectedType.equals("Video")) {
                fileChooser.getExtensionFilters().add(videoFilter);
            } else if (selectedType.equals("Image")) {
                fileChooser.getExtensionFilters().add(imageFilter);
            }

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                try {
                    // Copy file to uploads directory
                    File uploadsDir = new File("uploads");
                    if (!uploadsDir.exists()) uploadsDir.mkdirs();
                    File destFile = new File(uploadsDir, selectedFile.getName());
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // save the  file path in the Document object
                    String filePath = destFile.toURI().toString();

                    // Add to documents list
                    documents.add(new Document(
                            selectedFile.getName(),
                            selectedType,
                            "Size: " + selectedFile.length() / (1024 * 1024) + " MB",
                            filePath
                    ));
                    showNotification("File Selected", "Selected: " + selectedFile.getName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Error", "File upload failed: " + ex.getMessage());
                }
            }
        });

        uploadBtn.setOnAction(e -> {
            if (titleField.getText().isEmpty()) {
                showAlert("Error", "Title is required");
                return;
            }

            if (contentType.getValue() == null) {
                showAlert("Error", "Please select a content type");
                return;
            }

            statusLabel.setText("Uploading...");

            // Simulate upload progress
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(uploadProgress.progressProperty(), 0)),
                    new KeyFrame(Duration.seconds(2), new KeyValue(uploadProgress.progressProperty(), 1))
            );
            timeline.play();

            timeline.setOnFinished(event -> {
                statusLabel.setText("Upload complete!");

                // Add to appropriate list based on content type
                String type = contentType.getValue();
                if (type.equals("Video")) {
                    videos.add(new Video(
                            titleField.getText(),
                            "00:00", // Placeholder duration
                            LocalDate.now()
                    ));
                }

                showNotification("Upload Complete", titleField.getText() + " has been uploaded");
                titleField.clear();
                descriptionField.clear();
                contentType.setValue(null);
                uploadProgress.setProgress(0);
                statusLabel.setText("");
            });
        });

        HBox buttonBox = new HBox(10, browseBtn, uploadBtn);

        uploadBox.getChildren().addAll(
                new Label("Upload New Content"),
                contentType,
                titleField,
                descriptionField,
                buttonBox,
                uploadProgress,
                statusLabel
        );

        return uploadBox;
    }

    // ==================== Assessments (15 marks) ====================
    private Tab createAssessmentsTab() {
        Tab assessmentsTab = new Tab("Assessments");
        assessmentsTab.setClosable(false);

        VBox assessmentsBox = new VBox(15);
        assessmentsBox.setPadding(new Insets(15));

        // Assessment Creation
        TitledPane createPane = new TitledPane("Create Assessment", createAssessmentCreationPane());
        styleTitledPane(createPane);

        // Assessment List
        TitledPane listPane = new TitledPane("Current Assessments", createAssessmentListPane());
        styleTitledPane(listPane);

        // Grading
        TitledPane gradingPane = new TitledPane("Grading Center", createGradingPane());
        styleTitledPane(gradingPane);

        assessmentsBox.getChildren().addAll(createPane, listPane, gradingPane);
        assessmentsTab.setContent(assessmentsBox);
        return assessmentsTab;
    }

    private VBox createAssessmentCreationPane() {
        VBox createBox = new VBox(15);
        createBox.setPadding(new Insets(15));

        // Assessment type selection with icons
        ToggleGroup assessmentTypeGroup = new ToggleGroup();

        HBox typeSelection = new HBox(10);
        typeSelection.setAlignment(Pos.CENTER);

        for (String type : new String[]{"Quiz", "Assignment", "Exam", "Project"}) {
            VBox typeBox = new VBox(5);
            typeBox.setAlignment(Pos.CENTER);
            typeBox.setPadding(new Insets(10));
            typeBox.setStyle("-fx-border-color: #dddddd; -fx-border-radius: 5;");

            ImageView icon = new ImageView();
            icon.setFitWidth(30);
            icon.setFitHeight(30);

            // Set appropriate icon based on type
            switch (type) {
                case "Quiz":
                    icon.setImage(new Image("https://cdn-icons-png.flaticon.com/512/2232/2232688.png"));
                    break;
                case "Assignment":
                    icon.setImage(new Image("https://cdn-icons-png.flaticon.com/512/3652/3652191.png"));
                    break;
                case "Exam":
                    icon.setImage(new Image("https://cdn-icons-png.flaticon.com/512/2936/2936886.png"));
                    break;
                case "Project":
                    icon.setImage(new Image("https://cdn-icons-png.flaticon.com/512/2936/2936881.png"));
                    break;
            }

            RadioButton radio = new RadioButton(type);
            radio.setToggleGroup(assessmentTypeGroup);

            typeBox.getChildren().addAll(icon, radio);
            typeSelection.getChildren().add(typeBox);
        }

        TextField titleField = new TextField();
        titleField.setPromptText("Assessment Title");

        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setValue(LocalDate.now().plusDays(7));

        TextField weightField = new TextField("10");
        weightField.setPromptText("Weight (%)");

        TextArea instructionsArea = new TextArea();
        instructionsArea.setPromptText("Instructions");
        instructionsArea.setPrefHeight(80);

        // Multiple Choice Questions Section
        VBox mcqSection = new VBox(10);
        mcqSection.setPadding(new Insets(10));
        mcqSection.setStyle("-fx-border-color: #dddddd; -fx-border-width: 1px;");

        Label mcqTitle = new Label("Multiple Choice Questions");
        mcqTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        ListView<MCQuestion> mcqList = new ListView<>();
        mcqList.setPrefHeight(150);
        mcqList.setCellFactory(param -> new ListCell<MCQuestion>() {
            @Override
            protected void updateItem(MCQuestion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    Label questionLabel = new Label(item.getQuestion());
                    questionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                    VBox optionsBox = new VBox(5);
                    for (int i = 0; i < item.getOptions().length; i++) {
                        HBox optionBox = new HBox(5);
                        Label optionLabel = new Label((i + 1) + ". " + item.getOptions()[i]);
                        if (i == item.getCorrectAnswer()) {
                            optionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
                        }
                        optionBox.getChildren().add(optionLabel);
                        optionsBox.getChildren().add(optionBox);
                    }

                    box.getChildren().addAll(questionLabel, optionsBox);
                    setGraphic(box);
                }
            }
        });

        TextField questionField = new TextField();
        questionField.setPromptText("Question Text");

        GridPane optionsGrid = new GridPane();
        optionsGrid.setVgap(5);
        optionsGrid.setHgap(10);
        TextField[] optionFields = new TextField[4];
        ToggleGroup correctAnswerGroup = new ToggleGroup();
        RadioButton[] optionButtons = new RadioButton[4];

        for (int i = 0; i < 4; i++) {
            optionFields[i] = new TextField();
            optionFields[i].setPromptText("Option " + (i + 1));
            optionButtons[i] = new RadioButton("Correct");
            optionButtons[i].setToggleGroup(correctAnswerGroup);

            optionsGrid.addRow(i, optionButtons[i], optionFields[i]);
        }

        Button addQuestionBtn = new Button("Add Question");
        addQuestionBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        addQuestionBtn.setOnAction(e -> {
            if (questionField.getText().isEmpty()) {
                showAlert("Error", "Question text is required");
                return;
            }

            String[] options = new String[4];
            int correctIndex = -1;

            for (int i = 0; i < 4; i++) {
                if (optionFields[i].getText().isEmpty()) {
                    showAlert("Error", "All options must be filled");
                    return;
                }
                options[i] = optionFields[i].getText();
                if (optionButtons[i].isSelected()) {
                    correctIndex = i;
                }
            }

            if (correctIndex == -1) {
                showAlert("Error", "Please select the correct answer");
                return;
            }

            MCQuestion newQuestion = new MCQuestion(
                    questionField.getText(),
                    options,
                    correctIndex
            );

            mcqList.getItems().add(newQuestion);
            questionField.clear();
            for (TextField field : optionFields) {
                field.clear();
            }
            correctAnswerGroup.selectToggle(null);
        });

        // Save Assessment Button
        Button saveAssessmentBtn = new Button("Save Assessment");
        saveAssessmentBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        saveAssessmentBtn.setOnAction(e -> {
            if (titleField.getText().isEmpty()) {
                showAlert("Error", "Assessment title is required");
                return;
            }

            if (assessmentTypeGroup.getSelectedToggle() == null) {
                showAlert("Error", "Please select an assessment type");
                return;
            }

            if (dueDatePicker.getValue() == null) {
                showAlert("Error", "Due date is required");
                return;
            }

            try {
                int weight = Integer.parseInt(weightField.getText());
                if (weight <= 0 || weight > 100) {
                    showAlert("Error", "Weight must be between 1 and 100");
                    return;
                }

                Assessment newAssessment = new Assessment(
                        titleField.getText(),
                        ((RadioButton) assessmentTypeGroup.getSelectedToggle()).getText(),
                        dueDatePicker.getValue(),
                        weight
                );

                assessments.add(newAssessment);

                // Clear form
                titleField.clear();
                assessmentTypeGroup.selectToggle(null);
                dueDatePicker.setValue(null);
                weightField.clear();
                instructionsArea.clear();
                mcqList.getItems().clear();

                showNotification("Assessment Saved", newAssessment.getTitle() + " has been created");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Weight must be a number");
            }
        });

        mcqSection.getChildren().addAll(mcqTitle, mcqList);

        createBox.getChildren().addAll(
                typeSelection,
                titleField,
                new HBox(10, new Label("Due Date:"), dueDatePicker),
                new HBox(10, new Label("Weight (%):"), weightField),
                instructionsArea,
                new Separator(),
                mcqSection,
                questionField,
                optionsGrid,
                addQuestionBtn,
                new Separator(),
                saveAssessmentBtn
        );

        return createBox;
    }

    private VBox createAssessmentListPane() {
        VBox listBox = new VBox(10);
        listBox.setPadding(new Insets(15));

        TableView<Assessment> assessmentTable = new TableView<>();
        assessmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Assessment, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Assessment, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Assessment, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        TableColumn<Assessment, String> weightCol = new TableColumn<>("Weight");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));

        assessmentTable.getColumns().addAll(titleCol, typeCol, dueDateCol, weightCol);
        assessmentTable.setItems(assessments);

        HBox buttonBox = new HBox(10);
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button publishBtn = new Button("Publish");

        editBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
        publishBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        editBtn.setOnAction(e -> {
            Assessment selected = assessmentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAssessmentEditDialog(selected);
            } else {
                showAlert("Error", "Please select an assessment to edit");
            }
        });

        deleteBtn.setOnAction(e -> {
            Assessment selected = assessmentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (showConfirmation("Delete Assessment",
                        "Are you sure you want to delete " + selected.getTitle() + "?")) {
                    assessments.remove(selected);
                    showNotification("Assessment Deleted", selected.getTitle() + " has been removed");
                }
            } else {
                showAlert("Error", "Please select an assessment to delete");
            }
        });

        publishBtn.setOnAction(e -> {
            Assessment selected = assessmentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showNotification("Assessment Published", selected.getTitle() + " has been published to students");
            } else {
                showAlert("Error", "Please select an assessment to publish");
            }
        });

        buttonBox.getChildren().addAll(editBtn, deleteBtn, publishBtn);

        listBox.getChildren().addAll(
                new Label("Current Assessments"),
                assessmentTable,
                buttonBox
        );
        return listBox;
    }

    private void showAssessmentEditDialog(Assessment assessment) {
        Dialog<Assessment> dialog = new Dialog<>();
        dialog.setTitle("Edit Assessment");
        dialog.setHeaderText("Edit Assessment Details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(assessment.getTitle());
        ChoiceBox<String> typeSelector = new ChoiceBox<>(
                FXCollections.observableArrayList("Quiz", "Assignment", "Exam", "Project")
        );
        typeSelector.setValue(assessment.getType());
        DatePicker dueDatePicker = new DatePicker(assessment.getDueDate());
        TextField weightField = new TextField(String.valueOf(assessment.getWeight()));

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeSelector, 1, 1);
        grid.add(new Label("Due Date:"), 0, 2);
        grid.add(dueDatePicker, 1, 2);
        grid.add(new Label("Weight (%):"), 0, 3);
        grid.add(weightField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    assessment.setTitle(titleField.getText());
                    assessment.setType(typeSelector.getValue());
                    assessment.setDueDate(dueDatePicker.getValue());
                    assessment.setWeight(Integer.parseInt(weightField.getText()));
                    return assessment;
                } catch (NumberFormatException e) {
                    showAlert("Error", "Weight must be a number");
                    return null;
                }
            }
            return null;
        });

        Optional<Assessment> result = dialog.showAndWait();
        result.ifPresent(updatedAssessment -> {
            showNotification("Assessment Updated", updatedAssessment.getTitle() + " has been updated");
        });
    }

    private VBox createGradingPane() {
        VBox gradingBox = new VBox(15);
        gradingBox.setPadding(new Insets(15));

        // Student Selection
        HBox studentSelection = new HBox(10);
        ChoiceBox<Assessment> assessmentChoice = new ChoiceBox<>(assessments);
        ChoiceBox<Student> studentChoice = new ChoiceBox<>(students);
        Button loadBtn = new Button("Load Submission");
        loadBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        loadBtn.setOnAction(e -> {
            if (assessmentChoice.getValue() == null || studentChoice.getValue() == null) {
                showAlert("Error", "Please select both an assessment and a student");
                return;
            }

            showNotification("Load Submission",
                    "Loading submission for " + studentChoice.getValue().getName() +
                            " on " + assessmentChoice.getValue().getTitle());
        });

        studentSelection.getChildren().addAll(
                new Label("Assessment:"), assessmentChoice,
                new Label("Student:"), studentChoice,
                loadBtn
        );

        // Submission View
        TitledPane submissionPane = new TitledPane("Student Submission", new VBox());
        styleTitledPane(submissionPane);

        TextArea submissionContent = new TextArea();
        submissionContent.setPromptText("Student's submission will appear here...");
        submissionContent.setPrefHeight(200);
        ((VBox) submissionPane.getContent()).getChildren().add(submissionContent);

        // Rubric section
        TitledPane rubricPane = new TitledPane("Grading Rubric", new VBox());
        styleTitledPane(rubricPane);

        TableView<RubricItem> rubricTable = new TableView<>();

        TableColumn<RubricItem, String> criteriaCol = new TableColumn<>("Criteria");
        criteriaCol.setCellValueFactory(new PropertyValueFactory<>("criteria"));

        TableColumn<RubricItem, String> maxCol = new TableColumn<>("Max Points");
        maxCol.setCellValueFactory(new PropertyValueFactory<>("maxPoints"));

        TableColumn<RubricItem, String> earnedCol = new TableColumn<>("Earned");
        earnedCol.setCellValueFactory(cellData -> {
            RubricItem item = cellData.getValue();
            TextField pointsField = new TextField();
            pointsField.setTextFormatter(new TextFormatter<>(change ->
                    change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));
            pointsField.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    item.setEarnedPoints(Double.parseDouble(newVal));
                } catch (NumberFormatException e) {
                    item.setEarnedPoints(0);
                }
            });
            return new SimpleStringProperty("");
        });
        earnedCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    RubricItem rubricItem = getTableView().getItems().get(getIndex());
                    TextField pointsField = new TextField(String.valueOf(rubricItem.getEarnedPoints()));
                    pointsField.setTextFormatter(new TextFormatter<>(change ->
                            change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));
                    pointsField.textProperty().addListener((obs, oldVal, newVal) -> {
                        try {
                            rubricItem.setEarnedPoints(Double.parseDouble(newVal));
                        } catch (NumberFormatException e) {
                            rubricItem.setEarnedPoints(0);
                        }
                    });
                    setGraphic(pointsField);
                }
            }
        });

        rubricTable.getColumns().addAll(criteriaCol, maxCol, earnedCol);

        // Sample rubric items
        ObservableList<RubricItem> rubricItems = FXCollections.observableArrayList(
                new RubricItem("Correctness", 50),
                new RubricItem("Code Quality", 30),
                new RubricItem("Documentation", 20)
        );
        rubricTable.setItems(rubricItems);

        ((VBox) rubricPane.getContent()).getChildren().add(rubricTable);

        // Grading Form
        VBox gradingForm = new VBox(10);
        TextField gradeField = new TextField();
        gradeField.setPromptText("Grade (0-100)");

        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("Feedback");
        feedbackArea.setPrefHeight(100);

        Button saveGradeBtn = new Button("Save Grade");
        saveGradeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        saveGradeBtn.setOnAction(e -> {
            if (assessmentChoice.getValue() == null || studentChoice.getValue() == null) {
                showAlert("Error", "Please select both an assessment and a student");
                return;
            }

            try {
                double grade = Double.parseDouble(gradeField.getText());
                if (grade < 0 || grade > 100) {
                    showAlert("Error", "Grade must be between 0 and 100");
                    return;
                }

                showNotification("Grade Saved",
                        "Grade of " + grade + " saved for " + studentChoice.getValue().getName() +
                                " on " + assessmentChoice.getValue().getTitle());

                gradeField.clear();
                feedbackArea.clear();
            } catch (NumberFormatException ex) {
                showAlert("Error", "Grade must be a number");
            }
        });

        gradingForm.getChildren().addAll(
                new Label("Grading"),
                gradeField,
                feedbackArea,
                saveGradeBtn
        );

        gradingBox.getChildren().addAll(
                studentSelection,
                submissionPane,
                rubricPane,
                gradingForm
        );

        return gradingBox;
    }

    // ==================== Reporting (15 marks) ====================
    private Tab createReportingTab() {
        Tab reportTab = new Tab("Reporting");
        reportTab.setClosable(false);

        // Add tabbed interface for different report types
        TabPane reportTabs = new TabPane();

        Tab analyticsTab = new Tab("Analytics", createChartsPane());
        Tab gradebookTab = new Tab("Gradebook", createGradebookPane());
        Tab transcriptTab = new Tab("Transcripts", createTranscriptPane());

        reportTabs.getTabs().addAll(analyticsTab, gradebookTab, transcriptTab);

        VBox reportingBox = new VBox(15, reportTabs);
        reportingBox.setPadding(new Insets(15));
        reportTab.setContent(reportingBox);

        return reportTab;
    }

    private VBox createChartsPane() {
        VBox chartsBox = new VBox(15);
        chartsBox.setPadding(new Insets(15));

        // Performance Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Average Grade");

        BarChart<String, Number> performanceChart = new BarChart<>(xAxis, yAxis);
        performanceChart.setTitle("Course Performance");
        performanceChart.setLegendVisible(false);
        performanceChart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Grades");
        for (Course course : courses) {
            series.getData().add(new XYChart.Data<>(course.getName(), course.getAverageGrade()));
        }
        performanceChart.getData().add(series);

        // Progress Chart
        CategoryAxis progressXAxis = new CategoryAxis();
        NumberAxis progressYAxis = new NumberAxis(0, 100, 10);
        progressYAxis.setLabel("Completion %");

        LineChart<String, Number> progressChart = new LineChart<>(progressXAxis, progressYAxis);
        progressChart.setTitle("Student Progress Over Time");

        XYChart.Series<String, Number> progressSeries = new XYChart.Series<>();
        progressSeries.setName("Average Progress");

        // Sample data - in real app would come from database
        progressSeries.getData().add(new XYChart.Data<>("Week 1", 20));
        progressSeries.getData().add(new XYChart.Data<>("Week 2", 35));
        progressSeries.getData().add(new XYChart.Data<>("Week 3", 50));
        progressSeries.getData().add(new XYChart.Data<>("Week 4", 65));
        progressSeries.getData().add(new XYChart.Data<>("Week 5", 80));

        progressChart.getData().add(progressSeries);

        // Attendance Chart
        PieChart attendanceChart = new PieChart();
        attendanceChart.setTitle("Attendance Distribution");

        attendanceChart.getData().addAll(
                new PieChart.Data("Present", 85),
                new PieChart.Data("Absent", 10),
                new PieChart.Data("Late", 5)
        );

        // Layout charts in a grid
        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(15);
        chartsGrid.setVgap(15);
        chartsGrid.setPadding(new Insets(10));

        chartsGrid.add(performanceChart, 0, 0);
        chartsGrid.add(progressChart, 1, 0);
        chartsGrid.add(attendanceChart, 0, 1, 2, 1);

        chartsBox.getChildren().addAll(
                new Label("Performance Analytics"),
                chartsGrid
        );

        return chartsBox;
    }

    private VBox createGradebookPane() {
        VBox gradebookBox = new VBox(10);
        gradebookBox.setPadding(new Insets(15));

        TableView<GradebookEntry> gradebookTable = new TableView<>();
        gradebookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<GradebookEntry, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("student"));

        TableColumn<GradebookEntry, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("course"));

        // Dynamic columns for assessments
        for (Assessment assessment : assessments) {
            TableColumn<GradebookEntry, String> assessmentCol = new TableColumn<>(assessment.getTitle());
            assessmentCol.setCellValueFactory(cellData -> {
                String grade = cellData.getValue().getGrades().get(assessment.getTitle());
                return new SimpleStringProperty(grade != null ? grade : "-");
            });
            gradebookTable.getColumns().add(assessmentCol);
        }

        TableColumn<GradebookEntry, String> finalGradeCol = new TableColumn<>("Final Grade");
        finalGradeCol.setCellValueFactory(new PropertyValueFactory<>("finalGrade"));

        gradebookTable.getColumns().addAll(studentCol, courseCol, finalGradeCol);

        // Generate sample data
        ObservableList<GradebookEntry> gradebookData = FXCollections.observableArrayList();
        for (Student student : students) {
            for (Course course : courses) {
                GradebookEntry entry = new GradebookEntry(student.getName(), course.getName());
                for (Assessment assessment : assessments) {
                    entry.addGrade(assessment.getTitle(), String.format("%.1f", 70 + Math.random() * 30));
                }
                entry.setFinalGrade(String.format("%.1f", 75 + Math.random() * 25));
                gradebookData.add(entry);
            }
        }

        gradebookTable.setItems(gradebookData);

        // Export buttons
        HBox exportButtons = new HBox(10);
        Button exportCSVBtn = new Button("Export to CSV");
        Button exportPDFBtn = new Button("Export to PDF");
        Button printBtn = new Button("Print Gradebook");

        exportCSVBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        exportPDFBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
        printBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        exportCSVBtn.setOnAction(e -> {
            showNotification("Export", "Gradebook exported to CSV");
            // In a real application, this would generate an actual CSV file
        });

        exportPDFBtn.setOnAction(e -> {
            showNotification("Export", "Gradebook exported to PDF");
            // In a real application, this would generate an actual PDF
        });

        printBtn.setOnAction(e -> {
            showNotification("Print", "Gradebook sent to printer");
            // In a real application, this would print the gradebook
        });

        exportButtons.getChildren().addAll(exportCSVBtn, exportPDFBtn, printBtn);

        gradebookBox.getChildren().addAll(
                new Label("Gradebook"),
                gradebookTable,
                exportButtons
        );

        return gradebookBox;
    }

    private VBox createTranscriptPane() {
        VBox transcriptBox = new VBox(15);
        transcriptBox.setPadding(new Insets(15));

        // Student Selection
        HBox selectionBox = new HBox(10);
        ChoiceBox<Student> studentChoice = new ChoiceBox<>(students);
        Button generateBtn = new Button("Generate Transcript");
        generateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        // Transcript Preview
        TextArea transcriptPreview = new TextArea();
        transcriptPreview.setEditable(false);
        transcriptPreview.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        transcriptPreview.setText("Transcript will appear here...");

        generateBtn.setOnAction(e -> {
            if (studentChoice.getValue() == null) {
                showAlert("Error", "Please select a student");
                return;
            }

            Student selected = studentChoice.getValue();
            StringBuilder transcript = new StringBuilder();
            transcript.append("OFFICIAL TRANSCRIPT\n\n");
            transcript.append("Student: ").append(selected.getName()).append("\n");
            transcript.append("ID: ").append(selected.getId()).append("\n");
            transcript.append("Date: ").append(LocalDate.now()).append("\n\n");
            transcript.append("COURSES AND GRADES\n");
            transcript.append("----------------------------------------\n");

            for (Course course : courses) {
                transcript.append(String.format("%-30s %.1f\n", course.getName(), course.getAverageGrade()));
            }

            transcript.append("\nGPA: 3.5\n");
            transcript.append("----------------------------------------\n");

            transcriptPreview.setText(transcript.toString());
        });

        selectionBox.getChildren().addAll(
                new Label("Select Student:"), studentChoice, generateBtn
        );

        // Export Buttons
        HBox exportButtons = new HBox(10);
        Button savePDFBtn = new Button("Save as PDF");
        Button printBtn = new Button("Print Transcript");
        savePDFBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
        printBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        savePDFBtn.setOnAction(e -> {
            if (transcriptPreview.getText().equals("Transcript will appear here...")) {
                showAlert("Error", "Please generate a transcript first");
            } else {
                showNotification("Export", "Transcript saved as PDF");
                // In a real application, this would generate an actual PDF
            }
        });

        printBtn.setOnAction(e -> {
            if (transcriptPreview.getText().equals("Transcript will appear here...")) {
                showAlert("Error", "Please generate a transcript first");
            } else {
                showNotification("Print", "Transcript sent to printer");
                // In a real application, this would print the transcript
            }
        });

        exportButtons.getChildren().addAll(savePDFBtn, printBtn);

        transcriptBox.getChildren().addAll(
                new Label("Transcript Generation"),
                selectionBox,
                transcriptPreview,
                exportButtons
        );

        return transcriptBox;
    }

    private Tab createCertificationTab() {
        Tab tab  = new Tab("Certificate");
        VBox certBox = new VBox(15);
        certBox.setPadding(new Insets(15));

        // Student Selection
        HBox selectionBox = new HBox(10);
        ChoiceBox<Student> studentChoice = new ChoiceBox<>(students);
        ChoiceBox<Course> courseChoice = new ChoiceBox<>(courses);
        Button generateBtn = new Button("Generate Certificate");
        generateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        // Certificate Preview
        StackPane certPreview = new StackPane();
        certPreview.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px;");
        certPreview.setPrefSize(600, 400);

        Image image = new Image("solidarity.jpeg");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(150);
        imageView.setFitHeight(200);

        Label titleLabel = new Label("Certificate of Completion");
        Label awardedLabel = new Label("This certificate is awarded to");
        Label studentNameLabel = new Label("[Student Name]");
        Label courseLabel = new Label("for successfully completing the course");
        Label courseNameLabel = new Label("[Course Name]");
        Label dateLabel = new Label("on [Date]  from Sebakeng college.");

        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        awardedLabel.setStyle("-fx-font-size: 16px;");
        studentNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        courseLabel.setStyle("-fx-font-size: 16px;");
        courseNameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        dateLabel.setStyle("-fx-font-size: 14px;");

        VBox certDetails = new VBox(20);
        certDetails.setAlignment(Pos.CENTER);
        certDetails.setPadding(new Insets(20));
        certDetails.getChildren().addAll(
                titleLabel, awardedLabel, studentNameLabel,
                courseLabel, courseNameLabel, dateLabel
        );

        certPreview.getChildren().add(certDetails);

        generateBtn.setOnAction(e -> {
            if (studentChoice.getValue() == null || courseChoice.getValue() == null) {
                showAlert("Error", "Please select both a student and a course");
                return;
            }

            Student student = studentChoice.getValue();
            Course course = courseChoice.getValue();

            studentNameLabel.setText(student.getName());
            courseNameLabel.setText(course.getName());
            dateLabel.setText("on " + LocalDate.now().toString());

            showNotification("Certificate", "Certificate generated for " + student.getName());
        });

        selectionBox.getChildren().addAll(
                new Label("Student:"), studentChoice,
                new Label("Course:"), courseChoice,
                generateBtn
        );

        // Export Buttons
        HBox exportButtons = new HBox(10);
        Button savePDFBtn = new Button("Save as PDF");
        Button printBtn = new Button("Print Certificate");
        savePDFBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
        printBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        savePDFBtn.setOnAction(e -> {
            if (studentNameLabel.getText().equals("[Student Name]")) {
                showAlert("Error", "Please generate a certificate first");
            } else {
                showNotification("Export", "Certificate saved as PDF");
                // In a real application, this would generate an actual PDF
            }
        });

        printBtn.setOnAction(e -> {
            if (studentNameLabel.getText().equals("[Student Name]")) {
                showAlert("Error", "Please generate a certificate first");
            } else {
                showNotification("Print", "Certificate sent to printer");
                // In a real application, this would print the certificate
            }
        });

        exportButtons.getChildren().addAll(savePDFBtn, printBtn);

        certBox.getChildren().addAll(
                new Label("Certificate Generation"),
                selectionBox,
                certPreview,
                exportButtons
        );
// Set the content of the tab
        tab.setContent(certBox);
        return tab;
    }

    // ==================== Additional Features ====================
    private Tab createStudentsTab() {
        Tab studentsTab = new Tab("Students");
        studentsTab.setClosable(false);

        VBox studentsContent = new VBox(15);
        studentsContent.setPadding(new Insets(15));

        // Create pagination control
        Pagination pagination = new Pagination();
        pagination.setPageCount((int) Math.ceil(students.size() / 5.0)); // 5 items per page
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(5);

        // Student Table
        TableView<Student> studentTable = new TableView<>();
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Student, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Student, String> enrolledCol = new TableColumn<>("Enrolled Courses");
        enrolledCol.setCellValueFactory(cellData -> {
            int count = (int) (Math.random() * 4) + 1; // Random number of courses for demo
            return new SimpleStringProperty(count + " courses");
        });

        studentTable.getColumns().addAll(idCol, nameCol, emailCol, enrolledCol);

        // Update table content based on pagination
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            int fromIndex = newIndex.intValue() * 5;
            int toIndex = Math.min(fromIndex + 5, students.size());
            studentTable.setItems(FXCollections.observableArrayList(students.subList(fromIndex, toIndex)));
        });

        // Initial load
        pagination.setCurrentPageIndex(0);

        // Student Search
        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search students...");
        Button searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        searchBtn.setOnAction(e -> {
            String query = searchField.getText().toLowerCase();
            if (query.isEmpty()) {
                // Reset to original pagination
                pagination.setPageCount((int) Math.ceil(students.size() / 5.0));
                pagination.setCurrentPageIndex(0);
            } else {
                ObservableList<Student> filtered = FXCollections.observableArrayList();
                for (Student student : students) {
                    if (student.getName().toLowerCase().contains(query) ||
                            student.getId().toLowerCase().contains(query) ||
                            student.getEmail().toLowerCase().contains(query)) {
                        filtered.add(student);
                    }
                }
                // Update pagination for filtered results
                pagination.setPageCount((int) Math.ceil(filtered.size() / 5.0));
                pagination.setCurrentPageIndex(0);
            }
        });

        searchBox.getChildren().addAll(searchField, searchBtn);

        // Student Details Form
        GridPane studentForm = new GridPane();
        studentForm.setVgap(10);
        studentForm.setHgap(10);
        studentForm.setPadding(new Insets(15));
        studentForm.setStyle("-fx-border-color: #dddddd; -fx-border-width: 1px;");

        TextField idField = new TextField();
        idField.setPromptText("Student ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("Date of Birth");

        studentForm.addRow(0, new Label("Student ID:"), idField);
        studentForm.addRow(1, new Label("Full Name:"), nameField);
        studentForm.addRow(2, new Label("Email:"), emailField);
        studentForm.addRow(3, new Label("Date of Birth:"), dobPicker);

        HBox studentButtons = new HBox(10);
        Button addBtn = new Button("Add Student");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");

        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        updateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");

        addBtn.setOnAction(e -> {
            if (idField.getText().isEmpty() || nameField.getText().isEmpty() || emailField.getText().isEmpty()) {
                showAlert("Error", "ID, name and email are required");
                return;
            }

            Student newStudent = new Student(
                    idField.getText(),
                    nameField.getText(),
                    emailField.getText()
            );

            students.add(newStudent);

            idField.clear();
            nameField.clear();
            emailField.clear();
            dobPicker.setValue(null);

            showNotification("Student Added", newStudent.getName() + " has been added");

            // Update pagination
            pagination.setPageCount((int) Math.ceil(students.size() / 5.0));
        });

        updateBtn.setOnAction(e -> {
            Student selected = studentTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Error", "Please select a student to update");
                return;
            }

            if (nameField.getText().isEmpty() || emailField.getText().isEmpty()) {
                showAlert("Error", "Name and email are required");
                return;
            }

            selected.setName(nameField.getText());
            selected.setEmail(emailField.getText());

            studentTable.refresh();
            showNotification("Student Updated", selected.getName() + " has been updated");
        });

        deleteBtn.setOnAction(e -> {
            Student selected = studentTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Error", "Please select a student to delete");
                return;
            }

            if (showConfirmation("Delete Student",
                    "Are you sure you want to delete " + selected.getName() + "?")) {
                students.remove(selected);
                showNotification("Student Deleted", selected.getName() + " has been removed");

                // Update pagination
                pagination.setPageCount((int) Math.ceil(students.size() / 5.0));
                if (pagination.getCurrentPageIndex() >= pagination.getPageCount()) {
                    pagination.setCurrentPageIndex(pagination.getPageCount() - 1);
                }
            }
        });

        // Populate form when student is selected
        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                idField.setText(newSelection.getId());
                nameField.setText(newSelection.getName());
                emailField.setText(newSelection.getEmail());
                dobPicker.setValue(null); // No DOB in our simple model
            }
        });

        studentButtons.getChildren().addAll(addBtn, updateBtn, deleteBtn);

        studentsContent.getChildren().addAll(
                searchBox,
                pagination,
                studentTable,
                new Separator(),
                studentForm,
                studentButtons
        );

        studentsTab.setContent(studentsContent);
        return studentsTab;
    }

    private Tab createCommunicationTab() {
        Tab commTab = new Tab("Communication");
        commTab.setClosable(false);

        VBox commContent = new VBox(15);
        commContent.setPadding(new Insets(15));

        // Announcements
        TitledPane announcementsPane = new TitledPane("Announcements", createAnnouncementsPane());
        styleTitledPane(announcementsPane);

        // Messaging
        TitledPane messagingPane = new TitledPane("Messaging", createMessagingPane());
        styleTitledPane(messagingPane);

        // Notifications
        TitledPane notificationsPane = new TitledPane("Notifications", createNotificationsPane());
        styleTitledPane(notificationsPane);

        commContent.getChildren().addAll(
                announcementsPane,
                messagingPane,
                notificationsPane
        );

        commTab.setContent(commContent);
        return commTab;
    }

    private VBox createAnnouncementsPane() {
        VBox announcementsBox = new VBox(10);
        announcementsBox.setPadding(new Insets(15));

        ListView<Announcement> announcementsList = new ListView<>(announcements);
        announcementsList.setCellFactory(param -> new ListCell<Announcement>() {
            @Override
            protected void updateItem(Announcement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                    HBox metaBox = new HBox(10);
                    Label authorLabel = new Label("By: " + item.getAuthor());
                    Label dateLabel = new Label(item.getDate().toString());
                    authorLabel.setStyle("-fx-font-size: 11px;");
                    dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
                    metaBox.getChildren().addAll(authorLabel, dateLabel);

                    Label contentLabel = new Label(item.getContent());
                    contentLabel.setWrapText(true);

                    box.getChildren().addAll(titleLabel, metaBox, contentLabel);
                    setGraphic(box);
                }
            }
        });

        announcementsBox.getChildren().addAll(
                new Label("Latest Announcements"),
                announcementsList
        );

        return announcementsBox;
    }

    private VBox createMessagingPane() {
        VBox messagingBox = new VBox(15);
        messagingBox.setPadding(new Insets(15));

        // Contacts List
        ListView<Student> contactsList = new ListView<>(students);
        contactsList.setCellFactory(param -> new ListCell<Student>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    Circle statusCircle = new Circle(5);
                    statusCircle.setFill(Color.GREEN);
                    Label nameLabel = new Label(item.getName());
                    box.getChildren().addAll(statusCircle, nameLabel);
                    setGraphic(box);
                }
            }
        });
        contactsList.setPrefWidth(200);

        // Message History
        ListView<Message> messageHistory = new ListView<>();
        messageHistory.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    HBox header = new HBox(10);
                    Label authorLabel = new Label(item.getAuthor());
                    authorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    Label timeLabel = new Label(item.getTimestamp());
                    timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                    header.getChildren().addAll(authorLabel, timeLabel);

                    Label contentLabel = new Label(item.getContent());
                    contentLabel.setWrapText(true);

                    box.getChildren().addAll(header, contentLabel);
                    setGraphic(box);
                }
            }
        });

        contactsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                messageHistory.setItems(FXCollections.observableArrayList(
                        new Message(newVal.getName(), "10:30 AM", "Hello, how are you?"),
                        new Message(currentUserName, "10:32 AM", "I'm doing well, thanks!"),
                        new Message(newVal.getName(), "10:33 AM", "Do you have questions about the assignment?")
                ));
            }
        });

        // Message Composition
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Type your message here...");
        messageArea.setPrefHeight(100);

        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        sendBtn.setOnAction(e -> {
            Student selected = contactsList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Error", "Please select a contact");
                return;
            }

            if (messageArea.getText().isEmpty()) {
                showAlert("Error", "Message cannot be empty");
                return;
            }

            Message newMessage = new Message(
                    currentUserName,
                    LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")),
                    messageArea.getText()
            );

            messageHistory.getItems().add(newMessage);
            messageArea.clear();
            messageHistory.scrollTo(messageHistory.getItems().size() - 1);
        });

        // Layout
        HBox messagingLayout = new HBox(10);
        VBox messageContainer = new VBox(10,
                new Label("Message History"),
                messageHistory,
                messageArea,
                sendBtn
        );

        messagingLayout.getChildren().addAll(contactsList, messageContainer);
        messagingBox.getChildren().addAll(
                new Label("Direct Messaging"),
                messagingLayout
        );

        return messagingBox;
    }

    private VBox createNotificationsPane() {
        VBox notificationsBox = new VBox(10);
        notificationsBox.setPadding(new Insets(15));

        ListView<Notification> notificationsList = new ListView<>(notifications);
        notificationsList.setCellFactory(param -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                    HBox metaBox = new HBox(10);
                    Label dateLabel = new Label(item.getDate().toString());
                    Label newLabel = new Label("NEW");
                    newLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

                    if (item.getDate().isAfter(LocalDate.now().minusDays(1))) {
                        metaBox.getChildren().addAll(newLabel, dateLabel);
                    } else {
                        metaBox.getChildren().add(dateLabel);
                    }

                    Label contentLabel = new Label(item.getMessage());
                    contentLabel.setWrapText(true);

                    box.getChildren().addAll(titleLabel, metaBox, contentLabel);
                    setGraphic(box);
                }
            }
        });

        Button markAllReadBtn = new Button("Mark All as Read");
        markAllReadBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold;");

        markAllReadBtn.setOnAction(e -> {
            showNotification("Notifications", "All notifications marked as read");
            // In a real application, this would update the notification status
        });

        notificationsBox.getChildren().addAll(
                new Label("Your Notifications"),
                notificationsList,
                markAllReadBtn
        );

        return notificationsBox;
    }

    // ==================== Helper Methods ====================
    private void styleTitledPane(TitledPane pane) {
        pane.setStyle("-fx-text-fill: #2a2a2a;");
        pane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
            if (isNowExpanded) {
                javafx.scene.Node titleNode = pane.lookup(".title");
                if (titleNode != null) {
                    titleNode.setStyle("-fx-background-color: #3c7fb1; -fx-background-radius: 4; -fx-text-fill: white;");
                }
            }
        });
    }
    private void saveTranscriptAsPDF(String content, String filename) {
        try {
            com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
            PdfWriter.getInstance(pdfDoc, new FileOutputStream(filename));
            pdfDoc.open();
            pdfDoc.add(new com.itextpdf.text.Paragraph(content));
            pdfDoc.close();
            showNotification("PDF Saved", "Transcript saved as " + filename);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save PDF: " + e.getMessage());
        }
    }
    private void saveMessageToDatabase(Message msg) {
        String query = "INSERT INTO messages (author, timestamp, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, msg.getAuthor());
            pstmt.setString(2, msg.getTimestamp());
            pstmt.setString(3, msg.getContent());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showNotification(String title, String message) {
        // Create a notification popup
        Stage notificationStage = new Stage();
        notificationStage.initOwner(primaryStage);
        notificationStage.initModality(Modality.NONE);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);

        Button closeBtn = new Button("OK");
        closeBtn.setOnAction(e -> notificationStage.close());

        vbox.getChildren().addAll(titleLabel, messageLabel, closeBtn);

        Scene scene = new Scene(vbox, 300, 150);
        notificationStage.setScene(scene);
        notificationStage.setTitle(title);
        notificationStage.show();
    }

    private void initializeSampleData() {
        // Courses
        courses.addAll(
                new Course("Introduction to Programming", "Basic programming concepts", 0.75, 85.5),
                new Course("Database Systems", "Relational database design", 0.65, 78.2),
                new Course("Web Development", "HTML, CSS, JavaScript", 0.82, 88.7),
                new Course("Data Structures", "Algorithms and data structures", 0.58, 76.4),
                new Course("Machine Learning", "Introduction to ML algorithms", 0.45, 72.1)
        );

        // Students
        students.addAll(
                new Student("1001", "letsoela@gmail.com", "letsoela@gmail.com"),
                new Student("1002", "molemohi@gmail.com", "molemohi@gmail.com"),
                new Student("1003", "malesela@gmail.com", "malesela@gmail.com"),
                new Student("1004", "bokang@gmail.com", "bokang@gmail.com"),
                new Student("1005", "sephewe@gmail.com", "sephewe@gmail.com")
        );

        // Assessments
        assessments.addAll(
                new Assessment("Quiz 1", "Multiple Choice", LocalDate.now().plusDays(7), 15),
                new Assessment("Assignment 1", "Programming", LocalDate.now().plusDays(14), 25),
                new Assessment("Midterm Exam", "Written", LocalDate.now().plusDays(21), 30),
                new Assessment("Final Project", "Project", LocalDate.now().plusDays(28), 30)
        );

        // Announcements
        announcements.addAll(
                new Announcement("Welcome to Semester", "Welcome to the new semester!", "Dr. Nkhatho", LocalDate.now()),
                new Announcement("Assignment Due Date", "Assignment 1 due date extended", "Mrs Mastebo", LocalDate.now().minusDays(1)),
                new Announcement("System Maintenance", "HaLMS will be down for maintenance", "Masutsa", LocalDate.now().minusDays(3))
        );

        // Messages
        messages.addAll(
                new Message("Dr. Nkhatho", "10:30 AM", "Welcome to the course! Please check the syllabus."),
                new Message("Mrs Mastebo", "11:45 AM", "Does anyone understand question 3 on the assignment?"),
                new Message("Mr Mokhamo", "12:15 PM", "I can help with question 3 after class")
        );

        // Notifications
        notifications.addAll(
                new Notification("New Assignment", "Assignment 2 has been posted", LocalDate.now()),
                new Notification("Grade Posted", "Your grade for Quiz 1 is available", LocalDate.now().minusDays(1)),
                new Notification("System Update", "LMS will be down for maintenance", LocalDate.now().minusDays(2))
        );

        // Videos
        videos.addAll(
                new Video("Introduction to Java", "45:32", LocalDate.now().minusDays(2)),
                new Video("Object-Oriented Programming", "52:18", LocalDate.now().minusDays(5)),
                new Video("GUI Development with JavaFX", "38:45", LocalDate.now().minusDays(7))
        );
    }

    // ==================== Data Model Classes ====================
    public static class Course {
        private String name;
        private String description;
        private double progress;
        private double averageGrade;

        public Course(String name, String description, double progress, double averageGrade) {
            this.name = name;
            this.description = description;
            this.progress = progress;
            this.averageGrade = averageGrade;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public double getProgress() {
            return progress;
        }

        public double getAverageGrade() {
            return averageGrade;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setProgress(double progress) {
            this.progress = progress;
        }

        public void setAverageGrade(double averageGrade) {
            this.averageGrade = averageGrade;
        }

        public void saveToDatabase() {
            String query = "INSERT INTO courses (course_name, description, lecturer_id) VALUES (?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, this.name);
                pstmt.setString(2, this.description);
                pstmt.setInt(3, 2); // Assuming lecturer_id 2 is the default lecturer

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            // You could store the generated ID if needed
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void updateInDatabase() {
            String query = "UPDATE courses SET course_name = ?, description = ? WHERE course_name = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setString(1, this.name);
                pstmt.setString(2, this.description);
                pstmt.setString(3, this.name);

                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void deleteFromDatabase() {
            String query = "DELETE FROM courses WHERE course_name = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setString(1, this.name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Student {
        private String id;
        private String name;
        private String email;

        public Student(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class Assessment {
        private String title;
        private String type;
        private LocalDate dueDate;
        private int weight;

        public Assessment(String title, String type, LocalDate dueDate, int weight) {
            this.title = title;
            this.type = type;
            this.dueDate = dueDate;
            this.weight = weight;
        }

        public String getTitle() {
            return title;
        }

        public String getType() {
            return type;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public int getWeight() {
            return weight;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }

    public static class Announcement {
        private String title;
        private String content;
        private String author;
        private LocalDate date;

        public Announcement(String title, String content, String author, LocalDate date) {
            this.title = title;
            this.content = content;
            this.author = author;
            this.date = date;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getAuthor() {
            return author;
        }

        public LocalDate getDate() {
            return date;
        }
    }

    public static class Message {
        private String author;
        private String timestamp;
        private String content;

        public Message(String author, String timestamp, String content) {
            this.author = author;
            this.timestamp = timestamp;
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getContent() {
            return content;
        }
    }

    public static class Notification {
        private String title;
        private String message;
        private LocalDate date;

        public Notification(String title, String message, LocalDate date) {
            this.title = title;
            this.message = message;
            this.date = date;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public LocalDate getDate() {
            return date;
        }
    }
            public static class Video {
                private String title;
                private String duration;
                private LocalDate uploadDate;
                private String filePath;  // Add this field

                public Video(String title, String duration, LocalDate uploadDate) {
                    this.title = title;
                    this.duration = duration;
                    this.uploadDate = uploadDate;
                }

                // Add getter and setter for filePath
                public String getFilePath() {
                    return filePath;
                }

                public void setFilePath(String filePath) {
                    this.filePath = filePath;
                }

        public String getTitle() {
            return title;
        }

        public String getDuration() {
            return duration;
        }

        public LocalDate getUploadDate() {
            return uploadDate;
        }
    }

    public static class Document {
        private String name;
        private String type;
        private String size;
        private String filePath; // path to the uploaded file

        public Document(String name, String type, String size, String filePath) {
            this.name = name;
            this.type = type;
            this.size = size;
            this.filePath = filePath;

        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getSize() {
            return size;
        }

        public String getFilePath() {
            return filePath;
        }
    }

    public static class MCQuestion {
        private String question;
        private String[] options;
        private int correctAnswer;

        public MCQuestion(String question, String[] options, int correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() {
            return question;
        }

        public String[] getOptions() {
            return options;
        }

        public int getCorrectAnswer() {
            return correctAnswer;
        }
    }

    public static class GradebookEntry {
        private String student;
        private String course;
        private Map<String, String> grades;
        private String finalGrade;

        public GradebookEntry(String student, String course) {
            this.student = student;
            this.course = course;
            this.grades = new HashMap<>();
        }

        public String getStudent() {
            return student;
        }

        public String getCourse() {
            return course;
        }

        public Map<String, String> getGrades() {
            return grades;
        }

        public String getFinalGrade() {
            return finalGrade;
        }

        public void addGrade(String assessment, String grade) {
            grades.put(assessment, grade);
        }

        public void setFinalGrade(String grade) {
            this.finalGrade = grade;
        }
    }

    public static class RubricItem {
        private String criteria;
        private double maxPoints;
        private double earnedPoints;

        public RubricItem(String criteria, double maxPoints) {
            this.criteria = criteria;
            this.maxPoints = maxPoints;
            this.earnedPoints = 0;
        }

        public String getCriteria() {
            return criteria;
        }

        public double getMaxPoints() {
            return maxPoints;
        }

        public double getEarnedPoints() {
            return earnedPoints;
        }

        public void setEarnedPoints(double points) {
            this.earnedPoints = points;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}