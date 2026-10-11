package com.omega.omegamart.service;

import com.omega.omegamart.dao.OrderDao;
import com.omega.omegamart.dao.ProductDao;
import com.omega.omegamart.dao.ReviewDao;
import com.omega.omegamart.exception.ResourceNotFoundException;
import com.omega.omegamart.exception.UnauthorizedException;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service managing customer reviews, star ratings, and verified-purchase validation.
 */
public class ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewDao reviewDao;
    private final OrderDao orderDao;
    private final ProductDao productDao;

    public ReviewService() {
        this(new ReviewDao(), new OrderDao(), new ProductDao());
    }

    public ReviewService(ReviewDao reviewDao, OrderDao orderDao, ProductDao productDao) {
        this.reviewDao = reviewDao;
        this.orderDao = orderDao;
        this.productDao = productDao;
    }

    public List<Review> getProductReviews(Long productId) {
        if (productId == null || productId <= 0) {
            throw new ValidationException("Invalid product ID.");
        }
        return reviewDao.findByProductId(productId);
    }

    /**
     * Submits a new review.
     * Enforces:
     * 1. Valid star rating (1-5).
     * 2. Non-empty comment.
     * 3. Verified qualifying purchase: user must have purchased the product.
     * 4. Single review policy: duplicate submissions rejected.
     */
    public Review submitReview(Long buyerId, Long productId, int rating, String comment) {
        // 1. Validation
        if (productId == null || productId <= 0) {
            throw new ValidationException("Invalid product ID.");
        }
        if (productDao.findById(productId).isEmpty()) {
            throw new ResourceNotFoundException("Product not found.");
        }
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5 stars.");
        }
        if (comment == null || comment.trim().isEmpty() || comment.trim().length() > 1000) {
            throw new ValidationException("Review comment is required (maximum 1000 characters).");
        }

        // 2. Prevent duplicate reviews
        if (reviewDao.hasUserReviewedProduct(buyerId, productId)) {
            throw new ValidationException("You have already submitted a review for this product.");
        }

        // 3. Verified Purchase Requirement
        Optional<Long> qualifyingOrderOpt = orderDao.getQualifyingOrderIdForReview(buyerId, productId);
        if (qualifyingOrderOpt.isEmpty()) {
            LOGGER.warn("Unverified review attempt. Buyer {} has no completed purchase for product {}", buyerId, productId);
            throw new UnauthorizedException("Reviews can only be submitted after purchasing this product.");
        }

        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(buyerId);
        review.setOrderId(qualifyingOrderOpt.get());
        review.setRating(rating);
        review.setComment(comment.trim());

        try {
            return reviewDao.createReview(review);
        } catch (SQLException e) {
            LOGGER.error("Failed to persist review: {}", e.getMessage(), e);
            throw new RuntimeException("Could not submit review.", e);
        }
    }

    public boolean canUserReviewProduct(Long buyerId, Long productId) {
        if (buyerId == null || productId == null) return false;
        if (reviewDao.hasUserReviewedProduct(buyerId, productId)) return false;
        return orderDao.getQualifyingOrderIdForReview(buyerId, productId).isPresent();
    }
}
