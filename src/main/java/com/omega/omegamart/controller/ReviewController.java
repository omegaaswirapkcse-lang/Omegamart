package com.omega.omegamart.controller;

import com.omega.omegamart.exception.UnauthorizedException;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.User;
import com.omega.omegamart.service.ReviewService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller handling verified product reviews and star rating submissions.
 */
@WebServlet(name = "ReviewController", urlPatterns = {"/review/submit"})
public class ReviewController extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        String productIdParam = req.getParameter("productId");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/product?id=" + productIdParam);
            return;
        }

        try {
            Long productId = Long.parseLong(productIdParam);
            int rating = Integer.parseInt(req.getParameter("rating"));
            String comment = req.getParameter("comment");

            reviewService.submitReview(currentUser.getId(), productId, rating, comment);
            session.setAttribute("flashSuccess", "Thank you! Your verified purchase review has been published.");

        } catch (UnauthorizedException | ValidationException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("flashError", "Failed to submit review. Please try again.");
        }

        resp.sendRedirect(req.getContextPath() + "/product?id=" + productIdParam);
    }
}
