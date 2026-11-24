package fr.umfds.spmanager.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

public class LoginE2ETest extends BaseTests {

    @Test
    @DisplayName("Connexion réussie Administrateur, vérification de l'identité et déconnexion")
    public void testLoginAdminSuccessAndLogout() {
        // 1. Saisie des identifiants et soumission
        login("admin", "admin");

        // 2. Vérification des éléments du bandeau
        String userInfo = getText(By.id("user-info"));
        assertTrue(userInfo.contains("Admin System") && userInfo.contains("ADMIN"),
                "Le bandeau doit afficher le nom et le rôle ADMIN");

        // 3. Vérification de la visibilité des sections
        WebElement dashboard = driver.findElement(By.id("dashboard-section"));
        assertFalse(dashboard.getAttribute("class").contains("hidden"),
                "Le tableau de bord doit être visible après connexion");

        // 4. Déconnexion
        logout();

        // 5. Vérification du retour à la page de login
        WebElement authSection = driver.findElement(By.id("auth-section"));
        assertFalse(authSection.getAttribute("class").contains("hidden"),
                "La section d'authentification doit être de nouveau visible");
    }


    @Test
    @DisplayName("Connexion avec mot de passe invalide : l'utilisateur reste sur la page de connexion")
    public void testLoginWithInvalidPassword() {
        write(By.id("login-input"), "admin");
        write(By.id("password-input"), "mauvais_mot_de_passe");
        click(By.cssSelector("#login-form button[type='submit']"));
        acceptAlert();
        // L'utilisateur ne doit pas accéder au dashboard
        WebElement authSection = driver.findElement(By.id("auth-section"));
        assertFalse(authSection.getAttribute("class").contains("hidden"),
                "La page de login doit rester affichée en cas d'erreur");

        WebElement userNav = driver.findElement(By.id("user-nav"));
        assertTrue(userNav.getAttribute("class").contains("hidden"),
                "La barre utilisateur doit rester masquée");
    }


    @Test
    @DisplayName("Connexion Enseignant : affichage des onglets spécifiques du rôle Enseignant")
    public void testLoginTeacherShowsTeacherTabs() {
        login("turing", "alan");

        String userInfo = getText(By.id("user-info"));
        assertTrue(userInfo.contains("Alan Turing") && userInfo.contains("TEACHER"));

        // Vérification des onglets Enseignant
        String tabsText = getText(By.id("role-tabs"));
        assertTrue(tabsText.contains("Tous les Sujets"));
        assertTrue(tabsText.contains("Mes Sujets"));
        assertTrue(tabsText.contains("Proposer un Sujet"));
        assertFalse(tabsText.contains("Enseignants"), "Un enseignant ne doit pas voir l'onglet admin Enseignants");
    }
}
