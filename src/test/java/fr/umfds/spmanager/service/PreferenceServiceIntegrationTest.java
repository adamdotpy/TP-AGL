package fr.umfds.spmanager.service;

import fr.umfds.spmanager.config.DatabaseConfig;
import fr.umfds.spmanager.dto.PreferenceRequest;
import fr.umfds.spmanager.dto.PreferenceResponse;
import fr.umfds.spmanager.exception.BadRequestException;
import fr.umfds.spmanager.exception.ForbiddenException;
import fr.umfds.spmanager.exception.NotFoundException;
import fr.umfds.spmanager.model.Preference;
import fr.umfds.spmanager.model.Role;
import fr.umfds.spmanager.model.StudentGroup;
import fr.umfds.spmanager.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import fr.umfds.spmanager.security.AuthenticatedUser;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration métier + persistance : le service et les repositories
 * sont réels, la base SQLite est une vraie base (fichier jetable dans target/).
 */
public class PreferenceServiceIntegrationTest {

    // Comptes issus de data.sql
    private static final int ID_TEACHER_TURING = 2;
    private static final int ID_STUDENT_JDUPONT = 7;
    private static final int ID_STUDENT_CGOMEZ = 8;

    private PreferenceService preferenceService;
    private GroupRepository groupRepository;
    private int groupId;

    @BeforeEach
    public void setUp() {
        File dbFile = new File("target/test_agl.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        DatabaseConfig.setDbUrl("jdbc:sqlite:target/test_agl.db");
        DatabaseConfig.initDatabase(); // Réinitialise le schéma et les données initiales

        preferenceService = new PreferenceService();
        groupRepository = new GroupRepository();

        // Un groupe dont jdupont est membre
        StudentGroup group = groupRepository.save(new StudentGroup("Groupe test"));
        groupRepository.addMember(group.getId(), ID_STUDENT_JDUPONT);
        groupId = group.getId();
    }

    private AuthenticatedUser jdupont() {
        return new AuthenticatedUser(ID_STUDENT_JDUPONT, "jdupont", Role.STUDENT);
    }

    @Test
    void savePreferencesPersistsRanksInOrder() {
        PreferenceResponse response = preferenceService.savePreferences(
                jdupont(), groupId, new PreferenceRequest(List.of(3, 1, 5, 2, 4)));

        assertEquals(5, response.rankedSubjects().size());

        // On relit directement en base : la persistance est bien effective
        List<Preference> saved = preferenceService.getPreferencesByGroupId(groupId);
        assertEquals(5, saved.size());
        int[] expectedSubjects = {3, 1, 5, 2, 4};
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, saved.get(i).getRank());
            assertEquals(expectedSubjects[i], saved.get(i).getSubjectId());
        }
    }

    @Test
    void savePreferencesTwiceReplacesPreviousChoices() {
        preferenceService.savePreferences(jdupont(), groupId, new PreferenceRequest(List.of(1, 2, 3, 4, 5)));
        preferenceService.savePreferences(jdupont(), groupId, new PreferenceRequest(List.of(5, 4, 3, 2, 1)));

        List<Preference> saved = preferenceService.getPreferencesByGroupId(groupId);
        assertEquals(5, saved.size());
        assertEquals(5, saved.get(0).getSubjectId());
        assertEquals(1, saved.get(4).getSubjectId());
    }

    @Test
    void teacherCannotSavePreferences() {
        AuthenticatedUser teacher = new AuthenticatedUser(ID_TEACHER_TURING, "turing", Role.TEACHER);
        assertThrows(ForbiddenException.class,
                () -> preferenceService.savePreferences(teacher, groupId, new PreferenceRequest(List.of(1, 2, 3, 4, 5))));
    }

    @Test
    void nonMemberCannotSavePreferences() {
        AuthenticatedUser outsider = new AuthenticatedUser(ID_STUDENT_CGOMEZ, "cgomez", Role.STUDENT);
        assertThrows(ForbiddenException.class,
                () -> preferenceService.savePreferences(outsider, groupId, new PreferenceRequest(List.of(1, 2, 3, 4, 5))));
        assertTrue(preferenceService.getPreferencesByGroupId(groupId).isEmpty());
    }

    @Test
    void unknownGroupThrowsNotFound() {
        assertThrows(NotFoundException.class,
                () -> preferenceService.savePreferences(jdupont(), 9999, new PreferenceRequest(List.of(1, 2, 3, 4, 5))));
    }

    @Test
    void unknownSubjectThrowsNotFoundAndSavesNothing() {
        assertThrows(NotFoundException.class,
                () -> preferenceService.savePreferences(jdupont(), groupId, new PreferenceRequest(List.of(1, 2, 3, 4, 9999))));
        assertTrue(preferenceService.getPreferencesByGroupId(groupId).isEmpty());
    }

    @Test
    void lessThanFivePreferencesThrowsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> preferenceService.savePreferences(jdupont(), groupId, new PreferenceRequest(List.of(1, 2, 3))));
    }

    @Test
    void duplicatePreferencesThrowBadRequest() {
        assertThrows(BadRequestException.class,
                () -> preferenceService.savePreferences(jdupont(), groupId, new PreferenceRequest(List.of(1, 1, 2, 3, 4))));
    }

    @Test
    void nullRequestThrowsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> preferenceService.savePreferences(jdupont(), groupId, null));
    }
}
