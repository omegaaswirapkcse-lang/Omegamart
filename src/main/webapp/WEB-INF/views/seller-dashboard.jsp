<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Seller Dashboard - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
    <div>
        <h2>Seller Inventory Dashboard</h2>
        <p style="color: var(--text-secondary);">Manage your product catalog, update stock levels, and monitor customer sales.</p>
    </div>
    <div>
        <a href="${pageContext.request.contextPath}/seller/product/new" class="btn btn-primary">
            + Add New Product
        </a>
    </div>
</div>

<div class="card" style="padding: 1.5rem;">
    <h3 style="font-size: 1.2rem; margin-bottom: 1.25rem;">My Listed Products (<c:out value="${products.size()}"/>)</h3>

    <c:choose>
        <c:when test="${empty products}">
            <div style="text-align: center; padding: 3rem 1rem; color: var(--text-secondary);">
                <p style="margin-bottom: 1rem;">You have not listed any products yet.</p>
                <a href="${pageContext.request.contextPath}/seller/product/new" class="btn btn-outline btn-sm">List Your First Product</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Product</th>
                            <th>Category</th>
                            <th>Price</th>
                            <th>Stock</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${products}">
                            <tr>
                                <td>
                                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                                        <img src="<c:out value='${p.imageUrl}'/>" alt=""
                                             style="width: 45px; height: 45px; object-fit: cover; border-radius: var(--radius-sm);"
                                             onerror="this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600'">
                                        <div>
                                            <strong><c:out value="${p.name}"/></strong>
                                        </div>
                                    </div>
                                </td>
                                <td><c:out value="${p.categoryName}"/></td>
                                <td>₹<fmt:formatNumber value="${p.price}" minFractionDigits="2"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.stockQuantity > 5}">
                                            <span style="color: var(--success); font-weight: 600;"><c:out value="${p.stockQuantity}"/> units</span>
                                        </c:when>
                                        <c:when test="${p.stockQuantity > 0}">
                                            <span style="color: var(--warning); font-weight: 600;"><c:out value="${p.stockQuantity}"/> (Low Stock)</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: var(--danger); font-weight: 600;">0 (Out of Stock)</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.isActive}">
                                            <span class="status-badge status-confirmed">Active</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status-badge" style="background: #f1f5f9; color: #64748b;">Inactive</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div style="display: flex; gap: 0.5rem;">
                                        <a href="${pageContext.request.contextPath}/seller/product/edit?id=${p.id}" class="btn btn-outline btn-sm">
                                            Edit
                                        </a>
                                        <form action="${pageContext.request.contextPath}/seller/product/delete" method="POST" style="margin: 0;"
                                              onsubmit="return confirm('Are you sure you want to remove this product listing?');">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                            <input type="hidden" name="id" value="${p.id}"/>
                                            <button type="submit" class="btn btn-outline btn-sm" style="color: var(--danger); border-color: #fecaca;">
                                                Delete
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="common/footer.jsp"/>
