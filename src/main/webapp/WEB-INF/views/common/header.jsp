<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle != null ? pageTitle : 'OmegaMart - Multi-Vendor Marketplace'}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<nav class="navbar">
    <div class="nav-container">
        <a href="${pageContext.request.contextPath}/" class="brand-logo">
            Omega<span>Mart</span>
        </a>

        <form action="${pageContext.request.contextPath}/products" method="GET" class="nav-search">
            <span class="search-icon">🔍</span>
            <input type="text" name="q" placeholder="Search laptops, shoes, books, electronics..." value="<c:out value='${param.q}'/>"/>
        </form>

        <ul class="nav-links">
            <li><a href="${pageContext.request.contextPath}/products">Catalog</a></li>

            <c:choose>
                <c:when test="${not empty sessionScope.currentUser}">
                    <c:if test="${sessionScope.currentUser.role == 'BUYER'}">
                        <li>
                            <a href="${pageContext.request.contextPath}/cart">
                                Cart
                            </a>
                        </li>
                        <li><a href="${pageContext.request.contextPath}/orders">My Orders</a></li>
                    </c:if>

                    <c:if test="${sessionScope.currentUser.role == 'SELLER'}">
                        <li><a href="${pageContext.request.contextPath}/seller/dashboard">Seller Dashboard</a></li>
                        <li><a href="${pageContext.request.contextPath}/seller/orders">Customer Orders</a></li>
                    </c:if>

                    <c:if test="${sessionScope.currentUser.role == 'ADMIN'}">
                        <li><a href="${pageContext.request.contextPath}/admin/dashboard">Admin Panel</a></li>
                    </c:if>

                    <li>
                        <span style="font-size: 0.85rem; color: var(--text-secondary); margin-right: 0.25rem;">
                            Hello, <strong><c:out value="${sessionScope.currentUser.fullName}"/></strong>
                            (<span style="color: var(--primary); font-weight: 600;"><c:out value="${sessionScope.currentUser.role}"/></span>)
                        </span>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Logout</a>
                    </li>
                </c:when>
                <c:otherwise>
                    <li><a href="${pageContext.request.contextPath}/login" class="btn btn-outline btn-sm">Sign In</a></li>
                    <li><a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-sm">Register</a></li>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>
</nav>

<div class="main-content">
    <jsp:include page="alerts.jsp"/>
