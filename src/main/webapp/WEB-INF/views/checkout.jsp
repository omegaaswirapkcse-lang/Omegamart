<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Checkout - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<h2 style="margin-bottom: 1.5rem;">Secure Checkout</h2>

<!-- CLEAR SIMULATED PAYMENT NOTICE -->
<div class="alert alert-info" style="margin-bottom: 2rem;">
    <span>💳</span>
    <div>
        <strong>Demonstration Notice:</strong> Payment gateway simulation is active.
        No actual bank cards or credit funds will be debited. This transaction verifies ACID atomic order persistence and inventory decrements.
    </div>
</div>

<div style="display: grid; grid-template-columns: 2fr 1fr; gap: 2rem;">
    <!-- SHIPPING & PAYMENT FORM -->
    <div class="card">
        <h3 style="font-size: 1.25rem; margin-bottom: 1.25rem;">1. Shipping & Delivery Address</h3>
        <form action="${pageContext.request.contextPath}/checkout/process" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}"/>

            <div class="form-group">
                <label class="form-label">Full Delivery Address *</label>
                <textarea name="shippingAddress" rows="3" class="form-control"
                          placeholder="House No, Street, Landmark, City, State, PIN Code" required maxlength="500"><c:out value="${shippingAddress != null ? shippingAddress : 'College Hostel Block B, Anna University Campus, Guindy, Chennai, TN 600025'}"/></textarea>
            </div>

            <h3 style="font-size: 1.25rem; margin-top: 2rem; margin-bottom: 1rem;">2. Payment Method</h3>
            <div style="padding: 1rem; border: 1px solid var(--border-color); border-radius: var(--radius-md); background: #f8fafc; margin-bottom: 2rem;">
                <label style="display: flex; align-items: center; gap: 0.75rem; font-weight: 600; cursor: pointer;">
                    <input type="radio" name="paymentType" value="MOCK" checked/>
                    <span>Instant Mock Payment (Simulated Instant Clearance)</span>
                </label>
                <p style="font-size: 0.85rem; color: var(--text-secondary); margin-left: 1.8rem; margin-top: 0.35rem;">
                    Pre-authorized student capstone mock payment token. Confirms instantly upon order placement.
                </p>
            </div>

            <button type="submit" class="btn btn-primary btn-lg" style="width: 100%;">
                ✓ Place Order (₹<fmt:formatNumber value="${cartTotal}" minFractionDigits="2"/>)
            </button>
        </form>
    </div>

    <!-- ORDER SUMMARY SIDEBAR -->
    <div class="card" style="height: fit-content;">
        <h3 style="font-size: 1.2rem; margin-bottom: 1rem;">Order Items (<c:out value="${cartItems.size()}"/>)</h3>
        <div style="display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1.5rem;">
            <c:forEach var="item" items="${cartItems}">
                <div style="display: flex; justify-content: space-between; font-size: 0.9rem;">
                    <div>
                        <strong><c:out value="${item.product.name}"/></strong>
                        <div style="color: var(--text-muted); font-size: 0.8rem;">Qty: ${item.quantity} × ₹<fmt:formatNumber value="${item.product.price}" minFractionDigits="2"/></div>
                    </div>
                    <span style="font-weight: 600;">₹<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2"/></span>
                </div>
            </c:forEach>
        </div>

        <hr style="border: 0; border-top: 1px solid var(--border-color); margin: 1rem 0;">

        <div style="display: flex; justify-content: space-between; font-size: 1.2rem; font-weight: 800;">
            <span>Total Payable:</span>
            <span style="color: var(--primary);">₹<fmt:formatNumber value="${cartTotal}" minFractionDigits="2"/></span>
        </div>
    </div>
</div>

<jsp:include page="common/footer.jsp"/>
