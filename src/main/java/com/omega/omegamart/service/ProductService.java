package com.omega.omegamart.service;

import com.omega.omegamart.dao.CategoryDao;
import com.omega.omegamart.dao.ProductDao;
import com.omega.omegamart.exception.ResourceNotFoundException;
import com.omega.omegamart.exception.UnauthorizedException;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.Category;
import com.omega.omegamart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service managing product catalog, inventory, and seller product operations.
 */
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private final ProductDao productDao;
    private final CategoryDao categoryDao;

    public ProductService() {
        this(new ProductDao(), new CategoryDao());
    }

    public ProductService(ProductDao productDao, CategoryDao categoryDao) {
        this.productDao = productDao;
        this.categoryDao = categoryDao;
    }

    public Product getProductById(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Invalid product ID.");
        }
        return productDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " was not found."));
    }

    public List<Product> getProducts(String searchKeyword, Long categoryId, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 12;
        int offset = (page - 1) * pageSize;
        return productDao.findAll(searchKeyword, categoryId, offset, pageSize);
    }

    public int getProductCount(String searchKeyword, Long categoryId) {
        return productDao.countAll(searchKeyword, categoryId);
    }

    public List<Product> getSellerProducts(Long sellerId) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("Invalid seller ID.");
        }
        return productDao.findBySellerId(sellerId);
    }

    public List<Product> getAllProductsForAdmin() {
        return productDao.findAllForAdmin();
    }

    public Product createProduct(Long sellerId, Long categoryId, String name, String description,
                                 BigDecimal price, Integer stockQuantity, String imageUrl) {
        validateProductInput(name, categoryId, price, stockQuantity);

        Product product = new Product();
        product.setSellerId(sellerId);
        product.setCategoryId(categoryId);
        product.setName(name.trim());
        product.setDescription(description != null ? description.trim() : "");
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setImageUrl(sanitizeImageUrl(imageUrl));
        product.setIsActive(true);

        try {
            return productDao.createProduct(product);
        } catch (SQLException e) {
            LOGGER.error("Failed to create product for seller {}: {}", sellerId, e.getMessage(), e);
            throw new RuntimeException("Error occurred while saving product.", e);
        }
    }

    public Product updateProduct(Long sellerId, Long productId, Long categoryId, String name,
                                 String description, BigDecimal price, Integer stockQuantity,
                                 String imageUrl, Boolean isActive) {
        validateProductInput(name, categoryId, price, stockQuantity);

        Product existing = getProductById(productId);

        // Security: Strict product ownership enforcement
        if (!existing.getSellerId().equals(sellerId)) {
            LOGGER.warn("Unauthorized product modification attempt. User {} tried to edit product {}", sellerId, productId);
            throw new UnauthorizedException("You are not authorized to modify this product.");
        }

        existing.setCategoryId(categoryId);
        existing.setName(name.trim());
        existing.setDescription(description != null ? description.trim() : "");
        existing.setPrice(price);
        existing.setStockQuantity(stockQuantity);
        existing.setImageUrl(sanitizeImageUrl(imageUrl));
        if (isActive != null) {
            existing.setIsActive(isActive);
        }

        try {
            boolean updated = productDao.updateProduct(existing);
            if (!updated) {
                throw new RuntimeException("Failed to update product.");
            }
            return existing;
        } catch (SQLException e) {
            LOGGER.error("Failed to update product {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Error occurred while updating product.", e);
        }
    }

    public void deleteProduct(Long sellerId, Long productId) {
        Product existing = getProductById(productId);

        // Strict ownership check
        if (!existing.getSellerId().equals(sellerId)) {
            LOGGER.warn("Unauthorized product deletion attempt. User {} tried to delete product {}", sellerId, productId);
            throw new UnauthorizedException("You are not authorized to delete this product.");
        }

        try {
            boolean deleted = productDao.deleteProduct(productId, sellerId);
            if (!deleted) {
                throw new RuntimeException("Failed to delete product.");
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to delete product {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Error occurred while deleting product.", e);
        }
    }

    public void adminDeleteProduct(Long productId) {
        // Verification of existence
        getProductById(productId);
        try {
            boolean deleted = productDao.adminDeleteProduct(productId);
            if (!deleted) {
                throw new RuntimeException("Failed to delete product as admin.");
            }
        } catch (SQLException e) {
            LOGGER.error("Admin deletion failed for product {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Error occurred during administrative deletion.", e);
        }
    }

    public List<Category> getAllCategories() {
        return categoryDao.findAll();
    }

    private void validateProductInput(String name, Long categoryId, BigDecimal price, Integer stockQuantity) {
        if (name == null || name.trim().isEmpty() || name.trim().length() > 255) {
            throw new ValidationException("Product name is required (max 255 characters).");
        }
        if (categoryId == null || categoryId <= 0) {
            throw new ValidationException("Please select a valid product category.");
        }
        if (categoryDao.findById(categoryId).isEmpty()) {
            throw new ValidationException("Selected category does not exist.");
        }
        if (price == null || price.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ValidationException("Product price must be at least 0.01.");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new ValidationException("Stock quantity cannot be negative.");
        }
    }

    private String sanitizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600";
        }
        String trimmed = imageUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600";
    }
}
