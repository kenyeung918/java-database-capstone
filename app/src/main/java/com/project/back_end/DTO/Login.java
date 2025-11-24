package com.project.back_end.DTO;

public class Login {
    
    // 1. 'identifier' field:
    private String identifier;
    
    // 2. 'password' field:
    private String password;

    // 3. Constructors:

    // Default constructor (required for frameworks like Spring)
    public Login() {
    }

    // Parameterized constructor for convenience
    public Login(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    // 4. Getters and Setters:

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Optional: Helper methods for better usability

    /**
     * Checks if the identifier is an email based on common email pattern
     * @return true if the identifier contains '@' symbol
     */
    public boolean isEmailIdentifier() {
        return identifier != null && identifier.contains("@");
    }

    /**
     * Checks if the identifier is likely a username (no '@' symbol)
     * @return true if the identifier doesn't contain '@' symbol
     */
    public boolean isUsernameIdentifier() {
        return identifier != null && !identifier.contains("@");
    }

    /**
     * Validates that both identifier and password are provided
     * @return true if both fields are not null and not empty
     */
    public boolean isValid() {
        return identifier != null && !identifier.trim().isEmpty() &&
               password != null && !password.trim().isEmpty();
    }

    // Optional: toString method for debugging
    @Override
    public String toString() {
        return "Login{" +
                "identifier='" + identifier + '\'' +
                ", password='[PROTECTED]'" + // Don't log actual password
                '}';
    }

    // Optional: equals and hashCode methods for testing
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Login login = (Login) o;
        return java.util.Objects.equals(identifier, login.identifier) &&
               java.util.Objects.equals(password, login.password);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(identifier, password);
    }

    public String getEmail() {        
        throw new UnsupportedOperationException("Unimplemented method 'getEmail'");
    }
}