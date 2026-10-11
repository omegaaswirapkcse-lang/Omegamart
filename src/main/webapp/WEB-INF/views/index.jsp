<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="OmegaMart - Home" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div class="hero-banner">
    <h1>Welcome to OmegaMart</h1>
    <p>The next-generation multi-vendor e-commerce marketplace powered by Java Servlets, JDBC, and intelligent assistance.</p>
    <div style="display: flex; gap: 1rem; justify-content: center;">
        <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-lg" style="background: white; color: var(--primary);">
            Explore Products
        </a>
        <a href="${pageContext.request.contextPath}/register" class="btn btn-outline btn-lg" style="color: white; border-color: white;">
            Join as Seller
        </a>
    </div>
</div>

<h2 class="section-title">Shop by Category</h2>
<div class="category-chips">
    <a href="${pageContext.request.contextPath}/products" class="cat-chip active">All Products</a>
    <c:forEach var="cat" items="${categories}">
        <a href="${pageContext.request.contextPath}/products?category=${cat.id}" class="cat-chip">
            <c:out value="${cat.name}"/>
        </a>
    </c:forEach>
</div>

<div class="section-title">
    <span>Featured Products</span>
    <a href="${pageContext.request.contextPath}/products" style="font-size: 0.95rem; color: var(--primary); text-decoration: none;">View All &rarr;</a>
</div>

<div class="product-grid">
    <c:forEach var="prod" items="${featuredProducts}">
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

<jsp:include page="common/footer.jsp"/>
