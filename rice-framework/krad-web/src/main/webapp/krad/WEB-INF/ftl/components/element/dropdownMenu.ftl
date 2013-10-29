<#--

    Copyright 2005-2013 The Kuali Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.opensource.org/licenses/ecl2.php

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<#--
    Bootstrap dropdown menu component

 -->

<#macro uif_dropdownMenu element>

    <#if !element.renderedInList>
        <div id="${element.id!}" ${krad.attrBuild(element)} ${element.simpleDataAttributes}>
    </#if>

    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
        <@krad.template component=element.dropdownToggle/>

        <#if element.renderToggleCaret || element.renderToggleButton>
            <#local caretClass="caret"/>
            <#if element.renderToggleButton>
                <#-- tmp until styling is fixed -->
                <#--<#local caretClass="${caretClass} btn btn-xs"/>-->
            </#if>

            <span class="${caretClass}"></span>
        </#if>
    </a>

    <ul class="dropdown-menu">
        <#if element.menuActions??>
           <#list element.menuActions as menuAction>
              <@krad.template component=menuAction/>
            </#list>
        </#if>
    </ul>

    <#if !element.renderedInList>
        </div>
    </#if>

</#macro>
