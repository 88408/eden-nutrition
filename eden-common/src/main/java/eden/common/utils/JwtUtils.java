package eden.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /** 默认密钥（生产环境应从配置文件读取） */
    private static final String DEFAULT_SECRET = "EdenNutritionSecretKey2024ForJwtTokenGenerationAndValidation";

    /** 默认过期时间：24小时 */
    private static final long DEFAULT_EXPIRATION = 24 * 60 * 60 * 1000L;

    /** Token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** Header 名称 */
    public static final String TOKEN_HEADER = "Authorization";

    private String secret = DEFAULT_SECRET;
    private long expiration = DEFAULT_EXPIRATION;

    public JwtUtils() {
    }

    public JwtUtils(String secret, long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    /**
     * 获取密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return createToken(claims, username);
    }

    /**
     * 生成 Token（带额外信息）
     */
    public String generateToken(Long userId, String username, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("userId", userId);
        claims.put("username", username);
        return createToken(claims, username);
    }

    /**
     * 创建 Token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            logger.warn("Token解析失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 判断 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * 刷新 Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            claims.setIssuedAt(new Date());
            claims.setExpiration(new Date(System.currentTimeMillis() + expiration));
            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (JwtException e) {
            logger.error("Token刷新失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从请求头中提取 Token
     */
    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return bearerToken;
    }

    // Getters and Setters
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
