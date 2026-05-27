package Ressourcenmanager.auth;

import Ressourcenmanager.user.UserRole;
import java.io.Serializable;

public class SessionUser implements Serializable {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final UserRole role;

    public SessionUser(Long id, String firstName, String lastName, UserRole role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public Long getId() { return id; }
    public UserRole getRole() { return role; }
    public String getName() {
        return firstName + " " + lastName;
    }
}
