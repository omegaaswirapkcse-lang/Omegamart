<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Product Catalog - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div style="margin-bottom: 2rem;">
    <h2>Product Catalog</h2>
    <p style="color: var(--text-secondary);">Browse our wide range of products from certified multi-vendor sellers.</p>
</div>

<div class="category-chips">
    <a href="${pageContext.request.contextPath}/products"
       class="cat-chip ${empty selectedCategoryId ? 'active' : ''}">All Categories</a>
    <c:forEach var="cat" items="${categories}">
        <a href="${pageContext.request.contextPath}/products?category=${cat.id}&q=<c:out value='${searchKeyword}'/>"
           class="cat-chip ${selectedCategoryId == cat.id ? 'active' : ''}">
            <c:out value="${cat.name}"/>
        </a>
    </c:forEach>
</div>

<c:choose>
    <c:when test="${empty products}">
        <div class="card" style="text-align: center; padding: 4rem 2rem;">
            <div style="font-size: 3rem; margin-bottom: 1rem;">🔍</div>
            <h3>No products found</h3>
            <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">
                Try adjusting your search keyword or selecting a different category.
            </p>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-outline">Clear Filters</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="product-grid">
            <c:forEach var="prod" items="${products}">
                <div class="product-card">
                    <div class="product-image-wrap">
                        <img src="<c:out value='${prod.imageUrl}'/>" alt="<c:out value='${prod.name}'/>" loading="lazy"
                             onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'">
                    </div>
                    <div class="product-card-body">
                        <span class="product-category-tag"><c:out value="${prod.categoryName}"/></span>
                        <a href="${pageContext.request.contextPath}/product?id=${prod.id}" class="product-title">
                            <c:out value="${prod.name}"/>
                        </a>
                        <div class="product-rating">
                            <span class="star">★</span>
                            <strong><fmt:formatNumber value="${prod.averageRating}" maxFractionDigits="1" minFractionDigits="1"/></strong>
                            <span>(<c:out value="${prod.reviewCount}"/> reviews)</span>
                        </div>
                        <div class="product-footer">
                            <span class="product-price">₹<fmt:formatNumber value="${prod.price}" minFractionDigits="2"/></span>
                            <c:choose>
                                <c:when test="${prod.stockQuantity > 0}">
                                    <form action="${pageContext.request.contextPath}/cart/add" method="POST" style="margin: 0;">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                        <input type="hidden" name="productId" value="${prod.id}"/>
                                        <input type="hidden" name="quantity" value="1"/>
                                        <button type="submit" class="btn btn-primary btn-sm">Add to Cart</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-badge" style="background: #fee2e2; color: #991b1b;">Out of Stock</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>

        <!-- PAGINATION -->
        <c:if test="${totalPages > 1}">
            <div style="display: flex; justify-content: center; gap: 0.5rem; margin-top: 2rem;">
                <c:forEach var="pageNum" begin="1" end="${totalPages}">
                    <a href="${pageContext.request.contextPath}/products?page=${pageNum}&category=<c:out value='${selectedCategoryId}'/>&q=<c:out value='${searchKeyword}'/>"
                       class="btn btn-sm ${currentPage == pageNum ? 'btn-primary' : 'btn-outline'}">
                        ${pageNum}
                    </a>
                </c:forEach>
            </div>
        </c:if>
    </c:otherwise>
</c:choose>

<jsp:include page="common/footer.jsp"/>
