package com.omega.omegamart.controller;

import com.omega.omegamart.model.Category;
import com.omega.omegamart.model.Product;
import com.omega.omegamart.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Controller serving the marketplace homepage with categories and featured products.
 */
@WebServlet(name = "HomeController", urlPatterns = {"", "/home"})
public class HomeController extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Category> categories = productService.getAllCategories();
        List<Product> featuredProducts = productService.getProducts(null, null, 1, 8);

        req.setAttribute("categories", categories);
        req.setAttribute("featuredProducts", featuredProducts);
        req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
    }
}
