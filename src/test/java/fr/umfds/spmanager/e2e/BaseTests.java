package fr.umfds.spmanager.e2e;


import fr.umfds.spmanager.config.DatabaseConfig;
import fr.umfds.spmanager.controller.AuthController;
import fr.umfds.spmanager.controller.GroupController;
import fr.umfds.spmanager.controller.SubjectController;
import fr.umfds.spmanager.controller.UserController;
import fr.umfds.spmanager.exception.AppException;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

/**
 * Classe mère pour l'ensemble des tests E2E Selenium.
 * Gère le démarrage/arrêt du serveur Javalin et le cycle de vie du WebDriver.
 */
public abstract class BaseTests {

    protected static Javalin app;
    protected static int serverPort;
    protected static String baseUrl;

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeAll
    public static void startJavalinServer() {
        // Configuration de la base SQLite pour les tests E2E
        DatabaseConfig.setDbUrl("jdbc:sqlite:target/e2e_test_agl.db");
        DatabaseConfig.initDatabase();

        // Contrôleurs
        AuthController authController = new AuthController();
        UserController userController = new UserController();
        SubjectController subjectController = new SubjectController();
        GroupController groupController = new GroupController();

        // Création de l'application Javalin avec fichiers statiques frontend
        app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/frontend";
                staticFiles.location = Location.CLASSPATH;
            });
        });

        // Gestion des exceptions applicatives
        app.exception(AppException.class, (e, ctx) -> {
            ctx.status(e.getStatusCode());
            ctx.json(Map.of("error", e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500);
            ctx.json(Map.of("error", "Internal server error: " + e.getMessage()));
        });

        // Déclaration des routes de l'API
        app.post("/api/login", authController::login);
        app.get("/api/me", authController::getCurrentUser);
        app.post("/api/teachers", userController::createTeacher);
        app.get("/api/teachers", userController::getTeachers);
        app.post("/api/students", userController::createStudent);
        app.get("/api/students", userController::getStudents);
        app.get("/api/subjects", subjectController::getAllSubjects);
        app.get("/api/subjects/{id}", subjectController::getSubjectById);
        app.post("/api/subjects", subjectController::createSubject);
        app.put("/api/subjects/{id}", subjectController::updateSubject);
        app.post("/api/groups", groupController::createGroup);
        app.post("/api/groups/{id}/join", groupController::joinGroup);
        app.get("/api/groups", groupController::getAllGroups);
        app.get("/api/my-group", groupController::getMyGroup);
        app.post("/api/groups/{id}/preferences", groupController::savePreferences);

        // Démarrage sur un port dynamique
        app.start(0);
        serverPort = app.port();
        baseUrl = "http://localhost:" + serverPort;
    }

    @AfterAll
    public static void stopJavalinServer() {
        if (app != null) {
            app.stop();
        }
    }

    @BeforeEach
    public void setupDriverAndResetDatabase() {
        // Réinitialisation propre de la base SQLite avant chaque test
        File dbFile = new File("target/e2e_test_agl.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        DatabaseConfig.initDatabase();

        // Initialisation de Firefox WebDriver
        FirefoxOptions options = new FirefoxOptions();
        // Décommenter pour exécuter en mode sans fenêtre (headless) :
        // options.addArguments("-headless");

        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        // Navigation vers l'application
        driver.get(baseUrl);
    }

    @AfterEach
    public void tearDownDriver() {
        if (driver != null) {
            driver.quit();
            System.out.println("QUITTING");
        }
    }

    // =========================================================================
    // MÉTHODES D'AIDE ET ACTIONS RÉUTILISABLES
    // =========================================================================

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitForClickable(locator).click();
    }

    protected void write(By locator, String text) {
        WebElement el = waitForVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForVisible(locator).getText();
    }

    protected void login(String username, String password) {
        write(By.id("login-input"), username);
        write(By.id("password-input"), password);
        click(By.cssSelector("#login-form button[type='submit']"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-nav")));
    }

    protected void logout() {
        click(By.id("logout-btn"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("auth-section")));
    }

    protected void clickTab(String tabLabel) {
        By tabLocator = By.xpath(String.format("//nav[@id='role-tabs']//button[contains(text(), '%s')]", tabLabel));
        click(tabLocator);
    }

    protected void acceptAlert(){
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();     // OK
    }

    protected void takeScreenshot(String testName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destDir = Paths.get("target", "screenshots");
            Files.createDirectories(destDir);
            Path destFile = destDir.resolve(testName + "_" + System.currentTimeMillis() + ".png");
            Files.copy(srcFile.toPath(), destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
