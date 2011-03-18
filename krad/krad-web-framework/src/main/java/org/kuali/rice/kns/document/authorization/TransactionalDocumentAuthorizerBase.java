/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.rice.kns.document.authorization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.util.KNSConstants;

/**
 * Base class for all TransactionalDocumentAuthorizers.
 */
public class TransactionalDocumentAuthorizerBase extends DocumentAuthorizerBase
		implements TransactionalDocumentAuthorizer {
	public final Set<String> getEditModes(Document document, Person user,
			Set<String> editModes) {
		Set<String> unauthorizedEditModes = new HashSet<String>();
		for (String editMode : editModes) {
			Map<String, String> additionalPermissionDetails = new HashMap<String, String>();
			additionalPermissionDetails.put(KimConstants.AttributeConstants.EDIT_MODE, editMode);
			if (permissionExistsByTemplate(
					document,
					KNSConstants.KNS_NAMESPACE,
					KimConstants.PermissionTemplateNames.USE_TRANSACTIONAL_DOCUMENT,
					additionalPermissionDetails)
					&& !isAuthorizedByTemplate(
							document,
							KNSConstants.KNS_NAMESPACE,
							KimConstants.PermissionTemplateNames.USE_TRANSACTIONAL_DOCUMENT,
							user.getPrincipalId(), additionalPermissionDetails,
							null)) {
				unauthorizedEditModes.add(editMode);
			}
		}
		editModes.removeAll(unauthorizedEditModes);
		return editModes;
	}
}
