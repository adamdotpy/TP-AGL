package fr.umfds.spmanager.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTeacherE2ETest extends BaseTests {

    @Test
    @DisplayName("Administrateur : création de l'enseignant Margaret Hamilton, visible dans la liste")
    public void testAdminCreatesTeacher() {
        // 1. Connexion en tant qu'administrateur
        login("admin", "admin");
        // 2. Onglet "Enseignants" (clickTab attend que l'onglet soit cliquable)
        clickTab("Enseignants");
        // 3. Remplissage du formulaire (write attend déjà que chaque champ soit visible)
        write(By.id("teacher-login"), "hamilton");
        write(By.id("teacher-password"), "margaret");
        write(By.id("teacher-firstname"), "Margaret");
        write(By.id("teacher-lastname"), "Hamilton");
        // 4. Clic sur "Créer l'enseignant" : le bouton submit du formulaire
        click(By.cssSelector("#create-teacher-form button[type='submit']"));
        // 5a. Alerte : on vérifie son message puis on l'accepte
        //     (tant qu'elle est ouverte, Firefox refuse les autres commandes)
        Alert alert = wait.until(ExpectedConditions.alertIsPresent()); // "wait" hérité de BaseTests
        assertEquals("Enseignant créé avec succès !", alert.getText());
        alert.accept();
        // 5b. Margaret apparaît dans la liste des enseignants
        //     waitForVisible attend que app.js ait redessiné la liste
        WebElement margaret = waitForVisible(By.xpath(
                "//ul[@id='admin-teachers-list']/li/span[text()='Margaret Hamilton (hamilton)']"));
        assertEquals("Margaret Hamilton (hamilton)", margaret.getText());
        // 6. Capture d'écran de validation (dans target/screenshots/)
        takeScreenshot("adminCreatesTeacher_hamilton");
    }
}
