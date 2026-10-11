package com.omega.omegamart.filter;

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
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Filter preventing Cross-Site Request Forgery (CSRF) on state-changing requests.
 */
@WebFilter(filterName = "CsrfFilter", urlPatterns = {"/*"})
public class CsrfFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfFilter.class);
    private static final String CSRF_SESSION_ATTR = "csrfToken";
    private static final String CSRF_PARAM_NAME = "csrfToken";
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(true);
        String sessionToken = (String) session.getAttribute(CSRF_SESSION_ATTR);

        if (sessionToken == null) {
            byte[] randomBytes = new byte[24];
            secureRandom.nextBytes(randomBytes);
            sessionToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            session.setAttribute(CSRF_SESSION_ATTR, sessionToken);
        }

        request.setAttribute(CSRF_PARAM_NAME, sessionToken);

        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Validate state-changing requests (excluding JSON APIs like /api/chat which use application/json)
        if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))
                && !uri.contains("/api/")) {

            String requestToken = request.getParameter(CSRF_PARAM_NAME);
            if (requestToken == null || !requestToken.equals(sessionToken)) {
                LOGGER.warn("CSRF token validation failed for URI: {}", uri);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
    }
}
