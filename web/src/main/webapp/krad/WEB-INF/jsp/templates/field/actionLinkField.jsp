<%--
 Copyright 2006-2007 The Kuali Foundation
 
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl2.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/krad/WEB-INF/jsp/tldHeader.jsp" %>

<tiles:useAttribute name="field"
                    classname="org.kuali.rice.krad.uif.field.ActionField"/>

<%--
    HTML Link to Submit Form Via JavaScript
    
 --%>

<krad:attributeBuilder component="${field}"/>
<c:set var="pound" value="#"/>
<c:if test="${!empty field.navigateToPageId}">
  <c:set var="name" value="name=\" ${field.navigateToPageId}\""/>
  <c:set var="href" value="href=\" ${pound}${field.navigateToPageId}\""/>
</c:if>

<c:set var="tabindex" value="tabindex=0"/>
<c:if test="${field.skipInTabOrder}">
  <c:set var="tabindex" value="tabindex=-1"/>
</c:if>

<c:if test="${field.actionLabel != null}">
  <c:set var="imageRole" value="role='presentation'"/>
</c:if>

<c:choose>
  <c:when test="${(field.actionImageField != null) && field.actionImageField.render}">
    <c:if test="${not empty field.actionImageField.height}">
      <c:set var="height" value="height='${field.actionImageField.height}'"/>
    </c:if>
    <c:if test="${not empty field.actionImageField.width}">
      <c:set var="width" value="width='${field.actionImageField.width}'"/>
    </c:if>
    <c:choose>
      <c:when test="${field.actionImageLocation != null && (field.actionImageLocation eq 'RIGHT')}">
        <a id="${field.id}" ${href}
           onclick="return false;"${name} ${style} ${styleClass} ${tabindex}>${field.actionLabel}
          <img ${imageRole}
                  class="actionImage rightActionImage ${field.actionImageField.styleClassesAsString}" ${height} ${width}
                  style="${field.actionImageField.style}" src="${field.actionImageField.source}"
                  alt="${field.actionImageField.altText}"/>
        </a>
      </c:when>
      <c:otherwise>
        <a id="${field.id}" ${href} onclick="return false;"${name} ${style} ${styleClass} ${tabindex}><img ${imageRole}
                class="actionImage leftActionImage ${field.actionImageField.styleClassesAsString}" ${height} ${width}
                style="${field.actionImageField.style}" src="${field.actionImageField.source}"
                alt="${field.actionImageField.altText}"/>${field.actionLabel}
        </a>
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise>
    <a id="${field.id}" ${href}
       onclick="return false;"${name} ${style} ${styleClass} ${tabindex}>${field.actionLabel}</a>
  </c:otherwise>
</c:choose>