package fr.umfds.spmanager.security;

import fr.umfds.spmanager.model.Role;
import org.junit.jupiter.api.Test;

import static fr.umfds.spmanager.security.JwtUtil.generateToken;
import static fr.umfds.spmanager.security.JwtUtil.validateToken;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {
    @Test
    void testJetonValide() {
        // tests generateToken(uid, login, role) function then validates the output token
        // should return an AuthenticatedUser with the same userId, login & role
        int testId = 1;
        String testUsername = "test";
        Role testRole = Role.STUDENT;

        String token = generateToken(testId, testUsername, testRole);
        AuthenticatedUser user = validateToken(token);
        assertEquals(testId, user.getUserId());
        assertEquals(testUsername, user.getLogin());
        assertEquals(testRole, user.getRole());
    }

    @Test
    void testJetonAltere() {
        // creates a token then modifies it
        // should return an invalid token (null)
        int testId = 2;
        String testUsername = "test2";
        Role testRole = Role.STUDENT;

        String token = generateToken(testId, testUsername, testRole);

        // on modifie UN seul caractère, au milieu de la signature (après le dernier point)
        int pos = token.lastIndexOf('.') + 5;
        char original = token.charAt(pos);
        char modifie = (original == 'A') ? 'B' : 'A'; // toujours différent de l'original
        String tokenAltere = token.substring(0, pos) + modifie + token.substring(pos + 1);

        assertNotEquals(token, tokenAltere); // garde-fou : le jeton a bien changé
        assertNull(validateToken(tokenAltere));
    }

}
