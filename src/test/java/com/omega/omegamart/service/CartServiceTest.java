package com.omega.omegamart.service;

import com.omega.omegamart.dao.CartDao;
import com.omega.omegamart.dao.ProductDao;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.CartItem;
import com.omega.omegamart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartDao cartDao;

    @Mock
    private ProductDao productDao;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartDao, productDao);
    }

    @Test
    @DisplayName("Should successfully add product to cart if stock is sufficient")
    void testAddToCartSuccess() throws SQLException {
        Product p = new Product(1L, 2L, 1L, "Desk Lamp", "LED", new BigDecimal("19.99"), 10, "", true);
        when(productDao.findById(1L)).thenReturn(Optional.of(p));

        cartService.addToCart(5L, 1L, 2);

        verify(cartDao, times(1)).addToCart(5L, 1L, 2);
    }

    @Test
    @DisplayName("Should reject adding to cart if requested quantity exceeds stock")
    void testAddToCartExceedsStock() {
        Product p = new Product(1L, 2L, 1L, "Desk Lamp", "LED", new BigDecimal("19.99"), 3, "", true);
        when(productDao.findById(1L)).thenReturn(Optional.of(p));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                cartService.addToCart(5L, 1L, 5)
        );

        assertTrue(ex.getMessage().contains("exceeds available stock"));
    }

    @Test
    @DisplayName("Should prevent sellers from purchasing their own listed products")
    void testSellerCannotBuyOwnProduct() {
        // Seller ID is 2L, Buyer ID attempting is also 2L
        Product p = new Product(1L, 2L, 1L, "Desk Lamp", "LED", new BigDecimal("19.99"), 10, "", true);
        when(productDao.findById(1L)).thenReturn(Optional.of(p));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                cartService.addToCart(2L, 1L, 1)
        );

        assertTrue(ex.getMessage().contains("Sellers cannot add their own products"));
    }

    @Test
    @DisplayName("Should accurately calculate server-side cart total from database prices")
    void testCalculateCartTotal() {
        List<CartItem> items = new ArrayList<>();

        Product p1 = new Product();
        p1.setPrice(new BigDecimal("25.50"));
        CartItem item1 = new CartItem(1L, 5L, 1L, 2); // 2 * 25.50 = 51.00
        item1.setProduct(p1);
        items.add(item1);

        Product p2 = new Product();
        p2.setPrice(new BigDecimal("10.00"));
        CartItem item2 = new CartItem(2L, 5L, 2L, 3); // 3 * 10.00 = 30.00
        item2.setProduct(p2);
        items.add(item2);

        BigDecimal total = cartService.calculateCartTotal(items);
        assertEquals(new BigDecimal("81.00"), total);
    }
}
