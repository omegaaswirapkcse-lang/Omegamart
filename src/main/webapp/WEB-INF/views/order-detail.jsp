<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Order Receipt ${order.orderNumber} - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
    <div>
        <h2>Order Confirmation Receipt</h2>
        <p style="color: var(--text-secondary);">Order Reference: <strong><c:out value="${order.orderNumber}"/></strong></p>
    </div>
    <div>
        <span class="status-badge status-${order.status.toLowerCase()}" style="font-size: 0.9rem; padding: 0.4rem 0.8rem;">
            <c:out value="${order.status}"/>
        </span>
    </div>
</div>

<div style="display: grid; grid-template-columns: 2fr 1fr; gap: 2rem;">
    <!-- ORDER ITEMS -->
    <div class="card" style="padding: 1.5rem;">
        <h3 style="font-size: 1.15rem; margin-bottom: 1rem;">Purchased Items</h3>
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Item</th>
                        <th>Seller</th>
                        <th>Unit Price</th>
                        <th>Qty</th>
                        <th>Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${order.items}">
                        <tr>
                            <td>
                                <a href="${pageContext.request.contextPath}/product?id=${item.productId}"
                                   style="font-weight: 600; text-decoration: none; color: var(--secondary);">
                                    <c:out value="${item.productName}"/>
                                </a>
                            </td>
                            <td><c:out value="${item.sellerName}"/></td>
                            <td>₹<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2"/></td>
                            <td><c:out value="${item.quantity}"/></td>
                            <td style="font-weight: 700;">₹<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <!-- ORDER SUMMARY AND METADATA -->
    <div class="card" style="padding: 1.5rem; height: fit-content;">
        <h3 style="font-size: 1.15rem; margin-bottom: 1rem;">Order Overview</h3>
        <div style="display: flex; flex-direction: column; gap: 0.75rem; font-size: 0.92rem;">
            <div>
                <span style="color: var(--text-muted); display: block;">Customer:</span>
                <strong><c:out value="${order.buyerName}"/></strong> (<c:out value="${order.buyerEmail}"/>)
            </div>
            <div>
                <span style="color: var(--text-muted); display: block;">Order Date:</span>
                <fmt:formatDate value="${order.createdAt}" pattern="dd MMMM yyyy, HH:mm:ss"/>
            </div>
            <div>
                <span style="color: var(--text-muted); display: block;">Payment Mode:</span>
                <span class="status-badge" style="background: #e0f2fe; color: #0369a1;">Simulated Mock Payment</span>
            </div>
            <div>
                <span style="color: var(--text-muted); display: block;">Shipping Address:</span>
                <div style="background: #f8fafc; padding: 0.5rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color); margin-top: 0.25rem;">
                    <c:out value="${order.shippingAddress}"/>
                </div>
            </div>
        </div>

        <hr style="border: 0; border-top: 1px solid var(--border-color); margin: 1.25rem 0;">

        <div style="display: flex; justify-content: space-between; font-size: 1.25rem; font-weight: 800;">
            <span>Total Paid:</span>
            <span style="color: var(--primary);">₹<fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2"/></span>
        </div>
    </div>
</div>

<jsp:include page="common/footer.jsp"/>
