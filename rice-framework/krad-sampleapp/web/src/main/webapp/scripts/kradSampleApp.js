/*
 * Copyright 2005-2013 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Shows the appHeader and footer, selects appropriate links in nav menus, removes
 * padding from views rendered in lightboxes, and handles tab swap action fire if using large example
 */
jQuery(function () {
    jQuery(".demo-appHeader, .demo-appFooter, .demo-thirdTier").show();
    linkSelection();
    if (jQuery("input[name='" + kradVariables.RENDERED_IN_LIGHTBOX + "']").length
            && jQuery("input[name='" + kradVariables.RENDERED_IN_LIGHTBOX + "']").val() == "true") {
        jQuery(".uif-view").css("padding-top", "0");
    }
    jQuery(document).on(kradVariables.PAGE_LOAD_EVENT, function(){
        handleTabSwap("input#Demo-CurrentExampleIndex_control");
        updateHtmlViewer();
    });
});

/**
 * Update the html code viewer with the html output for the example being viewed
 */
function updateHtmlViewer(){
    var content = jQuery("[data-parent='ComponentLibrary-TabGroup']:visible");

    // If the content has a single child, and that child is a link opening another view, then don't show the html
    // because this is showing an example outside of the scope of what should be shown here
    var anchor = content.find("a");
    if (content.children().length === 1 && anchor.length === 1
            && anchor.attr("href") != null && anchor.attr("href").indexOf(kradVariables.VIEW_ID) !== -1 ) {
        jQuery("div#ComponentLibrary-HtmlCodeViewer").hide();
        return;
    }

    // Otherwise retrieve and show the html content
    // Remove extraneous content from a copy of the content to only show relevant html
    content = content.clone();
    content.find("> [data-header_for='" + content.attr("id") + "']").remove();
    content.find("> .uif-instructionalMessage[data-parent='" + content.attr("id") + "']").remove();
    var exampleHtml = content.html();

    if (exampleHtml){
        exampleHtml = exampleHtml.trim();
        exampleHtml = formatHtml(exampleHtml);

        var htmlSourcePreElement = jQuery("<pre class='prettyprint linenums prettyprinted'></pre>");
        htmlSourcePreElement.text(exampleHtml);

        jQuery("div#ComponentLibrary-HtmlCodeViewer > div > pre").replaceWith(htmlSourcePreElement);
        prettyPrint();
    }

    jQuery("div#ComponentLibrary-HtmlCodeViewer").show();
}

/**
 * Setup call for exhibit tabs, adds a handler for the tabsactivate event to switch the source in the syntaxHighlighter
 * based on tab index and write the tabIndex value to the Demo-CurrentExampleIndex_control
 */
function setupExhibitHandlers() {
    jQuery("#ComponentLibrary-TabGroup_tabs").on("tabsactivate", function (event, ui) {
        var tabIndex = ui.newTab.index();
        jQuery("input#Demo-CurrentExampleIndex_control").val(tabIndex);

        //alert("got here");
        //main source code viewer
        var source = jQuery("#demo-exhibitSource > pre:eq(" + tabIndex + ")");
        if (source != null && source.length) {
            jQuery("div#ComponentLibrary-MainCodeViewer > div > pre").replaceWith(jQuery(source)[0].outerHTML);
        }

        showAdditionalSource(tabIndex);
        updateHtmlViewer();
    });
}

function showAdditionalSource(tabIndex) {
    //additional source code viewers
    var additionalSource1 = jQuery("#demo-additionalExhibitSource1 > pre[data-index='" + tabIndex + "']");
    if (additionalSource1 != null && additionalSource1.length) {
        jQuery("div#ComponentLibrary-AdditionalCodeViewer1 > div > pre").replaceWith(jQuery(additionalSource1)[0].outerHTML);
        jQuery("div#ComponentLibrary-AdditionalCodeViewer1").show();
    }
    else {
        jQuery("div#ComponentLibrary-AdditionalCodeViewer1").hide();
    }

    var additionalSource2 = jQuery("#demo-additionalExhibitSource2 > pre[data-index='" + tabIndex + "']");
    if (additionalSource2 != null && additionalSource2.length) {
        jQuery("div#ComponentLibrary-AdditionalCodeViewer2 > div > pre").replaceWith(jQuery(additionalSource2)[0].outerHTML);
        jQuery("div#ComponentLibrary-AdditionalCodeViewer2").show();
    }
    else {
        jQuery("div#ComponentLibrary-AdditionalCodeViewer2").hide();
    }
}

/**
 * Adds css classes the appropriate links in the header and in the componentLibrary navigation based on user selection
 * so they appear active
 */
function linkSelection() {
    var viewDiv = jQuery("div.uif-formView");
    if (jQuery(viewDiv).is(".demo-componentLibView") || jQuery(viewDiv).is(".demo-componentLibHome")) {
        var viewId = viewDiv.attr("id");
        var link = jQuery("#" + kradVariables.NAVIGATION_ID).find("a[href*='" + viewId + "']");
        if (link.length) {
            jQuery(link).css("color", "#222222");
            var accordionLi = jQuery(link).closest("li.uif-accordionTab");
            var index = jQuery(accordionLi).index();
            jQuery(accordionLi).parent().accordion("option", "active", index);
        }

        jQuery("a#Demo-LibraryLink").addClass("active");
    }
    else if (jQuery(viewDiv).is(".demo-sampleAppHomeView")) {
        jQuery("a#Demo-HomeLink").addClass("active");
    }
    else {
        jQuery("a#Demo-DemoLink").addClass("active");
    }
}

/**
 * Activates the tab (hidden in the case where large example is being used)  in order to retain
 * the ability to show the correct example tab and source in the syntaxHighlighter without needing additional handlers
 *
 * @param control the large example dropdown control
 */
function handleTabSwap(control) {
    var tab = jQuery(control);
    var tabValue = tab.val();
    if (tabValue != undefined && tabValue != "") {
        var tabDom = tab[0];
        var tabNum = tabDom.selectedIndex;
        jQuery("#ComponentLibrary-TabGroup_tabs").tabs("option", "active", tabNum);
    }
    else{
        showAdditionalSource(0);
    }
}

function showLibraryNav() {

    if (jQuery("#Uif-Navigation").is(":hidden")) {
        jQuery(".demo-noTabs > div.ui-tabs > div.ui-tabs-panel").animate({width:'675px'}, 25, function () {
            jQuery(".demo-noTabs > div.ui-tabs > div.ui-tabs-panel").css("overflow-y", "hidden");
            jQuery(".demo-noTabs > div.ui-tabs > div.ui-tabs-panel").css("overflow-x", "scroll");
        });
        jQuery(".uif-pageContentWrapper").animate({width:'700px', marginLeft: '256px'}, 25);

        jQuery("#Uif-Navigation").css("width", "0");
        jQuery("#Uif-Navigation").show();
        jQuery("#Uif-Navigation").addClass("force-show");
        jQuery("#Uif-Navigation").animate({width:'220px'}, 300, function () {
            jQuery("#ComponentLibrary-ShowNavLink").text("<< Close Library Navigation");
        });
    }
    else {

        jQuery("#Uif-Navigation").animate({width:'1px'}, {duration:300, queue:false, complete:function () {
            jQuery("#ComponentLibrary-ShowNavLink").text(">> Show Library Navigation");
            jQuery("#Uif-Navigation").removeClass("force-show");
            jQuery("#Uif-Navigation").hide();
        }});

        jQuery(".uif-pageContentWrapper").animate({width:'940px', marginLeft: '20px'}, 425);
        jQuery(".demo-noTabs > div.ui-tabs > div.ui-tabs-panel").animate({width:'916px'}, 450, function () {
            jQuery(".demo-noTabs > div.ui-tabs > div.ui-tabs-panel").css("overflow-y", "hidden");
            jQuery(".demo-noTabs > div.ui-tabs > div.ui-tabs-panel").css("overflow-x", "hidden");
        });

    }

}

/**
 * Custom totaling function that takes the values and subtracts them from the startingValue.  If no starting value is
 * supplied, subtracts the values from 0.
 *
 * @param values values to subtract from startingValue
 * @param startingValue(optional) value to subtract the values from, if not supplied this will be 0
 */
function subtractValues(values, startingValue) {
    if (!startingValue) {
        //subtract the values from 0 if no startingValue provided
        startingValue = 0;
    }

    //subtract each value, values supplied will always be numeric
    for (var i = 0; i < values.length; i++) {
        startingValue -= values[i];
    }

    //return value, whatever is returned is displayed in the total
    return startingValue;
}

function showGroupOutlines(button) {
    var groups = jQuery(button).closest(".uif-verticalBoxLayout").find("> .uif-group").not(":first");
    groups = jQuery(groups).add(jQuery(groups).find(".uif-group"));
    if (groups.hasClass("demo-outlineGroup")) {
        groups.removeClass("demo-outlineGroup");
    }
    else {
        groups.addClass("demo-outlineGroup");
    }
}

function showItemOutlines(button) {
    var groups = jQuery(button).closest(".uif-verticalBoxLayout").find("> .uif-group").not(":first");
    var items = groups.find(".uif-boxLayoutVerticalItem, .uif-boxLayoutHorizontalItem");
    if (items.hasClass("demo-outlineItem")) {
        items.removeClass("demo-outlineItem");
    }
    else {
        items.addClass("demo-outlineItem");
    }
}

