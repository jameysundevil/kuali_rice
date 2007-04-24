/*
 * Copyright 2005-2006 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.core.datadictionary.exception;

import org.kuali.core.datadictionary.DataDictionaryException;

/**
 * Exception thrown when validation of a delegated property of an attributeReference fails
 * 
 * 
 */
public class ReferenceValidationException extends DataDictionaryException {

    private static final long serialVersionUID = 1847020660795308066L;

    /**
     * @param message
     */
    public ReferenceValidationException(String message) {
        super(message);
    }

    /**
     * @param message
     */
    public ReferenceValidationException(String message, Throwable t) {
        super(message, t);
    }
}