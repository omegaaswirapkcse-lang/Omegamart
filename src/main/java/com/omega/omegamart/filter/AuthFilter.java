package com.omega.omegamart.filter;

import com.omega.omegamart.model.Role;
import com.omega.omegamart.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filter enforcing server-side authentication and role-based access control (RBAC).
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI().substring(contextPath.length());

        // Allow static resources through
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") || uri.equals("/favicon.ico")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        // 1. Admin route protection
        if (uri.startsWith("/admin")) {
            if (currentUser == null) {
                response.sendRedirect(contextPath + "/login?redirect=" + uri);
                return;
            }
            if (currentUser.getRole() != Role.ADMIN) {
                LOGGER.warn("Access denied: User {} ({}) attempted to access admin route {}",
                        currentUser.getEmail(), currentUser.getRole(), uri);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Administrator role required.");
                return;
            }
        }

        // 2. Seller route protection
        if (uri.startsWith("/seller")) {
            if (currentUser == null) {
                response.sendRedirect(contextPath + "/login?redirect=" + uri);
                return;
            }
            if (currentUser.getRole() != Role.SELLER) {
                LOGGER.warn("Access denied: User {} ({}) attempted to access seller route {}",
                        currentUser.getEmail(), currentUser.getRole(), uri);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Seller role required.");
                return;
            }
        }

        // 3. Buyer/Customer protected routes (cart, checkout, my-orders)
        if (uri.startsWith("/buyer") || uri.startsWith("/cart") || uri.startsWith("/checkout") || uri.startsWith("/my-orders")) {
            if (currentUser == null) {
                response.sendRedirect(contextPath + "/login?redirect=" + uri);
                return;
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
