package com.seafoodpos.model;

public class User {
    private int    id;
    private String username;
    private String password;
    private String fullName;
    private Role   role;
    private boolean active;
    private String createdAt;

    public User() {}

    public User(int id, String username, String password, String fullName,
                Role role, boolean active, String createdAt) {
        this.id        = id;
        this.username  = username;
        this.password  = password;
        this.fullName  = fullName;
        this.role      = role;
        this.active    = active;
        this.createdAt = createdAt;
    }

    public int     getId()        { return id; }
    public void    setId(int id)  { this.id = id; }
    public String  getUsername()  { return username; }
    public void    setUsername(String u) { this.username = u; }
    public String  getPassword()  { return password; }
    public void    setPassword(String p) { this.password = p; }
    public String  getFullName()  { return fullName; }
    public void    setFullName(String fn){ this.fullName = fn; }
    public Role    getRole()      { return role; }
    public void    setRole(Role r){ this.role = r; }
    public boolean isActive()     { return active; }
    public void    setActive(boolean a){ this.active = a; }
    public String  getCreatedAt() { return createdAt; }
    public void    setCreatedAt(String c){ this.createdAt = c; }

    @Override
    public String toString() {
        return fullName + " (" + username + ") - " + (role != null ? role.getDisplayName() : "");
    }
}
