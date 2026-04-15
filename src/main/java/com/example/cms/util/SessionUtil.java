package com.example.cms.util;

import com.example.cms.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionUtil {

    private static final String SESSION_USER_KEY = "loggedInUser";

    /**
     * Store user in session after login
     */
    public void setUser(HttpSession session, User user) {
        session.setAttribute(SESSION_USER_KEY, user);
    }

    /**
     * Retrieve logged-in user from session
     */
    public User getUser(HttpSession session) {
        return (User) session.getAttribute(SESSION_USER_KEY);
    }

    /**
     * Check if a user is logged in
     */
    public boolean isLoggedIn(HttpSession session) {
        return getUser(session) != null;
    }

    /**
     * Invalidate session on logout
     */
    public void invalidate(HttpSession session) {
        session.invalidate();
    }

    /**
     * Check if logged-in user has a specific role
     */
    public boolean hasRole(HttpSession session, User.Role role) {
        User user = getUser(session);
        return user != null && user.getRole() == role;
    }

    /**
     * Check if user is ADMIN
     */
    public boolean isAdmin(HttpSession session) {
        return hasRole(session, User.Role.ADMIN);
    }

    /**
     * Check if user is ORGANIZER
     */
    public boolean isOrganizer(HttpSession session) {
        return hasRole(session, User.Role.ORGANIZER);
    }

    /**
     * Check if user is regular USER
     */
    public boolean isUser(HttpSession session) {
        return hasRole(session, User.Role.USER);
    }

    /**
     * Get logged-in user ID
     */
    public Long getUserId(HttpSession session) {
        User user = getUser(session);
        return user != null ? user.getId() : null;
    }

    /**
     * Get logged-in user role
     */
    public User.Role getUserRole(HttpSession session) {
        User user = getUser(session);
        return user != null ? user.getRole() : null;
    }
}
