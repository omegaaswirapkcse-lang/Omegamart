<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Shopping Cart - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<h2 style="margin-bottom: 1.5rem;">Your Shopping Cart</h2>

<c:choose>
    <c:when test="${empty cartItems}">
        <div class="card" style="text-align: center; padding: 4rem 2rem;">
            <div style="font-size: 3.5rem; margin-bottom: 1rem;">🛒</div>
            <h3>Your cart is empty</h3>
            <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">Explore our catalog to find exciting products!</p>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Start Shopping</a>
        </div>
    </c:when>
    <c:otherwise>
        <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 2rem;">
            <!-- CART ITEMS TABLE -->
            <div class="card" style="padding: 1.5rem;">
                <div class="table-responsive">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Product</th>
                                <th>Unit Price</th>
                                <th>Quantity</th>
                                <th>Subtotal</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${cartItems}">
                                <tr>
                                    <td>
                                        <div style="display: flex; align-items: center; gap: 0.75rem;">
                                            <img src="<c:out value='${item.product.imageUrl}'/>" alt=""
                                                 style="width: 50px; height: 50px; object-fit: cover; border-radius: var(--radius-sm);"
                                                 onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'">
                                            <div>
                                                <a href="${pageContext.request.contextPath}/product?id=${item.productId}"
                                                   style="font-weight: 600; text-decoration: none; color: var(--secondary);">
                                                    <c:out value="${item.product.name}"/>
                                                </a>
                                            </div>
                                        </div>
                                    </td>
                                    <td>₹<fmt:formatNumber value="${item.product.price}" minFractionDigits="2"/></td>
                                    <td>
                                        <form action="${pageContext.request.contextPath}/cart/update" method="POST" style="display: flex; align-items: center; gap: 0.5rem; margin: 0;">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                            <input type="hidden" name="productId" value="${item.productId}"/>
                                            <input type="number" name="quantity" value="${item.quantity}" min="1" max="${item.product.stockQuantity}"
                                                   style="width: 65px; padding: 0.3rem;" class="form-control"/>
                                            <button type="submit" class="btn btn-outline btn-sm">Update</button>
                                        </form>
                                    </td>
                                    <td style="font-weight: 700;">₹<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2"/></td>
                                    <td>
                                        <form action="${pageContext.request.contextPath}/cart/remove" method="POST" style="margin: 0;">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                            <input type="hidden" name="productId" value="${item.productId}"/>
                                            <button type="submit" class="btn btn-outline btn-sm" style="color: var(--danger); border-color: #fecaca;">
                                                ✕ Remove
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- ORDER SUMMARY -->
            <div class="card" style="padding: 1.5rem; height: fit-content;">
                <h3 style="font-size: 1.25rem; margin-bottom: 1.25rem;">Order Summary</h3>
                <div style="display: flex; justify-content: space-between; margin-bottom: 0.75rem; color: var(--text-secondary);">
                    <span>Subtotal:</span>
                    <span>₹<fmt:formatNumber value="${cartTotal}" minFractionDigits="2"/></span>
                </div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 0.75rem; color: var(--text-secondary);">
                    <span>Estimated Shipping:</span>
                    <span style="color: var(--success); font-weight: 600;">FREE</span>
                </div>
                <hr style="border: 0; border-top: 1px solid var(--border-color); margin: 1rem 0;">
                <div style="display: flex; justify-content: space-between; font-size: 1.25rem; font-weight: 800; margin-bottom: 1.5rem;">
                    <span>Total Amount:</span>
                    <span style="color: var(--primary);">₹<fmt:formatNumber value="${cartTotal}" minFractionDigits="2"/></span>
                </div>
                <a href="${pageContext.request.contextPath}/checkout" class="btn btn-primary btn-lg" style="width: 100%;">
                    Proceed to Checkout &rarr;
                </a>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="common/footer.jsp"/>
