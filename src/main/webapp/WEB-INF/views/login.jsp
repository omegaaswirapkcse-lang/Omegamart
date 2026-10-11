<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Sign In - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div style="max-width: 460px; margin: 2rem auto;">
    <div class="card">
        <h2 style="font-size: 1.6rem; margin-bottom: 0.5rem; text-align: center;">Sign In to OmegaMart</h2>
        <p style="color: var(--text-secondary); text-align: center; margin-bottom: 1.5rem; font-size: 0.92rem;">
            Enter your credentials to access your account.
        </p>

        <c:if test="${param.loggedOut == 'true'}">
            <div class="alert alert-info">
                You have been safely signed out.
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
            <input type="hidden" name="redirect" value="<c:out value='${param.redirect}'/>"/>

            <div class="form-group">
                <label class="form-label" for="email">Email Address</label>
                <input type="email" id="email" name="email" class="form-control"
                       value="<c:out value='${email != null ? email : param.email}'/>" required autofocus/>
            </div>

            <div class="form-group">
                <label class="form-label" for="password">Password</label>
                <input type="password" id="password" name="password" class="form-control" required/>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">
                Sign In
            </button>
        </form>

        <!-- CAPSTONE DEMO CREDENTIALS QUICK-FILL HELPER -->
        <div style="margin-top: 1.75rem; padding-top: 1.25rem; border-top: 1px solid var(--border-color);">
            <div style="font-size: 0.8rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; margin-bottom: 0.6rem; text-align: center;">
                Evaluation Demo Quick-Fill
            </div>
            <div style="display: flex; gap: 0.5rem; justify-content: center; flex-wrap: wrap;">
                <button type="button" class="btn btn-outline btn-sm" onclick="fillCredentials('admin@omegamart.com', 'Admin@12345')">
                    👑 Admin
                </button>
                <button type="button" class="btn btn-outline btn-sm" onclick="fillCredentials('seller1@omegamart.com', 'Seller@12345')">
                    🏬 Seller
                </button>
                <button type="button" class="btn btn-outline btn-sm" onclick="fillCredentials('buyer1@omegamart.com', 'Buyer@12345')">
                    🛒 Buyer
                </button>
            </div>
        </div>

        <div style="margin-top: 1.5rem; text-align: center; font-size: 0.9rem; color: var(--text-secondary);">
            Don't have an account?
            <a href="${pageContext.request.contextPath}/register" style="color: var(--primary); font-weight: 600; text-decoration: none;">
                Register here
            </a>
        </div>
    </div>
</div>

<jsp:include page="common/footer.jsp"/>
