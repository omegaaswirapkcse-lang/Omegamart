package com.omega.omegamart.controller;

import com.omega.omegamart.exception.AuthenticationException;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.Role;
import com.omega.omegamart.model.User;
import com.omega.omegamart.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller handling user registration, authentication, session regeneration, and logout.
 */
@WebServlet(name = "AuthController", urlPatterns = {"/login", "/register", "/logout"})
public class AuthController extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path)) {
            handleLogout(req, resp);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        if ("/login".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        } else if ("/register".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/login".equals(path)) {
            handleLogin(req, resp);
        } else if ("/register".equals(path)) {
            handleRegister(req, resp);
        } else if ("/logout".equals(path)) {
            handleLogout(req, resp);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String redirect = req.getParameter("redirect");

        try {
            User user = authService.login(email, password);

            // Session Fixation Protection: Invalidate old session and regenerate
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession newSession = req.getSession(true);
            newSession.setMaxInactiveInterval(30 * 60); // 30 minutes explicit timeout
            newSession.setAttribute("currentUser", user);

            if (redirect != null && !redirect.trim().isEmpty() && !redirect.startsWith("http")) {
                resp.sendRedirect(req.getContextPath() + redirect);
            } else if (user.getRole() == Role.ADMIN) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            } else if (user.getRole() == Role.SELLER) {
                resp.sendRedirect(req.getContextPath() + "/seller/dashboard");
            } else {
                resp.sendRedirect(req.getContextPath() + "/");
            }

        } catch (AuthenticationException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String fullName = req.getParameter("fullName");
        String roleStr = req.getParameter("role");

        Role role = Role.fromString(roleStr);

        try {
            authService.register(email, password, fullName, role);
            req.getSession(true).setAttribute("flashSuccess", "Registration successful! You can now log in.");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (ValidationException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("email", email);
            req.setAttribute("fullName", fullName);
            req.setAttribute("role", roleStr);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login?loggedOut=true");
    }
}
