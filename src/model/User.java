package model;

import common.PasswordUtils;

import java.io.Serial;
import java.io.Serializable;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String passwordHash;
    private Role role;

    public User(String username, String password, Role role) {
        this.username = username;
        this.passwordHash = PasswordUtils.hash(password); // Compute hash
        this.role = role;
    }

    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public void setRole(Role newRole) { role = newRole; }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }

    // Check password
    public boolean checkPassword(String password) {
        return PasswordUtils.verify(password, passwordHash);
    }
}