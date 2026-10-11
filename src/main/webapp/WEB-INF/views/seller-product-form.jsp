<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isEdit" value="${product != null}"/>
<c:set var="pageTitle" value="${isEdit ? 'Edit Product' : 'Add New Product'} - OmegaMart" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div style="max-width: 700px; margin: 0 auto;">
    <div style="margin-bottom: 1.5rem;">
        <a href="${pageContext.request.contextPath}/seller/dashboard" style="color: var(--primary); text-decoration: none; font-size: 0.9rem;">
            &larr; Back to Dashboard
        </a>
        <h2 style="margin-top: 0.5rem;"><c:out value="${isEdit ? 'Edit Product Listing' : 'Create New Product Listing'}"/></h2>
    </div>

    <div class="card">
        <form action="${pageContext.request.contextPath}/seller/product/${isEdit ? 'edit' : 'new'}" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
            <c:if test="${isEdit}">
                <input type="hidden" name="id" value="${product.id}"/>
            </c:if>

            <div class="form-group">
                <label class="form-label">Product Title *</label>
                <input type="text" name="name" class="form-control"
                       value="<c:out value='${product != null ? product.name : param.name}'/>" required maxlength="255"/>
            </div>

            <div class="form-group">
                <label class="form-label">Category *</label>
                <select name="categoryId" class="form-control" required>
                    <option value="">-- Select Category --</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.id}" ${(product != null && product.categoryId == cat.id) || param.categoryId == cat.id ? 'selected' : ''}>
                            <c:out value="${cat.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                <div class="form-group">
                    <label class="form-label">Price (INR ₹) *</label>
                    <input type="number" step="0.01" min="0.01" name="price" class="form-control"
                           value="<c:out value='${product != null ? product.price : param.price}'/>" required/>
                </div>
                <div class="form-group">
                    <label class="form-label">Stock Quantity *</label>
                    <input type="number" min="0" name="stockQuantity" class="form-control"
                           value="<c:out value='${product != null ? product.stockQuantity : param.stockQuantity}'/>" required/>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">Image URL</label>
                <input type="url" name="imageUrl" class="form-control"
                       placeholder="https://images.unsplash.com/..."
                       value="<c:out value='${product != null ? product.imageUrl : param.imageUrl}'/>"/>
                <small style="color: var(--text-muted); font-size: 0.8rem;">Leave blank to use default marketplace placeholder.</small>
            </div>

            <div class="form-group">
                <label class="form-label">Product Description</label>
                <textarea name="description" rows="4" class="form-control"
                          placeholder="Detailed specifications, features, warranty..."><c:out value="${product != null ? product.description : param.description}"/></textarea>
            </div>

            <c:if test="${isEdit}">
                <div class="form-group" style="display: flex; align-items: center; gap: 0.5rem;">
                    <input type="checkbox" id="isActive" name="isActive" value="true" ${product.isActive ? 'checked' : ''}/>
                    <label for="isActive" style="font-size: 0.92rem; font-weight: 500; cursor: pointer;">
                        Active and Visible in Catalog
                    </label>
                </div>
            </c:if>

            <div style="display: flex; gap: 1rem; margin-top: 2rem;">
                <button type="submit" class="btn btn-primary" style="flex: 1;">
                    <c:out value="${isEdit ? 'Save Changes' : 'Publish Product'}"/>
                </button>
                <a href="${pageContext.request.contextPath}/seller/dashboard" class="btn btn-outline">
                    Cancel
                </a>
            </div>
        </form>
    </div>
</div>

<jsp:include page="common/footer.jsp"/>
