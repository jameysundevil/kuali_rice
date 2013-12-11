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
package org.kuali.rice.krad.labs.inquiries;

import org.junit.Test;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class LabsInquiryPresentationControllerAft extends LabsInquiryBase {

    /**
     * /kr-krad/kradsampleapp?viewId=KradInquirySample-PageR3C3
     */
    public static final String BOOKMARK_URL = "/kr-krad/kradsampleapp?viewId=KradInquirySample-PageR3C3";

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Override
    protected void navigate() throws Exception {
    	navigateToInquiry("Inquiry with Presentation Controller");
    }

    protected void testInquiryPresentationController() throws InterruptedException {
    	waitAndClickByLinkText("Link to Inquiry with Authorizer configured");
        
    	// Lightbox
        waitAndClickByXpath("//a[@title='Travel Per Diem Expense ']");
        gotoLightBox();
        String[][] lightBoxLabeledText = {{"ID:", "10000"},
                {"Travel Authorization Document Id:", "10000"},
                {"Primary Destination:", "10000"},
                //{"Date of Use:", "12/09/2013"}, //As dates are changed
                {"Breakfast Value:", "10"}, 
                {"Lunch Value:", "10"},
                {"Dinner Value:", "15"},
                {"Amount estimated for incidentals:", "20"},
                {"Mileage rate:", "DO"},
                {"Number of estimated miles:", "30"}};
        assertLabeledTextPresent(lightBoxLabeledText);
        clickCollapseAll();
        assertLabeledTextNotPresent(lightBoxLabeledText);
        clickExpandAll();
        assertLabeledTextPresent(lightBoxLabeledText);
        waitAndClickButtonByText("Close");
        selectTopFrame();
        String[][] LabeledText = {{"ID:", "10000"},
                {"Primary Destination:", "10000"},
                //{"Date of Use:", "12/09/2013"}
                };
        assertLabeledTextPresent(LabeledText);
        clickCollapseAll();
        assertLabeledTextNotPresent(LabeledText);
        clickExpandAll();
        assertLabeledTextPresent(LabeledText);
    }

    @Test
    public void testInquiryPresentationControllerBookmark() throws Exception {
    	testInquiryPresentationController();
        passed();
    }

    @Test
    public void testInquiryPresentationControllerNav() throws Exception {
    	testInquiryPresentationController();
        passed();
    }
}
