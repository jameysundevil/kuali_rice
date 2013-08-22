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
package org.kuali.rice.scripts.beans

import groovy.util.logging.Log
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Tests for the {@link org.kuali.rice.scripts.beans.LookupDefinitionBeanTransformer} class.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Log
class LookupDefinitionBeanTransformerTest extends BeanTransformerTestBase {

    LookupDefinitionBeanTransformer lookupDefinitionBeanTransformer;
    String defaultTestFilePath;
    String defaultTestBeanID = "TravelerDetail-lookupDefinition-parentBean";

    @Before
    void setUp() {
        super.setUp();
        defaultTestFilePath = getDictionaryTestDir() + "LookupDefinitionSample.xml";
        lookupDefinitionBeanTransformer = new LookupDefinitionBeanTransformer();
        lookupDefinitionBeanTransformer.init(config);
    }

    /**
     * transform lookup definition is responsible for converting a lookup definition into
     * a uif lookup view.  Verifies that
     *
     */
    @Test
    void testTransformLookupDefinitionBean() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        def resultNode;
        def beanNode = ddRootNode.bean.find { defaultTestBeanID.equals(it.@id) }; ;
        try {
            resultNode = lookupDefinitionBeanTransformer.transformLookupDefinitionBean(beanNode);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("exception occurred in testing");
        }

        // confirm a uif inquiry view was generated and has the correct elements
        checkBeanParentExists(ddRootNode, "Uif-LookupView");
        checkBeanPropertyExists(resultNode, "criteriaFields");
        checkBeanPropertyExists(resultNode, "resultFields");
    }

    /**
     * Converts lookup fields properties into criteria fields and handles all children bean nodes properly
     *
     * Success Criteria
     *  - property is renamed as criteria fields
     *  - all 'field definitions' that are attributeNames are turned into LookupCriteriaInputField
     */
    @Test
    public void testTransformLookupFieldsProperties() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        def beanNode = ddRootNode.bean.find { "LookupDefinition".equals(it.@parent) };
        String parentName = beanNode.@parent;
        beanNode.replaceNode {
            bean(parent: "Uif-LookupView") {
                lookupDefinitionBeanTransformer.transformLookupFieldsProperty(delegate, beanNode);
            }
        }


        // confirm lookup fields has been replaced with criteria fields
        Assert.assertEquals("lookupFields not longer exists", 0, ddRootNode.findAll { parentName.equals(it.@name) }.size());
        Assert.assertEquals("criteriaFields exists", 1, ddRootNode.bean.property.findAll { "criteriaFields".equals(it.@name) }.size());
    }

    /**
     * Tests conversion of lookup definition's result fields into appropriate property
     *
     */
    @Test
    public void testTransformResultFieldsProperties() {
        def ddRootNode = getFileRootNode(defaultTestFilePath);
        def beanNode = ddRootNode.bean.find { "LookupDefinition".equals(it.@parent) };
        def resultNode = beanNode.replaceNode {
            bean(parent: "Uif-LookupView") {
                lookupDefinitionBeanTransformer.transformResultFieldsProperty(delegate, beanNode);
            }
        }

        // confirm lookup fields has been replaced with criteria fields
        checkBeanPropertyExists(resultNode, "resultFields");
        def resultsFieldProperty = resultNode.property.find { "resultFields".equals(it.@name) };
        def dataFieldSize = resultsFieldProperty.list.bean.findAll { "Uif-DataField".equals(it.@parent) }.size();
        Assert.assertEquals("number of converted data fields did not match", 11, dataFieldSize);
    }

}
