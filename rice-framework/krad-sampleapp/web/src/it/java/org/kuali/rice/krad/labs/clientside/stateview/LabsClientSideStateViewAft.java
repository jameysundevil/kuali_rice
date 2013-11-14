/**
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
package org.kuali.rice.krad.labs.clientside.stateview;

import org.junit.Test;
import org.kuali.rice.testtools.selenium.WebDriverLegacyITBase;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class LabsClientSideStateViewAft extends WebDriverLegacyITBase {

    /**
     * /kr-krad/labs?viewId=Lab-ClientSideState&formKey=ff000d97-13e9-4130-81a3-bc0217e8e0eb&
     * cacheKey=otutyty24mo0f76n59ebqxvtpg&pageId=Lab-ClientSideState-Page1
     */
    public static final String BOOKMARK_URL = "/kr-krad/labs?viewId=Lab-ClientSideState&formKey=ff000d97-13e9-4130-81a3-bc0217e8e0eb&cacheKey=otutyty24mo0f76n59ebqxvtpg&pageId=Lab-ClientSideState-Page1";

    /**
     * inactivatableCollection[0].active
     */
    private static final String ACTIVE_COMPONENT_NAME = "inactivatableCollection[0].active";

    /**
     * inactivatableCollection[0].active
     */
    private static final String ACTIVE_COMPONENT2_NAME = "inactivatableCollection2[0].active";
    
    /**
     * //div[4]/div[2]/button
     */
    private static final String AJAX_BUTTON_XPATH = "//div[4]/div[2]/button";
    
    /**
     * //div[4]/div[2]/button[2]
     */
    private static final String SUBMIT_BUTTON_XPATH = "//div[4]/div[2]/button[2]";

    /**
     * This overridden method ...
     * 
     * @see org.kuali.rice.testtools.selenium.AutomatedFunctionalTestBase#navigate()
     */
    @Override
    protected void navigate() throws Exception {
        // TODO deep - THIS METHOD NEEDS JAVADOCS
        //Navigation is not defined for this functionality so we only do have URL in this Smoke Test.
    }

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Test
    public void testClientSideStateViewBookmark() throws Exception {
        testClientSideStateView();
    }

    public void testClientSideStateView() throws Exception {
        waitAndClickByName(ACTIVE_COMPONENT_NAME);
        waitAndClickByName(ACTIVE_COMPONENT2_NAME);
        waitAndClickByXpath(AJAX_BUTTON_XPATH);
        Thread.sleep(5000);
        assertFalse(isElementPresentByName(ACTIVE_COMPONENT_NAME));
        assertFalse(isElementPresentByName(ACTIVE_COMPONENT2_NAME));
        waitAndClickByXpath(SUBMIT_BUTTON_XPATH);
        Thread.sleep(5000);
        assertFalse(isElementPresentByName(ACTIVE_COMPONENT_NAME));
        assertFalse(isElementPresentByName(ACTIVE_COMPONENT2_NAME));
    }
}