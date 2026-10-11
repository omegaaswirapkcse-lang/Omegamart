package com.omega.omegamart.controller;

import com.omega.omegamart.exception.ResourceNotFoundException;
import com.omega.omegamart.exception.UnauthorizedException;
import com.omega.omegamart.model.Order;
import com.omega.omegamart.model.User;
import com.omega.omegamart.service.OrderService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller allowing buyers to view their historical orders and order receipt details.
 */
@WebServlet(name = "OrderController", urlPatterns = {"/orders", "/order"})
public class OrderController extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/orders");
            return;
        }

        String path = req.getServletPath();

        if ("/orders".equals(path)) {
            List<Order> orders = orderService.getBuyerOrders(currentUser.getId());
            req.setAttribute("orders", orders);
            req.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(req, resp);

        } else if ("/order".equals(path)) {
            String idParam = req.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/orders");
                return;
            }

            try {
                Long orderId = Long.parseLong(idParam.trim());
                Order order = orderService.getOrderDetails(orderId, currentUser.getId(), currentUser.getRole());
                req.setAttribute("order", order);
                req.getRequestDispatcher("/WEB-INF/views/order-detail.jsp").forward(req, resp);
            } catch (ResourceNotFoundException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found.");
            } catch (UnauthorizedException e) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to view this order.");
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID.");
            }
        }
    }

    private User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("currentUser") : null;
    }
}
