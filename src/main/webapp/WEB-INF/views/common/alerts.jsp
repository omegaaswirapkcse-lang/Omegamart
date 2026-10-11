<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty sessionScope.flashSuccess}">
    <div class="alert alert-success">
        <span>✓</span>
        <div><c:out value="${sessionScope.flashSuccess}"/></div>
    </div>
    <c:remove var="flashSuccess" scope="session"/>
</c:if>

<c:if test="${not empty sessionScope.flashError}">
    <div class="alert alert-danger">
        <span>⚠️</span>
        <div><c:out value="${sessionScope.flashError}"/></div>
    </div>
    <c:remove var="flashError" scope="session"/>
</c:if>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger">
        <span>⚠️</span>
        <div><c:out value="${errorMessage}"/></div>
    </div>
</c:if>
