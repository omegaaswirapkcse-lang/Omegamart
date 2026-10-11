package com.omega.omegamart.service;

import com.omega.omegamart.dao.CartDao;
import com.omega.omegamart.dao.ProductDao;
import com.omega.omegamart.exception.ResourceNotFoundException;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.CartItem;
import com.omega.omegamart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Service managing shopping cart operations, stock availability checks, and server-side totals.
 */
public class CartService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);

    private final CartDao cartDao;
    private final ProductDao productDao;

    public CartService() {
        this(new CartDao(), new ProductDao());
    }

    public CartService(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    public List<CartItem> getCart(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("Invalid user ID.");
        }
        return cartDao.getCartItems(userId);
    }

    public void addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than zero.");
        }
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ValidationException("This product is currently inactive and cannot be purchased.");
        }

        // Sellers cannot purchase their own products
        if (userId.equals(product.getSellerId())) {
            throw new ValidationException("Sellers cannot add their own products to the cart.");
        }

        if (product.getStockQuantity() < quantity) {
            throw new ValidationException("Requested quantity (" + quantity + ") exceeds available stock (" + product.getStockQuantity() + ").");
        }

        try {
            cartDao.addToCart(userId, productId, quantity);
        } catch (SQLException e) {
            LOGGER.error("Error adding product {} to cart for user {}: {}", productId, userId, e.getMessage(), e);
            throw new RuntimeException("Could not update shopping cart.", e);
        }
    }

    public void updateQuantity(Long userId, Long productId, int quantity) {
        if (quantity < 0) {
            throw new ValidationException("Quantity cannot be negative.");
        }
        if (quantity == 0) {
            removeFromCart(userId, productId);
            return;
        }

        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

        if (product.getStockQuantity() < quantity) {
            throw new ValidationException("Requested quantity (" + quantity + ") exceeds available stock (" + product.getStockQuantity() + ").");
        }

        try {
            cartDao.updateQuantity(userId, productId, quantity);
        } catch (SQLException e) {
            LOGGER.error("Error updating cart quantity: {}", e.getMessage(), e);
            throw new RuntimeException("Could not update cart quantity.", e);
        }
    }

    public void removeFromCart(Long userId, Long productId) {
        try {
            cartDao.removeFromCart(userId, productId);
        } catch (SQLException e) {
            LOGGER.error("Error removing product from cart: {}", e.getMessage(), e);
            throw new RuntimeException("Could not remove item from cart.", e);
        }
    }

    public int getCartCount(Long userId) {
        return cartDao.getCartCount(userId);
    }

    public BigDecimal calculateCartTotal(List<CartItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (CartItem item : items) {
                total = total.add(item.getSubtotal());
            }
        }
        return total;
    }
}
