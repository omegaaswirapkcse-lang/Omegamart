<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Seller Customer Orders - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<h2 style="margin-bottom: 0.5rem;">Customer Orders for Your Products</h2>
<p style="color: var(--text-secondary); margin-bottom: 1.5rem;">
    Review customer purchases containing products sold by your vendor account.
</p>

<c:choose>
    <c:when test="${empty orders}">
        <div class="card" style="text-align: center; padding: 4rem 2rem;">
            <div style="font-size: 3rem; margin-bottom: 1rem;">📋</div>
            <h3>No customer orders yet</h3>
            <p style="color: var(--text-secondary);">Orders placed by buyers for your listed products will appear here.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div style="display: flex; flex-direction: column; gap: 1.25rem;">
            <c:forEach var="ord" items="${orders}">
                <div class="card" style="padding: 1.5rem; margin-bottom: 0;">
                    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem; margin-bottom: 1rem;">
                        <div>
                            <strong>Order Reference: <c:out value="${ord.orderNumber}"/></strong>
                            <div style="font-size: 0.85rem; color: var(--text-muted);">
                                Customer: <strong><c:out value="${ord.buyerName}"/></strong> | Placed on <fmt:formatDate value="${ord.createdAt}" pattern="dd MMM yyyy, HH:mm"/>
                            </div>
                        </div>
                        <div>
                            <span class="status-badge status-${ord.status.toLowerCase()}"><c:out value="${ord.status}"/></span>
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="table" style="font-size: 0.88rem;">
                            <thead>
                                <tr>
                                    <th>Your Item</th>
                                    <th>Unit Price</th>
                                    <th>Ordered Quantity</th>
                                    <th>Vendor Subtotal</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${ord.items}">
                                    <tr>
                                        <td><strong><c:out value="${item.productName}"/></strong></td>
                                        <td>₹<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2"/></td>
                                        <td><c:out value="${item.quantity}"/></td>
                                        <td style="font-weight: 700;">₹<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <div style="margin-top: 0.75rem; font-size: 0.88rem; color: var(--text-secondary);">
                        <strong>Delivery Address:</strong> <c:out value="${ord.shippingAddress}"/>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="common/footer.jsp"/>
