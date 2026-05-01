package org.example.iws_websitesneaker.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.example.iws_websitesneaker.entity.TaiKhoan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtil {

    // Secret key đủ mạnh (256 bits)
    @Value("${jwt.secret:myVeryLongSecretKeyThatIsAtLeast256BitsLongForSecurityPurposesAndShouldBeKeptSecretInProduction1234567890}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours
    private long expiration;

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(TaiKhoan user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getEmail())  // Sử dụng API mới
                .claim("userId", user.getId())
                .claim("vaiTro", user.getVaiTro().name())
                .claim("maTaiKhoan", user.getMaTaiKhoan())
                .issuedAt(now)
                .expiration(expiryDate)  // Sử dụng API mới
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Integer extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Integer.class));
    }

    public String extractVaiTro(String token) {
        return extractClaim(token, claims -> claims.get("vaiTro", String.class));
    }

    public String extractMaTaiKhoan(String token) {
        return extractClaim(token, claims -> claims.get("maTaiKhoan", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()  // Sử dụng API mới nhất quán
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();  // Sử dụng getPayload() thay vì getBody()
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;  // Coi như expired nếu có lỗi
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean validateToken(String token) {
        try {
            // Kiểm tra blacklist trước
            if (isTokenBlacklisted(token)) {
                return false;
            }

            // Parse và validate token
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            // Kiểm tra expiration
            return !isTokenExpired(token);

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpirationTime() {
        return expiration;
    }

    // Utility method để debug token
    public void printTokenClaims(String token) {
        try {
            Claims claims = extractAllClaims(token);
            System.out.println("=== TOKEN CLAIMS ===");
            System.out.println("Subject (email): " + claims.getSubject());
            System.out.println("UserId: " + claims.get("userId"));
            System.out.println("VaiTro: " + claims.get("vaiTro"));
            System.out.println("MaTaiKhoan: " + claims.get("maTaiKhoan"));
            System.out.println("Issued At: " + claims.getIssuedAt());
            System.out.println("Expires At: " + claims.getExpiration());
            System.out.println("==================");
        } catch (Exception e) {
            System.out.println("Error parsing token: " + e.getMessage());
        }
    }
}
