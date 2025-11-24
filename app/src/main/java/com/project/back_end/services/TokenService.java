package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
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

    /**
     * Generates a JWT token for a given user's identifier
     * @param identifier The unique identifier for the user — username for Admin, email for Doctor and Patient
     * @return The generated JWT token
     */
    public String generateToken(String identifier) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(identifier)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the identifier (subject) from a JWT token
     * @param token The JWT token from which the identifier is to be extracted
     * @return The identifier extracted from the token
     */
    public String extractIdentifier(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates the JWT token for a given user type (admin, doctor, or patient)
     * @param token The JWT token to be validated
     * @param user The type of user (e.g., admin, doctor, patient)
     * @return true if the token is valid for the specified user type, false otherwise
     */
    public boolean validateToken(String token, String user) {
        try {
            // First validate the token structure and expiration
            if (!isTokenValid(token)) {
                return false;
            }

            // Extract identifier from token
            String identifier = extractIdentifier(token);

            // Check if user exists in database based on user type
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

    // Generic claim extraction method
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from token - FIXED for JJWT 0.12.6
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extract expiration date
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Check if token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Basic token validation (structure and expiration)
    private boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {        
        throw new UnsupportedOperationException("Unimplemented method 'getUsernameFromToken'");
    }
}