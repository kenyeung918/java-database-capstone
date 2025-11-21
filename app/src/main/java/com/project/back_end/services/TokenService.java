package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class TokenService {
    
    // Constructor Injection for Dependencies
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration:604800000}") // 7 days default
    private long expiration;

    @Autowired
    public TokenService(AdminRepository adminRepository,
                       DoctorRepository doctorRepository,
                       PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // Get signing key method
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generate token method
    public String generateToken(String identifier, String role) {
        return generateToken(identifier, role, new HashMap<>());
    }

    public String generateToken(String identifier, String role, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(identifier)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract identifier method
    public String extractIdentifier(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Alias method for backward compatibility
    public String getUsernameFromToken(String token) {
        return extractIdentifier(token);
    }

    // Extract role method
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Generic claim extraction method
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract expiration date
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Check if token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Add token validation beyond just user existence
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Enhanced validateToken method with comprehensive checks
    public boolean validateToken(String token, String user) {
        try {
            // Basic token validation
            if (!isTokenValid(token)) {
                return false;
            }

            final String identifier = extractIdentifier(token);
            final String tokenRole = extractRole(token);

            // Check if token role matches requested user type
            if (!user.equalsIgnoreCase(tokenRole)) {
                return false;
            }

            // Check if user exists in database based on role
            switch (user.toLowerCase()) {
                case "admin":
                    return adminRepository.findByUsername(identifier) != null;
                case "doctor":
                    return doctorRepository.findByEmail(identifier) != null;
                case "patient":
                    return patientRepository.findByEmail(identifier) != null;
                default:
                    return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    // Basic token validation without role check
    public boolean validateToken(String token) {
        return isTokenValid(token);
    }

    // Additional utility methods

    /**
     * Generate token with additional user information
     */
    public String generateTokenWithUserInfo(String identifier, String role, Long userId, String userName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userName", userName);
        claims.put("role", role);
        
        return generateToken(identifier, role, claims);
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract user name from token
     */
    public String extractUserName(String token) {
        return extractClaim(token, claims -> claims.get("userName", String.class));
    }

    /**
     * Refresh token by generating a new one with same claims
     */
    public String refreshToken(String token) {
        try {
            final String identifier = extractIdentifier(token);
            final String role = extractRole(token);
            final Claims claims = extractAllClaims(token);
            
            // Create new claims map without auto-generated fields
            Map<String, Object> newClaims = new HashMap<>();
            claims.forEach((key, value) -> {
                if (!key.equals("iat") && !key.equals("exp")) {
                    newClaims.put(key, value);
                }
            });
            
            return generateToken(identifier, role, newClaims);
        } catch (Exception e) {
            throw new RuntimeException("Cannot refresh invalid token: " + e.getMessage());
        }
    }

    /**
     * Get time until token expiration in milliseconds
     */
    public long getTimeUntilExpiration(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Check if token is about to expire (within threshold)
     */
    public boolean isTokenAboutToExpire(String token, long thresholdMs) {
        try {
            long timeUntilExpiration = getTimeUntilExpiration(token);
            return timeUntilExpiration > 0 && timeUntilExpiration <= thresholdMs;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Validate token and check specific user existence
     */
    public boolean validateTokenForUser(String token, String expectedUserType, String expectedIdentifier) {
        try {
            if (!isTokenValid(token)) {
                return false;
            }

            final String actualIdentifier = extractIdentifier(token);
            final String actualRole = extractRole(token);

            // Check if token matches expected user type and identifier
            if (!expectedUserType.equalsIgnoreCase(actualRole) || 
                !expectedIdentifier.equals(actualIdentifier)) {
                return false;
            }

            // Verify user exists in database
            switch (expectedUserType.toLowerCase()) {
                case "admin":
                    return adminRepository.findByUsername(expectedIdentifier) != null;
                case "doctor":
                    return doctorRepository.findByEmail(expectedIdentifier) != null;
                case "patient":
                    return patientRepository.findByEmail(expectedIdentifier) != null;
                default:
                    return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract token issuance date
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Get token age in milliseconds
     */
    public long getTokenAge(String token) {
        try {
            Date issuedAt = extractIssuedAt(token);
            return System.currentTimeMillis() - issuedAt.getTime();
        } catch (Exception e) {
            return -1;
        }
    }
}