package com.omega.omegamart.service;

import com.omega.omegamart.dao.CategoryDao;
import com.omega.omegamart.dao.ProductDao;
import com.omega.omegamart.exception.UnauthorizedException;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.Category;
import com.omega.omegamart.model.Product;
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
class ProductServiceTest {

    @Mock
    private ProductDao productDao;

    @Mock
    private CategoryDao categoryDao;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productDao, categoryDao);
    }

    @Test
    @DisplayName("Should successfully create a product for a seller")
    void testCreateProductSuccess() throws SQLException {
        Category cat = new Category(1L, "Electronics", "Devices");
        when(categoryDao.findById(1L)).thenReturn(Optional.of(cat));
        when(productDao.createProduct(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        Product created = productService.createProduct(
                2L, 1L, "Mechanical Keyboard", "RGB switches",
                new BigDecimal("49.99"), 20, "https://example.com/img.jpg"
        );

        assertNotNull(created);
        assertEquals(100L, created.getId());
        assertEquals(2L, created.getSellerId());
        verify(productDao, times(1)).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("Should reject invalid price and negative stock")
    void testCreateProductInvalidPriceAndStock() {
        assertThrows(ValidationException.class, () ->
                productService.createProduct(2L, 1L, "Item", "Desc", new BigDecimal("-5.00"), 10, "")
        );

        assertThrows(ValidationException.class, () ->
                productService.createProduct(2L, 1L, "Item", "Desc", new BigDecimal("10.00"), -1, "")
        );
    }

    @Test
    @DisplayName("Should enforce seller ownership: reject update if seller does not own product")
    void testUpdateProductUnauthorized() throws SQLException {
        Category cat = new Category(1L, "Electronics", "Devices");
        when(categoryDao.findById(1L)).thenReturn(Optional.of(cat));

        // Product owned by seller 2L
        Product existingProduct = new Product(50L, 2L, 1L, "Original Name", "Desc",
                new BigDecimal("20.00"), 5, "", true);
        when(productDao.findById(50L)).thenReturn(Optional.of(existingProduct));

        // Seller 99L attempts to update product 50L
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                productService.updateProduct(99L, 50L, 1L, "Hacked Name", "Desc",
                        new BigDecimal("20.00"), 5, "", true)
        );

        assertTrue(ex.getMessage().contains("not authorized"));
        verify(productDao, never()).updateProduct(any(Product.class));
    }

    @Test
    @DisplayName("Should enforce seller ownership: reject delete if seller does not own product")
    void testDeleteProductUnauthorized() {
        Product existingProduct = new Product(50L, 2L, 1L, "Original Name", "Desc",
                new BigDecimal("20.00"), 5, "", true);
        when(productDao.findById(50L)).thenReturn(Optional.of(existingProduct));

        // Seller 99L attempts to delete product 50L
        assertThrows(UnauthorizedException.class, () ->
                productService.deleteProduct(99L, 50L)
        );
    }
}
