package com.omega.omegamart.controller;

import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.CartItem;
import com.omega.omegamart.model.User;
import com.omega.omegamart.service.CartService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller handling shopping cart viewing, item additions, updates, and removals.
 */
@WebServlet(name = "CartController", urlPatterns = {"/cart", "/cart/add", "/cart/update", "/cart/remove"})
public class CartController extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/cart");
            return;
        }

        List<CartItem> cartItems = cartService.getCart(currentUser.getId());
        BigDecimal total = cartService.calculateCartTotal(cartItems);

        req.setAttribute("cartItems", cartItems);
        req.setAttribute("cartTotal", total);
        req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/cart");
            return;
        }

        String path = req.getServletPath();

        try {
            if ("/cart/add".equals(path)) {
                Long productId = Long.parseLong(req.getParameter("productId"));
                int quantity = Integer.parseInt(req.getParameter("quantity"));
                cartService.addToCart(currentUser.getId(), productId, quantity);
                req.getSession().setAttribute("flashSuccess", "Item added to your shopping cart!");

                String redirect = req.getParameter("redirect");
                if (redirect != null && !redirect.trim().isEmpty() && !redirect.startsWith("http")) {
                    resp.sendRedirect(req.getContextPath() + redirect);
                    return;
                }
                resp.sendRedirect(req.getContextPath() + "/cart");

            } else if ("/cart/update".equals(path)) {
                Long productId = Long.parseLong(req.getParameter("productId"));
                int quantity = Integer.parseInt(req.getParameter("quantity"));
                cartService.updateQuantity(currentUser.getId(), productId, quantity);
                resp.sendRedirect(req.getContextPath() + "/cart");

            } else if ("/cart/remove".equals(path)) {
                Long productId = Long.parseLong(req.getParameter("productId"));
                cartService.removeFromCart(currentUser.getId(), productId);
                req.getSession().setAttribute("flashSuccess", "Item removed from your cart.");
                resp.sendRedirect(req.getContextPath() + "/cart");
            }
        } catch (ValidationException e) {
            req.getSession().setAttribute("flashError", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/cart");
        } catch (Exception e) {
            req.getSession().setAttribute("flashError", "An unexpected error occurred with your cart.");
            resp.sendRedirect(req.getContextPath() + "/cart");
        }
    }

    private User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("currentUser") : null;
    }
}
