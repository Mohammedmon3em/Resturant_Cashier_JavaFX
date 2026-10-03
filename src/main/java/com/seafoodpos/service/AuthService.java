package com.seafoodpos.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.seafoodpos.db.UserDAO;
import com.seafoodpos.model.User;
import com.seafoodpos.util.SessionManager;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) throws AuthException {
        if (username == null || username.isBlank())
            throw new AuthException("Username is required / \u0627\u0633\u0645 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645 \u0645\u0637\u0644\u0648\u0628");
        if (password == null || password.isBlank())
            throw new AuthException("Password is required / \u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631 \u0645\u0637\u0644\u0648\u0628\u0629");
        User user = userDAO.findByUsername(username.trim());
        if (user == null)
            throw new AuthException("Invalid username or password / \u0627\u0633\u0645 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645 \u0623\u0648 \u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631 \u063a\u064a\u0631 \u0635\u062d\u064a\u062d\u0629");
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
        if (!result.verified)
            throw new AuthException("Invalid username or password / \u0627\u0633\u0645 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645 \u0623\u0648 \u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631 \u063a\u064a\u0631 \u0635\u062d\u064a\u062d\u0629");
        SessionManager.getInstance().setCurrentUser(user);
        return user;
    }

    public void logout() { SessionManager.getInstance().logout(); }

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
    }
}
