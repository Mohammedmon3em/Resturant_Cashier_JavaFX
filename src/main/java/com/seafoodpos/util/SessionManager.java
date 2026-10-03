package com.seafoodpos.util;

import com.seafoodpos.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public User getCurrentUser()        { return currentUser; }
    public void setCurrentUser(User u)  { this.currentUser = u; }
    public void logout()                { this.currentUser = null; }
    public boolean isLoggedIn()         { return currentUser != null; }
}
