package com.omega.omegamart.controller;

import com.omega.omegamart.dao.UserDao;
import com.omega.omegamart.model.Order;
import com.omega.omegamart.model.Product;
import com.omega.omegamart.model.User;
import com.omega.omegamart.service.OrderService;
import com.omega.omegamart.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Controller providing administrative oversight, user management, and product moderation.
 */
@WebServlet(name = "AdminController", urlPatterns = {"/admin/dashboard", "/admin/product/delete"})
public class AdminController extends HttpServlet {

    private final UserDao userDao = new UserDao();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/admin/dashboard".equals(path)) {
            List<User> users = userDao.findAll();
            List<Product> products = productService.getAllProductsForAdmin();
            List<Order> orders = orderService.getAllOrdersForAdmin();

            req.setAttribute("users", users);
            req.setAttribute("products", products);
            req.setAttribute("orders", orders);

            req.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();

        if ("/admin/product/delete".equals(path)) {
            try {
                Long productId = Long.parseLong(req.getParameter("id"));
                productService.adminDeleteProduct(productId);
                req.getSession().setAttribute("flashSuccess", "Product listing successfully moderated/removed by Administrator.");
            } catch (Exception e) {
                req.getSession().setAttribute("flashError", "Failed to remove product: " + e.getMessage());
            }
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        }
    }
}
