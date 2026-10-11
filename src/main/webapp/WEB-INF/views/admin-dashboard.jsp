<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Administrator Dashboard - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div style="margin-bottom: 2rem;">
    <h2>Administrator Oversight Console</h2>
    <p style="color: var(--text-secondary);">Marketplace governance, user directory, product moderation, and global order review.</p>
</div>

<!-- METRIC CARDS -->
<div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; margin-bottom: 2rem;">
    <div class="card" style="padding: 1.5rem; margin-bottom: 0;">
        <span style="color: var(--text-muted); font-size: 0.85rem; font-weight: 600; text-transform: uppercase;">Total Users</span>
        <div style="font-size: 2rem; font-weight: 800; color: var(--secondary); margin-top: 0.25rem;"><c:out value="${users.size()}"/></div>
    </div>
    <div class="card" style="padding: 1.5rem; margin-bottom: 0;">
        <span style="color: var(--text-muted); font-size: 0.85rem; font-weight: 600; text-transform: uppercase;">Total Products</span>
        <div style="font-size: 2rem; font-weight: 800; color: var(--primary); margin-top: 0.25rem;"><c:out value="${products.size()}"/></div>
    </div>
    <div class="card" style="padding: 1.5rem; margin-bottom: 0;">
        <span style="color: var(--text-muted); font-size: 0.85rem; font-weight: 600; text-transform: uppercase;">Total Orders</span>
        <div style="font-size: 2rem; font-weight: 800; color: var(--success); margin-top: 0.25rem;"><c:out value="${orders.size()}"/></div>
    </div>
</div>

<!-- USERS TABLE -->
<div class="card" style="padding: 1.5rem; margin-bottom: 2rem;">
    <h3 style="font-size: 1.2rem; margin-bottom: 1rem;">Registered Marketplace Accounts</h3>
    <div class="table-responsive">
        <table class="table">
            <thead>
                <tr>
                    <th>User ID</th>
                    <th>Full Name</th>
                    <th>Email Address</th>
                    <th>Role</th>
                    <th>Registration Date</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="u" items="${users}">
                    <tr>
                        <td>#<c:out value="${u.id}"/></td>
                        <td><strong><c:out value="${u.fullName}"/></strong></td>
                        <td><c:out value="${u.email}"/></td>
                        <td>
                            <span class="status-badge" style="background: ${u.role == 'ADMIN' ? '#fee2e2; color: #991b1b' : (u.role == 'SELLER' ? '#fef3c7; color: #92400e' : '#e0f2fe; color: #0369a1')}">
                                <c:out value="${u.role}"/>
                            </span>
                        </td>
                        <td><fmt:formatDate value="${u.createdAt}" pattern="dd MMM yyyy, HH:mm"/></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<!-- PRODUCT MODERATION TABLE -->
<div class="card" style="padding: 1.5rem; margin-bottom: 2rem;">
    <h3 style="font-size: 1.2rem; margin-bottom: 1rem;">Product Catalog Moderation</h3>
    <div class="table-responsive">
        <table class="table">
            <thead>
                <tr>
                    <th>Product</th>
                    <th>Seller</th>
                    <th>Category</th>
                    <th>Price</th>
                    <th>Stock</th>
                    <th>Status</th>
                    <th>Moderation Action</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="p" items="${products}">
                    <tr>
                        <td>
                            <strong><c:out value="${p.name}"/></strong>
                        </td>
                        <td><c:out value="${p.sellerName}"/></td>
                        <td><c:out value="${p.categoryName}"/></td>
                        <td>₹<fmt:formatNumber value="${p.price}" minFractionDigits="2"/></td>
                        <td><c:out value="${p.stockQuantity}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${p.isActive}">
                                    <span class="status-badge status-confirmed">Active</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-badge" style="background: #fee2e2; color: #991b1b;">Moderated / Inactive</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:if test="${p.isActive}">
                                <form action="${pageContext.request.contextPath}/admin/product/delete" method="POST" style="margin: 0;"
                                      onsubmit="return confirm('Moderate and deactivate this product listing?');">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                    <input type="hidden" name="id" value="${p.id}"/>
                                    <button type="submit" class="btn btn-outline btn-sm" style="color: var(--danger); border-color: #fecaca;">
                                        Moderate / Remove
                                    </button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<!-- GLOBAL ORDERS TABLE -->
<div class="card" style="padding: 1.5rem;">
    <h3 style="font-size: 1.2rem; margin-bottom: 1rem;">Platform Order Transactions</h3>
    <div class="table-responsive">
        <table class="table">
            <thead>
                <tr>
                    <th>Order Ref</th>
                    <th>Buyer</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Date</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="ord" items="${orders}">
                    <tr>
                        <td><strong><c:out value="${ord.orderNumber}"/></strong></td>
                        <td><c:out value="${ord.buyerName}"/></td>
                        <td>₹<fmt:formatNumber value="${ord.totalAmount}" minFractionDigits="2"/></td>
                        <td><span class="status-badge status-${ord.status.toLowerCase()}"><c:out value="${ord.status}"/></span></td>
                        <td><fmt:formatDate value="${ord.createdAt}" pattern="dd MMM yyyy, HH:mm"/></td>
                        <td>
                            <a href="${pageContext.request.contextPath}/order?id=${ord.id}" class="btn btn-outline btn-sm">
                                View Order
                            </a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="common/footer.jsp"/>
