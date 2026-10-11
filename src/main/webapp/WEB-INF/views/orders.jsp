<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="My Orders - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<h2 style="margin-bottom: 1.5rem;">My Order History</h2>

<c:choose>
    <c:when test="${empty orders}">
        <div class="card" style="text-align: center; padding: 4rem 2rem;">
            <div style="font-size: 3rem; margin-bottom: 1rem;">📦</div>
            <h3>No orders found</h3>
            <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">You have not placed any orders yet.</p>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Start Shopping</a>
        </div>
    </c:when>
    <c:otherwise>
        <div style="display: flex; flex-direction: column; gap: 1rem;">
            <c:forEach var="ord" items="${orders}">
                <div class="card" style="padding: 1.5rem; margin-bottom: 0;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem;">
                        <div>
                            <span style="font-weight: 700; color: var(--secondary); font-size: 1.05rem;"><c:out value="${ord.orderNumber}"/></span>
                            <span style="color: var(--text-muted); font-size: 0.85rem; margin-left: 0.75rem;">
                                Placed on <fmt:formatDate value="${ord.createdAt}" pattern="dd MMM yyyy, HH:mm"/>
                            </span>
                        </div>
                        <div>
                            <span class="status-badge status-${ord.status.toLowerCase()}"><c:out value="${ord.status}"/></span>
                        </div>
                    </div>

                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <div style="font-size: 0.9rem; color: var(--text-secondary);">
                                Items Count: <strong><c:out value="${ord.items.size()}"/></strong>
                            </div>
                            <div style="font-size: 1.15rem; font-weight: 800; color: var(--secondary); margin-top: 0.25rem;">
                                Total: ₹<fmt:formatNumber value="${ord.totalAmount}" minFractionDigits="2"/>
                            </div>
                        </div>
                        <a href="${pageContext.request.contextPath}/order?id=${ord.id}" class="btn btn-outline btn-sm">
                            View Receipt & Details &rarr;
                        </a>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="common/footer.jsp"/>
