package com.omega.omegamart.controller;

import com.omega.omegamart.exception.ResourceNotFoundException;
import com.omega.omegamart.exception.UnauthorizedException;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.Category;
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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller providing multi-vendor seller inventory and order fulfillment management.
 */
@WebServlet(name = "SellerController", urlPatterns = {
        "/seller/dashboard",
        "/seller/product/new",
        "/seller/product/edit",
        "/seller/product/delete",
        "/seller/orders"
})
public class SellerController extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User seller = getCurrentSeller(req);
        if (seller == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/seller/dashboard");
            return;
        }

        String path = req.getServletPath();

        if ("/seller/dashboard".equals(path)) {
            List<Product> products = productService.getSellerProducts(seller.getId());
            req.setAttribute("products", products);
            req.getRequestDispatcher("/WEB-INF/views/seller-dashboard.jsp").forward(req, resp);

        } else if ("/seller/product/new".equals(path)) {
            List<Category> categories = productService.getAllCategories();
            req.setAttribute("categories", categories);
            req.getRequestDispatcher("/WEB-INF/views/seller-product-form.jsp").forward(req, resp);

        } else if ("/seller/product/edit".equals(path)) {
            String idParam = req.getParameter("id");
            if (idParam == null) {
                resp.sendRedirect(req.getContextPath() + "/seller/dashboard");
                return;
            }
            try {
                Long productId = Long.parseLong(idParam.trim());
                Product product = productService.getProductById(productId);

                // Enforce ownership
                if (!product.getSellerId().equals(seller.getId())) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not own this product.");
                    return;
                }

                List<Category> categories = productService.getAllCategories();
                req.setAttribute("product", product);
                req.setAttribute("categories", categories);
                req.getRequestDispatcher("/WEB-INF/views/seller-product-form.jsp").forward(req, resp);
            } catch (ResourceNotFoundException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found.");
            }

        } else if ("/seller/orders".equals(path)) {
            List<Order> orders = orderService.getSellerOrders(seller.getId());
            req.setAttribute("orders", orders);
            req.getRequestDispatcher("/WEB-INF/views/seller-orders.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User seller = getCurrentSeller(req);
        if (seller == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getServletPath();

        if ("/seller/product/new".equals(path)) {
            handleCreateProduct(req, resp, seller);
        } else if ("/seller/product/edit".equals(path)) {
            handleUpdateProduct(req, resp, seller);
        } else if ("/seller/product/delete".equals(path)) {
            handleDeleteProduct(req, resp, seller);
        }
    }

    private void handleCreateProduct(HttpServletRequest req, HttpServletResponse resp, User seller)
            throws ServletException, IOException {
        try {
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            Long categoryId = Long.parseLong(req.getParameter("categoryId"));
            BigDecimal price = new BigDecimal(req.getParameter("price"));
            int stock = Integer.parseInt(req.getParameter("stockQuantity"));
            String imageUrl = req.getParameter("imageUrl");

            productService.createProduct(seller.getId(), categoryId, name, description, price, stock, imageUrl);
            req.getSession().setAttribute("flashSuccess", "Product successfully listed!");
            resp.sendRedirect(req.getContextPath() + "/seller/dashboard");

        } catch (ValidationException | NumberFormatException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("categories", productService.getAllCategories());
            req.getRequestDispatcher("/WEB-INF/views/seller-product-form.jsp").forward(req, resp);
        }
    }

    private void handleUpdateProduct(HttpServletRequest req, HttpServletResponse resp, User seller)
            throws ServletException, IOException {
        try {
            Long productId = Long.parseLong(req.getParameter("id"));
            String name = req.getParameter("name");
            String description = req.getParameter("description");
            Long categoryId = Long.parseLong(req.getParameter("categoryId"));
            BigDecimal price = new BigDecimal(req.getParameter("price"));
            int stock = Integer.parseInt(req.getParameter("stockQuantity"));
            String imageUrl = req.getParameter("imageUrl");
            boolean isActive = req.getParameter("isActive") != null;

            productService.updateProduct(seller.getId(), productId, categoryId, name, description, price, stock, imageUrl, isActive);
            req.getSession().setAttribute("flashSuccess", "Product successfully updated!");
            resp.sendRedirect(req.getContextPath() + "/seller/dashboard");

        } catch (UnauthorizedException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (ValidationException | NumberFormatException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("categories", productService.getAllCategories());
            req.getRequestDispatcher("/WEB-INF/views/seller-product-form.jsp").forward(req, resp);
        }
    }

    private void handleDeleteProduct(HttpServletRequest req, HttpServletResponse resp, User seller)
            throws IOException {
        try {
            Long productId = Long.parseLong(req.getParameter("id"));
            productService.deleteProduct(seller.getId(), productId);
            req.getSession().setAttribute("flashSuccess", "Product listing removed.");
            resp.sendRedirect(req.getContextPath() + "/seller/dashboard");
        } catch (UnauthorizedException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            req.getSession().setAttribute("flashError", "Failed to remove product: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/seller/dashboard");
        }
    }

    private User getCurrentSeller(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("currentUser") : null;
    }
}
