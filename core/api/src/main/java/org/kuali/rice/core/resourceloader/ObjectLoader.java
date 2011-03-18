/*
 * Copyright 2005-2007 The Kuali Foundation
 *
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
package org.kuali.rice.core.resourceloader;

import org.kuali.rice.core.reflect.ObjectDefinition;

/**
 * Loads and constructs instances of Objects from the given {@link ObjectDefinition}.
 *
 * @see ObjectDefinition
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface ObjectLoader {

	/**
	 * Constructs an instance of the Object using the given ObjectDefinition classname.
	 */
	public <T extends Object> T getObject(ObjectDefinition definition);

}
