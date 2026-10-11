<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Create Account - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div style="max-width: 480px; margin: 2rem auto;">
    <div class="card">
        <h2 style="font-size: 1.6rem; margin-bottom: 0.5rem; text-align: center;">Join OmegaMart</h2>
        <p style="color: var(--text-secondary); text-align: center; margin-bottom: 1.5rem; font-size: 0.92rem;">
            Create a customer or vendor account.
        </p>

        <form action="${pageContext.request.contextPath}/register" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}"/>

            <div class="form-group">
                <label class="form-label">Full Name *</label>
                <input type="text" name="fullName" class="form-control"
                       value="<c:out value='${fullName != null ? fullName : param.fullName}'/>" required maxlength="100"/>
            </div>

            <div class="form-group">
                <label class="form-label">Email Address *</label>
                <input type="email" name="email" class="form-control"
                       value="<c:out value='${email != null ? email : param.email}'/>" required/>
            </div>

            <div class="form-group">
                <label class="form-label">Password * (Min 8 characters)</label>
                <input type="password" name="password" class="form-control" minlength="8" required/>
            </div>

            <div class="form-group">
                <label class="form-label">I want to register as *</label>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 0.4rem;">
                    <label style="border: 1px solid var(--border-color); padding: 0.75rem; border-radius: var(--radius-md); display: flex; align-items: center; gap: 0.5rem; cursor: pointer;">
                        <input type="radio" name="role" value="BUYER" ${empty role || role == 'BUYER' ? 'checked' : ''}/>
                        <span>🛒 <strong>Buyer</strong></span>
                    </label>
                    <label style="border: 1px solid var(--border-color); padding: 0.75rem; border-radius: var(--radius-md); display: flex; align-items: center; gap: 0.5rem; cursor: pointer;">
                        <input type="radio" name="role" value="SELLER" ${role == 'SELLER' ? 'checked' : ''}/>
                        <span>🏬 <strong>Seller</strong></span>
                    </label>
                </div>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 1rem;">
                Create Account
            </button>
        </form>

        <div style="margin-top: 1.5rem; text-align: center; font-size: 0.9rem; color: var(--text-secondary);">
            Already have an account?
            <a href="${pageContext.request.contextPath}/login" style="color: var(--primary); font-weight: 600; text-decoration: none;">
                Sign in here
            </a>
        </div>
    </div>
</div>

<jsp:include page="common/footer.jsp"/>
