package com.omega.omegamart.controller;

import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.CartItem;
import com.omega.omegamart.model.Order;
import com.omega.omegamart.model.User;
import com.omega.omegamart.service.CartService;
import com.omega.omegamart.service.OrderService;

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
 * Controller handling the checkout procedure with simulated payment confirmation.
 */
@WebServlet(name = "CheckoutController", urlPatterns = {"/checkout", "/checkout/process"})
public class CheckoutController extends HttpServlet {

    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        List<CartItem> cartItems = cartService.getCart(currentUser.getId());
        if (cartItems.isEmpty()) {
            req.getSession().setAttribute("flashError", "Your cart is empty. Add items before proceeding to checkout.");
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        BigDecimal total = cartService.calculateCartTotal(cartItems);

        req.setAttribute("cartItems", cartItems);
        req.setAttribute("cartTotal", total);
        req.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        String shippingAddress = req.getParameter("shippingAddress");

        try {
            Order completedOrder = orderService.checkout(currentUser.getId(), shippingAddress);
            req.getSession().setAttribute("flashSuccess", "Order successfully placed! Order Number: " + completedOrder.getOrderNumber());
            resp.sendRedirect(req.getContextPath() + "/order?id=" + completedOrder.getId());
        } catch (ValidationException e) {
            req.setAttribute("errorMessage", e.getMessage());
            List<CartItem> cartItems = cartService.getCart(currentUser.getId());
            BigDecimal total = cartService.calculateCartTotal(cartItems);
            req.setAttribute("cartItems", cartItems);
            req.setAttribute("cartTotal", total);
            req.setAttribute("shippingAddress", shippingAddress);
            req.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "An unexpected error occurred during checkout. Please try again.");
            List<CartItem> cartItems = cartService.getCart(currentUser.getId());
            BigDecimal total = cartService.calculateCartTotal(cartItems);
            req.setAttribute("cartItems", cartItems);
            req.setAttribute("cartTotal", total);
            req.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(req, resp);
        }
    }

    private User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("currentUser") : null;
    }
}
