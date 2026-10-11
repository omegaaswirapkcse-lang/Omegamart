package com.omega.omegamart.service;

import com.omega.omegamart.dao.OrderDao;
import com.omega.omegamart.dao.ProductDao;
import com.omega.omegamart.dao.ReviewDao;
import com.omega.omegamart.exception.UnauthorizedException;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.Product;
import com.omega.omegamart.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewDao reviewDao;

    @Mock
    private OrderDao orderDao;

    @Mock
    private ProductDao productDao;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewDao, orderDao, productDao);
    }

    @Test
    @DisplayName("Should successfully submit review if buyer has a verified qualifying purchase")
    void testSubmitReviewVerifiedPurchaseSuccess() throws SQLException {
        Product p = new Product(1L, 2L, 1L, "Laptop", "Desc", new BigDecimal("500"), 10, "", true);
        when(productDao.findById(1L)).thenReturn(Optional.of(p));
        when(reviewDao.hasUserReviewedProduct(4L, 1L)).thenReturn(false);
        // Buyer 4 has qualifying order 101L
        when(orderDao.getQualifyingOrderIdForReview(4L, 1L)).thenReturn(Optional.of(101L));

        when(reviewDao.createReview(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(500L);
            return r;
        });

        Review result = reviewService.submitReview(4L, 1L, 5, "Outstanding performance!");

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals(101L, result.getOrderId());
        verify(reviewDao, times(1)).createReview(any(Review.class));
    }

    @Test
    @DisplayName("Should reject review submission if buyer has not purchased the product")
    void testSubmitReviewUnverifiedPurchaseRejected() {
        Product p = new Product(1L, 2L, 1L, "Laptop", "Desc", new BigDecimal("500"), 10, "", true);
        when(productDao.findById(1L)).thenReturn(Optional.of(p));
        when(reviewDao.hasUserReviewedProduct(4L, 1L)).thenReturn(false);
        // No qualifying order found
        when(orderDao.getQualifyingOrderIdForReview(4L, 1L)).thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                reviewService.submitReview(4L, 1L, 5, "Great product!")
        );

        assertTrue(ex.getMessage().contains("only be submitted after purchasing"));
    }

    @Test
    @DisplayName("Should reject duplicate review if user already reviewed product")
    void testSubmitDuplicateReviewRejected() {
        Product p = new Product(1L, 2L, 1L, "Laptop", "Desc", new BigDecimal("500"), 10, "", true);
        when(productDao.findById(1L)).thenReturn(Optional.of(p));
        when(reviewDao.hasUserReviewedProduct(4L, 1L)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                reviewService.submitReview(4L, 1L, 5, "Duplicate review")
        );

        assertTrue(ex.getMessage().contains("already submitted a review"));
    }

    @Test
    @DisplayName("Should validate rating between 1 and 5 stars")
    void testInvalidRatingBounds() {
        Product p = new Product(1L, 2L, 1L, "Laptop", "Desc", new BigDecimal("500"), 10, "", true);
        when(productDao.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(ValidationException.class, () ->
                reviewService.submitReview(4L, 1L, 0, "Rating zero is invalid")
        );

        assertThrows(ValidationException.class, () ->
                reviewService.submitReview(4L, 1L, 6, "Rating six is invalid")
        );
    }
}
