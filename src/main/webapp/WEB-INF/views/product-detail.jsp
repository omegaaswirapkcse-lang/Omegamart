<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="${product.name} - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 3rem; margin-bottom: 3rem;">
    <!-- PRODUCT IMAGE -->
    <div>
        <div style="border-radius: var(--radius-lg); overflow: hidden; background: #f1f5f9; border: 1px solid var(--border-color); height: 420px;">
            <img src="<c:out value='${product.imageUrl}'/>" alt="<c:out value='${product.name}'/>"
                 style="width: 100%; height: 100%; object-fit: cover;"
                 onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'">
        </div>
    </div>

    <!-- PRODUCT DETAILS -->
    <div>
        <span class="product-category-tag"><c:out value="${product.categoryName}"/></span>
        <h1 style="font-size: 2rem; margin-bottom: 0.5rem; color: var(--secondary);"><c:out value="${product.name}"/></h1>

        <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 1.25rem;">
            <div class="product-rating" style="margin-bottom: 0;">
                <span class="star" style="font-size: 1.2rem;">★</span>
                <strong><fmt:formatNumber value="${product.averageRating}" maxFractionDigits="1" minFractionDigits="1"/></strong>
                <span>(<c:out value="${product.reviewCount}"/> reviews)</span>
            </div>
            <span style="color: var(--text-muted);">|</span>
            <span style="color: var(--text-secondary); font-size: 0.9rem;">
                Sold by: <strong><c:out value="${product.sellerName}"/></strong>
            </span>
        </div>

        <div style="font-size: 2rem; font-weight: 800; color: var(--secondary); margin-bottom: 1.5rem;">
            ₹<fmt:formatNumber value="${product.price}" minFractionDigits="2"/>
        </div>

        <div style="margin-bottom: 1.5rem;">
            <p style="color: var(--text-secondary); line-height: 1.7;"><c:out value="${product.description}"/></p>
        </div>

        <div style="margin-bottom: 2rem; padding: 1rem; background: var(--bg-surface); border-radius: var(--radius-md); border: 1px solid var(--border-color);">
            <div style="margin-bottom: 0.5rem; font-size: 0.92rem;">
                <strong>Availability:</strong>
                <c:choose>
                    <c:when test="${product.stockQuantity > 0}">
                        <span style="color: var(--success); font-weight: 600;">In Stock (<c:out value="${product.stockQuantity}"/> units remaining)</span>
                    </c:when>
                    <c:otherwise>
                        <span style="color: var(--danger); font-weight: 600;">Out of Stock</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <c:if test="${product.stockQuantity > 0}">
            <form action="${pageContext.request.contextPath}/cart/add" method="POST" style="display: flex; gap: 1rem; align-items: center;">
                <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                <input type="hidden" name="productId" value="${product.id}"/>
                <div style="width: 100px;">
                    <input type="number" name="quantity" value="1" min="1" max="${product.stockQuantity}" class="form-control" required/>
                </div>
                <button type="submit" class="btn btn-primary btn-lg" style="flex: 1;">
                    🛒 Add to Shopping Cart
                </button>
            </form>
        </c:if>
    </div>
</div>

<!-- REVIEWS SECTION -->
<div style="margin-top: 3rem;">
    <h2 class="section-title">Customer Reviews & Ratings</h2>

    <c:if test="${canReview}">
        <div class="card" style="margin-bottom: 2rem; background: #f0fdf4; border-color: #bbf7d0;">
            <h3 style="font-size: 1.15rem; color: #166534; margin-bottom: 0.75rem;">
                ✓ Verified Purchase: Leave Your Review
            </h3>
            <form action="${pageContext.request.contextPath}/review/submit" method="POST">
                <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                <input type="hidden" name="productId" value="${product.id}"/>
                <div class="form-group">
                    <label class="form-label">Rating (1 to 5 Stars):</label>
                    <select name="rating" class="form-control" style="max-width: 150px;" required>
                        <option value="5">★★★★★ (5 Stars)</option>
                        <option value="4">★★★★☆ (4 Stars)</option>
                        <option value="3">★★★☆☆ (3 Stars)</option>
                        <option value="2">★★☆☆☆ (2 Stars)</option>
                        <option value="1">★☆☆☆☆ (1 Star)</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label">Review Feedback:</label>
                    <textarea name="comment" class="form-control" rows="3" placeholder="Share your experience with this product..." maxlength="1000" required></textarea>
                </div>
                <button type="submit" class="btn btn-primary btn-sm">Submit Review</button>
            </form>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty reviews}">
            <div class="card" style="text-align: center; color: var(--text-secondary); padding: 3rem;">
                No reviews yet for this product. Verified buyers can submit reviews after purchase.
            </div>
        </c:when>
        <c:otherwise>
            <div style="display: flex; flex-direction: column; gap: 1rem;">
                <c:forEach var="rev" items="${reviews}">
                    <div class="card" style="padding: 1.25rem; margin-bottom: 0;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                            <div style="display: flex; align-items: center; gap: 0.75rem;">
                                <strong><c:out value="${rev.userName}"/></strong>
                                <span class="status-badge status-confirmed" style="font-size: 0.72rem;">Verified Buyer</span>
                            </div>
                            <div class="product-rating" style="margin-bottom: 0;">
                                <span class="star">
                                    <c:forEach begin="1" end="${rev.rating}">★</c:forEach>
                                </span>
                            </div>
                        </div>
                        <p style="color: var(--text-primary); font-size: 0.95rem;"><c:out value="${rev.comment}"/></p>
                        <span style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.5rem; display: block;">
                            Reviewed on <fmt:formatDate value="${rev.createdAt}" pattern="dd MMM yyyy"/>
                        </span>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="common/footer.jsp"/>
