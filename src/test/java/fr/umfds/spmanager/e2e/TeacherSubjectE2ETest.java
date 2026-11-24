package fr.umfds.spmanager.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

public class TeacherSubjectE2ETest extends BaseTests {

    @Test
    @DisplayName("Enseignant — Publication et Modification d'un Sujet ")
    public void testPublicationSujet() {
        login("knuth", "donald");
        clickTab("Proposer un Sujet");
        write(By.id("subject-title"), "Algorithmes de compression modernes");
        write(By.id("subject-description"), "Étude et implémentation d'algorithmes de compression sans perte.");
        click(By.cssSelector("#create-subject-form button[type='submit']"));
        Alert alert = wait.until(ExpectedConditions.alertIsPresent()); // "wait" hérité de BaseTests
        assertEquals("Sujet publié avec succès !", alert.getText());
        alert.accept();
        // 5
        clickTab("Mes Sujets");
        // 6
        WebElement sujetUp = waitForVisible(By.xpath(
                "//div[@id='my-subjects-list']/div[@class='card']/h3[normalize-space()='Algorithmes de compression modernes']"));
        assertEquals("Algorithmes de compression modernes", sujetUp.getText());

        // 7. Bouton "Modifier" de la carte qui a ce titre
        //    [h3[...]] = "la carte qui contient ce h3", puis //button = un bouton dans cette carte
        click(By.xpath("//div[@id='my-subjects-list']/div[@class='card'][h3[normalize-space()='Algorithmes de compression modernes']]"
                + "//button[normalize-space()='Modifier']"));

        // 8. Modale : nouveau titre, Enregistrer, puis alerte
        write(By.id("edit-subject-title"), "Algorithmes de compression modernes v2"); // write fait clear() avant
        click(By.cssSelector("#edit-subject-form button[type='submit']"));
        Alert alertModif = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("Sujet mis à jour avec succès !", alertModif.getText());
        alertModif.accept();

        // 9. Le nouveau titre s'affiche tout de suite dans "Mes Sujets", sans recharger la page
        WebElement sujetModifie = waitForVisible(By.xpath(
                "//div[@id='my-subjects-list']/div[@class='card']/h3[normalize-space()='Algorithmes de compression modernes v2']"));
        assertEquals("Algorithmes de compression modernes v2", sujetModifie.getText());
    }
}
