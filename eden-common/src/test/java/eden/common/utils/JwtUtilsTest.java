package eden.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String SECRET = "EdenNutritionSecretKey2024ForJwtTokenGenerationAndValidationTestKey"; // Ensure length > 256 bits
    private final long EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_ShouldReturnNonEmptyString() {
        // Act
        String token = jwtUtils.generateToken(1L, "testUser");

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void parseToken_ValidToken_ShouldReturnClaims() {
        // Arrange
        Long userId = 100L;
        String username = "testUser";
        String token = jwtUtils.generateToken(userId, username);

        // Act
        Claims claims = jwtUtils.parseToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals(String.valueOf(userId), String.valueOf(claims.get("userId"))); // Claims might store numbers differently
        assertEquals(username, claims.get("username"));
        assertEquals(username, claims.getSubject());
    }

    @Test
    void parseToken_WithExtraClaims_ShouldReturnAllClaims() {
        // Arrange
        Long userId = 101L;
        String username = "admin";
        Map<String, Object> extras = new HashMap<>();
        extras.put("role", "ADMIN");
        
        String token = jwtUtils.generateToken(userId, username, extras);

        // Act
        Claims claims = jwtUtils.parseToken(token);

        // Assert
        assertEquals("ADMIN", claims.get("role"));
        assertEquals(username, claims.get("username"));
    }

    @Test
    void parseToken_ExpiredToken_ShouldThrowException() throws InterruptedException {
        // Arrange: Create a token with very short expiration
        JwtUtils shortLivedJwt = new JwtUtils(SECRET, 1L); // 1ms expiration
        String token = shortLivedJwt.generateToken(1L, "expiredUser");

        // Allow token to expire
        Thread.sleep(10);

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            shortLivedJwt.parseToken(token);
        });
    }
}
