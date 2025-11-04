package xyz.shurlin.demo2.data.network;

import java.time.LocalDateTime;

public class RegisterRequest {
    private String username;
    private String password;
    private String displayName;
    private String email;

    public RegisterRequest(String username, String password, String displayName, String email) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }
}
