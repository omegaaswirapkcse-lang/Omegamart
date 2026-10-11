package com.omega.omegamart.controller;

import com.omega.omegamart.exception.ResourceNotFoundException;
import com.omega.omegamart.model.Category;
import com.omega.omegamart.model.Product;
import com.omega.omegamart.model.Review;
import com.omega.omegamart.model.User;
import com.omega.omegamart.service.ProductService;
import com.omega.omegamart.service.ReviewService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller handling product discovery, search, category filtering, and product details.
 */
@WebServlet(name = "ProductCatalogController", urlPatterns = {"/products", "/product"})
public class ProductCatalogController extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/products".equals(path)) {
            handleCatalog(req, resp);
        } else if ("/product".equals(path)) {
            handleProductDetail(req, resp);
        }
    }

    private void handleCatalog(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String search = req.getParameter("q");
        String categoryIdParam = req.getParameter("category");
        String pageParam = req.getParameter("page");

        Long categoryId = null;
        if (categoryIdParam != null && !categoryIdParam.trim().isEmpty()) {
            try {
                categoryId = Long.parseLong(categoryIdParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        int page = 1;
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        int pageSize = 12;

        List<Product> products = productService.getProducts(search, categoryId, page, pageSize);
        int totalProducts = productService.getProductCount(search, categoryId);
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
        List<Category> categories = productService.getAllCategories();

        req.setAttribute("products", products);
        req.setAttribute("categories", categories);
        req.setAttribute("searchKeyword", search);
        req.setAttribute("selectedCategoryId", categoryId);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalProducts", totalProducts);

        req.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(req, resp);
    }

    private void handleProductDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        try {
            Long productId = Long.parseLong(idParam.trim());
            Product product = productService.getProductById(productId);
            List<Review> reviews = reviewService.getProductReviews(productId);

            HttpSession session = req.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
            boolean canReview = false;
            if (currentUser != null) {
                canReview = reviewService.canUserReviewProduct(currentUser.getId(), productId);
            }

            req.setAttribute("product", product);
            req.setAttribute("reviews", reviews);
            req.setAttribute("canReview", canReview);
            req.getRequestDispatcher("/WEB-INF/views/product-detail.jsp").forward(req, resp);

        } catch (NumberFormatException | ResourceNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found.");
        }
    }
}
