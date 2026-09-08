package fr.umfds.spmanager;

import fr.umfds.spmanager.config.AppConfig;
import fr.umfds.spmanager.config.DatabaseConfig;
import fr.umfds.spmanager.controller.AuthController;
import fr.umfds.spmanager.controller.GroupController;
import fr.umfds.spmanager.controller.SubjectController;
import fr.umfds.spmanager.controller.UserController;
import fr.umfds.spmanager.exception.AppException;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        // Initialisation de la base SQLite
        DatabaseConfig.initDatabase();

        // Contrôleurs
        AuthController authController = new AuthController();
        UserController userController = new UserController();
        SubjectController subjectController = new SubjectController();
        GroupController groupController = new GroupController();

        // Création de l'application Javalin
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/frontend";
                staticFiles.location = Location.CLASSPATH;
            });
        });

        // Gestion des exceptions
        app.exception(AppException.class, (e, ctx) -> {
            ctx.status(e.getStatusCode());
            ctx.json(Map.of("error", e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500);
            ctx.json(Map.of("error", "Internal server error: " + e.getMessage()));
        });

        // Routes d'authentification
        app.post("/api/login", authController::login);
        app.get("/api/me", authController::getCurrentUser);

        // Routes d'administration (création enseignants et étudiants)
        app.post("/api/teachers", userController::createTeacher);
        app.get("/api/teachers", userController::getTeachers);
        app.post("/api/students", userController::createStudent);
        app.get("/api/students", userController::getStudents);

        // Routes des sujets
        app.get("/api/subjects", subjectController::getAllSubjects);
        app.get("/api/subjects/{id}", subjectController::getSubjectById);
        app.post("/api/subjects", subjectController::createSubject);
        app.put("/api/subjects/{id}", subjectController::updateSubject);

        // Routes des groupes et préférences
        app.post("/api/groups", groupController::createGroup);
        app.post("/api/groups/{id}/join", groupController::joinGroup);
        app.get("/api/groups", groupController::getAllGroups);
        app.get("/api/my-group", groupController::getMyGroup);
        app.post("/api/groups/{id}/preferences", groupController::savePreferences);

        // US13

        app.get("/api/groups/{id}", groupController::getGroupDetails);


        int port = AppConfig.getServerPort();
        app.start(port);
        System.out.println("Application lancée sur http://localhost:" + port);
    }
}
